package marabillas.loremar.beedio.browser.listeners

import android.graphics.Bitmap

interface OnWebPageChangedListener {
    fun onWebPageChanged(title: String?, url: String?, favicon: Bitmap?)
}

interface OnWebPageTitleRecievedListener {
    fun onWebPageTitleRecieved(title: String?)
}