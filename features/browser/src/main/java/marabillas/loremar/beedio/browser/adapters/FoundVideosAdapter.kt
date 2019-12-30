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

import android.animation.ValueAnimator
import android.graphics.Typeface
import android.text.format.Formatter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.databinding.FoundVideosListItemBinding
import marabillas.loremar.beedio.browser.viewmodel.VideoDetectionVM
import kotlin.math.roundToInt

class FoundVideosAdapter : RecyclerView.Adapter<FoundVideosAdapter.FoundVideosViewHolder>() {
    var eventsListener: EventsListener? = null

    private var foundVideos = mutableListOf<VideoDetectionVM.FoundVideo>()
    private var isSelectionMode = false

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

    fun switchToSelectionMode(isSelectionMode: Boolean) {
        this.isSelectionMode = isSelectionMode
        notifyDataSetChanged()
    }

    inner class FoundVideosViewHolder(private val binding: FoundVideosListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        private var origHeight = 0
        private var expandedHeight = 0
        private var isExpanded = false

        init {
            binding.apply {
                foundVideoCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    eventsListener?.onItemCheckedChanged(adapterPosition, isChecked)
                }
                foundVideoMore.setOnClickListener {
                    if (isExpanded)
                        collapseItem()
                    else
                        expandItem()
                }
            }
        }

        fun bind(foundVideo: VideoDetectionVM.FoundVideo) {
            binding.apply {
                foundVideoName.text = itemView.resources.getString(
                        R.string.found_video_item_name, foundVideo.name, foundVideo.ext)
                foundVideoSize.text = Formatter.formatFileSize(itemView.context, foundVideo.size.toLong())
                if (isSelectionMode) {
                    foundVideoMore.visibility = GONE
                    foundVideoCheckbox.visibility = VISIBLE
                    foundVideoCheckbox.isChecked = foundVideo.isSelected
                } else {
                    foundVideoMore.visibility = VISIBLE
                    foundVideoCheckbox.visibility = GONE
                }
            }

            itemView.apply {
                doOnLayout {
                    origHeight = measuredHeight
                    setContentsVisibility(true)
                    doOnNextLayout {
                        expandedHeight = measuredHeight
                        doOnPreDraw { setContentsVisibility(false) }
                    }
                }
            }
        }

        private fun expandItem() {
            binding.apply {
                foundVideoSize.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topToBottom = -1
                    bottomToBottom = PARENT_ID
                    bottomMargin = (8 * itemView.resources.displayMetrics.density).roundToInt()
                }
                foundVideoName.apply {
                    setTypeface(typeface, Typeface.BOLD)
                }
            }

            ValueAnimator.ofFloat(0f, 1f).apply {
                addUpdateListener {
                    val value = it.animatedValue as Float
                    itemView.apply {
                        updateLayoutParams<ViewGroup.LayoutParams> {
                            height = (origHeight + value * (expandedHeight - origHeight)).roundToInt()
                        }
                    }
                    transformPadding(value)
                    transformIcon(value)
                }
                doOnStart { setContentsVisibility(true) }
                doOnEnd {
                    setContentsVisibility(true)
                    isExpanded = true
                    binding.foundVideoSize.apply {
                        visibility = GONE
                        updateLayoutParams<ConstraintLayout.LayoutParams> {
                            startToEnd = -1
                            startToStart = PARENT_ID
                            marginStart = 0
                        }
                    }
                    showSize()
                }
            }.start()
        }

        private fun collapseItem() {
            ValueAnimator.ofFloat(1f, 0f).apply {
                addUpdateListener {
                    val value = it.animatedValue as Float
                    itemView.apply {
                        updateLayoutParams<ViewGroup.LayoutParams> {
                            height = (origHeight + value * (expandedHeight - origHeight)).roundToInt()
                        }
                    }
                    transformPadding(value)
                    transformIcon(value)
                }
                doOnEnd {
                    setContentsVisibility(false)
                    isExpanded = false
                    binding.apply {
                        foundVideoSize.apply {
                            visibility = GONE
                            updateLayoutParams<ConstraintLayout.LayoutParams> {
                                startToStart = -1
                                startToEnd = R.id.found_video_icon
                                marginStart = (16 * itemView.resources.displayMetrics.density).roundToInt()
                                bottomToBottom = -1
                                topToBottom = R.id.found_video_name
                                bottomMargin = 0
                            }
                        }
                        foundVideoName.apply {
                            typeface = Typeface.create(typeface, Typeface.NORMAL)
                        }
                        showSize()
                    }
                }
            }.start()
        }

        private fun transformPadding(value: Float) {
            itemView.apply {
                val px8 = (8 * resources.displayMetrics.density).roundToInt()
                val px16 = (16 * resources.displayMetrics.density).roundToInt()
                val newPadding = (px16 - (px16 - px8) * value).roundToInt()
                setPadding(newPadding, px16, newPadding, px16)
            }
        }

        private fun transformIcon(value: Float) {
            binding.foundVideoIcon.apply {
                val px32 = (32 * resources.displayMetrics.density).roundToInt()
                val px40 = (40 * resources.displayMetrics.density).roundToInt()
                val newIconSize = (px32 + (px40 - px32) * value).roundToInt()
                updateLayoutParams<ViewGroup.LayoutParams> {
                    width = newIconSize
                    height = newIconSize
                }
                setPadding((4 + 4 * value).roundToInt())
            }
        }

        private fun setContentsVisibility(isVisible: Boolean) {
            val visibility = if (isVisible) VISIBLE else GONE
            binding.apply {
                foundVideoContentSpace.visibility = visibility
                foundVideoDelete.visibility = visibility
                foundVideoRename.visibility = visibility
                foundVideoDownload.visibility = visibility
            }
        }

        private fun showSize() {
            TransitionManager.beginDelayedTransition(itemView as ViewGroup, Slide(Gravity.BOTTOM))
            binding.foundVideoSize.visibility = VISIBLE
        }
    }

    interface EventsListener {
        fun onItemCheckedChanged(index: Int, isChecked: Boolean)
    }
}