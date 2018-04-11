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
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import marabillas.loremar.lmvideodownloader.download_feature.DownloadManager;
import marabillas.loremar.lmvideodownloader.download_feature.Downloads;

public class LMvd extends Activity implements TextView.OnEditorActionListener, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    //todo add getInstance and replace all instances of "getActivity" or "Activity activity" with
    //this method
    private EditText webBox;
    private BrowserManager browserManager;
    private Uri appLinkData;
    private DrawerLayout layout;
    private Intent downloadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        webBox = findViewById(R.id.web);
        webBox.setOnEditorActionListener(this);

        ImageButton go = findViewById(R.id.go);
        go.setOnClickListener(this);

        if (getFragmentManager().findFragmentByTag("BM") == null) {
            getFragmentManager().beginTransaction().add(browserManager = new BrowserManager(),
                    "BM").commit();
        }
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        //String appLinkAction = appLinkIntent.getAction();
        appLinkData = appLinkIntent.getData();

        layout = findViewById(R.id.drawer);
        ImageView menu = findViewById(R.id.menuButton);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.openDrawer(Gravity.START);
            }
        });

        NavigationView navigationView = findViewById(R.id.menu);
        navigationView.setNavigationItemSelectedListener(this);

        downloadService = new Intent(this, DownloadManager.class);
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
        if(getCurrentFocus()!=null) {
            Utils.hideSoftKeyboard(this, getCurrentFocus().getWindowToken());
            System.out.println("opening webview");
            new WebConnect(webBox, this).connect();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        layout.closeDrawers();
        Fragment fragment;
        switch (item.getTitle().toString()) {
            case "Home":
                browserManager.hideCurrentWindow();
                fragment = getFragmentManager().findFragmentByTag("Downloads");
                if(fragment!=null) {
                    getFragmentManager().beginTransaction().remove(fragment).commit();
                }
                setOnBackPressedListener(null);
                break;
            case "Browser":
                browserManager.unhideCurrentWindow();
                fragment = getFragmentManager().findFragmentByTag("Downloads");
                if(fragment!=null) {
                    getFragmentManager().beginTransaction().remove(fragment).commit();
                }
                break;
            case "Downloads":
                browserManager.hideCurrentWindow();
                getFragmentManager().beginTransaction().add(R.id.main, new Downloads(),
                        "Downloads").commit();
                break;
        }
        return true;
    }

    public interface OnBackPressedListener {
        void onBackpressed();
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    public BrowserManager getBrowserManager() {
        return browserManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(appLinkData!=null) {
            browserManager.newWindow(appLinkData.toString());
        }
    }

    public Intent getDownloadService() {
        return downloadService;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onRequestPermissionsResultCallback.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
    }

    ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback;

    void setOnRequestPermissionsResultListener(ActivityCompat.OnRequestPermissionsResultCallback
                                                       onRequestPermissionsResultCallback) {
        this.onRequestPermissionsResultCallback = onRequestPermissionsResultCallback;
    }

}
