package marabillas.loremar.beedio.browserapp

import dagger.Module
import dagger.android.ContributesAndroidInjector
import marabillas.loremar.beedio.base.di.FragmentScope
import marabillas.loremar.beedio.browser.uicontrollers.BrowserSearchWidgetControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.BrowserTitleControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewSwitcherSheetFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewsControllerFragment

@Module
abstract class FragmentBindingModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeBrowserTitleControllerFragment(): BrowserTitleControllerFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeWebViewsControllerFragment(): WebViewsControllerFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeBrowserSearchWidgetControllerFragment(): BrowserSearchWidgetControllerFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeWebViewSwitcherSheetFragment(): WebViewSwitcherSheetFragment
}