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

package marabillas.loremar.beedio.browser.adapters

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.databinding.FoundVideosListItemBinding
import marabillas.loremar.beedio.browser.viewmodel.VideoDetectionVM
import marabillas.loremar.beedio.browser.views.ExpandingItemView
import marabillas.loremar.beedio.browser.views.RenameDialog

class FoundVideosAdapter : RecyclerView.Adapter<FoundVideosAdapter.FoundVideosViewHolder>() {
    var eventsListener: EventsListener? = null

    private var foundVideos = mutableListOf<VideoDetectionVM.FoundVideo>()
    private var isSelectionMode = false
    private var expandedViewHolder: FoundVideosViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoundVideosViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<FoundVideosListItemBinding>(
                inflater, R.layout.found_videos_list_item, parent, false)
        return FoundVideosViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return foundVideos.count()
    }

    override fun onBindViewHolder(holder: FoundVideosViewHolder, position: Int) {
        holder.bind(foundVideos[position])
    }

    fun loadData(foundVideos: List<VideoDetectionVM.FoundVideo>) {
        this.foundVideos = foundVideos.toMutableList()
        notifyDataSetChanged()
    }

    fun addItem(foundVideo: VideoDetectionVM.FoundVideo) {
        foundVideos.add(foundVideo)
        notifyItemInserted(foundVideos.lastIndex)
    }

    fun removeItem(index: Int) {
        foundVideos.removeAt(index)
        notifyItemRemoved(index)
    }

    fun switchToSelectionMode(isSelectionMode: Boolean) {
        this.isSelectionMode = isSelectionMode
        notifyDataSetChanged()
    }

    inner class FoundVideosViewHolder(private val binding: FoundVideosListItemBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private val expandingItemView: ExpandingItemView = itemView as ExpandingItemView

        init {
            val onClickListener = this
            binding.apply {
                foundVideoCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    eventsListener?.onItemCheckedChanged(adapterPosition, isChecked)
                }
                foundVideoMore.setOnClickListener(onClickListener)
                foundVideoDelete.setOnClickListener(onClickListener)
                foundVideoRename.setOnClickListener(onClickListener)
                foundVideoDetailsMore.setOnClickListener(onClickListener)
            }
        }

        fun bind(foundVideo: VideoDetectionVM.FoundVideo) {
            expandingItemView.apply {
                measureCollapseExpandHeights = true

                binding.apply {
                    foundVideoName.text = itemView.resources.getString(
                            R.string.found_video_item_name, foundVideo.name, foundVideo.ext)
                    foundVideoSize.text = Formatter.formatFileSize(context, foundVideo.size.toLong())
                    if (isSelectionMode) {
                        foundVideoMore.visibility = GONE
                        foundVideoCheckbox.visibility = VISIBLE
                        foundVideoCheckbox.isChecked = foundVideo.isSelected
                    } else {
                        foundVideoMore.visibility = VISIBLE
                        foundVideoCheckbox.visibility = GONE
                    }
                }

                if (isExpanded) setAsCollapsed()
            }
        }

        override fun onClick(v: View?) {
            when (v) {
                binding.foundVideoMore -> {
                    if (expandingItemView.isExpanded)
                        expandingItemView.collapse()
                    else {
                        if (expandedViewHolder?.expandingItemView?.isExpanded == true)
                            expandedViewHolder?.expandingItemView?.collapse()
                        expandingItemView.expand { expandedViewHolder = this }
                    }
                }
                binding.foundVideoDelete -> {
                    eventsListener?.onItemDelete(adapterPosition)
                }
                binding.foundVideoRename -> {
                    RenameDialog(expandingItemView.context) {
                        eventsListener?.onItemRename(adapterPosition, it)
                    }
                }
            }
        }
    }

    interface EventsListener {
        fun onItemCheckedChanged(index: Int, isChecked: Boolean)
        fun onItemDelete(index: Int)
        fun onItemRename(index: Int, newName: String)
    }
}