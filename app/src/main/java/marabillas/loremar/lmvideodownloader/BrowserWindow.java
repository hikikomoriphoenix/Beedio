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
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class BrowserWindow extends Fragment implements View.OnTouchListener, View.OnClickListener {
    private static final String TAG = "loremarTest";
    private String url;
    private String title;
    private View view;
    private WebView page;
    private List<Video> videos;
    private SSLSocketFactory defaultSSLSF;

    private View videosFoundHUD;
    private float prevX;
    private float prevY;
    private ProgressBar findingVideoInProgress;
    private int numLinksInspected;
    private TextView videosFoundText;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_UP:
                v.performClick();
                break;
            case MotionEvent.ACTION_DOWN:
                prevX = event.getRawX();
                prevY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getRawX() - prevX;
                videosFoundHUD.setX(videosFoundHUD.getX() + moveX);
                prevX = event.getRawX();
                float moveY = event.getRawY() - prevY;
                videosFoundHUD.setY(videosFoundHUD.getY() + moveY);
                prevY = event.getRawY();
                float width = getResources().getDisplayMetrics().widthPixels;
                float height = getResources().getDisplayMetrics().heightPixels;
                if((videosFoundHUD.getX() + videosFoundHUD.getWidth()) >= width
                        || videosFoundHUD.getX() <= 0) {
                    videosFoundHUD.setX(videosFoundHUD.getX() - moveX);
                }
                if((videosFoundHUD.getY() + videosFoundHUD.getHeight()) >= height
                        || videosFoundHUD.getY() <= 0) {
                    videosFoundHUD.setY(videosFoundHUD.getY() - moveY);
                }
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        url = data.getString("url");
        videos = new ArrayList<>();
        defaultSSLSF = HttpsURLConnection.getDefaultSSLSocketFactory();
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

        videosFoundHUD = view.findViewById(R.id.videosFoundHUD);
        videosFoundHUD.setOnTouchListener(this);
        videosFoundHUD.setOnClickListener(this);

        findingVideoInProgress = videosFoundHUD.findViewById(R.id.findingVideosInProgress);
        findingVideoInProgress.setVisibility(View.GONE);

        videos = new ArrayList<>();

        videosFoundText = videosFoundHUD.findViewById(R.id.videosFoundText);
        String zero = "Videos: 0 found";
        videosFoundText.setText(zero);
        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        numLinksInspected = 0;
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
            public void onLoadResource(final WebView view, final String url) {
                new Thread(){
                    @Override
                    public void run() {
                        String urlLowerCase = url.toLowerCase();
                        if(urlLowerCase.contains("mp4")||urlLowerCase.contains("video")){
                            numLinksInspected++;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if(findingVideoInProgress.getVisibility() == View.GONE) {
                                        findingVideoInProgress.setVisibility(View.VISIBLE);
                                    }
                                }
                            });

                            Utils.disableSSLCertificateChecking();
                            Log.i(TAG, "retreiving headers from " + url);
                            URLConnection uCon = null;
                            try {
                                uCon = new URL(url).openConnection();
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
                                        if(video.size==null) {
                                            video.size = "";
                                        }
                                        else {
                                            video.size = Formatter.formatShortFileSize(BrowserWindow
                                            .this.getActivity(), Long.parseLong(video.size));
                                        }
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
                                        boolean duplicate = false;
                                        for(Video v: videos){
                                            if(v.link.equals(video.link)) {
                                                duplicate = true;
                                                break;
                                            }
                                        }
                                        if(!duplicate) {
                                            videos.add(video);
                                            final String videosFound = String.valueOf(videos.size());
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String videosFoundString = "Videos: " +
                                                            videosFound + " found";
                                                    videosFoundText.setText(videosFoundString);
                                                }
                                            });
                                            String videoFound = "name:" + video.name + "\n" +
                                                    "link:" + video.link + "\n" +
                                                    "type:" + video.type + "\n" +
                                                    "size" + video.size;
                                            Log.i(TAG, videoFound);
                                        }
                                    }
                                    else Log.i(TAG, "not a video");
                                }
                                else {
                                    Log.i(TAG, "no content type");
                                }
                            }
                            else Log.i(TAG, "no connection");

                            //restore default sslsocketfactory
                            HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSF);
                            numLinksInspected--;
                            if(numLinksInspected <= 0) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        findingVideoInProgress.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    }
                }.start();
            }
        });
        page.loadUrl(url);
    }

    class Video{
        String size, type, link, name;
    }
}
