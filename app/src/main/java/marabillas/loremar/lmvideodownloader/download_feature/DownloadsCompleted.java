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

package marabillas.loremar.lmvideodownloader.download_feature;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import marabillas.loremar.lmvideodownloader.LMvdFragment;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.utils.RenameDialog;

public class DownloadsCompleted extends LMvdFragment implements DownloadsInProgress.OnAddDownloadedVideoToCompletedListener {
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
        View view = inflater.inflate(R.layout.downloads_completed, container, false);

        downloadsList = view.findViewById(R.id.downloadsCompletedList);
        TextView clearAllFinishedButton = view.findViewById(R.id.clearAllFinishedButton);
        TextView goToFolderButton = view.findViewById(R.id.goToFolder);

        videos = new ArrayList<>();
        File file = new File(getActivity().getFilesDir(), "completed.dat");
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                completedVideos = (CompletedVideos) objectInputStream.readObject();
                videos = completedVideos.getVideos();
                objectInputStream.close();
                fileInputStream.close();
                List<String> nonExistentFiles = new ArrayList<>();
                for (String video : videos) {
                    File videoFile = new File(Environment.getExternalStoragePublicDirectory
                            (Environment.DIRECTORY_DOWNLOADS), video);
                    if (!videoFile.exists()) {
                        nonExistentFiles.add(video);
                    }
                }
                for (String nonExistentVideo : nonExistentFiles) {
                    videos.remove(nonExistentVideo);
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }

        downloadsList.setAdapter(new DownloadedVideoAdapter());
        downloadsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        downloadsList.setHasFixedSize(true);
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView
                    .State state) {
                int verticalSpacing = (int) Math.ceil(TypedValue.applyDimension(TypedValue
                        .COMPLEX_UNIT_SP, 4, getActivity().getResources()
                        .getDisplayMetrics()));
                outRect.top = verticalSpacing;
                outRect.bottom = verticalSpacing;
            }
        };
        divider.setDrawable(getActivity().getResources().getDrawable(R.drawable.greydivider));
        downloadsList.addItemDecoration(divider);

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

        onNumDownloadsCompletedChangeListener.onNumDownloadsCompletedChange();

        return view;
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
                        public void onOK(String newName) {
                            File downloadsFolder = Environment.getExternalStoragePublicDirectory
                                    (Environment.DIRECTORY_DOWNLOADS);
                            File renamedFile = new File(downloadsFolder, newName + "." + type);
                            File file = new File(downloadsFolder, baseName + "." + type);
                            if (file.renameTo(renamedFile)) {
                                videos.set(getAdapterPosition(), newName + "." + type);
                                completedVideos.save(getActivity());
                                downloadsList.getAdapter().notifyItemChanged(getAdapterPosition());
                            } else {
                                Toast.makeText(getActivity(), "Failed: Invalid Filename", Toast
                                        .LENGTH_SHORT).show();
                            }
                        }
                    };
                }
            });

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    File file = new File(Environment.getExternalStoragePublicDirectory
                            (Environment.DIRECTORY_DOWNLOADS), baseName + "." + type);
                    Uri fileUri = FileProvider.getUriForFile(getActivity(), "marabillas.loremar" +
                            ".lmvideodownloader.fileprovider", file);
                    intent.setDataAndType(fileUri, "video/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                }
            });
        }

        void bind(String video) {
            baseName = video.substring(0, video.lastIndexOf("."));
            type = video.substring(video.lastIndexOf(".") + 1, video.length());
            name.setText(baseName);
            ext.setText(type);
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment
                    .DIRECTORY_DOWNLOADS), video);
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
