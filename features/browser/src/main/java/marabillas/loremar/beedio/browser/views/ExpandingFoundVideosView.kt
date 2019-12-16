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

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.transition.*
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.databinding.FoundVideosSheetBinding

class ExpandingFoundVideosView : FrameLayout {

    private lateinit var sheet: ViewGroup
    private lateinit var head: ViewGroup
    private lateinit var body: ViewGroup
    private var isExpanded = false
    private var headContractedLeft = 0
    private val animationDuration = 200L

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    private fun initView() {
        val inflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<FoundVideosSheetBinding>(
                inflater, R.layout.found_videos_sheet, null, false)
                .apply {
                    sheet = foundVideosSheet
                    head = foundVideosHead
                    body = foundVideosBody
                }
        LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
            binding.root.layoutParams = this
            addView(binding.root)
        }
        doOnLayout { headContractedLeft = binding.foundVideosHead.measuredWidth }

        head.setOnClickListener {
            if (!isExpanded)
                expand()
            else
                contract()
        }

        (binding.foundVideosBouncyIcon.drawable as AnimationDrawable).start()
    }

    private fun expand() {
        body.visibility = VISIBLE
        initTransition { isExpanded = true }
        sheet.updateLayout { height = MATCH_PARENT }
        animateHead(head.left, 0, MATCH_PARENT, false)
    }

    private fun contract() {
        initTransition {
            isExpanded = false
            body.visibility = GONE
        }
        sheet.updateLayout { height = WRAP_CONTENT }
        animateHead(0, headContractedLeft, WRAP_CONTENT, true)
    }

    private fun initTransition(doOnEnd: () -> Unit) {
        val sheetTransition = ChangeBounds().apply { addTarget(sheet) }
        val contentTransition = ChangeBounds().apply { addTarget(body) }
        val transitionSet = TransitionSet().apply {
            addTransition(sheetTransition)
            addTransition(contentTransition)
            duration = animationDuration
            doOnEnd { doOnEnd() }
        }
        TransitionManager.beginDelayedTransition(this, transitionSet)
    }

    private fun animateHead(startLeft: Int, endLeft: Int, endWidth: Int, isDelay: Boolean) {
        head.apply {
            ObjectAnimator.ofInt(this, "left", startLeft, endLeft).apply {
                if (isDelay) startDelay = animationDuration * 3 / 5
                duration = animationDuration * 2 / 5
                doOnEnd { updateLayout { width = endWidth } }
                start()
            }
        }
    }

    private fun View.updateLayout(block: ViewGroup.LayoutParams.() -> Unit) {
        updateLayoutParams<ViewGroup.LayoutParams> {
            block()
        }
    }

    private fun Transition.doOnEnd(block: TransitionListenerAdapter.() -> Unit) {
        addListener(object : TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                block()
            }
        })
    }
}