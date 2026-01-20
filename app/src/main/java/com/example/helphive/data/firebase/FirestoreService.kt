package com.example.helphive.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.example.helphive.data.model.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class FirestoreService @Inject constructor(private val firestore: FirebaseFirestore) {

    // In FirestoreService.kt
    fun <T> observeCollection(
        collectionPath: String,
        mapper: (DocumentSnapshot) -> T?
    ): Flow<List<T>> = callbackFlow {
        val listener = firestore.collection(collectionPath)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Optional: handle error
                    return@addSnapshotListener
                }

                val items = snapshot?.documents
                    ?.mapNotNull { mapper(it) }
                    ?: emptyList()

                trySend(items)
            }

        awaitClose { listener.remove() }
    }

    suspend fun saveUser(user: User): Result<String> {
        return try {
            firestore.collection("users")
                .document(user.userId)
                .set(user.toMap())
                .await()
            Result.success("User saved successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserIfNotExists(userId: String, name: String, email: String): Result<String> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (!document.exists()) {
                val user = User(
                    userId = userId,
                    name = name,
                    email = email
                )
                saveUser(user)
            } else {
                Result.success("User already exists")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<String> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()
            Result.success("User updated successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val user = User(
                    userId = document.getString("userId") ?: "",
                    name = document.getString("name") ?: "",
                    email = document.getString("email") ?: "",
                    profileImagePath = document.getString("profileImagePath") ?: "",
                    address = document.getString("address") ?: "",
                    gender = document.getString("gender") ?: ""
                )
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users")
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                User(
                    userId = doc.getString("userId") ?: "",
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    profileImagePath = doc.getString("profileImagePath") ?: "",
                    address = doc.getString("address") ?: "",
                    gender = doc.getString("gender") ?: ""
                )
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMood(mood: Mood): Result<String> {
        return try {
            firestore.collection("moods")
                .document(mood.moodId)
                .set(mood.toMap())
                .await()
            Result.success("Mood saved successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fixed: Removed orderBy to avoid index requirement
    suspend fun getUserMoods(userId: String): Result<List<Mood>> {
        return try {
            val snapshot = firestore.collection("moods")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val moods = snapshot.documents.mapNotNull { doc ->
                Mood(
                    moodId = doc.getString("moodId") ?: "",
                    userId = doc.getString("userId") ?: "",
                    emoji = doc.getString("emoji") ?: "",
                    note = doc.getString("note") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }.sortedByDescending { it.timestamp } // Sort client-side

            Result.success(moods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addKindness(kindness: Kindness): Result<String> {
        return try {
            firestore.collection("kindness")
                .document(kindness.id)
                .set(kindness.toMap())
                .await()
            Result.success("Kindness saved successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fixed: Removed orderBy to avoid index requirement - Added source constraint for offline
    // In FirestoreService.kt - Ensure user data is always included
    suspend fun getKindnessFeed(): Result<List<Kindness>> {
        return try {
            val snapshot = firestore.collection("kindness")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get(com.google.firebase.firestore.Source.DEFAULT)
                .await()

            val kindnessList = snapshot.documents.map { doc ->
                Kindness(
                    id = doc.getString("id") ?: doc.id,
                    userId = doc.getString("userId") ?: "",
                    text = doc.getString("text") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }

            // Collect unique user IDs
            val userIds = kindnessList.map { it.userId }.filter { it.isNotBlank() }.distinct()
            val userMap = mutableMapOf<String, Pair<String, String>>() // userId -> (name, profileImage)

            // Firestore whereIn allows max 10 elements; chunk
            userIds.chunked(10).forEach { chunk ->
                val usersSnap = firestore.collection("users")
                    .whereIn("userId", chunk)
                    .get(com.google.firebase.firestore.Source.DEFAULT)
                    .await()
                for (u in usersSnap.documents) {
                    val uid = u.getString("userId") ?: continue
                    val name = u.getString("name") ?: "Unknown User"
                    val img = u.getString("profileImagePath") ?: ""
                    userMap[uid] = Pair(name, img)
                }
            }

            // Enrich kindness list
            val enriched = kindnessList.map { k ->
                val userInfo = userMap[k.userId]
                if (userInfo != null) k.copy(userName = userInfo.first, userProfileImage = userInfo.second)
                else k.copy(userName = "Unknown User", userProfileImage = "")
            }

            Result.success(enriched)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun addHelpRequest(helpRequest: HelpRequest): Result<String> {
        return try {
            firestore.collection("help_requests")
                .document(helpRequest.requestId)
                .set(helpRequest.toMap())
                .await()
            Result.success("Help request saved successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fixed: Removed orderBy to avoid index requirement - Added source constraint for offline
    // In FirestoreService.kt
    suspend fun getHelpRequests(): Result<List<HelpRequest>> {
        return try {
            android.util.Log.d("FirestoreService", "Starting getHelpRequests call") // Debug log
            val snapshot = firestore.collection("help_requests")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get(com.google.firebase.firestore.Source.DEFAULT) // This ensures offline data is available
                .await()

            android.util.Log.d("FirestoreService", "Retrieved ${snapshot.size()} help request documents from Firestore") // Debug log

            val helpRequests = mutableListOf<HelpRequest>()

            for (doc in snapshot.documents) {
                // Log each document ID being processed
                android.util.Log.d("FirestoreService", "Processing help request doc ID: ${doc.id}") // Debug log

                val helpRequest = HelpRequest(
                    requestId = doc.getString("requestId") ?: doc.id, // Use doc.id as fallback for requestId
                    userId = doc.getString("userId") ?: "",
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    imagePath = doc.getString("imagePath") ?: ""
                )

                // Log user ID for the request
                android.util.Log.d("FirestoreService", "Fetching user data for userId: ${helpRequest.userId}") // Debug log

                // Get user info for this help request
                val userDoc = firestore.collection("users")
                    .document(helpRequest.userId)
                    .get(com.google.firebase.firestore.Source.DEFAULT)
                    .await()

                val userName = if (userDoc.exists()) {
                    userDoc.getString("name") ?: "Unknown User"
                } else {
                    android.util.Log.d("FirestoreService", "User document not found for userId: ${helpRequest.userId}") // Debug log
                    "Unknown User"
                }

                val userProfileImage = if (userDoc.exists()) {
                    userDoc.getString("profileImagePath") ?: ""
                } else {
                    ""
                }

                helpRequests.add(helpRequest.copy(
                    userName = userName,
                    userProfileImage = userProfileImage
                ))
            }

            android.util.Log.d("FirestoreService", "Successfully fetched ${helpRequests.size} help requests with user data") // Debug log
            Result.success(helpRequests) // Already sorted by orderBy
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Error in getHelpRequests: ${e.message}", e) // Error log
            Result.failure(e)
        }
    }

    // Fixed: Removed orderBy to avoid index requirement
    suspend fun getAllMoods(): Result<List<Mood>> {
        return try {
            val snapshot = firestore.collection("moods")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get(com.google.firebase.firestore.Source.DEFAULT) // This ensures offline data is available
                .await()

            val moods = snapshot.documents.mapNotNull { doc ->
                Mood(
                    moodId = doc.getString("moodId") ?: "",
                    userId = doc.getString("userId") ?: "",
                    emoji = doc.getString("emoji") ?: "",
                    note = doc.getString("note") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            } // Already sorted by orderBy

            Result.success(moods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHelpRequest(helpRequest: HelpRequest): Result<String> {
        return try {
            firestore.collection("help_requests")
                .document(helpRequest.requestId)
                .update(
                    "title", helpRequest.title,
                    "description", helpRequest.description,
                    "imagePath", helpRequest.imagePath
                )
                .await()
            Result.success("Help request updated successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteHelpRequest(requestId: String): Result<String> {
        return try {
            firestore.collection("help_requests")
                .document(requestId)
                .delete()
                .await()
            Result.success("Help request deleted successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addChatMessage(message: ChatMessage): Result<String> {
        return try {
            firestore.collection("chat_messages")
                .document(message.messageId)
                .set(message.toMap())
                .await()
            Result.success("Message sent successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fixed: Removed orderBy to avoid index requirement
    suspend fun getChatMessages(requestId: String): Result<List<ChatMessage>> {
        return try {
            val snapshot = firestore.collection("chat_messages")
                .whereEqualTo("requestId", requestId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get(com.google.firebase.firestore.Source.DEFAULT) // This ensures offline data is available
                .await()

            val messages = mutableListOf<ChatMessage>()

            for (doc in snapshot.documents) {
                val message = ChatMessage(
                    messageId = doc.getString("messageId") ?: "",
                    requestId = doc.getString("requestId") ?: "",
                    senderId = doc.getString("senderId") ?: "",
                    message = doc.getString("message") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )

                // Get sender info
                val userDoc = firestore.collection("users")
                    .document(message.senderId)
                    .get(com.google.firebase.firestore.Source.DEFAULT)
                    .await()
                if (userDoc.exists()) {
                    val senderName = userDoc.getString("name") ?: "Unknown User"
                    val senderProfileImage = userDoc.getString("profileImagePath") ?: ""

                    messages.add(message.copy(
                        senderName = senderName,
                        senderProfileImage = senderProfileImage
                    ))
                } else {
                    messages.add(message.copy(
                        senderName = "Unknown User",
                        senderProfileImage = ""
                    ))
                }
            }

            Result.success(messages) // Already sorted by orderBy
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Real-time chat messages observer - requires index for chat_messages collection:
    // Field 1: requestId (ASC), Field 2: timestamp (ASC)
    fun observeChatMessages(requestId: String): Flow<Result<List<ChatMessage>>> = callbackFlow {
        val listener = firestore.collection("chat_messages")
            .whereEqualTo("requestId", requestId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val messages = mutableListOf<ChatMessage>()
                for (doc in snapshot?.documents ?: emptyList()) {
                    val message = ChatMessage(
                        messageId = doc.getString("messageId") ?: "",
                        requestId = doc.getString("requestId") ?: "",
                        senderId = doc.getString("senderId") ?: "",
                        message = doc.getString("message") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )

                    messages.add(message)
                }

                trySend(Result.success(messages))
            }

        awaitClose { listener.remove() }
    }

    // Fixed: Removed orderBy to avoid index requirement
    // In FirestoreService.kt
    suspend fun getChatConversations(userId: String): Result<List<ChatConversation>> {
        return try {
            // Get all chat messages involving this user
            val snapshot = firestore.collection("chat_messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(200) // Reasonable limit
                .get(Source.DEFAULT)
                .await()

            val conversationsMap = mutableMapOf<String, ChatConversation>()
            val userCache = mutableMapOf<String, Pair<String, String>>() // userId -> (name, profileImage)

            for (doc in snapshot.documents) {
                val senderId = doc.getString("senderId") ?: continue
                val requestId = doc.getString("requestId") ?: continue
                val message = doc.getString("message") ?: continue
                val timestamp = doc.getLong("timestamp") ?: 0L

                // Get the other user in this conversation
                val otherUserId = getOtherUserIdInConversation(userId, requestId, senderId)
                if (otherUserId.isEmpty() || otherUserId == userId) continue

                // Cache user data if not already cached
                if (!userCache.containsKey(otherUserId)) {
                    val userDoc = firestore.collection("users")
                        .document(otherUserId)
                        .get(Source.DEFAULT)
                        .await()
                    if (userDoc.exists()) {
                        val userName = userDoc.getString("name") ?: "Unknown User"
                        val profileImage = userDoc.getString("profileImagePath") ?: ""
                        userCache[otherUserId] = Pair(userName, profileImage)
                    }
                }

                // Create or update conversation for this user
                val existing = conversationsMap[otherUserId]
                if (existing == null || timestamp > existing.lastMessageTime) {
                    val (userName, userProfileImage) = userCache[otherUserId] ?: Pair("Unknown User", "")
                    val unreadCount = if (senderId != userId && (existing?.unreadCount ?: 0) == 0) 1 else (existing?.unreadCount ?: 0)

                    conversationsMap[otherUserId] = ChatConversation(
                        requestId = requestId,
                        otherUserId = otherUserId,
                        otherUserName = userName,
                        lastMessage = message,
                        lastMessageTime = timestamp,
                        unreadCount = unreadCount
                    )
                }
            }

            // Convert map to list and sort by most recent message
            val conversations = conversationsMap.values
                .sortedByDescending { it.lastMessageTime }
                .toList()

            Result.success(conversations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getOtherUserIdInConversation(
        currentUserId: String,
        requestId: String,
        messageSenderId: String
    ): String {
        // Get help request to find the owner
        val helpRequestDoc = firestore.collection("help_requests")
            .document(requestId)
            .get(Source.DEFAULT)
            .await()

        return if (helpRequestDoc.exists()) {
            val ownerId = helpRequestDoc.getString("userId") ?: ""
            if (currentUserId == ownerId) {
                messageSenderId // Other user is the sender
            } else {
                ownerId // Other user is the help request owner
            }
        } else {
            // Fallback: if not a help request, use the other participant
            if (currentUserId == messageSenderId) {
                // This is tricky - we need another way to identify the other user
                // For now, return empty string (will be filtered out)
                ""
            } else {
                messageSenderId
            }
        }
    }
}

// File: com/example/helphive/data/firebase/FirestoreService.kt (Update the data class definition location if needed)
// Or move this to a shared model file like com.example.helphive.data.model.ChatConversation
data class ChatConversation(
    val requestId: String,
    val otherUserId: String, // Unique identifier for the other user
    val otherUserName: String = "Unknown User",
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val userProfileImage: String = "" // Add this field
) {
    // Add this for proper deduplication
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChatConversation
        return otherUserId == other.otherUserId
    }

    override fun hashCode(): Int {
        return otherUserId.hashCode()
    }
}