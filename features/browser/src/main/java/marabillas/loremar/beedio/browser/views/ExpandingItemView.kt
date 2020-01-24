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

package marabillas.loremar.beedio.browser.views

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.transition.Slide
import androidx.transition.TransitionManager
import marabillas.loremar.beedio.browser.R
import kotlin.math.roundToInt

class ExpandingItemView(context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    var isExpanded = false; private set

    private var visibleDetailsHeight = 0
    private var initialDetailsVisibility = View.GONE
    private var origHeight = 0
    private var expandHeight = 0

    private val nameView: TextView by lazy { findViewById<TextView>(R.id.found_video_name) }
    private val detailsView: FrameLayout by lazy { findViewById<FrameLayout>(R.id.found_video_details) }
    private val iconView: ImageView by lazy { findViewById<ImageView>(R.id.found_video_icon) }
    private val sizeView: TextView by lazy { findViewById<TextView>(R.id.found_video_size) }
    private val deleteBtn: ImageView by lazy { findViewById<ImageView>(R.id.found_video_delete) }
    private val renameBtn: ImageView by lazy { findViewById<ImageView>(R.id.found_video_rename) }
    private val downloadBtn: ImageView by lazy { findViewById<ImageView>(R.id.found_video_download) }

    fun measureExpandHeight() {
        initialDetailsVisibility = detailsView.visibility
        detailsView.visibility = View.INVISIBLE
        visibility = View.INVISIBLE
        requestLayout()
        postDelayed(20) {
            visibleDetailsHeight = detailsView.height
            detailsView.visibility = initialDetailsVisibility
            visibility = View.VISIBLE
            requestLayout()
            postDelayed(20) {
                if (!detailsView.isVisible)
                    origHeight = height
                val topHeight = maxOf(nameView.height, iconView.height)
                val otherHeights = (72 * resources.displayMetrics.density).roundToInt()

                expandHeight = topHeight + visibleDetailsHeight + otherHeights
                requestLayout()
            }
        }
    }

    fun setAsCollapsed() {
        val color = ResourcesCompat.getColor(resources, R.color.black1, null)
        background = ColorDrawable(color)

        iconView.apply {
            updateLayoutParams<ViewGroup.LayoutParams> {
                width = (32 * resources.displayMetrics.density).roundToInt()
                height = (32 * resources.displayMetrics.density).roundToInt()
            }
            setPadding(4)
        }
        nameView.apply {
            typeface = Typeface.create(typeface, Typeface.NORMAL)
        }
        sizeView.visibility = View.VISIBLE

        resetVideoSizeLayoutParamsOnCollapsedPosition()
        val padding = (16 * resources.displayMetrics.density).roundToInt()
        setPadding(padding, padding, padding, padding)
        updateLayoutParams<ViewGroup.LayoutParams> {
            height = WRAP_CONTENT
        }
        setContentsCollapsed(true)
        isExpanded = false
    }

    fun setAsExpanded() {
        val color = ResourcesCompat.getColor(resources, R.color.black2, null)
        background = ColorDrawable(color)

        iconView.apply {
            updateLayoutParams<ViewGroup.LayoutParams> {
                width = (40 * resources.displayMetrics.density).roundToInt()
                height = (40 * resources.displayMetrics.density).roundToInt()
            }
            setPadding(8)
        }
        nameView.apply {
            setTypeface(typeface, Typeface.BOLD)
        }
        sizeView.visibility = View.VISIBLE

        updateVideoSizeLayoutParamsOnExpandedPosition()
        val padding = (8 * resources.displayMetrics.density).roundToInt()
        setPadding(padding, padding, padding, padding)
        updateLayoutParams<ViewGroup.LayoutParams> {
            height = WRAP_CONTENT
        }
        setContentsCollapsed(false)
        isExpanded = true
    }

    fun expand(doAfter: () -> Unit) {
        val argbEvaluator = ArgbEvaluator()
        val color1 = ResourcesCompat.getColor(resources, R.color.black1, null)
        val color2 = ResourcesCompat.getColor(resources, R.color.black2, null)

        ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                val value = it.animatedValue as Float
                updateLayoutParams<ViewGroup.LayoutParams> {
                    height = (origHeight + value * (expandHeight - origHeight)).roundToInt()
                }
                val color = argbEvaluator.evaluate(value, color1, color2) as Int
                background = ColorDrawable(color)
                transformPadding(value)
                transformIcon(value)
            }
            doOnStart {
                sizeView.visibility = View.GONE
                nameView.apply {
                    setTypeface(typeface, Typeface.BOLD)
                }
                detailsView.isVisible = true
            }
            doOnEnd {
                isExpanded = true
                doAfter()

                updateVideoSizeLayoutParamsOnExpandedPosition()
                showTools()
                showSize()
            }
        }.start()
    }

    fun collapse() {
        val argbEvaluator = ArgbEvaluator()
        val color1 = ResourcesCompat.getColor(resources, R.color.black1, null)
        val color2 = ResourcesCompat.getColor(resources, R.color.black2, null)

        sizeView.visibility = View.GONE

        val collapse = ValueAnimator.ofFloat(1f, 0f).apply {
            addUpdateListener {
                val value = it.animatedValue as Float
                updateLayoutParams<ViewGroup.LayoutParams> {
                    height = (origHeight + value * (expandHeight - origHeight)).roundToInt()
                }
                val color = argbEvaluator.evaluate(value, color1, color2) as Int
                background = ColorDrawable(color)
                transformPadding(value)
                transformIcon(value)
            }
            doOnEnd {
                isExpanded = false
                resetVideoSizeLayoutParamsOnCollapsedPosition()
                nameView.apply {
                    typeface = Typeface.create(typeface, Typeface.NORMAL)
                }
                detailsView.isVisible = false
                showSize()
                updateLayoutParams<ViewGroup.LayoutParams> {
                    height = WRAP_CONTENT
                }
            }
        }

        hideTools { collapse.start() }
    }

    private fun transformPadding(value: Float) {
        val px8 = (8 * resources.displayMetrics.density).roundToInt()
        val px16 = (16 * resources.displayMetrics.density).roundToInt()
        val newPadding = (px16 - (px16 - px8) * value).roundToInt()
        setPadding(newPadding, px16, newPadding, px16)
    }

    private fun transformIcon(value: Float) {
        iconView.apply {
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

    private fun setContentsCollapsed(isTrue: Boolean) {
        detailsView.isVisible = !isTrue
        deleteBtn.isVisible = !isTrue
        renameBtn.isVisible = !isTrue
        downloadBtn.isVisible = !isTrue

        deleteBtn.translationX = if (isTrue) 0f else 48f
        downloadBtn.translationX = if (isTrue) 0f else -48f
    }

    private fun showSize() {
        val transition = Slide(Gravity.BOTTOM)
        TransitionManager.beginDelayedTransition(this, transition)
        sizeView.isVisible = true
    }

    private fun resetVideoSizeLayoutParamsOnCollapsedPosition() {
        sizeView.updateLayoutParams<LayoutParams> {
            startToStart = -1
            startToEnd = R.id.found_video_icon
            marginStart = (16 * resources.displayMetrics.density).roundToInt()
            bottomToBottom = -1
            topToBottom = R.id.found_video_name
            bottomMargin = 0
        }
    }

    private fun updateVideoSizeLayoutParamsOnExpandedPosition() {
        sizeView.updateLayoutParams<LayoutParams> {
            topToBottom = -1
            bottomToBottom = LayoutParams.PARENT_ID
            bottomMargin = (8 * resources.displayMetrics.density).roundToInt()
            startToEnd = -1
            startToStart = LayoutParams.PARENT_ID
            marginStart = 0
        }
    }

    private fun showTools() {
        deleteBtn.isVisible = true
        renameBtn.isVisible = true
        downloadBtn.isVisible = true

        ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                val value = it.animatedValue as Float
                deleteBtn.translationX = value * (48 * resources.displayMetrics.density)
                downloadBtn.translationX = -value * (48 * resources.displayMetrics.density)
            }
            duration = 200
        }.start()
    }

    private fun hideTools(actionAfterHiding: () -> Unit) {
        ValueAnimator.ofFloat(1f, 0f).apply {
            addUpdateListener {
                val value = it.animatedValue as Float
                deleteBtn.translationX = value * (48 * resources.displayMetrics.density)
                downloadBtn.translationX = -value * (48 * resources.displayMetrics.density)
            }
            doOnEnd {
                deleteBtn.isVisible = false
                renameBtn.isVisible = false
                downloadBtn.isVisible = false
                actionAfterHiding()
            }
            duration = 200
        }.start()
    }
}