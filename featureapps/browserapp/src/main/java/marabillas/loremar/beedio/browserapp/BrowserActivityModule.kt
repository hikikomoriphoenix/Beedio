package marabillas.loremar.beedio.browserapp

import dagger.Module
import dagger.Provides
import marabillas.loremar.beedio.base.di.ActivityScope
import marabillas.loremar.beedio.base.web.WebNavigation
import marabillas.loremar.beedio.base.web.WebNavigationImpl

@Module
class BrowserActivityModule {
    @ActivityScope
    @Provides
    fun provideWebNavigation(): WebNavigation {
        return WebNavigationImpl()
    }
}