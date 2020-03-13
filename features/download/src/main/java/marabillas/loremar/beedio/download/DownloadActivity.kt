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

package marabillas.loremar.beedio.download

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerAppCompatActivity
import marabillas.loremar.beedio.download.databinding.ActivityDownloadBinding
import marabillas.loremar.beedio.download.fragments.CompletedFragment
import marabillas.loremar.beedio.download.fragments.InProgressFragment
import marabillas.loremar.beedio.download.fragments.InactiveFragment
import marabillas.loremar.beedio.download.viewmodels.DownloadVM
import marabillas.loremar.beedio.download.viewmodels.InProgressVM
import timber.log.Timber
import javax.inject.Inject

class DownloadActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var inProgressFragment: InProgressFragment

    @Inject
    lateinit var completedFragment: CompletedFragment

    @Inject
    lateinit var inactiveFragment: InactiveFragment

    private lateinit var downloadVM: DownloadVM
    private lateinit var inProgressVM: InProgressVM
    private lateinit var binding: ActivityDownloadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_download)
        binding.lifecycleOwner = this

        downloadVM = ViewModelProvider(this::getViewModelStore, viewModelFactory).get(DownloadVM::class.java)
        inProgressVM = ViewModelProvider(this::getViewModelStore, viewModelFactory).get(InProgressVM::class.java)

        binding.mainContentDownload.apply {
            setSupportActionBar(downloadToolbar)
            downloadBottomNavigation.setOnNavigationItemSelectedListener {
                downloadVM.setSelectedNavItem(it.itemId)
                true
            }
        }

        downloadVM.observeSelectedNavItem(this, Observer {
            when (it) {
                R.id.download_menu_in_progress -> viewInProgress()
                R.id.download_menu_completed -> viewCompleted()
                R.id.download_menu_inactive -> viewInactive()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.download_tools_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun viewInProgress() {
        binding.mainContentDownload.apply {
            startButton.isVisible = true
            downloadToolbar.post {
                downloadToolbar.menu.setGroupVisible(R.id.completed_menu_group, false)
            }
        }

        if (!inProgressFragment.isAdded) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.list_container, inProgressFragment)
                    .commit()
        }
    }

    private fun viewCompleted() {
        binding.mainContentDownload.apply {
            startButton.isVisible = false
            downloadToolbar.post {
                downloadToolbar.menu.setGroupVisible(R.id.completed_menu_group, true)
            }
        }

        if (!completedFragment.isAdded) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.list_container, completedFragment)
                    .commit()
        }
    }

    private fun viewInactive() {
        if (!inactiveFragment.isAdded) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.list_container, inactiveFragment)
                    .commit()
        }
    }
}