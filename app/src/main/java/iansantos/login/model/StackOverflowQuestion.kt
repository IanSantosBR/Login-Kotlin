package iansantos.login.model

import com.google.gson.annotations.SerializedName

class StackOverflowQuestion {
    @SerializedName("title")
    var title: String? = null
    @SerializedName("link")
    var link: String? = null
    @SerializedName("owner")
    var owner: StackOverflowUser? = null
    @SerializedName("is_answered")
    var isAnswered: Boolean = false
    @SerializedName("tags")
    var tags: List<String>? = null
    @SerializedName("creation_date")
    var creationDate: Long = 0
}
