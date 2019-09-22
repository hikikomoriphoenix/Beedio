package marabillas.loremar.beedio.browserapp

import dagger.Module
import dagger.android.ContributesAndroidInjector
import marabillas.loremar.beedio.base.di.FragmentScope
import marabillas.loremar.beedio.browser.uicontrollers.BrowserTitleControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebPageNavigatorFragment

@Module
abstract class FragmentBindingModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeBrowserTitleControllerFragment(): BrowserTitleControllerFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeWebpPageNavigatorFragment(): WebPageNavigatorFragment
}