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

package marabillas.loremar.beedio.browser.fragment

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.Gravity.CENTER
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.base.extensions.*
import marabillas.loremar.beedio.base.media.VideoDetails
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.viewmodel.VideoDetectionVM
import marabillas.loremar.beedio.extractors.VideoInfo
import marabillas.loremar.beedio.extractors.extractors.youtube.YoutubeVideoInfo
import java.util.*
import javax.inject.Inject

class AllFormatsExtractFragment @Inject constructor() : DaggerFragment(), View.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var videoDetectionVM: VideoDetectionVM

    private val console; get() = constraintLayout(R.id.container_all_formats_extract_console)
    private val messages; get() = textView(R.id.text_all_formats_extract_console_messages)
    private val scrim; get() = view?.findViewById<View>(R.id.scrim_all_formats_extract)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_all_formats_extract, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            videoDetectionVM = ViewModelProvider(it, viewModelFactory).get(VideoDetectionVM::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterAnimation()
    }

    override fun onStart() {
        super.onStart()
        requireView().setOnClickListener(this)
        requireView().imageView(R.id.image_all_formats_extract_close_btn)
                .setOnClickListener(this)
        messages?.text = getString(R.string.extracting_links_for_all_formats)
        requireArguments().getString(ARG_URL)?.let {
            videoDetectionVM.extractAllFormats(it,
                    this::onReceiveReport,
                    this::onExtractionComplete)
        }
    }

    private fun onReceiveReport(report: String) {
        val updated = "${messages?.text}\n$report"
        messages?.text = updated
    }

    private fun onExtractionComplete(videoInfo: VideoInfo) {
        when (videoInfo) {
            is YoutubeVideoInfo -> videoInfo.handleYoutubeVidInfo()
        }
        dismiss()
    }

    private fun YoutubeVideoInfo.handleYoutubeVidInfo() {
        formats?.forEach { format ->
            val name = title
            val url = format.url
            val ext = format.ext
            val sourceWebpage = requireArguments().getString(ARG_URL)
            if (name != null && url != null && ext != null && sourceWebpage != null) {
                val foundVideo = VideoDetectionVM.FoundVideo(name = name,
                        url = url,
                        ext = ext,
                        size = (format.filesize ?: 0).toString(),
                        sourceWebPage = sourceWebpage,
                        sourceWebsite = "",
                        details = VideoDetails(title = title,
                                vcodec = format.vcodec,
                                acodec = format.acodec,
                                duration = duration?.toLong()?.toString()?.formatDuration(),
                                width = format.width?.toString(),
                                height = format.height?.toString(),
                                artist = artist,
                                album = album,
                                track = track))
                videoDetectionVM.addFoundVideo(foundVideo)
            }
        }
        Snackbar.make(requireActivity().rootView(),
                "Extracted ${formats?.count() ?: 0} links",
                Snackbar.LENGTH_SHORT)
                .apply {
                    view.updateLayoutParams<FrameLayout.LayoutParams> {
                        gravity = CENTER
                    }
                }
                .show()
    }

    private fun String.formatDuration(): String? {
        return try {
            val totalSecs = toLong() / 1000
            val mils = totalSecs % 1000
            val s = totalSecs % 60
            val m = totalSecs / 60 % 60
            val h = totalSecs / (60 * 60) % 24
            String.format(Locale.US, "%02d:%02d:%02d.%d", h, m, s, mils)
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun onClick(v: View?) {
        when {
            v == requireView() -> dismiss()
            v?.id == R.id.image_all_formats_extract_close_btn -> dismiss()
        }
    }

    private fun dismiss() = exitAnimation()

    private fun enterAnimation() {
        val transition = AutoTransition().apply {
            this.addListener(object : TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition) {
                    console?.updateLayoutParams<FrameLayout.LayoutParams> { height = WRAP_CONTENT }
                }
            })
        }
        console?.post {
            TransitionManager.beginDelayedTransition(view as ViewGroup, transition)
            console?.updateLayoutParams<FrameLayout.LayoutParams> { height = 256.toPixels(resources) }
        }
        console?.updateLayoutParams<FrameLayout.LayoutParams> { height = 0 }

        ValueAnimator.ofFloat(0f, 0.4f).apply {
            addUpdateListener {
                scrim?.alpha = animatedValue as Float
            }
            start()
        }
    }

    private fun exitAnimation() {
        val removeFragment = {
            parentFragmentManager.beginTransaction()
                    .remove(this)
                    .commit()
        }
        val transition = AutoTransition().apply {
            addListener(object : TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition) {
                    removeFragment()
                }
            })
        }
        view?.let { v ->
            TransitionManager.beginDelayedTransition(v as ViewGroup, transition)
            console?.updateLayoutParams<FrameLayout.LayoutParams> { height = 0 }

            ValueAnimator.ofFloat(0.4f, 0f).apply {
                addUpdateListener {
                    scrim?.alpha = animatedValue as Float
                }
                start()
            }
        }


    }

    companion object {
        const val ARG_URL = "ARG URL"
    }
}