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
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.viewmodel.VideoDetectionVM

class FoundVideosAdapter : RecyclerView.Adapter<FoundVideosAdapter.FoundVideosViewHolder>() {
    private var foundVideos = mutableListOf<VideoDetectionVM.FoundVideo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoundVideosViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.found_videos_list_item, parent, false)
        return FoundVideosViewHolder(itemView)
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

    class FoundVideosViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var name: TextView = itemView.findViewById(R.id.found_video_name)
        private var size: TextView = itemView.findViewById(R.id.found_video_size)

        fun bind(foundVideo: VideoDetectionVM.FoundVideo) {
            name.text = itemView.resources.getString(
                    R.string.found_video_item_name, foundVideo.name, foundVideo.ext)
            size.text = Formatter.formatFileSize(itemView.context, foundVideo.size.toLong())
        }

    }
}