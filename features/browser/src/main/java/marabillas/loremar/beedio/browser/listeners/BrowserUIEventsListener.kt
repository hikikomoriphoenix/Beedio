package marabillas.loremar.beedio.browser.listeners

import android.graphics.Bitmap
import marabillas.loremar.beedio.base.di.ActivityScope
import marabillas.loremar.beedio.browser.uicontrollers.TitleControllerInterface
import javax.inject.Inject

@ActivityScope
class BrowserUIEventsListener @Inject constructor() : OnWebPageChangedListener,
        OnWebPageTitleRecievedListener {

    var titleController: TitleControllerInterface? = null

    override fun onWebPageChanged(title: String?, url: String?, favicon: Bitmap?) {
        titleController?.updateTitle(title, url)
    }

    override fun onWebPageTitleRecieved(title: String?) {
        titleController?.updateTitle(title)
    }
}