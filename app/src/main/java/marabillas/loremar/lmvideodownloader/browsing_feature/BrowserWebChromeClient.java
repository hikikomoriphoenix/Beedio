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

package marabillas.loremar.lmvideodownloader.browsing_feature;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import marabillas.loremar.lmvideodownloader.history_feature.HistorySQLite;
import marabillas.loremar.lmvideodownloader.history_feature.VisitedPage;

public class BrowserWebChromeClient extends WebChromeClient {
    private ProgressBar progressView;
    private Context context;
    private FileChooseListener fileChooseListener;

    public static final int FILE_CHOOSER_REQUEST_CODE = 69125;

    BrowserWebChromeClient(Context context, ProgressBar progressView, FileChooseListener fileChooseListener) {
        this.context = context;
        this.progressView = progressView;
        this.fileChooseListener = fileChooseListener;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        progressView.setProgress(newProgress);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        VisitedPage vp = new VisitedPage();
        vp.title = title;
        vp.link = view.getUrl();
        new HistorySQLite(context).addPageToHistory(vp);
    }

    // file upload callback (Android 2.2 (API level 8) -- Android 2.3 (API level 10)) (hidden method)
    @SuppressWarnings("unused")
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, null);
    }

    // file upload callback (Android 3.0 (API level 11) -- Android 4.0 (API level 15)) (hidden method)
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        openFileChooser(uploadMsg, acceptType, null);
    }

    // file upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
    @SuppressWarnings("unused")
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        if (fileChooseListener.getValueCallbackSingleUri() != null) {
            fileChooseListener.getValueCallbackSingleUri().onReceiveValue(null);
        }
        fileChooseListener.setValueCallbackSingleUri(uploadMsg);

        startChooserActivity();
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        if (fileChooseListener.getValueCallbackMultiUri() != null) {
            fileChooseListener.getValueCallbackMultiUri().onReceiveValue(null);
        }
        fileChooseListener.setValueCallbackMultiUri(filePathCallback);

        startChooserActivity();
        return true;
    }

    private void startChooserActivity() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        if (context != null && context instanceof Activity) {
            ((Activity) context).startActivityForResult(Intent.createChooser(intent, "Choose File:"),
                    FILE_CHOOSER_REQUEST_CODE);
        }
    }

    public interface FileChooseListener {
        ValueCallback<Uri[]> getValueCallbackMultiUri();

        ValueCallback<Uri> getValueCallbackSingleUri();

        void setValueCallbackMultiUri(ValueCallback<Uri[]> valueCallback);

        void setValueCallbackSingleUri(ValueCallback<Uri> valueCallback);
    }
}
