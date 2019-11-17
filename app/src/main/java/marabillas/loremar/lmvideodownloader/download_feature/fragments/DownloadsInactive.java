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
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import marabillas.loremar.lmvideodownloader.LMvdFragment;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.download_feature.DownloadManager;
import marabillas.loremar.lmvideodownloader.download_feature.DownloadVideo;
import marabillas.loremar.lmvideodownloader.download_feature.OnDownloadWithNewLinkListener;
import marabillas.loremar.lmvideodownloader.download_feature.lists.InactiveDownloads;
import marabillas.loremar.lmvideodownloader.utils.RenameDialog;
import marabillas.loremar.lmvideodownloader.utils.Utils;

public class DownloadsInactive extends LMvdFragment implements DownloadsInProgress.OnAddDownloadItemToInactiveListener {
    private View view;
    private RecyclerView downloadsList;
    private List<DownloadVideo> downloads;
    private InactiveDownloads inactiveDownloads;

    private OnNumDownloadsInactiveChangeListener onNumDownloadsInactiveChangeListener;

    public interface OnNumDownloadsInactiveChangeListener {
        void onNumDownloadsInactiveChange();
    }

    public void setOnNumDownloadsInactiveChangeListener(OnNumDownloadsInactiveChangeListener
                                                                onNumDownloadsInactiveChangeListener) {
        this.onNumDownloadsInactiveChangeListener = onNumDownloadsInactiveChangeListener;
    }

    public int getNumDownloadsInactive() {
        return downloads.size();
    }

    @Override
    public void onResume() {
        super.onResume();
        downloadsList.getAdapter().notifyDataSetChanged();
        onNumDownloadsInactiveChangeListener.onNumDownloadsInactiveChange();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        downloads = new ArrayList<>();
        inactiveDownloads = InactiveDownloads.load(getActivity());
        downloads = inactiveDownloads.getInactiveDownloads();

        if (view == null) {
            view = inflater.inflate(R.layout.downloads_inactive, container, false);
            downloadsList = view.findViewById(R.id.downloadsInactiveList);

            downloadsList.setAdapter(new DownloadAdapter());
            downloadsList.setLayoutManager(new LinearLayoutManager(getActivity()));
            downloadsList.setHasFixedSize(true);
            downloadsList.addItemDecoration(Utils.createDivider(getActivity()));
        }

        return view;
    }

    @Override
    public void onAddDownloadItemToInactive(DownloadVideo inactiveDownload) {
        if (inactiveDownloads == null) {
            inactiveDownloads = new InactiveDownloads();
        }
        inactiveDownloads.add(getActivity(), inactiveDownload);
        downloads = inactiveDownloads.getInactiveDownloads();
        downloadsList.getAdapter().notifyItemInserted(downloads.size() - 1);
        onNumDownloadsInactiveChangeListener.onNumDownloadsInactiveChange();
    }

    private class DownloadAdapter extends RecyclerView.Adapter<DownloadItem> {
        @NonNull
        @Override
        public DownloadItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.downloads_inactive_item, parent, false);
            return new DownloadItem(view);
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

    private class DownloadItem extends RecyclerView.ViewHolder implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener, SourcePage.OnUpdateLinkListener {
        private TextView name;
        private TextView ext;
        private ImageView delete;
        private ImageView rename;
        private TextView progress;
        private TextView gotopage;

        private boolean adjustedLayout;

        DownloadItem(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.downloadInactiveName);
            ext = itemView.findViewById(R.id.downloadInactiveExt);
            delete = itemView.findViewById(R.id.deleteDownloadInactiveItem);
            rename = itemView.findViewById(R.id.renameDownloadInactiveVideo);
            progress = itemView.findViewById(R.id.downloadInactiveProgressText);
            gotopage = itemView.findViewById(R.id.goToPageButton);

            itemView.getViewTreeObserver().addOnGlobalLayoutListener(this);
            ext.getViewTreeObserver().addOnGlobalLayoutListener(this);
            rename.getViewTreeObserver().addOnGlobalLayoutListener(this);
            delete.getViewTreeObserver().addOnGlobalLayoutListener(this);
            adjustedLayout = false;

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage("Delete?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int position = getAdapterPosition();
                                    downloads.remove(position);
                                    inactiveDownloads.save(getActivity());
                                    downloadsList.getAdapter().notifyItemRemoved(position);
                                    onNumDownloadsInactiveChangeListener.onNumDownloadsInactiveChange();
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
                    final String downloadFolder = DownloadManager.getDownloadFolder();
                    if (downloadFolder != null) {
                        new RenameDialog(getActivity(), name.getText().toString()) {
                            @Override
                            public void onDismiss(DialogInterface dialog) {

                            }

                            @Override
                            public void onOK(String newName) {
                                File renamedFile = new File(downloadFolder, newName + "." + ext.getText());
                                File file = new File(downloadFolder, name.getText() + "" + "." + ext
                                        .getText());
                                if (file.renameTo(renamedFile)) {
                                    downloads.get(getAdapterPosition()).name = newName;
                                    inactiveDownloads.save(getActivity());
                                    downloadsList.getAdapter().notifyItemChanged(getAdapterPosition());
                                } else {
                                    Toast.makeText(getActivity(), "Failed: Invalid Filename", Toast
                                            .LENGTH_SHORT).show();
                                }
                            }
                        };
                    }
                }
            });

            gotopage.setOnClickListener(this);
        }

        void bind(DownloadVideo download) {
            name.setText(download.name);
            String extString = "." + download.type;
            ext.setText(extString);
            String downloaded;
            if (DownloadManager.getDownloadFolder() != null) {
                File file = new File(DownloadManager.getDownloadFolder(), download.name + extString);
                if (file.exists()) {
                    if (download.size != null) {
                        long downloadedSize = file.length();
                        downloaded = Formatter.formatFileSize(getActivity(), downloadedSize);
                        double percent = 100d * downloadedSize / Long.parseLong(download.size);
                        if (percent > 100d) {
                            percent = 100d;
                        }
                        DecimalFormat percentFormat = new DecimalFormat("00.00");
                        String percentFormatted = percentFormat.format(percent);
                        String formattedSize = Formatter.formatFileSize(getActivity(), Long
                                .parseLong(download.size));
                        String statusString = downloaded + " / " + formattedSize + " " + percentFormatted +
                                "%";
                        progress.setText(statusString);
                    } else {
                        long downloadedSize = file.length();
                        downloaded = Formatter.formatShortFileSize(getActivity(), downloadedSize);
                        progress.setText(downloaded);
                    }
                } else {
                    if (download.size != null) {
                        String formattedSize = Formatter.formatShortFileSize(getActivity(), Long
                                .parseLong(download.size));
                        String statusString = "0KB / " + formattedSize + " 0%";
                        progress.setText(statusString);
                    } else {
                        String statusString = "0kB";
                        progress.setText(statusString);
                    }
                }
            }
        }

        @Override
        public void onClick(View v) {
            Bundle data = new Bundle();
            data.putLong("size", Long.parseLong(downloads.get(getAdapterPosition()).size));
            data.putString("page", downloads.get(getAdapterPosition()).page);
            SourcePage sourcePage = new SourcePage();
            sourcePage.setArguments(data);
            sourcePage.setOnUpdateLinkListener(this);
            getFragmentManager().beginTransaction().add(android.R.id.content,
                    sourcePage, "updateSourcePage").commit();
        }

        @Override
        public void onGlobalLayout() {
            if (!adjustedLayout) {
                if (itemView.getWidth() != 0 && ext.getWidth() != 0 && rename.getWidth() != 0 && delete
                        .getWidth() != 0) {
                    int totalMargin = (int) TypedValue.applyDimension(TypedValue
                                    .COMPLEX_UNIT_DIP, 35,
                            getActivity().getResources().getDisplayMetrics());
                    int nameMaxWidth = itemView.getMeasuredWidth() - totalMargin - ext
                            .getMeasuredWidth() - rename.getMeasuredWidth() - delete
                            .getMeasuredWidth();
                    name.setMaxWidth(nameMaxWidth);
                    adjustedLayout = true;
                }
            }
        }

        @Override
        public void updateLink(final String link) {
            Log.i("loremarTest", "update link");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int position = getAdapterPosition();
                    DownloadVideo item = downloads.get(position);
                    DownloadVideo download = new DownloadVideo();
                    download.link = link;
                    download.type = item.type;
                    download.size = item.size;
                    download.page = item.page;
                    download.name = item.name;
                    downloads.remove(position);
                    inactiveDownloads.save(getActivity());
                    downloadsList.getAdapter().notifyItemRemoved(position);
                    onNumDownloadsInactiveChangeListener.onNumDownloadsInactiveChange();
                    onDownloadWithNewLinkListener.onDownloadWithNewLink(download);
                }
            });
        }
    }

    private OnDownloadWithNewLinkListener onDownloadWithNewLinkListener;

    void setOnDownloadWithNewLinkListener(OnDownloadWithNewLinkListener
                                                  onDownloadWithNewLinkListener) {
        this.onDownloadWithNewLinkListener = onDownloadWithNewLinkListener;
    }
}
