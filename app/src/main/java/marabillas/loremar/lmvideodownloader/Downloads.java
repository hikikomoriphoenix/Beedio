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

import android.app.Fragment;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Downloads extends Fragment implements LMvd.OnBackPressedListener {
    RecyclerView downloadsList;
    List<DownloadVideo> downloads;
    TextView downloadSpeed;
    TextView remaining;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.downloads, container, false);

        final DrawerLayout layout = getActivity().findViewById(R.id.drawer);
        ImageView menu = view.findViewById(R.id.menuButton);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.openDrawer(Gravity.START);
            }
        });

        downloadsList = view.findViewById(R.id.downloadsList);
        downloadsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        downloadsList.setAdapter(new DownloadListAdapter());
        downloadsList.setHasFixedSize(true);
        downloads = new ArrayList<>();
        File file = new File(getActivity().getFilesDir(), "downloads.dat");
        if(file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                DownloadQueues queues = (DownloadQueues) objectInputStream.readObject();
                downloads = queues.getList();
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

        downloadSpeed = view.findViewById(R.id.downloadSpeed);
        remaining = view.findViewById(R.id.remaining);

        ((LMvd)getActivity()).setOnBackPressedListener(this);

        return view;
    }

    @Override
    public void onBackpressed() {
        ((LMvd)getActivity()).getBrowserManager().unhideCurrentWindow();
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    class DownloadListAdapter extends RecyclerView.Adapter<DownloadItem > {

        @NonNull
        @Override
        public DownloadItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            return new DownloadItem(inflater.inflate(R.layout.download_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DownloadItem holder, int position) {
            holder.bind(downloads.get(position));
        }

        @Override
        public int getItemCount() {
            return downloads.size();
        }
    }

    class DownloadItem extends RecyclerView.ViewHolder implements ViewTreeObserver.OnGlobalLayoutListener {
        TextView name;
        TextView ext;
        ImageView rename;
        ImageView delete;
        ProgressBar progress;
        TextView status;

        boolean adjustedlayout;

        DownloadItem(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.downloadVideoName);
            ext = itemView.findViewById(R.id.downloadVideoExt);
            rename = itemView.findViewById(R.id.renameDownloadVideo);
            delete = itemView.findViewById(R.id.deleteDownloadItem);
            progress = itemView.findViewById(R.id.downloadProgressBar);
            status = itemView.findViewById(R.id.downloadProgressText);
            itemView.getViewTreeObserver().addOnGlobalLayoutListener(this);
            ext.getViewTreeObserver().addOnGlobalLayoutListener(this);
            rename.getViewTreeObserver().addOnGlobalLayoutListener(this);
            delete.getViewTreeObserver().addOnGlobalLayoutListener(this);
            adjustedlayout = false;
        }

        void bind(DownloadVideo video) {
            name.setText(video.name);
            String extString = "." + video.type;
            ext.setText(extString);
            String downloaded;
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment
                    .DIRECTORY_DOWNLOADS), video.name + extString);
            if (file.exists()) {
                if (video.size!=null) {
                    long downloadedSize = file.length();
                    downloaded = Formatter.formatShortFileSize(getActivity(), downloadedSize);
                    double percent = 100 * downloadedSize/Long.parseLong(video.size);
                    DecimalFormat percentFormat = new DecimalFormat("00.00");
                    String percentFormatted = percentFormat.format(percent);
                    progress.setProgress((int) percent);
                    String formattedSize = Formatter.formatShortFileSize(getActivity(), Long
                            .parseLong(video.size));
                    String statusString = downloaded + " / " + formattedSize + " " + percentFormatted +
                            "%";
                    status.setText(statusString);
                }
                else {
                    long downloadedSize = file.length();
                    downloaded = Formatter.formatShortFileSize(getActivity(), downloadedSize);
                    status.setText(downloaded);
                    progress.setProgress(0);
                }
            }
            else {
                if (video.size!=null) {
                    String formattedSize = Formatter.formatShortFileSize(getActivity(), Long
                            .parseLong(video.size));
                    String statusString = "0KB / " + formattedSize + " 0%";
                    status.setText(statusString);
                    progress.setProgress(0);
                }
                else {
                    String statusString = "0kB";
                    status.setText(statusString);
                    progress.setProgress(0);
                }
            }
        }

        @Override
        public void onGlobalLayout() {
            if (!adjustedlayout) {
                if (itemView.getWidth()!=0 && ext.getWidth()!=0 && rename.getWidth()!=0 && delete
                        .getWidth()!=0) {
                    int totalMargin = (int) TypedValue.applyDimension(TypedValue
                                    .COMPLEX_UNIT_DIP, 15,
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
