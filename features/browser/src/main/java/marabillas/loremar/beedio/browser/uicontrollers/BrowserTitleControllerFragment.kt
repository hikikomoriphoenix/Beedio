package marabillas.loremar.beedio.browser.uicontrollers

import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.browser.viewmodel.BrowserTitleState
import javax.inject.Inject

class BrowserTitleControllerFragment @Inject constructor() : DaggerFragment(), TitleControllerInterface {
    var titleState: BrowserTitleState? = null

    override fun updateTitle(title: String?, url: String?) {
        titleState?.title = title
        titleState?.url = url
    }

    override fun updateTitle(title: String?) {
        titleState?.title = title
    }
}