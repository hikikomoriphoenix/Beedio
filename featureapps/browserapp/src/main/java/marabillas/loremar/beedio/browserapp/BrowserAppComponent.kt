package marabillas.loremar.beedio.browserapp

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, ActivityBindingModule::class])
interface BrowserAppComponent : AndroidInjector<BrowserApp> {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance browserApp: BrowserApp): BrowserAppComponent
    }
}