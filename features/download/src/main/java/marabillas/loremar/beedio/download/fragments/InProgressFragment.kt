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

package marabillas.loremar.beedio.download.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.download.R
import marabillas.loremar.beedio.download.adapters.InProgressAdapter
import marabillas.loremar.beedio.download.viewmodels.InProgressVM
import javax.inject.Inject

class InProgressFragment @Inject constructor() : DaggerFragment(), InProgressAdapter.EventListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var inProgressVM: InProgressVM
    private lateinit var inProgressAdapter: InProgressAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        inProgressAdapter = InProgressAdapter()
        inProgressAdapter.eventListener = this
        return context?.run {
            RecyclerView(this).apply {
                adapter = inProgressAdapter
                layoutManager = LinearLayoutManager(context)
                layoutParams = ConstraintLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                (itemAnimator as SimpleItemAnimator?)?.supportsChangeAnimations = false
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            inProgressVM = ViewModelProvider(it::getViewModelStore, viewModelFactory).get(InProgressVM::class.java)
        }
    }

    override fun onStart() {
        super.onStart()
        inProgressVM.loadDownloadsList { inProgressAdapter.loadData(it) }
        inProgressVM.observeProgress(this, Observer {
            inProgressAdapter.updateProgress(it.progress, it.downloaded)
        })

        inProgressVM.observeInProgressListUpdate(this, Observer {
            inProgressAdapter.loadData(it)
        })

        inProgressVM.observeIsDownloading(this, Observer { downloading ->
            val startDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_play_arrow_black_24dp, null)
            val pauseDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_pause_black_24dp, null)
            startButton {
                if (downloading)
                    setImageDrawable(pauseDrawable)
                else
                    setImageDrawable(startDrawable)
            }
        })

        inProgressVM.observeIsFetching(this, Observer {
            inProgressAdapter.isFetching = it
        })

        inProgressVM.observeVideoDetails(this, Observer {
            inProgressAdapter.loadDetails(it, false)
        })

        startButton {
            setOnClickListener {
                inProgressVM.isDownloading?.let { downloading ->
                    if (downloading)
                        inProgressVM.pauseDownload()
                    else
                        inProgressVM.startDownload()
                }
            }
        }

    }

    private fun startButton(block: FloatingActionButton.() -> Unit) =
            activity?.findViewById<FloatingActionButton>(R.id.start_button)?.apply(block)

    override fun onRenameItem(index: Int, newName: String) = inProgressVM.renameItem(index, newName)

    override fun onDeleteItem(index: Int) = inProgressVM.deleteItem(index)
}