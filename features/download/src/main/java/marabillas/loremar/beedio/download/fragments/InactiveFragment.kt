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

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.base.extensions.toolbar
import marabillas.loremar.beedio.download.R
import marabillas.loremar.beedio.download.adapters.InactiveAdapter
import marabillas.loremar.beedio.download.viewmodels.InactiveVM
import javax.inject.Inject

class InactiveFragment @Inject constructor() : DaggerFragment(), InactiveAdapter.EventListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var inactiveAdapter: InactiveAdapter

    @Inject
    lateinit var sourcePageFragment: SourcePageFragment

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

        activity?.toolbar(R.id.download_toolbar)?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.inactive_menu_delete_all -> clearList()
                R.id.inactive_menu_help -> showHelpDialog()
            }
            true
        }
    }

    override fun onRemoveItem(index: Int) {
        inactiveVM.deleteItem(index)
        inactiveAdapter.removeItem(index)
    }

    private fun clearList() {
        inactiveVM.clearList()
        inactiveAdapter.clearList()
    }

    override fun onGoToSourcePage(index: Int, sourcePage: String) {
        if (!sourcePageFragment.isAdded) {
            val data = Bundle().apply {
                putInt(SourcePageFragment.ARG_INDEX, index)
                putString(SourcePageFragment.ARG_PAGE, sourcePage)
            }
            sourcePageFragment.arguments = data
            sourcePageFragment.setTargetFragment(this, SourcePageFragment.REQUEST_CODE_REFRESHED)
            fragmentManager
                    ?.beginTransaction()
                    ?.addToBackStack(null)
                    ?.add(android.R.id.content, sourcePageFragment, null)
                    ?.commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (
                requestCode == SourcePageFragment.REQUEST_CODE_REFRESHED &&
                resultCode == RESULT_OK
        )
            data?.getIntExtra(SourcePageFragment.RESULT_INDEX, -1)?.let {
                if (it != -1) inactiveAdapter.removeItem(it)
            }

        activity?.apply {
            val targetView = findViewById<View>(R.id.list_container)
            Snackbar.make(targetView, getString(R.string.fresh_link_added), Snackbar.LENGTH_SHORT)
                    .show()
        }
    }

    private fun showHelpDialog() {
        MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_live_help_black_24dp)
                .setTitle(getString(R.string.inactive_help_title))
                .setMessage(getString(R.string.inactive_help_message))
                .setNeutralButton(getString(R.string.inactive_help_close), null)
                .create()
                .show()
    }
}