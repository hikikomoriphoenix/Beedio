package marabillas.loremar.beedio.browser.uicontrollers

import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import marabillas.loremar.beedio.browser.R
import javax.inject.Inject
import kotlin.math.roundToInt

class WebViewSwitcherSheetCallback @Inject constructor() : BottomSheetBehavior.BottomSheetCallback() {

    private val onShowHeaderTransition = Slide(Gravity.TOP)

    var actionWhenHidden = { }

    override fun onStateChanged(p0: View, p1: Int) {
        val header = p0.findViewById<View>(R.id.browser_webview_switcher_sheet_header)
        val recyclerView = p0.findViewById<RecyclerView>(R.id.browser_webview_switcher_sheet_recyclerview)

        when (p1) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                TransitionManager.beginDelayedTransition(p0.parent as ViewGroup, onShowHeaderTransition)
                header.visibility = VISIBLE
                hideRecyclerViewMargin(recyclerView)
            }

            BottomSheetBehavior.STATE_COLLAPSED,
            BottomSheetBehavior.STATE_HALF_EXPANDED,
            BottomSheetBehavior.STATE_DRAGGING,
            BottomSheetBehavior.STATE_SETTLING -> {
                header.visibility = GONE
                showRecyclerViewMargin(recyclerView)
            }

            BottomSheetBehavior.STATE_HIDDEN -> {
                header.visibility = GONE
                showRecyclerViewMargin(recyclerView)
                actionWhenHidden()
            }
        }
    }

    override fun onSlide(p0: View, p1: Float) {}

    private fun showRecyclerViewMargin(recyclerView: RecyclerView) {
        val params = recyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = (16 * recyclerView.context.resources.displayMetrics.density).roundToInt()
        recyclerView.layoutParams = params
    }

    private fun hideRecyclerViewMargin(recyclerView: RecyclerView) {
        val params = recyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = (8 * recyclerView.context.resources.displayMetrics.density).roundToInt()
        recyclerView.layoutParams = params
    }
}