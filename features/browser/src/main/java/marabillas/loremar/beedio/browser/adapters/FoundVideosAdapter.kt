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

import android.graphics.drawable.BitmapDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.format.Formatter
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import marabillas.loremar.beedio.base.media.VideoDetails
import marabillas.loremar.beedio.base.media.VideoDetailsFetcher
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.databinding.FoundVideosListItemBinding
import marabillas.loremar.beedio.browser.viewmodel.VideoDetectionVM
import marabillas.loremar.beedio.browser.views.ExpandingItemView
import marabillas.loremar.beedio.sharedui.RenameDialog
import timber.log.Timber

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
                foundVideoDownload.setOnClickListener(onClickListener)
            }
        }

        fun bind(foundVideo: VideoDetectionVM.FoundVideo) {
            expandingItemView.apply {
                setIconToDefault()

                binding.apply {
                    foundVideoName.text = itemView.resources.getString(
                            R.string.found_video_item_name, foundVideo.name, foundVideo.ext)

                    if (foundVideo.size == "0")
                        foundVideoSize.text = context.getString(R.string.unknown_size)
                    else
                        foundVideoSize.text = Formatter.formatFileSize(context, foundVideo.size.toLong())

                    if (isSelectionMode) {
                        foundVideoMore.visibility = GONE
                        foundVideoCheckbox.visibility = VISIBLE
                        foundVideoCheckbox.isChecked = foundVideo.isSelected
                    } else {
                        foundVideoMore.visibility = VISIBLE
                        foundVideoCheckbox.visibility = GONE
                    }

                    when {
                        foundVideo.isFetchingDetails -> showFetchingDetails()
                        foundVideo.details == null -> showNoDetails()
                        else -> foundVideo.details?.showDetails()
                    }
                }

                if (isExpanded)
                    setAsCollapsed()

                measureExpandHeight()
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
                    val context = expandingItemView.context
                    val hint = context.getString(R.string.enter_new_title)
                    RenameDialog(context, hint) {
                        eventsListener?.onItemRename(adapterPosition, it)
                    }
                }
                binding.foundVideoDetailsMore -> {
                    binding.apply {
                        foundVideoDetailsMore.isVisible = false
                        foundVideoDetailsProgress.isVisible = true
                        val targetPosition = adapterPosition
                        eventsListener?.onFetchDetails(adapterPosition, object : VideoDetailsFetcher.FetchListener {
                            override fun onUnFetched(error: Throwable) {
                                if (targetPosition == adapterPosition) {
                                    Timber.e(error)
                                    showNoDetails()
                                    expandingItemView.apply {
                                        setAsExpanded()
                                        measureExpandHeight()
                                    }
                                }
                            }

                            override fun onFetched(details: VideoDetails) {
                                if (targetPosition == adapterPosition) {
                                    foundVideos[adapterPosition].details = details
                                    details.showDetails()
                                    expandingItemView.apply {
                                        setAsExpanded()
                                        measureExpandHeight()
                                    }
                                }
                            }
                        })
                    }
                }
                binding.foundVideoDownload -> {
                    eventsListener?.onDownloadItem(adapterPosition)
                }
            }
        }

        private fun VideoDetails.buildDetailsText(): Spannable {
            return SpannableStringBuilder().apply {
                appendDetail("Filename: ", filename)
                appendDetail("Title: ", title)
                appendDetail("vcodec: ", vcodec ?: "none")
                appendDetail("acodec: ", acodec ?: "none")
                appendDetail("Duration: ", duration)
                appendDetail("Filesize: ", filesize)
                appendDetail("Width: ", width)
                appendDetail("Height: ", height)
                appendDetail("Bitrate: ", bitrate)
                appendDetail("Framerate: ", framerate)
                appendDetail("Encoder: ", encoder)
                appendDetail("Encoded By: ", encodedBy)
                appendDetail("Date: ", date)
                appendDetail("Creation Time: ", creationTime)
                appendDetail("Artist: ", artist)
                appendDetail("Album: ", album)
                appendDetail("Album Artist: ", albumArtist)
                appendDetail("Track: ", track)
                appendDetail("Genre: ", genre)
                appendDetail("Composer: ", composer)
                appendDetail("Performer: ", performer)
                appendDetail("Copyright: ", copyright)
                appendDetail("Publisher: ", publisher)
                appendDetail("Language: ", language)
            }
        }

        private fun SpannableStringBuilder.appendDetail(entryLabel: String, entryValue: String?) {
            entryValue?.let {
                append(SpannableString(entryLabel).style()).appendln(it)
            }
        }

        private fun Spannable.style(): Spannable {
            val color = ResourcesCompat.getColor(itemView.resources, R.color.yellow, null)
            val span = ForegroundColorSpan(color)
            setSpan(span, 0, length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            return this
        }

        private fun VideoDetails.showDetails() {
            binding.apply {
                foundVideoDetailsProgress.isVisible = false
                foundVideoDetailsMore.isVisible = false
                foundVideoDetailsText.apply {
                    text = buildDetailsText()
                    isVisible = true
                }
                if (vcodec == null && acodec != null)
                    setIconToNoVideo()
                else if (vcodec != null && acodec == null)
                    setIconToNoAudio()

                foundVideoDetailsThumbnail.apply {
                    isVisible = false
                    if (thumbnail != null) {
                        val src = BitmapDrawable(itemView.resources, thumbnail)
                        setImageDrawable(src)
                        isVisible = true
                    }
                }
            }
        }

        private fun showNoDetails() {
            binding.apply {
                foundVideoDetailsProgress.isVisible = false
                foundVideoDetailsThumbnail.isVisible = false
                foundVideoDetailsText.isVisible = false
                foundVideoDetailsMore.isVisible = true
            }
        }

        private fun showFetchingDetails() {
            binding.apply {
                foundVideoDetailsProgress.isVisible = true
                foundVideoDetailsThumbnail.isVisible = false
                foundVideoDetailsText.isVisible = false
                foundVideoDetailsMore.isVisible = false
            }
        }

        private fun setIconToNoAudio() {
            binding.foundVideoIcon.apply {
                background = ResourcesCompat.getDrawable(itemView.resources, R.drawable.green_icon_background, null)
                val src = ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_noaudio, null)
                setImageDrawable(src)
            }
        }

        private fun setIconToNoVideo() {
            binding.foundVideoIcon.apply {
                background = ResourcesCompat.getDrawable(itemView.resources, R.drawable.blue_icon_background, null)
                val src = ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_audio_black_24dp, null)
                setImageDrawable(src)
            }
        }

        private fun setIconToDefault() {
            binding.foundVideoIcon.apply {
                background = ResourcesCompat.getDrawable(itemView.resources, R.drawable.yellow_icon_background, null)
                val src = ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_video_black_24dp, null)
                setImageDrawable(src)
            }
        }
    }

    interface EventsListener {
        fun onItemCheckedChanged(index: Int, isChecked: Boolean)
        fun onItemDelete(index: Int)
        fun onItemRename(index: Int, newName: String)
        fun onFetchDetails(index: Int, fetchListener: VideoDetailsFetcher.FetchListener)
        fun onDownloadItem(index: Int)
    }
}