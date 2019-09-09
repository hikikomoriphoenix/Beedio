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
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import marabillas.loremar.beedio.home.databinding.ActivityHomeBinding
import javax.inject.Inject

class HomeActivity : DaggerAppCompatActivity(), OnRecommendedClickListener {
    lateinit var binding: ActivityHomeBinding

    @Inject
    lateinit var searchWidgetControllerFragment: SearchWidgetControllerFragment
    @Inject
    lateinit var homeRecommendedFragment: HomeRecommendedFragment
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.lifecycleOwner = this

        val actionBar = binding.mainContentHome.homeToolbar
        setSupportActionBar(actionBar)
        actionBar.setNavigationOnClickListener { binding.navDrawer.openDrawer(GravityCompat.START) }

        supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, searchWidgetControllerFragment)
                .commit()
        binding.onSearchWidgetInteractionListener = searchWidgetControllerFragment

        binding.onRecommendedClickListener = this
    }

    override fun onRecommendedClick() {
        homeRecommendedFragment.show(supportFragmentManager, null)
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector
}
