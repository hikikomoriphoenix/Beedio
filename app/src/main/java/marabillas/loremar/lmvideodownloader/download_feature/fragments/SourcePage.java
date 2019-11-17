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

package marabillas.loremar.lmvideodownloader.download_feature.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import marabillas.loremar.lmvideodownloader.utils.Utils;

public class SourcePage extends WebViewFragment {
    private long size;
    private String page;
    private SSLSocketFactory defaultSSLSF;

    private OnUpdateLinkListener onUpdateLinkListener;

    interface OnUpdateLinkListener {
        void updateLink(String link);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        size = getArguments().getLong("size");
        page = getArguments().getString("page");
        defaultSSLSF = HttpsURLConnection.getDefaultSSLSocketFactory();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        WebSettings webSettings = getWebView().getSettings();
        webSettings.setJavaScriptEnabled(true);
        getWebView().setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }

            @Override
            public void onLoadResource(WebView view, final String url) {
                new Thread() {
                    @Override
                    public void run() {
                        String urlLowerCase = url.toLowerCase();
                        if (urlLowerCase.contains("mp4") || urlLowerCase.contains("video")) {
                            Utils.disableSSLCertificateChecking();
                            Log.i("loremarTest", "retreiving headers from " + url);
                            URLConnection uCon = null;
                            try {
                                uCon = new URL(url).openConnection();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (uCon != null) {
                                String contentType = uCon.getHeaderField("content-type");
                                if (contentType != null) {
                                    contentType = contentType.toLowerCase();

                                    String link = uCon.getHeaderField("Location");
                                    if (link == null) {
                                        link = uCon.getURL().toString();
                                    }

                                    try {
                                        if (contentType.contains("video")) {
                                            String host = new URL(page).getHost();
                                            String sizeString;
                                            if (host.contains("youtube.com") || (new URL(link).getHost().contains("googlevideo.com")
                                            )) {
                                                //link  = link.replaceAll("(range=)+(.*)+(&)",
                                                // "");
                                                int r = link.lastIndexOf("&range");
                                                if (r > 0) {
                                                    link = link.substring(0, r);
                                                }
                                                URLConnection ytCon;
                                                ytCon = new URL(link).openConnection();
                                                ytCon.connect();
                                                sizeString = ytCon.getHeaderField
                                                        ("content-length");
                                            } else {
                                                sizeString = uCon.getHeaderField(
                                                        "content-length");
                                            }

                                            if (sizeString != null && Long.parseLong(sizeString) == size) {
                                                Log.i("loremarTest", "video with same size found");
                                                onUpdateLinkListener.updateLink(link);
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        new AlertDialog.Builder(getActivity())
                                                                .setMessage("Link has been updated and video " +
                                                                        "is now being downloaded")
                                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        getFragmentManager().beginTransaction
                                                                                ().remove(SourcePage.this)
                                                                                .commit();
                                                                    }
                                                                })
                                                                .setCancelable(false)
                                                                .create()
                                                                .show();
                                                    }
                                                });
                                            }
                                        } else Log.i("loremarTest", "not a video");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.i("loremarTest", "no content type");
                                }
                            } else Log.i("loremarTest", "no connection");

                            HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSF);
                        }
                    }
                }.start();
            }
        });
        getWebView().loadUrl(page);
    }

    void setOnUpdateLinkListener(OnUpdateLinkListener onUpdateLinkListener) {
        this.onUpdateLinkListener = onUpdateLinkListener;
    }
}
