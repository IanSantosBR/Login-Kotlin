@file:Suppress("DEPRECATION")

package iansantos.login.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import iansantos.login.R
import java.util.*

private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mCallbackManager: CallbackManager? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var email: EditText? = null
    private var password: EditText? = null
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this, getString(R.string.admob_app_id))
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.admob_id)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        setContentView(R.layout.activity_login)
        title = "Entrar"
        val view = findViewById<View>(R.id.constraint_layout)
        view.requestFocus()
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        mAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d(TAG, "onAuthStateChanged:signed_in:${user.uid}")
                this@LoginActivity.startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                this@LoginActivity.finish()
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out")
            }
        }

        mCallbackManager = CallbackManager.Factory.create()
        val loginButton = findViewById<LoginButton>(R.id.facebook_login_button)
        loginButton.setReadPermissions("email", "public_profile")
        loginButton.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })
    }

    public override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    public override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth!!.removeAuthStateListener(mAuthListener!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        dialog = ProgressDialog.show(this@LoginActivity, "", "Carregando...", true)
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful)
            if (task.isSuccessful) {
                dialog!!.dismiss()
                Toast.makeText(this@LoginActivity, "Conectado", Toast.LENGTH_SHORT).show()
            } else {
                mAuth!!.signOut()
                LoginManager.getInstance().logOut()
                dialog!!.dismiss()
                Toast.makeText(this@LoginActivity, "Falha na autenticação", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun signIn(view: View) {
        hideKeyboard()
        if (areValidFields()) {
            dialog = ProgressDialog.show(this@LoginActivity, "", "Entrando...", true)
            mAuth!!.signInWithEmailAndPassword(email!!.text.toString(), password!!.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "signInWithEmail:success")
                            dialog!!.dismiss()
                            Toast.makeText(this@LoginActivity, "Conectado", Toast.LENGTH_SHORT).show()
                        } else {
                            dialog!!.dismiss()
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(this@LoginActivity, Objects.requireNonNull<Exception>(task.exception).message, Toast.LENGTH_SHORT).show()
                        }
                    }
        } else {
            Toast.makeText(this@LoginActivity, "Verifique os campos obrigatórios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun areValidFields(): Boolean {
        var areValidFields = true
        if (TextUtils.isEmpty(email!!.text.toString().trim { it <= ' ' })) {
            email!!.error = "Digite o email"
            areValidFields = false
        }
        if (TextUtils.isEmpty(password!!.text.toString().trim { it <= ' ' })) {
            password!!.error = "Digite a senha"
            areValidFields = false
        }
        return areValidFields
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        try {
            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            Objects.requireNonNull(inputManager).hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }

    }

    @Suppress("UNUSED_PARAMETER")
    fun register(view: View) {
        startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
    }

    companion object {
        lateinit var mInterstitialAd: InterstitialAd
    }
}