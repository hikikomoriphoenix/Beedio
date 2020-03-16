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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import marabillas.loremar.beedio.base.extensions.imageView
import marabillas.loremar.beedio.base.extensions.textView
import marabillas.loremar.beedio.download.R
import marabillas.loremar.beedio.download.viewmodels.InactiveVM
import javax.inject.Inject

class InactiveAdapter @Inject constructor() : RecyclerView.Adapter<InactiveAdapter.InactiveViewHolder>() {
    var eventListener: EventListener? = null

    private var inactiveList = mutableListOf<InactiveVM.InactiveItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InactiveViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.inactive_item, parent, false)
        return InactiveViewHolder(view)
    }

    override fun getItemCount(): Int = inactiveList.count()

    override fun onBindViewHolder(holder: InactiveViewHolder, position: Int) {
        holder.bind(inactiveList[position])
    }

    fun load(items: List<InactiveVM.InactiveItem>) {
        inactiveList = items.toMutableList()
        notifyDataSetChanged()
    }

    fun removeItem(index: Int) {
        inactiveList.removeAt(index)
        notifyItemRemoved(index)
    }

    fun clearList() {
        inactiveList.clear()
        notifyDataSetChanged()
    }

    inner class InactiveViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val title: TextView by lazy { itemView.textView(R.id.inactive_title) }
        private val downloaded: TextView by lazy { itemView.textView(R.id.inactive_downloaded) }
        private val removeBtn: ImageView by lazy { itemView.imageView(R.id.inactive_remove) }

        init {
            removeBtn.setOnClickListener(this)
            itemView.setOnClickListener(this)
        }

        fun bind(item: InactiveVM.InactiveItem) {
            title.text = item.filename
            downloaded.text = item.downloaded
        }

        override fun onClick(v: View?) {
            when (v) {
                removeBtn -> eventListener?.onRemoveItem(adapterPosition)
                itemView -> eventListener?.onGoToSourcePage(
                        adapterPosition,
                        inactiveList[adapterPosition].sourceWebpage
                )
            }
        }
    }

    interface EventListener {
        fun onRemoveItem(index: Int)
        fun onGoToSourcePage(index: Int, sourcePage: String)
    }
}