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
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

class HistoryAdapter @Inject constructor() :
        RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    var historyList = listOf<HistoryItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

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
        fun bind(item: HistoryItem) {
            itemView.apply {
                item.favicon?.let {
                    val drawable = BitmapDrawable(itemView.resources, it)
                    imageView(R.id.image_history_item_icon).setImageDrawable(drawable)
                }
                textView(R.id.text_history_item_title).text = item.title
                textView(R.id.text_history_item_url).text = item.url

                val formatter = DateTimeFormatter.RFC_1123_DATE_TIME
                textView(R.id.text_history_item_date_time).text = formatter.format(item.date)
            }
        }
    }
}