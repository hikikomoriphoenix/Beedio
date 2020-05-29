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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import marabillas.loremar.beedio.base.database.DownloadListDatabase
import marabillas.loremar.beedio.base.mvvm.MainViewModel
import marabillas.loremar.beedio.browser.viewmodel.*
import marabillas.loremar.beedio.download.viewmodels.*
import marabillas.loremar.beedio.history.HistoryViewModel

class ViewModelFactory(
        private val context: Context,
        private val downloadDB: DownloadListDatabase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            MainViewModel::class.java -> MainViewModel() as T
            BrowserTitleStateVM::class.java -> BrowserTitleStateVMImpl() as T
            WebPageNavigationVM::class.java -> WebPageNavigationVMImpl() as T
            WebViewsControllerVM::class.java -> WebViewsControllerVMImpl() as T
            BrowserSearchWidgetControllerVM::class.java -> BrowserSearchWidgetControllerVMImpl() as T
            BrowserActionBarStateVM::class.java -> BrowserActionBarStateVMImpl() as T
            BrowserSearchWidgetStateVM::class.java -> BrowserSearchWidgetStateVMImpl() as T
            WebViewsCountIndicatorVM::class.java -> WebViewsCountIndicatorVMImpl() as T
            VideoDetectionVM::class.java -> VideoDetectionVMImpl(context) as T
            AddBookmarkVM::class.java -> AddBookmarkVMImpl() as T
            BrowserHistoryVM::class.java -> BrowserHistoryVMImpl(context) as T
            DownloadVM::class.java -> DownloadVMImpl(downloadDB) as T
            InProgressVM::class.java -> InProgressVMImpl(context, downloadDB) as T
            CompletedVM::class.java -> CompletedVMImpl(downloadDB) as T
            InactiveVM::class.java -> InactiveVMImpl(context, downloadDB) as T
            HistoryViewModel::class.java -> HistoryViewModel(context) as T
            PageProgressVM::class.java -> PageProgressVMImpl() as T
            else -> throw IllegalArgumentException("Unidentified ViewModel")
        }
    }
}