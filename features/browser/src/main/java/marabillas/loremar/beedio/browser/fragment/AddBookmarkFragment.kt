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

package marabillas.loremar.beedio.browser.fragment

import android.content.DialogInterface.BUTTON_POSITIVE
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.base.database.BookmarksSQLite
import marabillas.loremar.beedio.base.extensions.*
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.adapters.AddBookmarkAdapter
import marabillas.loremar.beedio.browser.viewmodel.WebViewsControllerVM
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class AddBookmarkFragment @Inject constructor() : DaggerFragment(), Toolbar.OnMenuItemClickListener, AddBookmarkAdapter.ItemEventListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var addBookmarkAdapter: AddBookmarkAdapter

    @Inject
    lateinit var bookmarksSQLite: BookmarksSQLite

    private lateinit var webViewsControllerVM: WebViewsControllerVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_bookmark, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            webViewsControllerVM = ViewModelProvider(it, viewModelFactory).get(WebViewsControllerVM::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.bottomAppBar(R.id.add_bookmark_bottom_appbar)
                .replaceMenu(R.menu.add_bookmark_menu)

        view.recyclerView(R.id.recycler_add_bookmark).apply {
            adapter = addBookmarkAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onStart() {
        super.onStart()
        webViewsControllerVM.requestActiveWebView { webView ->
            textView(R.id.add_bookmark_title_value)?.apply {
                text = webView?.title
                addIcon(webView?.favicon)
                maxLines = 2
            }
            textView(R.id.add_bookmark_url_value)?.apply {
                text = webView?.url
                maxLines = 2
            }
            setDestFolderText()
        }

        updateFolders()

        requireView().bottomAppBar(R.id.add_bookmark_bottom_appbar)
                .setOnMenuItemClickListener(this)

        toolbar(R.id.add_bookmark_header)?.setNavigationOnClickListener { dismiss() }

        addBookmarkAdapter.itemEventListener = this

        floatingActionButton(R.id.floatingactionbutton_add_bookmark)?.setOnClickListener {
            webViewsControllerVM.requestActiveWebView { webView ->
                val bytes: ByteArray?
                val out = ByteArrayOutputStream()
                bytes = if (webView?.favicon != null && webView.favicon.compress(Bitmap.CompressFormat.PNG, 100,
                                out)) {
                    out.toByteArray()
                } else {
                    null
                }
                bookmarksSQLite.add(bytes, webView?.title, webView?.url)

                Snackbar.make(requireActivity().rootView(),
                        resources.getString(R.string.page_save_into_bookmarks),
                        Snackbar.LENGTH_SHORT).apply {

                    view.textView(com.google.android.material.R.id.snackbar_text)
                            .addIcon(webView?.favicon)
                    show()
                }
                dismiss()
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add_bookmark_menu_new_folder -> showCreateNewFolderDialog()
        }
        return true
    }

    private fun updateFolders() {
        val cursor = bookmarksSQLite.folders
        val folders = mutableListOf<String>()
        if (bookmarksSQLite.currentTable != BookmarksSQLite.ROOT_FOLDER)
            folders.add("...")
        while (cursor.moveToNext()) {
            folders.add(cursor.getString(cursor.getColumnIndex("title")))
        }
        cursor.close()
        addBookmarkAdapter.folders = folders
    }

    private fun showCreateNewFolderDialog() {
        val editText = EditText(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            hint = "Enter folder name"
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.create_new_folder))
                .setView(editText)
                .setPositiveButton("Create", null)
                .setNegativeButton("Cancel", null)
                .create()

        dialog.setOnShowListener {
            (it as AlertDialog).getButton(BUTTON_POSITIVE)
                    .setOnClickListener {
                        if (editText.text.isNullOrBlank())
                            editText.error = "Folder name must not be blank"
                        else {
                            bookmarksSQLite.addFolder(editText.text.toString())
                            dialog.dismiss()
                            updateFolders()
                        }
                    }
        }

        dialog.show()
    }

    override fun onFolderClick(name: String, position: Int) {
        if (position == 0 && bookmarksSQLite.currentTable != BookmarksSQLite.ROOT_FOLDER)
            navigateUp()
        else {
            // position accounts for the upfolder item. This item should be ignored when current
            // table is not root. Hence the extra increment is not needed(Suffix starts at 1).
            val suffix = if (bookmarksSQLite.currentTable == BookmarksSQLite.ROOT_FOLDER)
                position + 1
            else
                position
            bookmarksSQLite.currentTable = "${bookmarksSQLite.currentTable}_$suffix"
            textView(R.id.add_bookmark_dest_value)?.text = name
            updateFolders()
        }
    }

    private fun navigateUp() {
        val upperTable = bookmarksSQLite.currentTable.substringBeforeLast('_')
        bookmarksSQLite.currentTable = upperTable
        updateFolders()
        setDestFolderText()
    }

    private fun setDestFolderText() {
        if (bookmarksSQLite.currentTable == BookmarksSQLite.ROOT_FOLDER)
            textView(R.id.add_bookmark_dest_value)?.text = bookmarksSQLite.currentTable
        else {
            val positionInParentTable = bookmarksSQLite.currentTable
                    .substringAfterLast('_')
            val parentTable = bookmarksSQLite.currentTable
                    .substringBeforeLast('_')
            val c = bookmarksSQLite.bookmarksDatabase
                    .query(parentTable,
                            arrayOf("title"),
                            "oid = $positionInParentTable",
                            null, null, null, null)
            c.moveToNext()
            val dest = c.getString(c.getColumnIndex("title"))
            c.close()
            textView(R.id.add_bookmark_dest_value)?.text = dest
        }
    }

    private fun dismiss() {
        parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
    }

    private fun TextView.addIcon(bitmap: Bitmap?) {
        val drawable = if (bitmap != null) {
            val size = 24.toPixels(resources)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false)
            BitmapDrawable(resources, scaledBitmap)
        } else
            resources.drawable(R.drawable.ic_missing_favicon_placeholder)

        setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        compoundDrawablePadding = 8.toPixels(resources)
        gravity = Gravity.CENTER_VERTICAL
    }
}