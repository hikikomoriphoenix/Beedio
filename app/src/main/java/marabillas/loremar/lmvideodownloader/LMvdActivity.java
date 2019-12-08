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
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import marabillas.loremar.lmvideodownloader.bookmarks_feature.Bookmarks;
import marabillas.loremar.lmvideodownloader.browsing_feature.BrowserManager;
import marabillas.loremar.lmvideodownloader.browsing_feature.BrowserWebChromeClient;
import marabillas.loremar.lmvideodownloader.download_feature.fragments.Downloads;
import marabillas.loremar.lmvideodownloader.history_feature.History;
import marabillas.loremar.lmvideodownloader.options_feature.OptionsFragment;
import marabillas.loremar.lmvideodownloader.utils.Utils;

public class LMvdActivity extends Activity implements TextView.OnEditorActionListener,
        View.OnClickListener, AdapterView.OnItemClickListener, BrowserWebChromeClient.FileChooseListener {
    private EditText webBox;
    private BrowserManager browserManager;
    private Uri appLinkData;
    private DrawerLayout layout;

    private ValueCallback<Uri[]> fileChooseValueCallbackMultiUri;
    private ValueCallback<Uri> fileChooseValueCallbackSingleUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        webBox = findViewById(R.id.web);
        webBox.setOnEditorActionListener(this);

        ImageButton go = findViewById(R.id.go);
        go.setOnClickListener(this);

        if ((browserManager = (BrowserManager) getFragmentManager().findFragmentByTag("BM")) == null) {
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
                layout.openDrawer(GravityCompat.START);
            }
        });

        ListView listView = findViewById(R.id.menu);
        String[] menuItems = new String[]{"Home", "Browser", "Downloads", "Bookmarks",
                "History", "About", "Options"};
        ArrayAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_list_item_1, menuItems) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);

                int iconId = 0;
                switch (position) {
                    case 0:
                        iconId = R.drawable.ic_home_white_24dp;
                        break;
                    case 1:
                        iconId = R.drawable.ic_globe_white_24dp;
                        break;
                    case 2:
                        iconId = R.drawable.ic_download_white_24dp;
                        break;
                    case 3:
                        iconId = R.drawable.ic_star_white_24dp;
                        break;
                    case 4:
                        iconId = R.drawable.ic_history_white_24dp;
                        break;
                    case 5:
                        iconId = R.drawable.ic_info_outline_white_24dp;
                        break;
                    case 6:
                        iconId = R.drawable.ic_settings_white_24dp;
                }
                if (iconId != 0) {
                    Drawable icon = AppCompatResources.getDrawable(getContext(), iconId);
                    textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                    textView.setCompoundDrawablePadding((int) (16 * getResources().getDisplayMetrics().density));
                }

                return view;
            }
        };
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);

        RecyclerView videoSites = findViewById(R.id.homeSites);
        videoSites.setAdapter(new VideoStreamingSitesList(this));
        videoSites.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        new WebConnect(webBox, this).connect();
        return false;
    }

    @Override
    public void onBackPressed() {
        Fragment sourcePage = getFragmentManager().findFragmentByTag("updateSourcePage");
        if (sourcePage != null) {
            getFragmentManager().beginTransaction().remove(sourcePage).commit();
        } else if (layout.isDrawerVisible(GravityCompat.START)) {
            layout.closeDrawer(GravityCompat.START);
        } else if (LMvdApp.getInstance().getOnBackPressedListener() != null) {
            LMvdApp.getInstance().getOnBackPressedListener().onBackpressed();
        } else super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if (getCurrentFocus() != null) {
            Utils.hideSoftKeyboard(this, getCurrentFocus().getWindowToken());
            new WebConnect(webBox, this).connect();
        }
    }

    private void closeDownloads() {
        Fragment fragment = getFragmentManager().findFragmentByTag("Downloads");
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    private void closeBookmarks() {
        Fragment fragment = getFragmentManager().findFragmentByTag("Bookmarks");
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    private void closeHistory() {
        Fragment fragment = getFragmentManager().findFragmentByTag("History");
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    private void closeOptions() {
        Fragment fragment = getFragmentManager().findFragmentByTag("Options");
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    private void homeClicked() {
        browserManager.hideCurrentWindow();
        closeDownloads();
        closeBookmarks();
        closeHistory();
        closeOptions();
        setOnBackPressedListener(null);
    }

    public void browserClicked() {
        browserManager.unhideCurrentWindow();
        closeDownloads();
        closeBookmarks();
        closeHistory();
        closeOptions();
    }

    private void downloadsClicked() {
        closeBookmarks();
        closeHistory();
        closeOptions();
        if (getFragmentManager().findFragmentByTag("Downloads") == null) {
            browserManager.hideCurrentWindow();
            getFragmentManager().beginTransaction().add(R.id.main, new Downloads(),
                    "Downloads").commit();
        }
    }

    private void bookmarksClicked() {
        closeDownloads();
        closeHistory();
        closeOptions();
        if (getFragmentManager().findFragmentByTag("Bookmarks") == null) {
            browserManager.hideCurrentWindow();
            getFragmentManager().beginTransaction().add(R.id.main, new Bookmarks(), "Bookmarks")
                    .commit();
        }
    }

    private void historyClicked() {
        closeDownloads();
        closeBookmarks();
        closeOptions();
        if (getFragmentManager().findFragmentByTag("History") == null) {
            browserManager.hideCurrentWindow();
            getFragmentManager().beginTransaction().add(R.id.main, new History(), "History")
                    .commit();
        }
    }

    private void aboutClicked() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.about, ((ViewGroup) this.getWindow().getDecorView()), false);
        new AlertDialog.Builder(this)
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private void optionsClicked() {
        closeDownloads();
        closeBookmarks();
        closeHistory();
        if (getFragmentManager().findFragmentByTag("Options") == null) {
            browserManager.hideCurrentWindow();
            getFragmentManager().beginTransaction().add(R.id.main, new OptionsFragment(), "Options")
                    .commit();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        layout.closeDrawers();
        switch (position) {
            case 0:
                homeClicked();
                break;
            case 1:
                browserClicked();
                break;
            case 2:
                downloadsClicked();
                break;
            case 3:
                bookmarksClicked();
                break;
            case 4:
                historyClicked();
                break;
            case 5:
                aboutClicked();
                break;
            case 6:
                optionsClicked();
                break;
        }
    }

    public interface OnBackPressedListener {
        void onBackpressed();
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        LMvdApp.getInstance().setOnBackPressedListener(onBackPressedListener);
    }

    public BrowserManager getBrowserManager() {
        return browserManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (appLinkData != null) {
            browserManager.newWindow(appLinkData.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onRequestPermissionsResultCallback.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
    }

    private ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback;

    public void setOnRequestPermissionsResultListener(ActivityCompat
                                                              .OnRequestPermissionsResultCallback
                                                       onRequestPermissionsResultCallback) {
        this.onRequestPermissionsResultCallback = onRequestPermissionsResultCallback;
    }

    @Override
    public ValueCallback<Uri[]> getValueCallbackMultiUri() {
        return fileChooseValueCallbackMultiUri;
    }

    @Override
    public ValueCallback<Uri> getValueCallbackSingleUri() {
        return fileChooseValueCallbackSingleUri;
    }

    @Override
    public void setValueCallbackMultiUri(ValueCallback<Uri[]> valueCallback) {
        fileChooseValueCallbackMultiUri = valueCallback;
    }

    @Override
    public void setValueCallbackSingleUri(ValueCallback<Uri> valueCallback) {
        fileChooseValueCallbackSingleUri = valueCallback;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BrowserWebChromeClient.FILE_CHOOSER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && fileChooseValueCallbackSingleUri != null) {
                    fileChooseValueCallbackSingleUri.onReceiveValue(data.getData());
                    fileChooseValueCallbackSingleUri = null;
                } else if (data != null && fileChooseValueCallbackMultiUri != null) {
                    Uri[] dataUris;
                    try {
                        dataUris = new Uri[]{Uri.parse(data.getDataString())};
                    } catch (Exception e) {
                        dataUris = null;
                    }
                    fileChooseValueCallbackMultiUri.onReceiveValue(dataUris);
                    fileChooseValueCallbackMultiUri = null;
                }
            } else if (fileChooseValueCallbackSingleUri != null) {
                fileChooseValueCallbackSingleUri.onReceiveValue(null);
                fileChooseValueCallbackSingleUri = null;
            } else if (fileChooseValueCallbackMultiUri != null) {
                fileChooseValueCallbackMultiUri.onReceiveValue(null);
                fileChooseValueCallbackMultiUri = null;
            }
        }
    }
}
