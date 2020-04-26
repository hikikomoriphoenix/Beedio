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

import dagger.Module
import dagger.android.ContributesAndroidInjector
import marabillas.loremar.beedio.base.di.FragmentScope
import marabillas.loremar.beedio.base.di.WebNavigationModule
import marabillas.loremar.beedio.bookmarks.BookmarksFragment
import marabillas.loremar.beedio.browser.fragment.AddBookmarkFragment
import marabillas.loremar.beedio.browser.fragment.BrowserMainFragment
import marabillas.loremar.beedio.browser.uicontrollers.BrowserSearchWidgetControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.ExpandingFoundVideosFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewSwitcherSheetFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewsControllerFragment
import marabillas.loremar.beedio.download.fragments.*
import marabillas.loremar.beedio.history.HistoryFragment
import marabillas.loremar.beedio.home.HomeMainFragment
import marabillas.loremar.beedio.home.HomeRecommendedFragment
import marabillas.loremar.beedio.home.SearchWidgetControllerFragment

@Module
abstract class FragmentBindingModule {
    @FragmentScope
    @ContributesAndroidInjector(modules = [HomeMainFragmentModule::class])
    abstract fun contributeHomeMainFragment(): HomeMainFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [BrowserMainFragmentModule::class])
    abstract fun contributeBrowserMainFragment(): BrowserMainFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDownloadMainFragment(): DownloadMainFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [BookmarksFragmentModule::class])
    abstract fun contributeBookmarksFragment(): BookmarksFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeHistoryFragment(): HistoryFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeHomeRecommendedFragment(): HomeRecommendedFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [WebNavigationModule::class])
    abstract fun contributeSearchWidgetControllerFagment(): SearchWidgetControllerFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeWebViewsControllerFragment(): WebViewsControllerFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [WebNavigationModule::class])
    abstract fun contributeBrowserSearchWidgetControllerFragment(): BrowserSearchWidgetControllerFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeWebViewSwitcherSheetFragment(): WebViewSwitcherSheetFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeExpandingFoundVideosFragment(): ExpandingFoundVideosFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAddBookmarkFragment(): AddBookmarkFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeInProgressFragment(): InProgressFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeCompleteFragment(): CompletedFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeInactiveFragment(): InactiveFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeSourcePageFragment(): SourcePageFragment
}