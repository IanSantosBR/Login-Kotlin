package iansantos.login.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import iansantos.login.R
import iansantos.login.adapter.SearchAdapter
import iansantos.login.api.SearchService
import iansantos.login.api.SearchService.Companion.BASE_URL
import iansantos.login.model.StackOverflowQuestion
import iansantos.login.model.StackOverflowSearch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private var adapter: SearchAdapter? = null
    private var mAuth: FirebaseAuth? = null
    private var retrofit: Retrofit? = null
    private var progressBar: ProgressBar? = null
    private var recyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private val questions = ArrayList<StackOverflowQuestion>()
    private var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null
    private var searchEditText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressBar = findViewById(R.id.progressBar)
        searchEditText = findViewById(R.id.search_editText)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)
        retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        mAuth = FirebaseAuth.getInstance()
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(this@MainActivity, LinearLayout.VERTICAL))
        adapter = SearchAdapter(questions)
        recyclerView!!.adapter = adapter
        searchEditText!!.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                this@MainActivity.getData(findViewById(R.id.search_button))
                return@OnEditorActionListener true
            }
            false
        })
        swipeRefreshLayout!!.setOnRefreshListener {
            if (!searchEditText!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                swipeRefreshLayout!!.isRefreshing = true
                progressBar!!.visibility = View.INVISIBLE
                recyclerView!!.adapter = adapter
                this@MainActivity.getData(findViewById(R.id.search_button))
            } else {
                swipeRefreshLayout!!.isRefreshing = false
                Toast.makeText(this@MainActivity, "O campo de busca não pode estar vazio", Toast.LENGTH_SHORT).show()
            }
        }
        showAdvertising()
    }

    @Suppress("UNUSED_PARAMETER")
    fun signOut(view: View) {
        if (mAuth != null) {
            val alert = AlertDialog.Builder(this)
            alert.setMessage(String.format("Desconectar da conta %s?", Objects.requireNonNull<FirebaseUser>(mAuth!!.currentUser).email))
            alert.setCancelable(false)
            alert.setPositiveButton(android.R.string.yes) { _, _ ->
                mAuth!!.signOut()
                LoginManager.getInstance().logOut()
                this@MainActivity.startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                Toast.makeText(this@MainActivity, "Desconectado", Toast.LENGTH_LONG).show()
                this@MainActivity.finish()
            }
            alert.setNegativeButton(android.R.string.no) { dialogInterface, _ -> dialogInterface.dismiss() }
            alert.show()
        } else {
            Toast.makeText(this@MainActivity, "Falha ao desconectar", Toast.LENGTH_LONG).show()
        }
    }

    private fun showAdvertising() {
        if (LoginActivity.mInterstitialAd.isLoaded) {
            LoginActivity.mInterstitialAd.show()
        } else {
            Log.d(TAG, "The interstitial wasn't loaded yet.")
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun getData(view: View) {
        if (!searchEditText!!.text.toString().trim { it <= ' ' }.isEmpty()) {
            recyclerView!!.adapter = adapter
            progressBar!!.visibility = if (progressBar!!.visibility == View.GONE) View.VISIBLE else View.INVISIBLE
            hideKeyboard()
            val searchService = retrofit!!.create<SearchService>(SearchService::class.java)
            val requestData = searchService.getSearch(searchEditText!!.text.toString())
            requestData.enqueue(object : Callback<StackOverflowSearch> {
                override fun onResponse(call: Call<StackOverflowSearch>, response: Response<StackOverflowSearch>) {
                    progressBar!!.visibility = View.GONE
                    swipeRefreshLayout!!.isRefreshing = false
                    if (response.isSuccessful) {
                        val search = response.body()
                        for (question in Objects.requireNonNull<StackOverflowSearch>(search).items) {
                            val user = question.owner
                            Log.i(TAG, String.format("%s \n%s \n%s", question.title, question.link, user!!.name))
                        }
                        initAdapter(search!!.items)
                    } else {
                        Log.e(TAG, response.code().toString())
                    }
                }

                override fun onFailure(call: Call<StackOverflowSearch>, t: Throwable) {
                    progressBar!!.visibility = View.GONE
                    swipeRefreshLayout!!.isRefreshing = false
                    Log.d(TAG, t.message)
                }
            })
        } else {
            Toast.makeText(this@MainActivity, "O campo de busca não pode estar vazio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initAdapter(questionsList: List<StackOverflowQuestion>) {
        if (!questionsList.isEmpty()) {
            val adapter = SearchAdapter(questionsList)
            recyclerView!!.adapter = adapter
            adapter.setOnItemClickListener(object : SearchAdapter.ItemClickListener {
                override fun onItemClick(position: Int) {
                    val alert = AlertDialog.Builder(this@MainActivity)
                    val title = questionsList[position].title!!.replace("&#39;", "\'").replace("&amp;", "&").replace("&quot;", "\"")
                    alert.setMessage(String.format("Ir para o link da questão: \"%s\" ?", title))
                    alert.setCancelable(true)
                    alert.setPositiveButton(android.R.string.yes) { _, _ ->
                        val url = questionsList[position].link
                        this@MainActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                    alert.setNegativeButton(android.R.string.no) { dialogInterface, _ -> dialogInterface.dismiss() }
                    alert.show()
                }
            })
        } else {
            Toast.makeText(this@MainActivity, "Não foram encontrados resultados para a sua busca", Toast.LENGTH_SHORT).show()
        }
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
