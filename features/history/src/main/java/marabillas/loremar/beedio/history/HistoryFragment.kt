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

package marabillas.loremar.beedio.history

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.base.database.HistoryItem
import marabillas.loremar.beedio.base.extensions.recyclerView
import marabillas.loremar.beedio.base.extensions.toolbar
import marabillas.loremar.beedio.base.mvvm.MainViewModel
import javax.inject.Inject

class HistoryFragment : DaggerFragment(), HistoryAdapter.ItemEventListener, Toolbar.OnMenuItemClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var historyAdapter: HistoryAdapter

    private lateinit var mainViewModel: MainViewModel
    private lateinit var historyViewModel: HistoryViewModel

    private val toolbar by lazy { requireView().toolbar(R.id.toolbar_history) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            mainViewModel = ViewModelProvider(it, viewModelFactory).get(MainViewModel::class.java)
            historyViewModel = ViewModelProvider(it, viewModelFactory).get(HistoryViewModel::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView(R.id.recyclerview_history)?.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_history, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onStart() {
        super.onStart()
        historyViewModel.loadAllItems { historyAdapter.historyList = it.toMutableList() }

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { mainViewModel.setIsNavDrawerOpen(true) }
        toolbar.setOnMenuItemClickListener(this)

        historyAdapter.itemEventListener = this
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuitem_history_clear_all -> {
                historyViewModel.clearAll()
                historyAdapter.historyList = mutableListOf()
            }
        }
        return true
    }

    override fun onItemDelete(item: HistoryItem) {
        historyViewModel.deleteItem(item)
    }

    override fun onItemClicked(item: HistoryItem) {
        mainViewModel.goToBrowser(item.url)
    }
}