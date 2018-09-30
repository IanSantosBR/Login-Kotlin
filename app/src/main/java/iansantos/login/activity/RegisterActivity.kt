@file:Suppress("DEPRECATION")

package iansantos.login.activity

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import iansantos.login.R
import iansantos.login.model.CloudFirestoreUser
import java.util.*

private const val TAG = "RegisterActivity"

class RegisterActivity : AppCompatActivity() {
    private var databaseReference: CollectionReference? = null
    private var name: EditText? = null
    private var lastName: EditText? = null
    private var email: EditText? = null
    private var cpf: EditText? = null
    private var password: EditText? = null
    private var passwordConfirmation: EditText? = null
    private var mAuth: FirebaseAuth? = null
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val view = findViewById<View>(R.id.constraint_layout)
        view.requestFocus()
        name = findViewById(R.id.name)
        lastName = findViewById(R.id.last_name)
        email = findViewById(R.id.email)
        cpf = findViewById(R.id.cpf)
        password = findViewById(R.id.password)
        passwordConfirmation = findViewById(R.id.password_confirmation)
        mAuth = FirebaseAuth.getInstance()
        val database = FirebaseFirestore.getInstance()
        databaseReference = database.collection("users")
    }

    @Suppress("UNUSED_PARAMETER")
    fun registerNewUser(view: View) {
        hideKeyboard()
        if (areValidFields() && passwordsMatch()) {
            dialog = ProgressDialog.show(this@RegisterActivity, "", "Criando sua conta...", true)
            mAuth!!.createUserWithEmailAndPassword(email!!.text.toString(), password!!.text.toString()).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    dialog!!.dismiss()
                    this@RegisterActivity.saveUser()
                    Toast.makeText(this@RegisterActivity, "Cadastrado", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "createUserWithEmail:success")
                    this@RegisterActivity.finish()
                } else {
                    dialog!!.dismiss()
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this@RegisterActivity, Objects.requireNonNull<Exception>(task.exception).message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this@RegisterActivity, "Verifique os campos obrigatórios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUser() {
        val user = CloudFirestoreUser(name!!.text.toString(), lastName!!.text.toString(), email!!.text.toString(), cpf!!.text.toString(), password!!.text.toString())
        val data = HashMap<String, Any>()
        data["fullName"] = "${user.name} ${user.lastName}"
        data["email"] = user.email!!
        data["cpf"] = user.cpf!!
        data["password"] = user.password!!
        databaseReference!!.document(Objects.requireNonNull<String>(mAuth!!.uid)).set(data)
    }

    private fun areValidFields(): Boolean {
        var areValidFields = true
        if (TextUtils.isEmpty(name!!.text.toString().trim { it <= ' ' })) {
            name!!.error = "Digite o nome"
            areValidFields = false
        }
        if (TextUtils.isEmpty(lastName!!.text.toString().trim { it <= ' ' })) {
            lastName!!.error = "Digite o sobrenome"
            areValidFields = false
        }
        if (TextUtils.isEmpty(email!!.text.toString().trim { it <= ' ' })) {
            email!!.error = "Digite o email"
            areValidFields = false
        }
        if (TextUtils.isEmpty(cpf!!.text.toString().trim { it <= ' ' })) {
            cpf!!.error = "Digite o CPF"
            areValidFields = false
        }
        if (TextUtils.isEmpty(password!!.text.toString().trim { it <= ' ' })) {
            password!!.error = "Digite a senha"
            areValidFields = false
        }
        if (TextUtils.isEmpty(passwordConfirmation!!.text.toString().trim { it <= ' ' })) {
            passwordConfirmation!!.error = "Re-digite a senha"
            areValidFields = false
        }
        return areValidFields
    }

    private fun passwordsMatch(): Boolean {
        var passwordsMatch = true
        if (password!!.text.toString() != passwordConfirmation!!.text.toString()) {
            passwordsMatch = false
            password!!.error = "Senhas não correspondem"
            passwordConfirmation!!.error = "Senhas não correspondem"
        }
        return passwordsMatch
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        try {
            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            Objects.requireNonNull(inputManager).hideSoftInputFromWindow(Objects.requireNonNull(currentFocus).windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }
}