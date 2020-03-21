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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.home.databinding.ActivityHomeBinding
import javax.inject.Inject
import kotlin.math.roundToInt

class HomeMainFragment : DaggerFragment(), OnRecommendedClickListener {
    @Inject
    lateinit var searchWidgetControllerFragment: SearchWidgetControllerFragment

    @Inject
    lateinit var homeRecommendedFragment: HomeRecommendedFragment

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_content_home, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setupActionBar()
        bindViewModelOnUI()
        setupUIControllers()
        setupListeners()
        setupActionOnSearchWidgetLayoutChanges()
        setupDefaultSearchWidgetParamsIfNotYetSet()
    }

    private fun setupActionBar() {
        val toolbar = binding.mainContentHome.homeToolbar
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { binding.navDrawerHome.openDrawer(GravityCompat.START) }
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

        parentFragmentManager
                .beginTransaction()
                .add(android.R.id.content, searchWidgetControllerFragment)
                .commit()
    }

    private fun setupListeners() {
        binding.onSearchWidgetInteractionListener = searchWidgetControllerFragment
        binding.onRecommendedClickListener = this
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
            homeRecommendedFragment.show(parentFragmentManager, null)
    }
}