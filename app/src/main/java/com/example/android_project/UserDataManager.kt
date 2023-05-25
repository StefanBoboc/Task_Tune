import com.example.android_project.Users

object UserDataManager {
    private var user: Users? = null

    fun setUser(userData: Users) {
        user = userData
    }

    fun getUser(): Users? {
        return user
    }
}
