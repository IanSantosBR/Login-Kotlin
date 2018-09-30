package iansantos.login.model

import com.google.gson.annotations.SerializedName
import java.util.*

class StackOverflowSearch {
    @SerializedName("items")
    var items: List<StackOverflowQuestion> = ArrayList()
}
