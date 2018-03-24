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

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class LMvd extends Activity implements TextView.OnEditorActionListener, View.OnClickListener {
    private EditText webBox;
    private BrowserManager browserManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        webBox = findViewById(R.id.web);
        webBox.setOnEditorActionListener(this);

        ImageButton go = findViewById(R.id.go);
        go.setOnClickListener(this);

        if(getFragmentManager().findFragmentByTag("BM")==null) {
            getFragmentManager().beginTransaction().add(browserManager = new BrowserManager(),
                    "BM").commit();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        System.out.println("opening webview");
        new WebConnect(webBox, this).connect();
        return false;
    }

    OnBackPressedListener onBackPressedListener;
    @Override
    public void onBackPressed() {
        if(onBackPressedListener!=null) {
            onBackPressedListener.onBackpressed();
        }
        else super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        Utils.hideSoftKeyboard(this);
        System.out.println("opening webview");
        new WebConnect(webBox, this).connect();
    }

    interface OnBackPressedListener {
        void onBackpressed();
    }

    void setOnBackPressedListener (OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    BrowserManager getBrowserManager() {
        return browserManager;
    }
}
