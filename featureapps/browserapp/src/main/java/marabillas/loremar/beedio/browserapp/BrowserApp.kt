package marabillas.loremar.beedio.browserapp

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class BrowserApp : DaggerApplication() {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerBrowserAppComponent.factory().create(this)
}