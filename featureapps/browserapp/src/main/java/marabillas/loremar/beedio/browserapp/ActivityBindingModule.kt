package marabillas.loremar.beedio.browserapp

import dagger.Module
import dagger.android.ContributesAndroidInjector
import marabillas.loremar.beedio.base.di.ActivityScope
import marabillas.loremar.beedio.browser.BrowserActivity

@Module
abstract class ActivityBindingModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [BrowserActivityModule::class])
    abstract fun contributeBrowserActivity(): BrowserActivity
}