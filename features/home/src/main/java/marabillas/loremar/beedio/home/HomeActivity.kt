/*
 *     Beedio is an Android app for downloading videos
 *     Copyright (C) 2019 Loremar Marabillas
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.beedio.home

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatActivity
import marabillas.loremar.beedio.home.databinding.ActivityHomeBinding
import javax.inject.Inject
import kotlin.math.roundToInt

class HomeActivity : DaggerAppCompatActivity(), OnRecommendedClickListener {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    @Inject
    lateinit var searchWidgetControllerFragment: SearchWidgetControllerFragment
    @Inject
    lateinit var homeRecommendedFragment: HomeRecommendedFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.lifecycleOwner = this
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        val actionBar = binding.mainContentHome.homeToolbar
        setSupportActionBar(actionBar)
        actionBar.setNavigationOnClickListener { binding.navDrawerHome.openDrawer(GravityCompat.START) }

        searchWidgetControllerFragment.apply {
            searchWidgetStateHolder = viewModel
            homeAppBarStateHolder = viewModel
        }

        binding.mainContentHome.apply {
            searchWidgetStateHolder = viewModel
            homeAppBarStateHolder = viewModel
        }

        binding.onSearchWidgetInteractionListener = searchWidgetControllerFragment
        supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, searchWidgetControllerFragment)
                .commit()

        binding.onRecommendedClickListener = this
    }

    override fun onStart() {
        super.onStart()

        setupActionOnSearchWidgetLayoutChanges()

        if (viewModel.searchWidgetWidth.value == null || viewModel.searchWidgetVerticalBias.value == null) {
            setupDefaultSearchWidgetParams()
        }
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

    private fun setupDefaultSearchWidgetParams() {
        viewModel.searchWidgetWidth.value = (304 * resources.displayMetrics.density).roundToInt()
        viewModel.searchWidgetVerticalBias.value = 0.4f
    }

    override fun onRecommendedClick() {
        homeRecommendedFragment.show(supportFragmentManager, null)
    }
}
