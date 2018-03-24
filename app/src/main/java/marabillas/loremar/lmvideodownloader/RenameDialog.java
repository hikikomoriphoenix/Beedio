/*
 *     LM videodownloader is a browser app for android, made to easily
 *     download videos.
 *     Copyright (C) 2018 Loremar Marabillas
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

package marabillas.loremar.lmvideodownloader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by loremar on 3/20/18.
 *
 */

public abstract class RenameDialog implements DialogInterface.OnClickListener {
    private EditText text;

    RenameDialog(Context context, String hint) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        text = new EditText(context);
        text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        text.setHint(hint);
        dialog.setView(text);
        dialog.setMessage("Type new name:");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", this);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", this);
        dialog.show();
    }

    @Override
    public final void onClick(DialogInterface dialog, int which) {
        if(which == DialogInterface.BUTTON_POSITIVE) {
            onOK(text.getText().toString());
        }
    }

    abstract void onOK(String newName);
}
