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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

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

        TextView reload = view.findViewById(R.id.reload);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page.loadUrl(page.getUrl());
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
        view = inflater.inflate(R.layout.browser, container, false);
        page = view.findViewById(R.id.page);
        loadingPageProgress = view.findViewById(R.id.loadingPageProgress);
        loadingPageProgress.setVisibility(View.GONE);

        createTopBar();

        createNavigationBar();

        createVideosFoundHUD();

        createFoundVideosWindow();

        updateFoundVideosBar();

        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        numLinksInspected = 0;
        WebSettings webSettings = page.getSettings();
        webSettings.setJavaScriptEnabled(true);
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
                        if (urlLowerCase.contains("mp4") || urlLowerCase.contains("video") ||
                                urlLowerCase.contains("embed")) {
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

                                    //todo if url points to an html document, parse for video content

                                    contentType = contentType.toLowerCase();
                                    if (contentType.contains("video/mp4")) {
                                        String size = uCon.getHeaderField("content-length");
                                        String link = uCon.getHeaderField("Location");
                                        if (link == null) {
                                            link = uCon.getURL().toString();
                                        }
                                        String name = "video";
                                        if (title != null) {
                                            name = title;
                                        }
                                        String type = "mp4";
                                        videoList.addItem(size, type, link, name, page);

                                        updateFoundVideosBar();
                                        String videoFound = "name:" + name + "\n" +
                                                "link:" + link + "\n" +
                                                "type:" + type + "\n" +
                                                "size" + size;
                                        Log.i(TAG, videoFound);
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
        });
        page.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                loadingPageProgress.setProgress(newProgress);
            }
        });
        page.setOnLongClickListener(this);
        page.loadUrl(url);
    }

    private void updateFoundVideosBar() {
        final String videosFoundString = "Videos: " + videoList.getSize() + " found";
        final SpannableStringBuilder sb = new SpannableStringBuilder(videosFoundString);
        final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(0, 0, 255));
        final StyleSpan bss = new StyleSpan(Typeface.BOLD);
        sb.setSpan(fcs, 8, 10 + videoList.getSize() / 10, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(bss, 8, 10 + videoList.getSize() / 10, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
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
