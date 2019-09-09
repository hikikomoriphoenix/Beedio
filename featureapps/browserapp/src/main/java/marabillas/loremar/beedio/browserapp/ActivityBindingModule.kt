package marabillas.loremar.beedio.browserapp

import dagger.Module
import dagger.android.ContributesAndroidInjector
import marabillas.loremar.beedio.browser.BrowserActivity

@Module
abstract class ActivityBindingModule {
    @ActivityScope
    @ContributesAndroidInjector
    abstract fun contributeBrowserActivity(): BrowserActivity
}