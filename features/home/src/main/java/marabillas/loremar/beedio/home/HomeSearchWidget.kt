package marabillas.loremar.beedio.home

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import marabillas.loremar.beedio.home.databinding.HomeSearchWidgetBinding

class HomeSearchWidget(private val binding: HomeSearchWidgetBinding) : SearchWidget {

    override fun getView(): View = binding.root

    override fun getEditText(): EditText = binding.homeSearchEditText

    override fun getSearchIcon(): ImageView = binding.homeSearchIcon

    override fun getCloseButton(): ImageView = binding.homeSearchCloseBtn
}