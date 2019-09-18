package marabillas.loremar.beedio.home

import android.view.View
import android.widget.EditText
import android.widget.ImageView

interface SearchWidget {
    fun getView(): View

    fun getEditText(): EditText

    fun getSearchIcon(): ImageView

    fun getCloseButton(): ImageView
}