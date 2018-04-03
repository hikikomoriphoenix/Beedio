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

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by loremar on 3/23/18.
 *
 */

abstract class VideoList {
    private Context context;
    private RecyclerView view;
    private List<Video> videos;

    class Video {
        String size, type, link, name, page;
        boolean checked = false, expanded = false;
    }

    abstract void onItemDeleted();

    VideoList(Context context, RecyclerView view) {
        this.context = context;
        this.view = view;

        view.setAdapter(new VideoListAdapter());
        view.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration divider = new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView
                    .State state) {
                int verticalSpacing = (int) Math.ceil(TypedValue.applyDimension(TypedValue
                        .COMPLEX_UNIT_SP, 4, VideoList.this.context.getResources()
                        .getDisplayMetrics()));
                outRect.top = verticalSpacing;
                outRect.bottom = verticalSpacing;
            }
        };
        divider.setDrawable(context.getResources().getDrawable(R.drawable.greydivider));
        view.addItemDecoration(divider);
        view.setHasFixedSize(true);

        videos = new ArrayList<>();
    }

    void addItem(String size, String type, String link, String name, String page) {
        Video video  = new Video();
        video.size = size;
        video.type = type;
        video.link = link;
        video.name = name;
        video.page = page;

        boolean duplicate = false;
        for(Video v: videos){
            if(v.link.equals(video.link)) {
                duplicate = true;
                break;
            }
        }
        if(!duplicate) {
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
        for(int i=0; i<videos.size();) {
            if(videos.get(i).checked) {
                videos.remove(i);
            }
            else i++;
        }
        ((VideoListAdapter)view.getAdapter()).expandedItem = -1;
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
            LayoutInflater inflater = LayoutInflater.from(context);
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
                String extStr = "." + video.type;
                ext.setText(extStr);
                check.setChecked(video.checked);
                name.setText(video.name);
                if (video.expanded) {
                    expand.setVisibility(View.VISIBLE);
                } else {
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
                    new RenameDialog(context, name.getText().toString()) {
                        @Override
                        void onOK(String newName) {
                            adjustedLayout = false;
                            videos.get(getAdapterPosition()).name = newName;
                            notifyItemChanged(getAdapterPosition());
                        }
                    };
                } else if (v == expand.findViewById(R.id.videoFoundCopy)) {
                    ClipboardManager clipboardManager = (ClipboardManager) context.
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData link = ClipData.newPlainText("link address", videos.get
                            (getAdapterPosition()).link);
                    if (clipboardManager != null) {
                        clipboardManager.setPrimaryClip(link);
                    }
                } else if (v == expand.findViewById(R.id.videoFoundDelete)) {
                    AlertDialog dialog = new AlertDialog.Builder(context).create();
                    dialog.setMessage("Delete this item from the list?");
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "YES", new DialogInterface
                            .OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            videos.remove(getAdapterPosition());
                            expandedItem = -1;
                            notifyDataSetChanged();
                            onItemDeleted();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", new DialogInterface
                            .OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();
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
                                context.getResources().getDisplayMetrics());
                        int nameMaxWidth = itemView.getMeasuredWidth() - size.getMeasuredWidth() - ext
                                .getMeasuredWidth() - check.getMeasuredWidth() - totalMargin;
                        name.setMaxWidth(nameMaxWidth);
                        adjustedLayout = true;
                    }
                }

            }
        }
    }

    void saveCheckedItemsForDownloading() {
        File file = new File(context.getFilesDir(), "downloads.dat");
        DownloadQueues queues = new DownloadQueues();
        if(file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                queues = (DownloadQueues) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        for(Video video: videos) {
            if(video.checked) {
                queues.add(video.size, video.type, video.link, video.name, video.page);
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(queues);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(context, "Selected videos are queued for downloading. Go to Downloads " +
                "panel to start downloading videos", Toast.LENGTH_LONG).show();
    }
}
