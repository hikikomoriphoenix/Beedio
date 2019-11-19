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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import marabillas.loremar.lmvideodownloader.LMvdApp;
import marabillas.loremar.lmvideodownloader.PermissionRequestCodes;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.download_feature.DownloadManager;
import marabillas.loremar.lmvideodownloader.download_feature.DownloadPermissionHandler;
import marabillas.loremar.lmvideodownloader.download_feature.DownloadVideo;
import marabillas.loremar.lmvideodownloader.download_feature.lists.DownloadQueues;
import marabillas.loremar.lmvideodownloader.utils.RenameDialog;
import marabillas.loremar.lmvideodownloader.utils.Utils;

/**
 * Created by loremar on 3/23/18.
 */

public abstract class VideoList {
    private Activity activity;
    private RecyclerView view;
    private List<Video> videos;
    private VideoDetailsFetcher videoDetailsFetcher = new VideoDetailsFetcher();

    class Video {
        String size, type, link, name, page, website, details;
        boolean chunked = false, checked = false, expanded = false;
    }

    abstract void onItemDeleted();

    VideoList(Activity activity, RecyclerView view) {
        this.activity = activity;
        this.view = view;

        view.setAdapter(new VideoListAdapter());
        view.setLayoutManager(new LinearLayoutManager(activity));
        view.addItemDecoration(Utils.createDivider(activity));
        view.setHasFixedSize(true);

        videos = new ArrayList<>();
    }

    void recreateVideoList(RecyclerView view) {
        this.view = view;
        view.setAdapter(new VideoListAdapter());
        view.setLayoutManager(new LinearLayoutManager(activity));
        view.addItemDecoration(Utils.createDivider(activity));
        view.setHasFixedSize(true);
    }

    void addItem(@Nullable String size, String type, String link, String name, String page,
                 boolean chunked, String website) {
        Video video = new Video();
        video.size = size;
        video.type = type;
        video.link = link;
        video.name = name;
        video.page = page;
        video.chunked = chunked;
        video.website = website;

        boolean duplicate = false;
        for (Video v : videos) {
            if (v.link.equals(video.link)) {
                duplicate = true;
                break;
            }
        }
        if (!duplicate) {
            videos.add(video);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    view.getAdapter().notifyDataSetChanged();
                }
            });
        }
    }

    int getSize() {
        return videos.size();
    }

    void deleteCheckedItems() {
        for (int i = 0; i < videos.size(); ) {
            if (videos.get(i).checked) {
                videos.remove(i);
            } else i++;
        }
        ((VideoListAdapter) view.getAdapter()).expandedItem = -1;
        view.getAdapter().notifyDataSetChanged();
    }

    class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoItem> {
        int expandedItem = -1;

        List getVideos() {
            return videos;
        }

        @NonNull
        @Override
        public VideoItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(activity);
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
                if (video.size != null) {
                    String sizeFormatted = Formatter.formatShortFileSize(activity,
                            Long.parseLong(video.size));
                    size.setText(sizeFormatted);
                } else size.setText(" ");
                String extStr = "." + video.type;
                ext.setText(extStr);
                check.setChecked(video.checked);
                name.setText(video.name);
                if (video.expanded) {
                    expand.setVisibility(View.VISIBLE);
                    AppCompatTextView detailsText = expand.findViewById(R.id.videoFoundDetailsText);
                    detailsText.setVisibility(View.VISIBLE);
                    detailsText.setText(video.details);
                } else {
                    expand.setVisibility(View.GONE);
                }
                expand.findViewById(R.id.videoFoundRename).setOnClickListener(this);
                expand.findViewById(R.id.videoFoundDownload).setOnClickListener(this);
                expand.findViewById(R.id.videoFoundDelete).setOnClickListener(this);
                expand.findViewById(R.id.videoFoundDetailsBtn).setOnClickListener(this);
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                videos.get(getAdapterPosition()).checked = isChecked;
            }

            @Override
            public void onClick(View v) {
                if (v == expand.findViewById(R.id.videoFoundRename)) {
                    new RenameDialog(activity, name.getText().toString()) {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }

                        @Override
                        public void onOK(String newName) {
                            adjustedLayout = false;
                            videos.get(getAdapterPosition()).name = newName;
                            notifyItemChanged(getAdapterPosition());
                        }
                    };
                } else if (v == expand.findViewById(R.id.videoFoundDownload)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        new DownloadPermissionHandler(activity) {
                            @Override
                            public void onPermissionGranted() {
                                startDownload();
                            }
                        }.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                PermissionRequestCodes.DOWNLOADS);
                    } else {
                        startDownload();
                    }
                } else if (v == expand.findViewById(R.id.videoFoundDelete)) {
                    new AlertDialog.Builder(activity)
                            .setMessage("Delete this item from the list?")
                            .setPositiveButton("YES", new DialogInterface
                                    .OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    videos.remove(getAdapterPosition());
                                    expandedItem = -1;
                                    notifyDataSetChanged();
                                    onItemDeleted();
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface
                                    .OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create()
                            .show();
                } else if (v == expand.findViewById(R.id.videoFoundDetailsBtn)) {
                    ProgressBar progress = expand.findViewById(R.id.videoFoundExtractDetailsProgress);
                    progress.setVisibility(View.VISIBLE);
                    final int targetPosition = getAdapterPosition();
                    videoDetailsFetcher.fetchDetails(
                            videos.get(getAdapterPosition()).link,
                            new VideoDetailsFetcher.FetchDetailsListener() {
                                @Override
                                public void onUnFetched(final String message) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(activity, "Unable to fetch video details",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onFetched(final String details) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            videos.get(targetPosition).details = details;
                                            ProgressBar progress = expand.findViewById(
                                                    R.id.videoFoundExtractDetailsProgress);
                                            progress.setVisibility(View.GONE);
                                            if (targetPosition == getAdapterPosition()) {
                                                AppCompatTextView detailsText =
                                                        expand.findViewById(R.id.videoFoundDetailsText);
                                                detailsText.setVisibility(View.VISIBLE);
                                                detailsText.setText(details);
                                            }
                                        }
                                    });
                                }
                            });
                } else {
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
                if (!adjustedLayout) {
                    if (itemView.getWidth() != 0 && size.getWidth() != 0 && ext.getWidth() != 0 && check
                            .getWidth() != 0) {
                        int totalMargin = (int) TypedValue.applyDimension(TypedValue
                                        .COMPLEX_UNIT_DIP, 12,
                                activity.getResources().getDisplayMetrics());
                        int nameMaxWidth = itemView.getMeasuredWidth() - size.getMeasuredWidth() - ext
                                .getMeasuredWidth() - check.getMeasuredWidth() - totalMargin;
                        name.setMaxWidth(nameMaxWidth);
                        adjustedLayout = true;
                    }
                }

            }

            void startDownload() {
                Video video = videos.get(getAdapterPosition());
                DownloadQueues queues = DownloadQueues.load(activity);
                queues.insertToTop(video.size, video.type, video.link, video.name, video
                        .page, video.chunked, video.website);
                queues.save(activity);
                DownloadVideo topVideo = queues.getTopVideo();
                Intent downloadService = LMvdApp.getInstance().getDownloadService();
                DownloadManager.stop();
                downloadService.putExtra("link", topVideo.link);
                downloadService.putExtra("name", topVideo.name);
                downloadService.putExtra("type", topVideo.type);
                downloadService.putExtra("size", topVideo.size);
                downloadService.putExtra("page", topVideo.page);
                downloadService.putExtra("chunked", topVideo.chunked);
                downloadService.putExtra("website", topVideo.website);
                LMvdApp.getInstance().startService(downloadService);
                videos.remove(getAdapterPosition());
                expandedItem = -1;
                notifyDataSetChanged();
                onItemDeleted();
                Toast.makeText(activity, "Downloading video in the background. Check the " +
                        "Downloads panel to see progress", Toast.LENGTH_LONG).show();
            }
        }
    }

    void saveCheckedItemsForDownloading() {
        DownloadQueues queues = DownloadQueues.load(activity);
        for (Video video : videos) {
            if (video.checked) {
                queues.add(video.size, video.type, video.link, video.name, video.page, video
                        .chunked, video.website);
            }
        }

        queues.save(activity);

        Toast.makeText(activity, "Selected videos are queued for downloading. Go to Downloads " +
                "panel to start downloading videos", Toast.LENGTH_LONG).show();
    }

    void closeVideoDetailsFetcher() {
        videoDetailsFetcher.close();
    }
}
