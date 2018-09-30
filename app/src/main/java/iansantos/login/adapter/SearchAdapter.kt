package iansantos.login.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import iansantos.login.R
import iansantos.login.model.StackOverflowQuestion
import java.text.DateFormat
import java.util.*

class SearchAdapter(private val questionList: List<StackOverflowQuestion>) : androidx.recyclerview.widget.RecyclerView.Adapter<SearchAdapter.QuestionViewHolder>() {
    private var itemClickListener: ItemClickListener? = null

    fun setOnItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): QuestionViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.questions_list, viewGroup, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(questionViewHolder: QuestionViewHolder, i: Int) {
        val question = questionList[i]
        val title = question.title!!.replace("&#39;", "\'").replace("&amp;", "&").replace("&quot;", "\"")
        questionViewHolder.title.text = title
        questionViewHolder.link.text = question.link
        questionViewHolder.name.text = question.owner!!.name
        questionViewHolder.tags.text = TextUtils.join(", ", question.tags!!)
        questionViewHolder.isAnswered.setText(if (question.isAnswered) R.string.answered else R.string.not_answered)
        val date = Date(question.creationDate * 1000L)
        val formattedDate = DateFormat.getDateTimeInstance().format(date)
        questionViewHolder.creationDate.text = formattedDate
    }

    override fun getItemCount(): Int {
        return questionList.size
    }

    interface ItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class QuestionViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view), View.OnClickListener {
        var title: TextView = view.findViewById(R.id.title_textView)
        var link: TextView = view.findViewById(R.id.link_textView)
        var name: TextView = view.findViewById(R.id.name_textView)
        var isAnswered: TextView = view.findViewById(R.id.is_answered_textView)
        var tags: TextView = view.findViewById(R.id.tags_textView)
        var creationDate: TextView = view.findViewById(R.id.creation_date_textView)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (itemClickListener != null) {
                itemClickListener!!.onItemClick(adapterPosition)
            }
        }
    }
}
