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

package marabillas.loremar.beedio.home

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import marabillas.loremar.beedio.home.databinding.ActivityHomeBinding
import marabillas.loremar.beedio.sharedui.NavigationActivity
import javax.inject.Inject
import kotlin.math.roundToInt

class HomeActivity : NavigationActivity(), HasAndroidInjector, OnRecommendedClickListener {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>
    @Inject
    lateinit var searchWidgetControllerFragment: SearchWidgetControllerFragment
    @Inject
    lateinit var homeRecommendedFragment: HomeRecommendedFragment

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.lifecycleOwner = this

        setupActionBar()

        bindViewModelOnUI()

        setupUIControllers()

        setupListeners()

    }

    private fun setupActionBar() {
        val actionBar = binding.mainContentHome.homeToolbar
        setSupportActionBar(actionBar)
        actionBar.setNavigationOnClickListener { binding.navDrawerHome.openDrawer(GravityCompat.START) }
    }

    private fun bindViewModelOnUI() {
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        binding.mainContentHome.apply {
            searchWidgetStateHolder = viewModel
            homeAppBarStateHolder = viewModel
        }
    }

    private fun setupUIControllers() {
        setupSearchWidgetController()
    }

    private fun setupSearchWidgetController() {
        searchWidgetControllerFragment.apply {
            searchWidgetStateHolder = viewModel
            homeAppBarStateHolder = viewModel
        }

        supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, searchWidgetControllerFragment)
                .commit()
    }

    private fun setupListeners() {
        binding.onSearchWidgetInteractionListener = searchWidgetControllerFragment
        binding.onRecommendedClickListener = this
    }

    override fun onStart() {
        super.onStart()

        setupActionOnSearchWidgetLayoutChanges()

        setupDefaultSearchWidgetParamsIfNotYetSet()
    }

    private fun setupActionOnSearchWidgetLayoutChanges() {
        val searchWidgetView = binding.mainContentHome.homeSearchWidget.homeSearchWidgetLayout

        viewModel.searchWidgetWidth.observe(this, Observer {
            val params = searchWidgetView.layoutParams
            params.width = it
            searchWidgetView.layoutParams = params
        })

        viewModel.searchWidgetVerticalBias.observe(this, Observer {
            val params = searchWidgetView.layoutParams as ConstraintLayout.LayoutParams
            params.verticalBias = it
            searchWidgetView.layoutParams = params
        })
    }

    private fun setupDefaultSearchWidgetParamsIfNotYetSet() {
        if (viewModel.searchWidgetWidth.value == null || viewModel.searchWidgetVerticalBias.value == null) {
            viewModel.searchWidgetWidth.value = (304 * resources.displayMetrics.density).roundToInt()
            viewModel.searchWidgetVerticalBias.value = 0.4f
        }
    }

    override fun onRecommendedClick() {
        if (!homeRecommendedFragment.isAdded)
            homeRecommendedFragment.show(supportFragmentManager, null)
    }
}
