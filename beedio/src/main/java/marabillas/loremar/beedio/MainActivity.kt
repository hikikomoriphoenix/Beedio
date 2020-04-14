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

package marabillas.loremar.beedio

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import dagger.android.support.DaggerAppCompatActivity
import marabillas.loremar.beedio.base.mvvm.MainViewModel
import marabillas.loremar.beedio.browser.viewmodel.VideoDetectionVM
import timber.log.Timber
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, NavController.OnDestinationChangedListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mainViewModel: MainViewModel
    private lateinit var videoDetectionVM: VideoDetectionVM

    private val navController by lazy { findNavController(R.id.main_nav_host) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProvider(this::getViewModelStore, viewModelFactory).get(MainViewModel::class.java)
        videoDetectionVM = ViewModelProvider(this::getViewModelStore, viewModelFactory).get(VideoDetectionVM::class.java)

        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(this)
        mainViewModel.isNavDrawerOpenLiveData.observe(this, Observer {
            if (it == true)
                openNavDrawer()
            else
                closeNavDrawer()
        })
        mainViewModel.goToBrowserEvent.observeSend(this, Observer {
            val data = bundleOf("url" to it)
            navController.navigate(R.id.action_global_browserMainFragment, data)
        })
        mainViewModel.goToHomeEvent.observe(this, Observer {
            navController.navigate(R.id.action_global_homeMainFragment)
        })
    }

    override fun onStart() {
        super.onStart()
        navController.addOnDestinationChangedListener(this)
    }

    override fun onDestroy() {
        videoDetectionVM.closeDetailsFetcher()
        super.onDestroy()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_menu_home -> navController.navigate(R.id.action_global_homeMainFragment)

            R.id.nav_menu_browser -> navController.navigate(R.id.action_global_browserMainFragment)

            R.id.nav_menu_download -> navController.navigate(R.id.action_global_downloadMainFragment)

            R.id.nav_menu_bookmarks -> navController.navigate(R.id.action_global_bookmarksFragment)
        }
        return true
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        closeNavDrawer()
    }

    private fun openNavDrawer() = findViewById<DrawerLayout>(R.id.main_drawer_layout).openDrawer(GravityCompat.START)

    private fun closeNavDrawer() = findViewById<DrawerLayout>(R.id.main_drawer_layout).closeDrawer(GravityCompat.START)
}