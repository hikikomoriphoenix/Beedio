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

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import marabillas.loremar.lmvideodownloader.LMvdApp;
import marabillas.loremar.lmvideodownloader.LMvdFragment;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.download_feature.lists.CompletedVideos;
import marabillas.loremar.lmvideodownloader.utils.RenameDialog;
import marabillas.loremar.lmvideodownloader.utils.Utils;

public class DownloadsCompleted extends LMvdFragment implements DownloadsInProgress.OnAddDownloadedVideoToCompletedListener {
    private View view;
    private RecyclerView downloadsList;
    private List<String> videos;
    private CompletedVideos completedVideos;

    private OnNumDownloadsCompletedChangeListener onNumDownloadsCompletedChangeListener;

    public interface OnNumDownloadsCompletedChangeListener {
        void onNumDownloadsCompletedChange();
    }

    public void setOnNumDownloadsCompletedChangeListener(OnNumDownloadsCompletedChangeListener
                                                                 onNumDownloadsCompletedChangeListener) {
        this.onNumDownloadsCompletedChangeListener = onNumDownloadsCompletedChangeListener;
    }

    public int getNumDownloadsCompleted() {
        return videos.size();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        videos = new ArrayList<>();
        completedVideos = CompletedVideos.load(getActivity());
        videos = completedVideos.getVideos();

        if (view == null) {
            view = inflater.inflate(R.layout.downloads_completed, container, false);

            downloadsList = view.findViewById(R.id.downloadsCompletedList);
            TextView clearAllFinishedButton = view.findViewById(R.id.clearAllFinishedButton);
            TextView goToFolderButton = view.findViewById(R.id.goToFolder);

            downloadsList.setAdapter(new DownloadedVideoAdapter());
            downloadsList.setLayoutManager(new LinearLayoutManager(getActivity()));
            downloadsList.setHasFixedSize(true);
            downloadsList.addItemDecoration(Utils.createDivider(getActivity()));

            clearAllFinishedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage("Clear this list?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int length = videos.size();
                                    videos.clear();
                                    if (completedVideos != null) {
                                        completedVideos.save(getActivity());
                                    }
                                    downloadsList.getAdapter().notifyItemRangeRemoved(0, length);
                                    onNumDownloadsCompletedChangeListener.onNumDownloadsCompletedChange();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create()
                            .show();
                }
            });

            goToFolderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                    startActivity(intent);
                }
            });

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                goToFolderButton.setVisibility(View.GONE);
                clearAllFinishedButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            }

            // Check if the set download folder is the public download directory. If not hide the
            // "Go To Folder" button.
            String downloadFolder =
                    marabillas.loremar.lmvideodownloader.download_feature.DownloadManager.getDownloadFolder();
            File publicDownloadDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String defaultDownloadFolder = null;
            if (publicDownloadDirectory != null) {
                if (!publicDownloadDirectory.getAbsolutePath().endsWith("/")) {
                    defaultDownloadFolder = publicDownloadDirectory.getAbsolutePath() + "/";
                } else {
                    defaultDownloadFolder = publicDownloadDirectory.getAbsolutePath();
                }

            }
            if (downloadFolder == null || !downloadFolder.equals(defaultDownloadFolder)) {
                goToFolderButton.setVisibility(View.GONE);
                clearAllFinishedButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        List<String> nonExistentFiles = new ArrayList<>();
        String downloadFolder =
                marabillas.loremar.lmvideodownloader.download_feature.DownloadManager.getDownloadFolder();
        if (downloadFolder != null) {
            for (String video : videos) {
                File videoFile = new File(downloadFolder, video);
                if (!videoFile.exists()) {
                    nonExistentFiles.add(video);
                }
            }
            for (String nonExistentVideo : nonExistentFiles) {
                videos.remove(nonExistentVideo);
            }
        }
        downloadsList.getAdapter().notifyDataSetChanged();
        completedVideos.save(LMvdApp.getInstance().getApplicationContext());
        onNumDownloadsCompletedChangeListener.onNumDownloadsCompletedChange();
    }

    @Override
    public void onAddDownloadedVideoToCompleted(final String name, final String type) {
        if (completedVideos == null) {
            completedVideos = new CompletedVideos();
        }
        completedVideos.addVideo(getActivity(), name + "." + type);
        videos = completedVideos.getVideos();
        downloadsList.getAdapter().notifyItemInserted(0);
        onNumDownloadsCompletedChangeListener.onNumDownloadsCompletedChange();
    }

    private class DownloadedVideoAdapter extends RecyclerView.Adapter<VideoItem> {

        @NonNull
        @Override
        public VideoItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.downloads_completed_item, parent, false);
            return new VideoItem(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoItem holder, int position) {
            holder.bind(videos.get(position));
        }

        @Override
        public int getItemCount() {
            return videos.size();
        }
    }

    private class VideoItem extends RecyclerView.ViewHolder implements ViewTreeObserver.OnGlobalLayoutListener {
        private TextView name;
        private TextView ext;
        private ImageView delete;
        private ImageView rename;
        private TextView size;
        private TextView play;

        private String baseName;
        private String type;

        private boolean adjustedlayout;

        VideoItem(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.downloadCompletedName);
            ext = itemView.findViewById(R.id.downloadCompletedExt);
            delete = itemView.findViewById(R.id.deleteDownloadCompletedItem);
            rename = itemView.findViewById(R.id.renameDownloadCompletedVideo);
            size = itemView.findViewById(R.id.downloadCompletedSize);
            play = itemView.findViewById(R.id.playVideo);

            itemView.getViewTreeObserver().addOnGlobalLayoutListener(this);
            ext.getViewTreeObserver().addOnGlobalLayoutListener(this);
            rename.getViewTreeObserver().addOnGlobalLayoutListener(this);
            delete.getViewTreeObserver().addOnGlobalLayoutListener(this);
            adjustedlayout = false;

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage("Delete?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int position = getAdapterPosition();
                                    videos.remove(position);
                                    completedVideos.save(getActivity());
                                    downloadsList.getAdapter().notifyItemRemoved(position);
                                    onNumDownloadsCompletedChangeListener.onNumDownloadsCompletedChange();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create()
                            .show();
                }
            });

            rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new RenameDialog(getActivity(), baseName) {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }

                        @Override
                        public void onOK(String newName) {
                            String downloadFolder =
                                    marabillas.loremar.lmvideodownloader.download_feature.DownloadManager.getDownloadFolder();
                            if (downloadFolder != null) {
                                File renamedFile = new File(downloadFolder, newName + "." + type);
                                File file = new File(downloadFolder, baseName + "." + type);
                                if (file.renameTo(renamedFile)) {
                                    videos.set(getAdapterPosition(), newName + "." + type);
                                    completedVideos.save(getActivity());
                                    downloadsList.getAdapter().notifyItemChanged(getAdapterPosition());
                                } else {
                                    Toast.makeText(getActivity(), "Failed: Invalid Filename", Toast
                                            .LENGTH_SHORT).show();
                                }
                            }
                        }
                    };
                }
            });

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String downloadFolder =
                            marabillas.loremar.lmvideodownloader.download_feature.DownloadManager.getDownloadFolder();
                    if (downloadFolder != null) {
                        File file = new File(downloadFolder, baseName + "." + type);
                        Uri fileUri = FileProvider.getUriForFile(getActivity(), "marabillas.loremar" +
                                ".lmvideodownloader.fileprovider", file);
                        intent.setDataAndType(fileUri, "video/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                    }
                }
            });
        }

        void bind(String video) {
            baseName = video.substring(0, video.lastIndexOf("."));
            type = video.substring(video.lastIndexOf(".") + 1, video.length());
            name.setText(baseName);
            ext.setText(type);
            String downloadFolder =
                    marabillas.loremar.lmvideodownloader.download_feature.DownloadManager.getDownloadFolder();
            if (downloadFolder != null) {
                File file = new File(downloadFolder, video);
                if (file.exists()) {
                    String length = Formatter.formatFileSize(getActivity(), file.length());
                    size.setText(length);
                } else {
                    int position = getAdapterPosition();
                    videos.remove(position);
                    completedVideos.save(getActivity());
                    downloadsList.getAdapter().notifyItemRemoved(position);
                    onNumDownloadsCompletedChangeListener.onNumDownloadsCompletedChange();
                }
            }
        }

        @Override
        public void onGlobalLayout() {
            if (!adjustedlayout) {
                if (itemView.getWidth() != 0 && ext.getWidth() != 0 && rename.getWidth() != 0 && delete
                        .getWidth() != 0) {
                    int totalMargin = (int) TypedValue.applyDimension(TypedValue
                                    .COMPLEX_UNIT_DIP, 35,
                            getActivity().getResources().getDisplayMetrics());
                    int nameMaxWidth = itemView.getMeasuredWidth() - totalMargin - ext
                            .getMeasuredWidth() - rename.getMeasuredWidth() - delete
                            .getMeasuredWidth();
                    name.setMaxWidth(nameMaxWidth);
                    adjustedlayout = true;
                }
            }
        }
    }
}
