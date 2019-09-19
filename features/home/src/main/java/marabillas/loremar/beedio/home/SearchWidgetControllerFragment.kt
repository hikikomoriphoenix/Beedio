/*
 *     Beedio is an Android app for downloading videos
 *     Copyright (C) 2019 Loremar Marabillas
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.beedio.home

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.transition.ChangeBounds
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import marabillas.loremar.beedio.sharedui.OnTransitionEndListener
import marabillas.loremar.beedio.sharedui.hideSofKeyboard
import marabillas.loremar.beedio.sharedui.showSoftKeyboard
import javax.inject.Inject
import kotlin.math.roundToInt


class SearchWidgetControllerFragment @Inject constructor() : Fragment(), OnSearchWidgetInteractionListener {

    var searchWidgetStateHolder: SearchWidgetStateHolder? = null
    var homeAppBarStateHolder: HomeAppBarStateHolder? = null

    private val searchWidgetTransition = TransitionSet()
    private val collapseOnActivateEnd = OnTransitionEndListener(this::riseSearchWidget)
    private val collapseOnDeactivateEnd = OnTransitionEndListener(this::fallSearchWidget)
    private val riseEnd = OnTransitionEndListener(this::expandOnActivateSearchWidget)
    private val fallEnd = OnTransitionEndListener(this::expandOnDeactivateSearchWidget)
    private val expandOnActivateEnd = OnTransitionEndListener(this::enableSearchInput)
    private val expandOnDeactivateEnd = OnTransitionEndListener(this::disableSearchInput)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        searchWidgetTransition
                .addTransition(ChangeBounds())
                .addTransition(Slide(Gravity.TOP))
                .duration = 100
    }

    override fun onSearchWidgetClick() {
        activateSearchWidget()
    }

    override fun onSearchCloseClick() {
        searchWidgetStateHolder?.let {
            val textValue = it.searchWidgetText.value
            if (textValue != null && textValue.isNotEmpty()) {
                it.searchWidgetText.value = ""
            } else {
                deactivateSearchWidget()
            }
        }
    }

    private fun activateSearchWidget() {
        searchWidgetStateHolder?.let {
            if (it.editTextVisibility.value != View.VISIBLE) {
                collapseOnActivateSearchWidget()
            }
        }
    }

    private fun deactivateSearchWidget() {
        val activity = activity
        if (activity is Activity) {
            hideSofKeyboard(activity)
            collapseOnDeactivateSearchWidget()
        }
    }

    private fun collapseOnActivateSearchWidget() {
        searchWidgetStateHolder?.let {
            searchWidgetTransition.addListener(collapseOnActivateEnd)
            initSearchWidgetTransition()
            it.searchWidgetWidth.value = (56 * resources.displayMetrics.density).roundToInt()
            homeAppBarStateHolder?.homeAppBarVisibility?.value = View.GONE
        }
    }

    private fun collapseOnDeactivateSearchWidget() {
        searchWidgetStateHolder?.let {
            searchWidgetTransition.addListener(collapseOnDeactivateEnd)
            initSearchWidgetTransition()
            it.searchWidgetWidth.value = (56 * resources.displayMetrics.density).roundToInt()
        }
    }

    private fun riseSearchWidget() {
        searchWidgetStateHolder?.let {
            searchWidgetTransition.removeListener(collapseOnActivateEnd)
            searchWidgetTransition.addListener(riseEnd)
            initSearchWidgetTransition()
            it.searchWidgetVerticalBias.value = 0f
        }
    }

    private fun fallSearchWidget() {
        searchWidgetStateHolder?.let {
            searchWidgetTransition.removeListener(collapseOnDeactivateEnd)
            searchWidgetTransition.addListener(fallEnd)
            initSearchWidgetTransition()
            it.searchWidgetVerticalBias.value = 0.4f
        }
    }

    private fun expandOnActivateSearchWidget() {
        searchWidgetStateHolder?.let {
            searchWidgetTransition.removeListener(riseEnd)
            searchWidgetTransition.addListener(expandOnActivateEnd)
            initSearchWidgetTransition()
            it.searchWidgetWidth.value = ViewGroup.LayoutParams.MATCH_PARENT
        }

    }

    private fun expandOnDeactivateSearchWidget() {
        searchWidgetStateHolder?.let {
            searchWidgetTransition.removeListener(fallEnd)
            searchWidgetTransition.addListener(expandOnDeactivateEnd)
            initSearchWidgetTransition()
            it.searchWidgetWidth.value = (304 * resources.displayMetrics.density).roundToInt()

            homeAppBarStateHolder?.homeAppBarVisibility?.value = View.VISIBLE
        }
    }

    private fun enableSearchInput() {
        searchWidgetStateHolder?.let {
            searchWidgetTransition.removeListener(expandOnActivateEnd)

            val editText = activity?.findViewById<EditText>(R.id.home_search_edit_text)
            editText?.visibility = View.VISIBLE
            editText?.requestFocus()
            activity?.let { it1 -> showSoftKeyboard(it1) }

            it.searchIconVisibility.value = View.GONE
            it.searchCloseBtnVisibility.value = View.VISIBLE
            it.editTextVisibility.value = View.VISIBLE
        }
    }

    private fun disableSearchInput() {
        searchWidgetStateHolder?.let {
            searchWidgetTransition.removeListener(expandOnDeactivateEnd)

            it.searchCloseBtnVisibility.value = View.GONE
            it.searchIconVisibility.value = View.VISIBLE
            it.editTextVisibility.value = View.GONE
        }
    }

    private fun initSearchWidgetTransition() {
        activity?.let {
            TransitionManager.beginDelayedTransition(
                    it.findViewById(android.R.id.content),
                    searchWidgetTransition)
        }
    }
}