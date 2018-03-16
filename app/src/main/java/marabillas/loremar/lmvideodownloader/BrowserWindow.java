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

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class BrowserWindow extends Fragment {
    private String url;
    private String title;
    private View view;
    private WebView page;
    private List<Video> videos;

    private static final String TAG = "loremarTest";

    class Video{
        String size, type, link, name;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        url = data.getString("url");
        videos = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.browser, container, false);
        page = view.findViewById(R.id.page);
        Button prev = view.findViewById(R.id.prevButton);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView page = BrowserWindow.this.page;
                if(page.canGoBack()) page.goBack();
            }
        });
        Button next = view.findViewById(R.id.nextButton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView page = BrowserWindow.this.page;
                if(page.canGoForward()) page.goForward();
            }
        });
        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        WebSettings webSettings = page.getSettings();
        webSettings.setJavaScriptEnabled(true);
        page.setWebViewClient(new WebViewClient(){//it seems not setting webclient, launches
            //default browser instead of opening the page in webview
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageStarted(final WebView view, final String url, Bitmap favicon) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        TextView urlBox = BrowserWindow.this.view.findViewById(R.id.urlBox);
                        urlBox.setText(url);
                        BrowserWindow.this.title = view.getTitle();
                    }
                });
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onLoadResource(WebView view, final String url) {
                new Thread(){
                    @Override
                    public void run() {
                        String urlLowerCase = url.toLowerCase();
                        if(urlLowerCase.contains("mp4")||urlLowerCase.contains("video")){
                            HttpsURLConnection uCon = null;
                            try {
                                uCon = (HttpsURLConnection) new URL(url).openConnection();
                                uCon.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
                                uCon.connect();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (uCon != null) {
                                String contentType = uCon.getHeaderField("content-type");
                                if(contentType!=null) {
                                    contentType = contentType.toLowerCase();
                                    if (contentType.contains("video/mp4")) {
                                        Video video = new Video();
                                        video.size = uCon.getHeaderField("content-length");
                                        String link = uCon.getHeaderField("Location");
                                        if (link == null) {
                                            link = uCon.getURL().toString();
                                        }
                                        video.link = link;
                                        if (title != null) {
                                            video.name = title;
                                        } else {
                                            video.name = "video";
                                        }
                                        video.type = "mp4";
                                        videos.add(video);
                                        String videoFound = "name:" + video.name + "\n" +
                                                "link:" + video.link + "\n" +
                                                "type:" + video.type + "\n" +
                                                "size" + video.size;
                                        Log.i(TAG, videoFound);
                                    }
                                }
                            }
                        }
                    }
                }.start();
            }
        });
        page.loadUrl(url);
    }
}
