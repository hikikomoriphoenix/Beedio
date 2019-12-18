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

package marabillas.loremar.beedio.browser.uicontrollers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.browser.viewmodel.VideoDetectionVM
import marabillas.loremar.beedio.browser.views.ExpandingFoundVideosView
import javax.inject.Inject

class ExpandingFoundVideosFragment @Inject constructor() : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var videoDetectionVM: VideoDetectionVM
    private lateinit var foundVideosView: ExpandingFoundVideosView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return context?.let {
            ExpandingFoundVideosView(it).apply {
                CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                    layoutParams = this
                }
                foundVideosView = this
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.apply {
            videoDetectionVM = ViewModelProvider(this::getViewModelStore, viewModelFactory)
                    .get(VideoDetectionVM::class.java)

            val activity = this
            videoDetectionVM.apply {
                observeIsAnalyzing(activity, Observer { onIsAnalyzingChanged(it) })
                receiveForFoundVideo(activity, Observer { onVideoFound(it) })
            }
        }
    }

    private fun onIsAnalyzingChanged(isAnalyzing: Boolean) {
        foundVideosView.animateBouncingBug(isAnalyzing)
    }

    private fun onVideoFound(video: VideoDetectionVM.FoundVideo) {
        val count = videoDetectionVM.foundVideos.count()
        foundVideosView.updateFoundVideosCountText(count)
    }
}