/*
 * Beedio is an Android app for downloading videos
 * Copyright (C) 2019 Loremar Marabillas
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.beedio.history

import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import marabillas.loremar.beedio.base.database.HistoryItem
import marabillas.loremar.beedio.base.extensions.imageView
import marabillas.loremar.beedio.base.extensions.textView
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.net.URL
import javax.inject.Inject

class HistoryAdapter @Inject constructor() :
        RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    var historyList = mutableListOf<HistoryItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var itemEventListener: ItemEventListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int = historyList.count()

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            itemView.imageView(R.id.image_delete_btn).setOnClickListener {
                itemEventListener?.onItemDelete(historyList[adapterPosition])
                historyList.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)

            }

            itemView.setOnClickListener {
                itemEventListener?.onItemClicked(historyList[adapterPosition])
            }
        }

        fun bind(item: HistoryItem) {
            itemView.apply {
                item.favicon?.let {
                    val drawable = BitmapDrawable(itemView.resources, it)
                    imageView(R.id.image_history_item_icon).setImageDrawable(drawable)
                }

                val formatter = when {
                    item.date.toLocalDate().isEqual(LocalDate.now()) -> DateTimeFormatter.ofPattern("h:mm a")
                    item.date.year == LocalDate.now().year -> DateTimeFormatter.ofPattern("LLL d")
                    else -> DateTimeFormatter.ofPattern("LLL d, yyyy")
                }

                textView(R.id.text_history_item_date_time).text = formatter.format(item.date)

                textView(R.id.text_history_item_title).text = item.title

                textView(R.id.text_history_item_url).text = URL(item.url).host
            }
        }
    }

    interface ItemEventListener {
        fun onItemDelete(item: HistoryItem)
        fun onItemClicked(item: HistoryItem)
    }
}