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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.download.R
import marabillas.loremar.beedio.download.adapters.InProgressAdapter
import marabillas.loremar.beedio.download.databinding.DownloadItemDraggableBinding
import marabillas.loremar.beedio.download.viewmodels.InProgressVM
import javax.inject.Inject

class InProgressFragment @Inject constructor() : DaggerFragment(), InProgressAdapter.EventListener, View.OnTouchListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var inProgressVM: InProgressVM
    private lateinit var inProgressAdapter: InProgressAdapter
    private lateinit var draggable: DownloadItemDraggableBinding

    private val recyclerView: RecyclerView; get() = view as RecyclerView
    private val recyclerViewItemTouchDisabler = RecyclerViewItemTouchDisabler()
    private var y0 = 0f
    private var moveY = 0f
    private var upwardHeight = Int.MAX_VALUE
    private var downwardHeight = Int.MAX_VALUE

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDraggableItemView()
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

    override fun onEnableItemDrag(index: Int) {
        val recyclerView = (view as RecyclerView).apply {
            addOnItemTouchListener(recyclerViewItemTouchDisabler)
        }

        val selected = recyclerView.findViewHolderForAdapterPosition(index)
        draggable.apply {
            itemDraggableTitle.text = selected?.itemView?.findViewById<TextView>(R.id.item_in_queue_title)?.text
            itemDraggableDownloaded.text = selected?.itemView?.findViewById<TextView>(R.id.item_in_queue_downloaded)?.text
            itemDraggableClose.setOnClickListener { disableItemDrag() }
            root.y = selected?.itemView?.y ?: 0f

            selected?.itemView?.visibility = View.INVISIBLE
            root.isVisible = true
            inProgressAdapter.trenchPosition = index
        }
    }

    private fun disableItemDrag() {
        (view as RecyclerView).removeOnItemTouchListener(recyclerViewItemTouchDisabler)
        draggable.root.isVisible = false
        inProgressAdapter.trenchPosition = -1
        inProgressAdapter.notifyDataSetChanged()
    }

    private fun setupDraggableItemView() {
        val inflater = LayoutInflater.from(context)
        draggable = DataBindingUtil.inflate(inflater, R.layout.download_item_draggable, null, false)
        draggable.apply {
            (view?.parent as ViewGroup?)?.addView(root)
            root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                width = MATCH_PARENT
                height = WRAP_CONTENT
                val verticalMargin = resources.getDimensionPixelSize(R.dimen.in_queue_item_margin_vertical)
                val horizontalMargin = resources.getDimensionPixelSize(R.dimen.in_queue_item_margin_horizontal)
                topMargin = verticalMargin
                bottomMargin = verticalMargin
                leftMargin = horizontalMargin
                rightMargin = horizontalMargin
            }

            root.isVisible = false
        }
        draggable.root.setOnTouchListener(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v == draggable.root) {
            event?.let {
                when (event.action) {
                    ACTION_DOWN -> {
                        y0 = event.rawY
                        moveY = 0f
                        getTargetHeights()
                    }
                    ACTION_MOVE -> {
                        var deltaY = event.rawY - y0
                        y0 = event.rawY
                        draggable.root.y += deltaY
                        if (draggable.root.y <= 0 || draggable.root.y >= recyclerView.height) {
                            draggable.root.y -= deltaY
                            deltaY = 0f
                        }
                        moveY += deltaY
                        if (moveY >= downwardHeight) {
                            moveY -= downwardHeight
                            if (inProgressAdapter.trenchPosition + 1 < inProgressAdapter.itemCount) {
                                inProgressVM.moveItem(inProgressAdapter.trenchPosition,
                                        inProgressAdapter.trenchPosition + 1)
                                inProgressAdapter.moveItemDown()
                                getTargetHeights()
                            }
                        } else if (moveY <= -upwardHeight) {
                            moveY -= (-upwardHeight)
                            if (inProgressAdapter.trenchPosition - 1 >= 0) {
                                inProgressVM.moveItem(inProgressAdapter.trenchPosition,
                                        inProgressAdapter.trenchPosition - 1)
                                getTargetHeights()
                                val animate = draggable.root.y < upwardHeight
                                inProgressAdapter.moveItemUp(animate)
                            }
                        }
                    }
                    ACTION_UP -> {
                        y0 = 0f
                        moveY = 0f
                        disableItemDrag()
                    }
                }
            }
            return true
        } else
            return false
    }

    private fun getTargetHeights() {
        val above = inProgressAdapter.trenchPosition - 1
        if (above >= 0) {
            val aboveVh = recyclerView.findViewHolderForAdapterPosition(above)
            upwardHeight = aboveVh?.itemView?.height ?: Int.MAX_VALUE
        }
        val trenchVh = recyclerView.findViewHolderForAdapterPosition(
                inProgressAdapter.trenchPosition)
        downwardHeight = trenchVh?.itemView?.height ?: Int.MAX_VALUE
    }
}

private class RecyclerViewItemTouchDisabler : RecyclerView.OnItemTouchListener {
    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean = true

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}