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
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.base.extensions.color
import marabillas.loremar.beedio.base.mvvm.MainViewModel
import marabillas.loremar.beedio.download.R
import marabillas.loremar.beedio.download.databinding.MainContentDownloadBinding
import marabillas.loremar.beedio.download.viewmodels.DownloadVM
import marabillas.loremar.beedio.download.viewmodels.InProgressVM
import javax.inject.Inject

class DownloadMainFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var inProgressFragment: InProgressFragment

    @Inject
    lateinit var completedFragment: CompletedFragment

    @Inject
    lateinit var inactiveFragment: InactiveFragment

    private lateinit var mainViewModel: MainViewModel
    private lateinit var downloadVM: DownloadVM
    private lateinit var inProgressVM: InProgressVM
    private lateinit var binding: MainContentDownloadBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_content_download, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            mainViewModel = ViewModelProvider(it::getViewModelStore, viewModelFactory).get(MainViewModel::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        downloadVM = ViewModelProvider(this::getViewModelStore, viewModelFactory).get(DownloadVM::class.java)
        inProgressVM = ViewModelProvider(this::getViewModelStore, viewModelFactory).get(InProgressVM::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.download_tools_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onStart() {
        super.onStart()

        binding.apply {
            (activity as AppCompatActivity?)?.setSupportActionBar(downloadToolbar)
            downloadToolbar.setNavigationOnClickListener {
                mainViewModel.setIsNavDrawerOpen(true)
            }
            downloadBottomNavigation.apply {
                setOnNavigationItemSelectedListener {
                    downloadVM.setSelectedNavItem(it.itemId)
                    true
                }
                getOrCreateBadge(R.id.download_menu_in_progress)
                        .backgroundColor = resources.color(R.color.green)
                getOrCreateBadge(R.id.download_menu_completed)
                        .backgroundColor = resources.color(R.color.green)
                getOrCreateBadge(R.id.download_menu_inactive)
                        .backgroundColor = resources.color(R.color.green)
            }
        }

        downloadVM.observeSelectedNavItem(this, Observer {
            when (it) {
                R.id.download_menu_in_progress -> viewInProgress()
                R.id.download_menu_completed -> viewCompleted()
                R.id.download_menu_inactive -> viewInactive()
            }
        })

        downloadVM.observeInProgressCount(this, Observer { updateInProgressBadgeCount(it) })
        downloadVM.observeCompletedCount(this, Observer { updateCompletedBadgeCount(it) })
        downloadVM.observeInactiveCount(this, Observer { updateInactiveBadgeCount(it) })
    }

    private fun viewInProgress() {
        binding.apply {
            startButton.isVisible = true
            downloadToolbar.post {
                downloadToolbar.menu.apply {
                    setGroupVisible(R.id.completed_menu_group, false)
                    setGroupVisible(R.id.inactive_menu_group, false)
                }
            }
        }

        if (!inProgressFragment.isAdded) {
            parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.list_container, inProgressFragment)
                    .commit()
        }
    }

    private fun viewCompleted() {
        binding.apply {
            startButton.isVisible = false
            downloadToolbar.post {
                downloadToolbar.menu.apply {
                    setGroupVisible(R.id.completed_menu_group, true)
                    setGroupVisible(R.id.inactive_menu_group, false)
                }
            }
        }

        if (!completedFragment.isAdded) {
            parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.list_container, completedFragment)
                    .commit()
        }
    }

    private fun viewInactive() {
        binding.apply {
            startButton.isVisible = false
            downloadToolbar.post {
                downloadToolbar.menu.apply {
                    setGroupVisible(R.id.completed_menu_group, false)
                    setGroupVisible(R.id.inactive_menu_group, true)
                }
            }
        }

        if (!inactiveFragment.isAdded) {
            parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.list_container, inactiveFragment)
                    .commit()
        }
    }

    private fun updateInProgressBadgeCount(count: Int) {
        binding.downloadBottomNavigation
                .getOrCreateBadge(R.id.download_menu_in_progress)
                .apply {
                    if (count == 0)
                        isVisible = false
                    else {
                        isVisible = true
                        number = count
                    }
                }
    }

    private fun updateCompletedBadgeCount(count: Int) {
        binding.downloadBottomNavigation
                .getOrCreateBadge(R.id.download_menu_completed)
                .apply {
                    if (count == 0)
                        isVisible = false
                    else {
                        isVisible = true
                        number = count
                    }
                }
    }

    private fun updateInactiveBadgeCount(count: Int) {
        binding.downloadBottomNavigation
                .getOrCreateBadge(R.id.download_menu_inactive)
                .apply {
                    if (count == 0)
                        isVisible = false
                    else {
                        isVisible = true
                        number = count
                    }
                }
    }
}