package marabillas.loremar.beedio.browser.uicontrollers

import android.webkit.WebView
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

    override fun updateTitle(webView: WebView?, title: String?, url: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}