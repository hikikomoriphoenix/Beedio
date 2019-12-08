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
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
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

import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import marabillas.loremar.lmvideodownloader.LMvdActivity;
import marabillas.loremar.lmvideodownloader.LMvdApp;
import marabillas.loremar.lmvideodownloader.LMvdFragment;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.WebConnect;
import marabillas.loremar.lmvideodownloader.bookmarks_feature.AddBookmarkDialog;
import marabillas.loremar.lmvideodownloader.bookmarks_feature.Bookmark;
import marabillas.loremar.lmvideodownloader.utils.Utils;

public class BrowserWindow extends LMvdFragment implements View.OnTouchListener, View
        .OnClickListener, LMvdActivity.OnBackPressedListener, View.OnLongClickListener {
    private String url;
    private View view;
    private TouchableWebView page;
    private SSLSocketFactory defaultSSLSF;

    private View videosFoundHUD;
    private float prevX, prevY;
    private ProgressBar findingVideoInProgress;
    private TextView videosFoundText;
    private TextView getVideosText;
    private boolean moved = false;
    private GestureDetector gesture;
    private VideoDetectionInitiator videoDetectionInitiator;
    private boolean isDetecting;

    private View foundVideosWindow;
    private VideoList videoList;
    private TextView foundVideosQueue;
    private TextView foundVideosDelete;
    private TextView foundVideosClose;

    private TextView numWindows;

    private ProgressBar loadingPageProgress;

    private int orientation;
    private boolean loadedFirsTime;

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
            if (isDetecting) {
                foundVideosWindow.setVisibility(View.VISIBLE);
            } else {
                isDetecting = true;
                videosFoundText.setVisibility(View.VISIBLE);
                getVideosText.setVisibility(View.GONE);
                videoDetectionInitiator.initiate();
            }
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
        videoDetectionInitiator = new VideoDetectionInitiator(new ConcreteVideoContentSearch());
        SharedPreferences prefs = getActivity().getSharedPreferences("settings", 0);
        isDetecting = prefs.getBoolean(getString(R.string.autoVideoDetect), true);
        setRetainInstance(true);
    }

    private void createTopBar() {
        final DrawerLayout layout = getActivity().findViewById(R.id.drawer);
        ImageView menu = view.findViewById(R.id.menuButton);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.openDrawer(GravityCompat.START);
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
                View allWindows = getLMvdActivity().getBrowserManager().getAllWindows();
                if (allWindows.getParent() != null) {
                    ((ViewGroup) allWindows.getParent()).removeView(allWindows);
                }
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
        getVideosText = videosFoundHUD.findViewById(R.id.getVideosText);

        setupVideosFoundHUDText();
    }

    private void setupVideosFoundHUDText() {
        SharedPreferences prefs = getActivity().getSharedPreferences("settings", 0);
        isDetecting = prefs.getBoolean(getString(R.string.autoVideoDetect), true);
        if (isDetecting) {
            videosFoundText.setVisibility(View.VISIBLE);
            getVideosText.setVisibility(View.GONE);
        } else {
            videosFoundText.setVisibility(View.GONE);
            getVideosText.setVisibility(View.VISIBLE);
        }
    }

    private void createFoundVideosWindow() {
        View oldFoundVideosWindow = foundVideosWindow;
        foundVideosWindow = view.findViewById(R.id.foundVideosWindow);
        if (videoList != null) {
            videoList.recreateVideoList((RecyclerView) foundVideosWindow.findViewById(R.id.videoList));
        } else {
            videoList = new VideoList(getActivity(), (RecyclerView) foundVideosWindow.findViewById(R
                    .id.videoList)) {
                @Override
                void onItemDeleted() {
                    updateFoundVideosBar();
                }
            };
        }

        if (oldFoundVideosWindow != null) {
            switch (oldFoundVideosWindow.getVisibility()) {
                case View.VISIBLE:
                    foundVideosWindow.setVisibility(View.VISIBLE);
                    break;
                case View.GONE:
                    foundVideosWindow.setVisibility(View.GONE);
                    break;
                case View.INVISIBLE:
                    foundVideosWindow.setVisibility(View.INVISIBLE);
                    break;
            }
        } else {
            foundVideosWindow.setVisibility(View.GONE);
        }

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
        if (view == null || getResources().getConfiguration().orientation != orientation) {
            int visibility = View.VISIBLE;
            if (view != null) {
                visibility = view.getVisibility();
            }
            view = inflater.inflate(R.layout.browser, container, false);
            view.setVisibility(visibility);
            if (page == null) {
                page = view.findViewById(R.id.page);
            } else {
                View page1 = view.findViewById(R.id.page);
                ((ViewGroup) view).removeView(page1);
                ((ViewGroup) page.getParent()).removeView(page);
                ((ViewGroup) view).addView(page);
                ((ViewGroup) view).bringChildToFront(view.findViewById(R.id.videosFoundHUD));
                ((ViewGroup) view).bringChildToFront(view.findViewById(R.id.foundVideosWindow));
            }
            loadingPageProgress = view.findViewById(R.id.loadingPageProgress);
            loadingPageProgress.setVisibility(View.GONE);

            createTopBar();

            createNavigationBar();

            createVideosFoundHUD();

            createFoundVideosWindow();

            updateFoundVideosBar();

            if (getResources().getConfiguration().orientation != orientation) {
                getLMvdActivity().getBrowserManager().updateNumWindows();
                orientation = getResources().getConfiguration().orientation;
            }
        }

        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (!loadedFirsTime) {
            HandlerThread thread = new HandlerThread("Video Extraction Thread");
            thread.start();
            final Handler extractVideoHandler = new Handler(thread.getLooper());

            WebSettings webSettings = page.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            page.setWebViewClient(new WebViewClient() {//it seems not setting webclient, launches
                //default browser instead of opening the page in webview

                private VideoExtractionRunnable videoExtract = new VideoExtractionRunnable();
                private ConcreteVideoContentSearch videoSearch = new ConcreteVideoContentSearch();
                private String currentPage = page.getUrl();

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (!url.startsWith("intent")) {
                        return super.shouldOverrideUrlLoading(view, url);
                    }
                    return true;
                }

                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    if (!request.getUrl().toString().startsWith("intent")) {
                        return super.shouldOverrideUrlLoading(view, request);
                    }
                    return true;
                }

                @Override
                public void onPageStarted(final WebView view, final String url, Bitmap favicon) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            TextView urlBox = BrowserWindow.this.view.findViewById(R.id.urlBox);
                            urlBox.setText(url);
                            BrowserWindow.this.url = url;
                        }
                    });

                    loadingPageProgress.setVisibility(View.VISIBLE);
                    setupVideosFoundHUDText();
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    loadingPageProgress.setVisibility(View.GONE);
                }

                @Override
                public void onLoadResource(final WebView view, final String url) {
                    final String page = view.getUrl();
                    final String title = view.getTitle();

                    if (!page.equals(currentPage)) {
                        currentPage = page;
                        videoDetectionInitiator.clear();
                    }

                    videoExtract.setUrl(url);
                    videoExtract.setTitle(title);
                    videoExtract.setPage(page);
                    extractVideoHandler.post(videoExtract);
                }

                class VideoExtractionRunnable implements Runnable {
                    private String url = "https://";
                    private String title = "";
                    private String page = "";

                    public void setUrl(String url) {
                        this.url = url;
                    }

                    public void setTitle(String title) {
                        this.title = title;
                    }

                    public void setPage(String page) {
                        this.page = page;
                    }

                    @Override
                    public void run() {
                        try {
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
                                videoSearch.newSearch(url, page, title);

                                if (isDetecting) {
                                    videoSearch.run();
                                } else {
                                    videoDetectionInitiator.reserve(url, page, title);
                                }
                            }
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                    if (getActivity() != null) {
                        if (getActivity().getSharedPreferences("settings", 0).getBoolean(getString(R
                                .string.adBlockON), true)
                                && (url.contains("ad") || url.contains("banner") || url.contains("pop"))
                                && getLMvdActivity().getBrowserManager().isUrlAd(url)) {
                            Log.i("loremarTest", "Ads detected: " + url);
                            return new WebResourceResponse("text/javascript", "UTF-8", null);
                        }
                    }
                    return super.shouldInterceptRequest(view, url);
                }

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getLMvdActivity() !=
                            null) {
                        if (LMvdApp.getInstance().getSharedPreferences("settings", 0).getBoolean(getString
                                (R.string.adBlockON), true)
                                && (request.getUrl().toString().contains("ad") ||
                                request.getUrl().toString().contains("banner") ||
                                request.getUrl().toString().contains("pop"))
                                && getLMvdActivity().getBrowserManager().isUrlAd(request.getUrl()
                                .toString())) {
                            Log.i("loremarTest", "Ads detected: " + request.getUrl().toString());
                            return new WebResourceResponse(null, null, null);
                        } else return null;
                    } else {
                        return shouldInterceptRequest(view, request.getUrl().toString());
                    }
                }
            });
            page.setWebChromeClient(new BrowserWebChromeClient(getActivity(), loadingPageProgress,
                    getLMvdActivity()));
            page.setOnLongClickListener(this);
            page.loadUrl(url);
            loadedFirsTime = true;
        } else {
            TextView urlBox = view.findViewById(R.id.urlBox);
            urlBox.setText(url);
        }
    }

    @Override
    public void onDestroy() {
        videoList.closeVideoDetailsFetcher();
        page.stopLoading();
        page.destroy();
        super.onDestroy();
    }

    private void updateFoundVideosBar() {
        final String videosFoundString = "Videos: " + videoList.getSize() + " found";
        final SpannableStringBuilder sb = new SpannableStringBuilder(videosFoundString);
        final ForegroundColorSpan fcs = new ForegroundColorSpan(LMvdApp.getInstance()
                .getApplicationContext().getResources().getColor(R.color.darkColor));
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
        final String numWindowsString = "Windows[" + num + "]";
        final SpannableStringBuilder sb = new SpannableStringBuilder(numWindowsString);
        final ForegroundColorSpan fcs = new ForegroundColorSpan(LMvdApp.getInstance()
                .getApplicationContext().getResources().getColor(R.color.darkColor));
        final StyleSpan bss = new StyleSpan(Typeface.BOLD);
        sb.setSpan(fcs, 8, 8 + String.valueOf(num).length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(bss, 8, 8 + String.valueOf(num).length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
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


    class ConcreteVideoContentSearch extends VideoContentSearch {

        @Override
        public void onStartInspectingURL() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (findingVideoInProgress.getVisibility() == View.GONE) {
                        findingVideoInProgress.setVisibility(View.VISIBLE);
                    }
                }
            });

            Utils.disableSSLCertificateChecking();
        }

        @Override
        public void onFinishedInspectingURL(boolean finishedAll) {
            HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSF);
            if (finishedAll) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        findingVideoInProgress.setVisibility(View.GONE);
                    }
                });
            }
        }

        @Override
        public void onVideoFound(String size, String type, String link,
                                 String name, String page, boolean chunked,
                                 String website) {
            videoList.addItem(size, type, link, name, page, chunked, website);
            updateFoundVideosBar();
        }
    }
}
