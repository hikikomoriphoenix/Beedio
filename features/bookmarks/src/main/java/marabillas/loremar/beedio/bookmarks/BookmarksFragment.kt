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

package marabillas.loremar.beedio.bookmarks

import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.base.database.BookmarksSQLite
import marabillas.loremar.beedio.base.extensions.recyclerView
import marabillas.loremar.beedio.base.extensions.toPixels
import marabillas.loremar.beedio.base.extensions.toolbar
import marabillas.loremar.beedio.base.mvvm.MainViewModel
import marabillas.loremar.beedio.base.web.WebNavigation
import javax.inject.Inject

class BookmarksFragment @Inject constructor() :
        DaggerFragment(),
        BookmarksAdapter.ItemEventListener,
        View.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var bookmarksAdapter: BookmarksAdapter

    @Inject
    lateinit var webNavigation: WebNavigation

    private lateinit var mainViewModel: MainViewModel
    private lateinit var bookmarksSQLite: BookmarksSQLite
    private lateinit var bookmarksClipboardManager: BookmarksClipboardManager

    private val toolbar; get() = toolbar(R.id.bookmarks_toolbar)
    private val pasteBtn: ExtendedFloatingActionButton?
        get() = view?.findViewById(R.id.fab_paste_here)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bookmarks, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            mainViewModel = ViewModelProvider(it::getViewModelStore, viewModelFactory).get(MainViewModel::class.java)
            bookmarksSQLite = BookmarksSQLite(it)
            bookmarksClipboardManager = BookmarksClipboardManager(bookmarksSQLite)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bookmarks_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.recyclerView(R.id.bookmarks_recyclerview).apply {
            adapter = bookmarksAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener { mainViewModel.setIsNavDrawerOpen(true) }

        loadBookmarksData()

        bookmarksAdapter.itemEventListener = this

        pasteBtn?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            pasteBtn -> {
                val pasted = bookmarksClipboardManager.paste()
                if (pasted)
                    loadBookmarksData()
                else
                    Snackbar.make(requireView(), R.string.bookmark_not_exist, Snackbar.LENGTH_SHORT)
                            .show()
                if (bookmarksClipboardManager.isClipboardEmpty)
                    pasteBtn?.isVisible = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bookmarks_menu_new_folder -> {
                val editText = EditText(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    hint = resources.getString(R.string.enter_new_folder_name)
                }
                MaterialAlertDialogBuilder(requireContext()).setView(editText)
                        .setPositiveButton("OK") { _, _ ->
                            bookmarksSQLite.addFolder(editText.text.toString())
                            loadBookmarksData()
                            Snackbar.make(requireView(),
                                    resources.getString(R.string.new_folder_added),
                                    Snackbar.LENGTH_SHORT)
                                    .show()
                        }
                        .setNegativeButton("CANCEL", null)
                        .create()
                        .show()
            }
        }
        return true
    }

    private fun loadBookmarksData() {
        val bookmarks = mutableListOf<BookmarksItem>()
        if (bookmarksSQLite.currentTable != BookmarksSQLite.ROOT_FOLDER) {
            val b = BookmarksItem(
                    type = "upFolder",
                    icon = resources.getDrawable(R.drawable.ic_folder_yellow_24dp),
                    title = "..."
            )
            bookmarks.add(b)
        }
        val cursor: Cursor = bookmarksSQLite.bookmarks
        while (cursor.moveToNext()) {
            val type = cursor.getString(cursor.getColumnIndex("type"))
            val title = cursor.getString(cursor.getColumnIndex("title"))
            val icon: Drawable
            var url: String? = null
            if (type == "folder") {
                icon = resources.getDrawable(R.drawable.ic_folder_yellow_24dp)
            } else {
                val iconInBytes = cursor.getBlob(cursor.getColumnIndex("icon"))
                icon = if (iconInBytes != null) {
                    val iconBitmap = BitmapFactory.decodeByteArray(iconInBytes, 0, iconInBytes.size)
                    val size = 24.toPixels(resources)
                    val scaledBitmap = Bitmap.createScaledBitmap(iconBitmap, size, size, false)
                    BitmapDrawable(resources, scaledBitmap)
                } else {
                    resources.getDrawable(R.drawable.ic_bookmark_border_24dp)
                }
                url = cursor.getString(cursor.getColumnIndex("link"))
            }
            val b = BookmarksItem(
                    type = type,
                    icon = icon,
                    title = title,
                    url = url
            )
            bookmarks.add(b)
        }
        cursor.close()
        bookmarksAdapter.bookmarks = bookmarks
    }

    override fun onBookmarksItemClick(bookmarksItem: BookmarksItem, position: Int) {
        when (bookmarksItem.type) {
            "upFolder" -> {
                bookmarksSQLite.currentTable = bookmarksSQLite.currentTable.substringBeforeLast('_')
                loadBookmarksData()
            }
            "folder" -> {
                val suffix = if (bookmarksSQLite.currentTable == BookmarksSQLite.ROOT_FOLDER)
                    position + 1
                else
                    position
                bookmarksSQLite.currentTable = "${bookmarksSQLite.currentTable}_$suffix"
                loadBookmarksData()
            }
            "link" -> {
                bookmarksItem.url?.let {
                    val validInput = webNavigation.navigateTo(it)
                    mainViewModel.goToBrowser(validInput)
                }
            }
        }
    }

    override fun onShowRenameDialog(position: Int) {
        val editText = EditText(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            hint = resources.getString(R.string.enter_new_title)
        }
        MaterialAlertDialogBuilder(requireContext()).setView(editText)
                .setPositiveButton("OK") { _, _ ->
                    if (editText.text.isNotBlank()) {
                        if (bookmarksSQLite.currentTable == BookmarksSQLite.ROOT_FOLDER)
                            bookmarksSQLite.renameBookmarkTitle(position + 1, editText.text.toString())
                        else
                            bookmarksSQLite.renameBookmarkTitle(position, editText.text.toString())
                    }
                    loadBookmarksData()
                }
                .setNegativeButton("CANCEL", null)
                .create()
                .show()
    }

    override fun onCopyItem(position: Int) {
        if (bookmarksSQLite.currentTable == BookmarksSQLite.ROOT_FOLDER)
            bookmarksClipboardManager.copy(position + 1)
        else
            bookmarksClipboardManager.copy(position)
        pasteBtn?.isVisible = true
    }

    override fun onCutItem(position: Int) {
        if (bookmarksSQLite.currentTable == BookmarksSQLite.ROOT_FOLDER)
            bookmarksClipboardManager.cut(position + 1)
        else
            bookmarksClipboardManager.cut(position)
        pasteBtn?.isVisible = true
    }

    override fun onPasteItem(position: Int) {
        val pasted = if (bookmarksSQLite.currentTable == BookmarksSQLite.ROOT_FOLDER)
            bookmarksClipboardManager.paste(position + 1)
        else
            bookmarksClipboardManager.paste(position)

        if (pasted)
            loadBookmarksData()
        else
            Snackbar.make(requireView(), R.string.bookmark_not_exist, Snackbar.LENGTH_SHORT).show()

        if (bookmarksClipboardManager.isClipboardEmpty)
            pasteBtn?.isVisible = false
    }

    override fun onDeleteItem(position: Int) {
        if (bookmarksSQLite.currentTable == BookmarksSQLite.ROOT_FOLDER)
            bookmarksSQLite.delete(position + 1)
        else
            bookmarksSQLite.delete(position)
        loadBookmarksData()

        if (bookmarksClipboardManager.isClipboardEmpty)
            pasteBtn?.isVisible = false
    }
}