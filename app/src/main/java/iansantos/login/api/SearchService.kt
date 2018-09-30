package iansantos.login.api

import iansantos.login.model.StackOverflowSearch
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {

    @GET("search?order=desc&sort=activity&site=stackoverflow")
    fun getSearch(@Query("tagged") tagged: String): Call<StackOverflowSearch>

    companion object {
        const val BASE_URL = "https://api.stackexchange.com/2.2/"
    }
}