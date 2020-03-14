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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.download.adapters.InactiveAdapter
import marabillas.loremar.beedio.download.viewmodels.InactiveVM
import javax.inject.Inject

class InactiveFragment @Inject constructor() : DaggerFragment(), InactiveAdapter.EventListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var inactiveAdapter: InactiveAdapter

    private lateinit var inactiveVM: InactiveVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = inactiveAdapter
            layoutParams = ConstraintLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.apply {
            inactiveVM = ViewModelProvider(this::getViewModelStore, viewModelFactory).get(InactiveVM::class.java)
        }
    }

    override fun onStart() {
        super.onStart()
        inactiveVM.loadList {
            inactiveAdapter.load(it)
        }

        inactiveAdapter.eventListener = this
    }

    override fun onRemoveItem(index: Int) {
        inactiveVM.deleteItem(index)
        inactiveAdapter.removeItem(index)
    }
}