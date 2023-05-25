import com.example.android_project.Users
import com.google.firebase.database.*

class UserManager private constructor() {
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userRef: DatabaseReference = firebaseDatabase.reference.child("users")
    private var userData: Users? = null

    fun loadUsers(userId: String, callback: UserCallback) {
        // Check if the data is already loaded
        if (userData != null) {
            callback.onUserLoaded(userData)
            return
        }

        // Load the user data from Firebase
        userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userData = snapshot.getValue(Users::class.java)
                callback.onUserLoaded(userData)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onUserError(error.message)
            }
        })
    }

    interface UserCallback {
        fun onUserLoaded(userData: Users?)
        fun onUserError(errorMessage: String)
    }

    companion object {
        @Volatile
        private var instance: UserManager? = null

        fun getInstance(): UserManager =
            instance ?: synchronized(this) {
                instance ?: UserManager().also { instance = it }
            }
    }
}
