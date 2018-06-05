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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import marabillas.loremar.lmvideodownloader.LMvdActivity;
import marabillas.loremar.lmvideodownloader.LMvdFragment;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.WebConnect;
import marabillas.loremar.lmvideodownloader.bookmarks_feature.AddBookmarkDialog;
import marabillas.loremar.lmvideodownloader.bookmarks_feature.Bookmark;
import marabillas.loremar.lmvideodownloader.history_feature.HistorySQLite;
import marabillas.loremar.lmvideodownloader.history_feature.VisitedPage;
import marabillas.loremar.lmvideodownloader.utils.Utils;

public class BrowserWindow extends LMvdFragment implements View.OnTouchListener, View
        .OnClickListener, LMvdActivity.OnBackPressedListener, View.OnLongClickListener {
    private static final String TAG = "loremarTest";
    private String url;
    private View view;
    private TouchableWebView page;
    private SSLSocketFactory defaultSSLSF;

    private View videosFoundHUD;
    private float prevX, prevY;
    private ProgressBar findingVideoInProgress;
    private int numLinksInspected;
    private TextView videosFoundText;
    private boolean moved = false;
    private GestureDetector gesture;

    private View foundVideosWindow;
    private VideoList videoList;
    private TextView foundVideosQueue;
    private TextView foundVideosDelete;
    private TextView foundVideosClose;

    private TextView numWindows;

    private ProgressBar loadingPageProgress;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == videosFoundHUD) {
            gesture.onTouchEvent(event);

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (!moved) v.performClick();
                    moved = false;
                    break;
                case MotionEvent.ACTION_DOWN:
                    prevX = event.getRawX();
                    prevY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    moved = true;
                    float moveX = event.getRawX() - prevX;
                    videosFoundHUD.setX(videosFoundHUD.getX() + moveX);
                    prevX = event.getRawX();
                    float moveY = event.getRawY() - prevY;
                    videosFoundHUD.setY(videosFoundHUD.getY() + moveY);
                    prevY = event.getRawY();
                    float width = getResources().getDisplayMetrics().widthPixels;
                    float height = getResources().getDisplayMetrics().heightPixels;
                    if ((videosFoundHUD.getX() + videosFoundHUD.getWidth()) >= width
                            || videosFoundHUD.getX() <= 0) {
                        videosFoundHUD.setX(videosFoundHUD.getX() - moveX);
                    }
                    if ((videosFoundHUD.getY() + videosFoundHUD.getHeight()) >= height
                            || videosFoundHUD.getY() <= 0) {
                        videosFoundHUD.setY(videosFoundHUD.getY() - moveY);
                    }
                    break;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == videosFoundHUD) {
            foundVideosWindow.setVisibility(View.VISIBLE);
        } else if (v == foundVideosQueue) {
            videoList.saveCheckedItemsForDownloading();
            videoList.deleteCheckedItems();
            updateFoundVideosBar();
        } else if (v == foundVideosDelete) {
            videoList.deleteCheckedItems();
            updateFoundVideosBar();
        } else if (v == foundVideosClose) {
            foundVideosWindow.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        url = data.getString("url");
        defaultSSLSF = HttpsURLConnection.getDefaultSSLSocketFactory();
        setRetainInstance(true);
    }

    private void createTopBar() {
        final DrawerLayout layout = getActivity().findViewById(R.id.drawer);
        ImageView menu = view.findViewById(R.id.menuButton);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.openDrawer(Gravity.START);
            }
        });

        TextView close = view.findViewById(R.id.closeWindow);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage("Are you sure you want to close this window?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getLMvdActivity().getBrowserManager().closeWindow(BrowserWindow.this);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    private void createNavigationBar() {
        TextView prev = view.findViewById(R.id.prevButton);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView page = BrowserWindow.this.page;
                if (page.canGoBack()) page.goBack();
            }
        });
        TextView next = view.findViewById(R.id.nextButton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView page = BrowserWindow.this.page;
                if (page.canGoForward()) page.goForward();
            }
        });

        TextView bookmarkThis = view.findViewById(R.id.bookmarkButton);
        bookmarkThis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bookmark bookmark = new Bookmark();
                bookmark.icon = page.getFavicon();
                bookmark.title = page.getTitle();
                bookmark.url = page.getUrl();
                new AddBookmarkDialog(getActivity(), bookmark).show();
            }
        });

        TextView reload = view.findViewById(R.id.reload);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page.reload();
            }
        });

        numWindows = view.findViewById(R.id.numWindows);
        numWindows.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupWindow popupWindow = new PopupWindow(getActivity());
                popupWindow.setContentView(getLMvdActivity().getBrowserManager().getAllWindows());
                popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setFocusable(true);
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.GRAY));

                popupWindow.showAtLocation(numWindows, Gravity.BOTTOM | Gravity.END, 0,
                        view.findViewById(R.id.navigationBar).getHeight());
            }
        });

        TextView newWindow = view.findViewById(R.id.plusWindow);
        newWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                dialog.setMessage(getResources().getString(R.string.enter_web));
                final EditText text = new EditText(getActivity());
                text.setSingleLine(true);
                text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                        .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                text.setHint("type here");
                text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        Utils.hideSoftKeyboard(getActivity(), text.getWindowToken());
                        dialog.cancel();
                        new WebConnect(text, getLMvdActivity()).connect();
                        return false;
                    }
                });
                dialog.setView(text);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.hideSoftKeyboard(getActivity(), text.getWindowToken());
                        new WebConnect(text, getLMvdActivity()).connect();
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.hideSoftKeyboard(getActivity(), text.getWindowToken());
                    }
                });
                dialog.show();
            }
        });
    }

    private void createVideosFoundHUD() {
        videosFoundHUD = view.findViewById(R.id.videosFoundHUD);
        videosFoundHUD.setOnTouchListener(this);
        videosFoundHUD.setOnClickListener(this);
        gesture = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                videosFoundHUD.performClick();
                return true;
            }
        });

        findingVideoInProgress = videosFoundHUD.findViewById(R.id.findingVideosInProgress);
        findingVideoInProgress.setVisibility(View.GONE);

        videosFoundText = videosFoundHUD.findViewById(R.id.videosFoundText);
    }

    private void createFoundVideosWindow() {
        foundVideosWindow = view.findViewById(R.id.foundVideosWindow);
        videoList = new VideoList(getActivity(), (RecyclerView) foundVideosWindow.findViewById(R
                .id.videoList)) {
            @Override
            void onItemDeleted() {
                updateFoundVideosBar();
            }
        };

        foundVideosWindow.setVisibility(View.GONE);

        foundVideosQueue = foundVideosWindow.findViewById(R.id.foundVideosQueue);
        foundVideosDelete = foundVideosWindow.findViewById(R.id.foundVideosDelete);
        foundVideosClose = foundVideosWindow.findViewById(R.id.foundVideosClose);
        foundVideosQueue.setOnClickListener(this);
        foundVideosDelete.setOnClickListener(this);
        foundVideosClose.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.browser, container, false);
            page = view.findViewById(R.id.page);
            loadingPageProgress = view.findViewById(R.id.loadingPageProgress);
            loadingPageProgress.setVisibility(View.GONE);

            createTopBar();

            createNavigationBar();

            createVideosFoundHUD();

            createFoundVideosWindow();

            updateFoundVideosBar();
        }

        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        numLinksInspected = 0;
        WebSettings webSettings = page.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        page.setWebViewClient(new WebViewClient() {//it seems not setting webclient, launches
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
                    }
                });

                loadingPageProgress.setVisibility(View.VISIBLE);

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                loadingPageProgress.setVisibility(View.GONE);
            }

            @Override
            public void onLoadResource(final WebView view, final String url) {
                //Log.i("loremarTest", getClass().getEnclosingMethod().getName()+ "=url:" + url);
                final String page = view.getUrl();
                final String title = view.getTitle();
                new Thread() {
                    @Override
                    public void run() {
                        String urlLowerCase = url.toLowerCase();
                        String[] filters = getResources().getStringArray(R.array.videourl_filters);
                        boolean urlMightBeVideo = false;
                        for (String filter : filters) {
                            if (urlLowerCase.contains(filter)) {
                                urlMightBeVideo = true;
                                break;
                            }
                        }
                        if (urlMightBeVideo) {
                            numLinksInspected++;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (findingVideoInProgress.getVisibility() == View.GONE) {
                                        findingVideoInProgress.setVisibility(View.VISIBLE);
                                    }
                                }
                            });

                            Utils.disableSSLCertificateChecking();
                            Log.i(TAG, "retreiving headers from " + url);

                            URLConnection uCon = null;
                            try {
                                uCon = new URL(url).openConnection();
                                uCon.connect();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (uCon != null) {
                                String contentType = uCon.getHeaderField("content-type");

                                if (contentType != null) {
                                    contentType = contentType.toLowerCase();
                                    if (contentType.contains("video")) {
                                        addVideoToList(uCon, page, title, contentType);
                                    } else if (contentType.equals("application/x-mpegurl") ||
                                            contentType.equals("application/vnd.apple.mpegurl")) {
                                        addVideosToListFromM3U8(uCon, page, title, contentType);
                                    } else if (contentType.contains("audio")) {
                                        addAudio(uCon, page);
                                    } else Log.i(TAG, "Not a video. Content type = " +
                                            contentType);
                                } else {
                                    Log.i(TAG, "no content type");
                                }
                            } else Log.i(TAG, "no connection");

                            //restore default sslsocketfactory
                            HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSF);
                            numLinksInspected--;
                            if (numLinksInspected <= 0) {
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

            @android.support.annotation.Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (getActivity().getSharedPreferences("settings", 0).getBoolean(getString(R
                        .string.adBlockON), true)
                        && (url.contains("ad") || url.contains("banner") || url.contains("pop"))
                        && getLMvdActivity().getBrowserManager().checkUrlIfAds(url)) {
                    Log.i("loremarTest", "Ads detected: " + url);
                    return new WebResourceResponse(null, null, null);
                }
                return super.shouldInterceptRequest(view, url);
            }

            @android.support.annotation.Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (getActivity().getSharedPreferences("settings", 0).getBoolean(getString
                            (R.string.adBlockON), true)
                            && (request.getUrl().toString().contains("ad") ||
                            request.getUrl().toString().contains("banner") ||
                            request.getUrl().toString().contains("pop"))
                            && getLMvdActivity().getBrowserManager().checkUrlIfAds(request.getUrl()
                            .toString())) {
                        Log.i("loremarTest", "Ads detected: " + request.getUrl().toString());
                        return new WebResourceResponse(null, null, null);
                    } else return null;
                } else {
                    return shouldInterceptRequest(view, request.getUrl().toString());
                }
            }
        });
        page.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                loadingPageProgress.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                VisitedPage vp = new VisitedPage();
                vp.title = title;
                vp.link = view.getUrl();
                new HistorySQLite(getActivity()).addPageToHistory(vp);
            }
        });
        page.setOnLongClickListener(this);
        page.loadUrl(url);
    }

    private void addVideoToList(URLConnection uCon, String page, String title, String contentType) {
        try {
            String size = uCon.getHeaderField("content-length");
            String link = uCon.getHeaderField("Location");
            if (link == null) {
                link = uCon.getURL().toString();
            }

            String host = new URL(page).getHost();
            String website = null;
            boolean chunked = false;

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
                size = ytCon.getHeaderField("content-length");
            } else if (host.contains("dailymotion.com")) {
                chunked = true;
                website = "dailymotion.com";
                link = link.replaceAll("(frag\\()+(\\d+)+(\\))", "FRAGMENT");
                size = null;
            } else if (host.contains("vimeo.com") && link.endsWith("m4s")) {
                chunked = true;
                website = "vimeo.com";
                link = link.replaceAll("(segment-)+(\\d+)", "SEGMENT");
                size = null;
            }

            String name = "video";
            if (title != null) {
                name = title;
            }
            String type;
            switch (contentType) {
                case "video/mp4":
                    type = "mp4";
                    break;
                case "video/webm":
                    type = "webm";
                    break;
                case "video/mp2t":
                    type = "ts";
                    break;
                default:
                    type = "mp4";
                    break;
            }

            videoList.addItem(size, type, link, name, page, chunked, website);

            updateFoundVideosBar();
            String videoFound = "name:" + name + "\n" +
                    "link:" + link + "\n" +
                    "type:" + contentType + "\n" +
                    "size:" + size;
            Log.i(TAG, videoFound);
        } catch (IOException e) {
            Log.e("loremarTest", "Exception in adding video to " +
                    "list");
        }
    }

    private void addVideosToListFromM3U8(URLConnection uCon, String page, String title, String
            contentType) {
        try {
            String host;
            host = new URL(page).getHost();
            if (host.contains("twitter.com") || host.contains("metacafe.com") || host.contains
                    ("myspace.com")) {
                InputStream in = uCon.getInputStream();
                InputStreamReader inReader = new InputStreamReader(in);
                BufferedReader buffReader = new BufferedReader(inReader);
                String line;
                String prefix = null;
                String type = null;
                String name = "video";
                String website = null;
                if (title != null) {
                    name = title;
                }
                if (host.contains("twitter.com")) {
                    prefix = "https://video.twimg.com";
                    type = "ts";
                    website = "twitter.com";
                } else if (host.contains("metacafe.com")) {
                    String link = uCon.getURL().toString();
                    prefix = link.substring(0, link.lastIndexOf("/") + 1);
                    website = "metacafe.com";
                    type = "mp4";
                } else if (host.contains("myspace.com")) {
                    String link = uCon.getURL().toString();
                    website = "myspace.com";
                    type = "ts";
                    videoList.addItem(null, type, link, name, page, true, website);

                    updateFoundVideosBar();
                    String videoFound = "name:" + name + "\n" +
                            "link:" + link + "\n" +
                            "type:" + contentType + "\n" +
                            "size: null";
                    Log.i(TAG, videoFound);
                    return;
                }
                while ((line = buffReader.readLine()) != null) {
                    if (line.endsWith(".m3u8")) {
                        String link = prefix + line;
                        videoList.addItem(null, type, link, name, page, true, website);

                        updateFoundVideosBar();
                        String videoFound = "name:" + name + "\n" +
                                "link:" + link + "\n" +
                                "type:" + contentType + "\n" +
                                "size: null";
                        Log.i(TAG, videoFound);
                    }
                }
            } else {
                Log.i("loremarTest", "Content type is " + contentType + " but site is not " +
                        "supported");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addAudio(URLConnection uCon, String page) {
        String host;
        try {
            host = new URL(page).getHost();
            if (host.contains("youtube.com")) {
                for (int i = 0; i < videoList.getSize(); i++) {
                    if (page.equals(videoList.getPage(i)) && videoList.noAudio(i)) {
                        String link = uCon.getURL().toString();
                        int r = link.lastIndexOf("&range");
                        if (r > 0) {
                            link = link.substring(0, r);
                        }
                        videoList.addAudio(i, link);
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        page.stopLoading();
        page.destroy();
        super.onDestroy();
    }

    private void updateFoundVideosBar() {
        final String videosFoundString = "Videos: " + videoList.getSize() + " found";
        final SpannableStringBuilder sb = new SpannableStringBuilder(videosFoundString);
        final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(0, 0, 255));
        final StyleSpan bss = new StyleSpan(Typeface.BOLD);
        sb.setSpan(fcs, 8, 8 + String.valueOf(videoList.getSize()).length(), Spanned
                .SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(bss, 8, 8 + String.valueOf(videoList.getSize()).length(), Spanned
                .SPAN_INCLUSIVE_INCLUSIVE);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                videosFoundText.setText(sb);
            }
        });
    }

    @Override
    public void onBackpressed() {
        if (foundVideosWindow.getVisibility() == View.VISIBLE) {
            foundVideosWindow.setVisibility(View.GONE);
        } else if (page.canGoBack()) {
            page.goBack();
        } else {
            new AlertDialog.Builder(getActivity())
                    .setMessage("Are you sure you want to close this window?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getLMvdActivity().getBrowserManager().closeWindow(BrowserWindow.this);
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create()
                    .show();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        final WebView.HitTestResult hit = page.getHitTestResult();
        if (hit.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
            if (hit.getExtra() != null) {
                View point = new View(getActivity());
                point.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                        .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                if (view != null) {
                    ((ViewGroup) view).addView(point);
                }
                point.getLayoutParams().height = 10;
                point.getLayoutParams().width = 10;
                point.setX(page.getClickX());
                point.setY(page.getClickY());
                PopupMenu menu = new PopupMenu(getActivity(), point);
                menu.getMenu().add("Open in new window");
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        getLMvdActivity().getBrowserManager().newWindow(hit.getExtra());
                        return true;
                    }
                });
                menu.show();
            }
        }
        return true;
    }

    public void updateNumWindows(int num) {
        final String numWindowsString = "Windows(" + num + ")";
        final SpannableStringBuilder sb = new SpannableStringBuilder(numWindowsString);
        final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(0, 0, 255));
        final StyleSpan bss = new StyleSpan(Typeface.BOLD);
        sb.setSpan(fcs, 8, 10 + num / 10, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(bss, 8, 10 + num / 10, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                numWindows.setText(sb);
            }
        });
    }

    public WebView getWebView() {
        return page;
    }
}
