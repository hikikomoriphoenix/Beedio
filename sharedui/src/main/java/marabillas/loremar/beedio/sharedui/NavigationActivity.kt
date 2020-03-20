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

package marabillas.loremar.beedio.sharedui

import android.content.Intent
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView

abstract class NavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_menu_home -> navigate(getString(R.string.action_go_to_home))
            R.id.nav_menu_browser -> navigate(getString(R.string.action_go_to_browser))
            R.id.nav_menu_download -> navigate(getString(R.string.action_go_to_download))
        }
        return true
    }

    private fun navigate(intentAction: String) = startActivity(Intent(intentAction))

    override fun onStart() {
        super.onStart()
        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(this)
    }
}