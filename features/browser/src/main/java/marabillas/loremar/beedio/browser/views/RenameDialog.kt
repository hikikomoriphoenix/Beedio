/*
 * Beedio is an Android app for downloading videos
 * Copyright (C) 2019 Loremar Marabillas
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.beedio.browser.views

import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import marabillas.loremar.beedio.browser.R

class RenameDialog(context: Context, private val doOnDone: (String) -> Unit) {
    private lateinit var dialog: AlertDialog
    private val editText: EditText

    init {
        editText = EditText(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            hint = context.getString(R.string.enter_new_title)
            inputType = EditorInfo.TYPE_CLASS_TEXT
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE && processInput()) {
                    dialog.dismiss()
                    true
                } else {
                    false
                }
            }
        }

        dialog = AlertDialog.Builder(context)
                .setTitle("Rename")
                .setPositiveButton("DONE", null)
                .setNegativeButton("CANCEL", null)
                .setView(editText)
                .create()


        dialog.apply {
            setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    if (processInput()) dialog.dismiss()
                }
            }
            show()
        }
    }

    private fun processInput(): Boolean {
        val input = editText.text.toString()
        return if (input.isBlank()) {
            editText.error = "Text input can not be blank."
            false
        } else {
            doOnDone(input)
            true
        }
    }
}