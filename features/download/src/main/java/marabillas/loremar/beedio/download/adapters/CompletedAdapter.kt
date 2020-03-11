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

package marabillas.loremar.beedio.download.adapters

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import marabillas.loremar.beedio.download.R
import marabillas.loremar.beedio.download.viewmodels.CompletedVM
import javax.inject.Inject

class CompletedAdapter @Inject constructor() : RecyclerView.Adapter<CompletedAdapter.CompletedViewHolder>() {
    var eventListener: EventListener? = null

    private val completedList: MutableList<CompletedItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.completed_item, parent, false)
        return CompletedViewHolder(view)
    }

    override fun getItemCount(): Int = completedList.count()

    override fun onBindViewHolder(holder: CompletedViewHolder, position: Int) {
        holder.bind(completedList[position])
    }

    fun loadData(items: List<String>) {
        completedList.clear()
        items.mapTo(completedList) {
            CompletedItem(it)
        }
        notifyDataSetChanged()
    }

    fun addDetails(details: CompletedVM.CompletedItemMiniDetails) {
        completedList[details.index].apply {
            thumbnail = details.thumbnail
            duration = details.duration
        }
        notifyItemChanged(details.index)
    }

    inner class CompletedViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val title by lazy { getTextView(R.id.completed_title) }
        private val thumbnail by lazy { getImageView(R.id.completed_thumbnail) }
        private val duration by lazy { getTextView(R.id.completed_duration) }
        private val playBtn by lazy { getImageView(R.id.completed_play) }
        private val deleteBtn by lazy { getImageView(R.id.completed_delete) }

        init {
            playBtn.setOnClickListener(this)
        }

        fun bind(item: CompletedItem) {
            title.text = item.filename
            duration.text = item.duration
            val drawable = BitmapDrawable(itemView.resources, item.thumbnail)
            thumbnail.setImageDrawable(drawable)
        }

        override fun onClick(v: View?) {
            when (v) {
                playBtn -> eventListener?.onPlayVideo(completedList[adapterPosition].filename)
            }
        }

        private fun getTextView(resId: Int) = itemView.findViewById<TextView>(resId)
        private fun getImageView(resId: Int) = itemView.findViewById<ImageView>(resId)
    }

    data class CompletedItem(
            val filename: String,
            var thumbnail: Bitmap? = null,
            var duration: String? = null
    )

    interface EventListener {
        fun onPlayVideo(filename: String)
    }
}