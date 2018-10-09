package iansantos.login.model

import com.google.firebase.Timestamp

class CloudFirestoreUser(name: String, lastName: String, email: String, cpf: String, password: String) {
    var name: String? = name
        get() = (field!!.substring(0, 1).toUpperCase() + field!!.substring(1)).trim { it <= ' ' }
    var lastName: String? = lastName
        get() = (field!!.substring(0, 1).toUpperCase() + field!!.substring(1)).trim { it <= ' ' }
    var email: String? = email
        get() = field!!.toLowerCase().trim { it <= ' ' }
    var cpf: String? = cpf
    var password: String? = password
        get() = field!!.trim { it <= ' ' }
}

class CloudFirestoreUserPresence(
        var online: Boolean = true,
        var lastActive: Timestamp = Timestamp.now()
)