import com.example.android_project.databinding.ActivitySignUpBinding

class User {
    private var username: String? = null
    private var email: String
    private var uid: String
//    private lateinit var calendar: String

    constructor(email: String, uid: String, username: String?) {
        this.username = username
        this.email = email
        this.uid = uid
    }
}