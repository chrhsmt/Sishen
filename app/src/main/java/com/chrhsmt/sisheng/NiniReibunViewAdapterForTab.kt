package com.chrhsmt.sisheng


import android.os.Build
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chrhsmt.sisheng.NiniReibunFragmentForTab.OnListFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_tab_nini_reibun_item.view.*


/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class NiniReibunViewAdapterForTab(
        private val mValues: List<ReibunInfo.ReibunInfoItem>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<NiniReibunViewAdapterForTab.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as ReibunInfo.ReibunInfoItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_tab_nini_reibun_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mIdView.text = Integer.toString(item.id)
        var htmlStr: String = ReibunInfo.replaceNewLineWithBrTag(item.chinese + "\\n" + item.english)
        htmlStr = htmlStr.replace(item.strong_word, "<font color=\"red\">" + item.strong_word + "</font>")
        holder.mContentView.text = fromHtml(htmlStr)

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    fun fromHtml(source: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(source)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_number
        val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
