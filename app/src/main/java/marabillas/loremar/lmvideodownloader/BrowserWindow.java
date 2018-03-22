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
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
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

public class BrowserWindow extends Fragment implements View.OnTouchListener, View
        .OnClickListener, LMvd.OnBackPressedListener, View.OnLongClickListener {
    private static final String TAG = "loremarTest";
    private String url;
    private View view;
    private TouchableWebView page;
    private List<Video> videos;
    private SSLSocketFactory defaultSSLSF;

    private View videosFoundHUD;
    private float prevX, prevY;
    private ProgressBar findingVideoInProgress;
    private int numLinksInspected;
    private TextView videosFoundText;
    private boolean moved = false;

    private View foundVideosWindow;
    private RecyclerView videoList;

    private GestureDetector gesture;

    private TextView foundVideosQueue;
    private TextView foundVideosDelete;
    private TextView foundVideosClose;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v == videosFoundHUD) {
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
            videoList.getAdapter().notifyDataSetChanged();
        }
        else if (v == foundVideosQueue) {
            updateFoundVideosBar();
        }
        else if (v == foundVideosDelete) {
            for(int i=0; i<videos.size();) {
                if(videos.get(i).checked) {
                    videos.remove(i);
                }
                else i++;
            }
            ((VideoListAdapter)videoList.getAdapter()).expandedItem = -1;
            videoList.getAdapter().notifyDataSetChanged();
            updateFoundVideosBar();
        }
        else if (v == foundVideosClose) {
            foundVideosWindow.setVisibility(View.GONE);
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
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
        gesture = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                videosFoundHUD.performClick();
                return true;
            }
        });

        findingVideoInProgress = videosFoundHUD.findViewById(R.id.findingVideosInProgress);
        findingVideoInProgress.setVisibility(View.GONE);

        videos = new ArrayList<>();

        videosFoundText = videosFoundHUD.findViewById(R.id.videosFoundText);
        updateFoundVideosBar();

        foundVideosWindow = view.findViewById(R.id.foundVideosWindow);
        videoList = foundVideosWindow.findViewById(R.id.videoList);
        videoList.setAdapter(new VideoListAdapter());
        videoList.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView
                    .State state) {
                int verticalSpacing = (int) Math.ceil(TypedValue.applyDimension(TypedValue
                        .COMPLEX_UNIT_SP, 4, getResources().getDisplayMetrics()));
                outRect.top = verticalSpacing;
                outRect.bottom = verticalSpacing;
            }
        };
        divider.setDrawable(getResources().getDrawable(R.drawable.greydivider));
        videoList.addItemDecoration(divider);
        videoList.setHasFixedSize(true);
        foundVideosWindow.setVisibility(View.GONE);

        ((LMvd)getActivity()).setOnBackPressedListener(this);

        foundVideosQueue = foundVideosWindow.findViewById(R.id.foundVideosQueue);
        foundVideosDelete = foundVideosWindow.findViewById(R.id.foundVideosDelete);
        foundVideosClose = foundVideosWindow.findViewById(R.id.foundVideosClose);
        foundVideosQueue.setOnClickListener(this);
        foundVideosDelete.setOnClickListener(this);
        foundVideosClose.setOnClickListener(this);

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
                    }
                });
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onLoadResource(final WebView view, final String url) {
                final String page = view.getUrl();
                final String title = view.getTitle();
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
                                            video.size = " ";
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
                                        video.page = page;
                                        if(!duplicate) {
                                            videos.add(video);
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    videoList.getAdapter().notifyDataSetChanged();
                                                }
                                            });
                                            updateFoundVideosBar();
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
        page.setOnLongClickListener(this);
        page.loadUrl(url);
    }

    private void updateFoundVideosBar() {
        final String videosFoundString = "Videos: " + videos.size() + " found";
        final SpannableStringBuilder sb = new SpannableStringBuilder(videosFoundString);
        final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(0,0, 255));
        final StyleSpan bss = new StyleSpan(Typeface.BOLD);
        sb.setSpan(fcs, 8, 10 + videos.size()/10, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(bss, 8, 10 + videos.size()/10, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                videosFoundText.setText(sb);
            }
        });
    }

    @Override
    public void onBackpressedListener() {
        if(foundVideosWindow.getVisibility() == View.VISIBLE) {
            foundVideosWindow.setVisibility(View.GONE);
        }
        else if(page.canGoBack()) {
            page.goBack();
        }
        else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onDestroy() {
        ((LMvd)getActivity()).setOnBackPressedListener(null);
        super.onDestroy();
    }

    @Override
    public boolean onLongClick(View v) {
        WebView.HitTestResult hit = page.getHitTestResult();
        if(hit.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
            if(hit.getExtra()!=null) {
                Log.i(TAG, "a link!");
                View point = new View(getActivity());
                point.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                        .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                if (getView() != null) {
                    ((ViewGroup)getView()).addView(point);
                }
                point.getLayoutParams().height = 10;
                point.getLayoutParams().width = 10;
                point.setX(page.getClickX());
                point.setY(page.getClickY());
                PopupMenu menu = new PopupMenu(getActivity(), point);
                menu.getMenu().add("Open in new tab");
                menu.show();
            }
        }
        return true;
    }

    private class Video{
        String size, type, link, name, page;
        boolean checked=false, expanded=false;
    }

    private class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoItem> {
        int expandedItem = -1;

        @NonNull
        @Override
        public VideoItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return (new VideoItem(inflater.inflate(R.layout.videos_found_item, parent,
                    false)));
        }

        @Override
        public void onBindViewHolder(@NonNull VideoItem holder, int position) {
            holder.bind(videos.get(position));
        }

        @Override
        public int getItemCount() {
            return videos.size();
        }

        class VideoItem extends RecyclerView.ViewHolder implements CompoundButton
                .OnCheckedChangeListener, View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {
            TextView size;
            TextView name;
            TextView ext;
            CheckBox check;
            View expand;

            boolean adjustedLayout;

            VideoItem(View itemView) {
                super(itemView);
                adjustedLayout = false;
                size = itemView.findViewById(R.id.videoFoundSize);
                name = itemView.findViewById(R.id.videoFoundName);
                ext = itemView.findViewById(R.id.videoFoundExt);
                check = itemView.findViewById(R.id.videoFoundCheck);
                expand = itemView.findViewById(R.id.videoFoundExpand);
                check.setOnCheckedChangeListener(this);
                itemView.setOnClickListener(this);
                itemView.getViewTreeObserver().addOnGlobalLayoutListener(this);
                size.getViewTreeObserver().addOnGlobalLayoutListener(this);
                ext.getViewTreeObserver().addOnGlobalLayoutListener(this);
                check.getViewTreeObserver().addOnGlobalLayoutListener(this);
            }

            void bind(Video video) {
                size.setText(video.size);
                String extStr = "."+video.type;
                ext.setText(extStr);
                check.setChecked(video.checked);
                name.setText(video.name);
                if(video.expanded) {
                    expand.setVisibility(View.VISIBLE);
                }
                else {
                    expand.setVisibility(View.GONE);
                }
                expand.findViewById(R.id.videoFoundRename).setOnClickListener(this);
                expand.findViewById(R.id.videoFoundCopy).setOnClickListener(this);
                expand.findViewById(R.id.videoFoundDelete).setOnClickListener(this);
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                videos.get(getAdapterPosition()).checked = isChecked;
            }

            @Override
            public void onClick(View v) {
                if (v == expand.findViewById(R.id.videoFoundRename)) {
                    new RenameDialog(getActivity(), name.getText().toString()) {
                        @Override
                        void onOK(String newName) {
                            adjustedLayout = false;
                            videos.get(getAdapterPosition()).name = newName;
                            notifyItemChanged(getAdapterPosition());
                        }
                    };
                }
                else if (v == expand.findViewById(R.id.videoFoundCopy)) {
                    ClipboardManager clipboardManager = (ClipboardManager) getActivity().
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData link = ClipData.newPlainText("link address", videos.get
                            (getAdapterPosition()).link);
                    if (clipboardManager != null) {
                        clipboardManager.setPrimaryClip(link);
                    }
                }
                else if (v == expand.findViewById(R.id.videoFoundDelete)) {
                    AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                    dialog.setMessage("Delete this item from the list?");
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "YES", new DialogInterface
                            .OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            videos.remove(getAdapterPosition());
                            expandedItem = -1;
                            notifyDataSetChanged();
                            updateFoundVideosBar();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", new DialogInterface
                            .OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();
                }
                else {
                    if (expandedItem != -1) {
                        videos.get(expandedItem).expanded = false;
                        if (expandedItem != getAdapterPosition()) {
                            expandedItem = getAdapterPosition();
                            videos.get(getAdapterPosition()).expanded = true;
                        } else {
                            expandedItem = -1;
                        }
                    } else {
                        expandedItem = getAdapterPosition();
                        videos.get(getAdapterPosition()).expanded = true;
                    }
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onGlobalLayout() {
                if(!adjustedLayout) {
                    if (itemView.getWidth() != 0 && size.getWidth() != 0 && ext.getWidth() != 0 && check
                            .getWidth() != 0) {
                        int totalMargin = (int) TypedValue.applyDimension(TypedValue
                                        .COMPLEX_UNIT_DIP, 12,
                                getResources().getDisplayMetrics());
                        int nameMaxWidth = itemView.getMeasuredWidth() - size.getMeasuredWidth() - ext
                                .getMeasuredWidth() - check.getMeasuredWidth() - totalMargin;
                        name.setMaxWidth(nameMaxWidth);
                        adjustedLayout = true;
                    }
                }

            }
        }
    }
}
