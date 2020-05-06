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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.base.extensions.textView
import marabillas.loremar.beedio.base.media.VideoDetailsFetcher
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.adapters.FoundVideosAdapter
import marabillas.loremar.beedio.browser.fragment.AllFormatsExtractFragment
import marabillas.loremar.beedio.browser.viewmodel.BrowserTitleStateVM
import marabillas.loremar.beedio.browser.viewmodel.VideoDetectionVM
import marabillas.loremar.beedio.browser.viewmodel.WebViewsControllerVM
import marabillas.loremar.beedio.browser.views.ExpandingFoundVideosView
import java.net.URL
import javax.inject.Inject

class ExpandingFoundVideosFragment @Inject constructor() : DaggerFragment(),
        ExpandingFoundVideosView.ToolBarEventsListener, FoundVideosAdapter.EventsListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var allFormatsExtractFragment: AllFormatsExtractFragment

    private lateinit var videoDetectionVM: VideoDetectionVM
    private lateinit var webViewsControllerVM: WebViewsControllerVM
    private lateinit var titleStateVM: BrowserTitleStateVM
    private lateinit var foundVideosView: ExpandingFoundVideosView
    private lateinit var foundVideosRecyclerView: RecyclerView
    private val foundVideosAdapter = FoundVideosAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return context?.let {
            val toolbarListener = this
            ExpandingFoundVideosView(it).apply {
                CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                    layoutParams = this
                }
                foundVideosView = this
                foundVideosRecyclerView = findViewById<RecyclerView>(R.id.found_videos_recycler_view).apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = foundVideosAdapter
                }
                toolbarEventsListener = toolbarListener
                id = View.generateViewId()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.apply {
            videoDetectionVM = ViewModelProvider(this::getViewModelStore, viewModelFactory)
                    .get(VideoDetectionVM::class.java)
            webViewsControllerVM = ViewModelProvider(this::getViewModelStore, viewModelFactory)
                    .get(WebViewsControllerVM::class.java)
            titleStateVM = ViewModelProvider(this::getViewModelStore, viewModelFactory)
                    .get(BrowserTitleStateVM::class.java)

            val activity = this
            videoDetectionVM.apply {
                observeIsAnalyzing(activity, Observer { onIsAnalyzingChanged(it) })
                receiveForFoundVideo(activity, Observer { onVideoFound(it) })
            }
        }
    }

    override fun onStart() {
        super.onStart()
        foundVideosAdapter.eventsListener = this

        titleStateVM.observeUrl(this, Observer {
            val host = URL(it).host
            if (videoDetectionVM.isAllFormatsExtractionSupported(host)) {
                textView(R.id.found_video_menu_all_formats)?.isVisible = true
                foundVideosView.showAllFormatsMenuItem = true
            }
        })
    }

    private fun onIsAnalyzingChanged(isAnalyzing: Boolean) {
        foundVideosView.animateBouncingBug(isAnalyzing)
    }

    private fun onVideoFound(video: VideoDetectionVM.FoundVideo) {
        val count = videoDetectionVM.foundVideos.count()
        foundVideosView.updateFoundVideosCountText(count)
        foundVideosAdapter.addItem(video)
    }

    override fun onActivateSelection() {
        foundVideosAdapter.apply {
            loadData(videoDetectionVM.foundVideos)
            switchToSelectionMode(true)
        }
    }

    override fun onDeactivateSelection() {
        foundVideosAdapter.switchToSelectionMode(false)
    }

    override fun onSelectionAll() {
        videoDetectionVM.apply {
            foundVideos.all { it.isSelected }.also { allSelected ->
                if (allSelected)
                    unselectAll()
                else
                    selectAll()
            }
            foundVideosAdapter.loadData(foundVideos)
        }
    }

    override fun onItemCheckedChanged(index: Int, isChecked: Boolean) {
        videoDetectionVM.setSelection(index, isChecked)
    }

    override fun onItemDelete(index: Int) {
        videoDetectionVM.deleteItem(index)
        foundVideosAdapter.removeItem(index)
        foundVideosView.updateFoundVideosCountText(foundVideosAdapter.itemCount)
    }

    override fun onDeleteAllSelected() {
        videoDetectionVM.deleteAllSelected()
        foundVideosAdapter.loadData(videoDetectionVM.foundVideos)
        foundVideosView.updateFoundVideosCountText(foundVideosAdapter.itemCount)
    }

    override fun onItemRename(index: Int, newName: String) {
        videoDetectionVM.renameItem(index, newName)
        foundVideosAdapter.loadData(videoDetectionVM.foundVideos)
    }

    override fun onFetchDetails(index: Int, fetchListener: VideoDetailsFetcher.FetchListener) {
        videoDetectionVM.fetchDetails(index, fetchListener)
    }

    override fun onDownloadItem(index: Int) {
        videoDetectionVM.download(index)
        foundVideosAdapter.removeItem(index)
        foundVideosView.updateFoundVideosCountText(foundVideosAdapter.itemCount)
    }

    override fun onQueueAllSelected() {
        videoDetectionVM.queueAllSelected {
            onDeleteAllSelected()
        }
    }

    override fun onMergeSelected() {
        val showMessage: (String, Boolean) -> Unit = { s, b ->
            val length = if (b) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_SHORT
            Snackbar.make(requireView(), s, length).apply {
                view.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                    gravity = Gravity.CENTER
                }
                setAction("OK") { dismiss() }
                view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        ?.maxLines = 5
                show()
            }
        }

        if (videoDetectionVM.mergeSelected()) {
            foundVideosAdapter.loadData(videoDetectionVM.foundVideos)
            showMessage(getString(R.string.merge_success), false)
        } else
            showMessage(getString(R.string.merge_fail), true)
    }

    override fun onExtractAllFormats() {
        if (!allFormatsExtractFragment.isAdded) {
            webViewsControllerVM.requestActiveWebView { webView ->
                webView?.url?.let { url ->
                    allFormatsExtractFragment.arguments = Bundle().apply {
                        putString(AllFormatsExtractFragment.ARG_URL, url)
                    }
                    parentFragmentManager.beginTransaction()
                            .add(requireView().id, allFormatsExtractFragment, null)
                            .addToBackStack(null)
                            .commit()
                }
            }
        }
    }
}