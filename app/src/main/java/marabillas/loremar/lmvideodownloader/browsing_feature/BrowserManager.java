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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import marabillas.loremar.lmvideodownloader.LMvdFragment;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.browsing_feature.adblock.AdBlockManager;

/**
 * Created by loremar on 3/23/18.
 */

public class BrowserManager extends LMvdFragment {
    private List<BrowserWindow> windows;
    private RecyclerView allWindows;
    @Deprecated
    private AdBlocker adBlocker;
    private AdBlockManager adblock = new AdBlockManager();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        windows = new ArrayList<>();

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        allWindows = (RecyclerView) inflater.inflate(R.layout.all_windows_popup, (ViewGroup)
                getActivity().findViewById(android.R.id.content), false);
        allWindows.setLayoutManager(new LinearLayoutManager(getActivity()));
        allWindows.setAdapter(new AllWindowsAdapter());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupAdBlock();
    }

    private void setupAdBlock() {
        SharedPreferences prefs = getActivity().getSharedPreferences("settings", 0);
        String lastUpdated = prefs.getString(getString(R.string.adFiltersLastUpdated), "");
        adblock.update(lastUpdated, new AdBlockManager.UpdateListener() {
            @Override
            public void onAdBlockUpdateBegins() {
                Log.i("loremarTest", "Updating ad filters");
            }

            @Override
            public void onAdBlockUpdateEnds() {
                Log.i("loremarTest", "Total ad filters: " + adblock.filtersCount());
            }

            @Override
            public void onUpdateFiltersLastUpdated(String today) {
                Log.i("loremarTest", "Filters updated today: " + today);
                SharedPreferences prefs = getActivity().getSharedPreferences("settings", 0);
                prefs.edit().putString(getString(R.string.adFiltersLastUpdated), today).apply();
            }

            @Override
            public void onSaveFilters() {
                adblock.saveFilters(getActivity());
            }

            @Override
            public void onLoadFilters() {
                Log.i("loremarTest", "Ad filters are up to date. Loading filters.");
                adblock.loadFilters(getActivity());
            }
        });
    }

    @Deprecated
    private void setupAdBlocker() {
        try {
            File file = new File(getActivity().getFilesDir(), "ad_filters.dat");
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                adBlocker = (AdBlocker) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();
            } else {
                adBlocker = new AdBlocker();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(adBlocker);
                objectOutputStream.close();
                fileOutputStream.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void newWindow(String url) {
        Bundle data = new Bundle();
        data.putString("url", url);
        BrowserWindow window = new BrowserWindow();
        window.setArguments(data);
        getFragmentManager().beginTransaction()
                .add(R.id.main, window, null)
                .commit();
        windows.add(window);
        getLMvdActivity().setOnBackPressedListener(window);
        if (windows.size() > 1) {
            window = windows.get(windows.size() - 2);
            if (window != null && window.getView() != null) {
                window.getView().setVisibility(View.GONE);
            }
        }
        updateNumWindows();
        allWindows.getAdapter().notifyDataSetChanged();
    }

    public void closeWindow(BrowserWindow window) {
        windows.remove(window);
        getFragmentManager().beginTransaction().remove(window).commit();
        if (windows.size() > 0) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow != null && topWindow.getView() != null) {
                topWindow.getView().setVisibility(View.VISIBLE);
            }
            getLMvdActivity().setOnBackPressedListener(topWindow);
        } else {
            getLMvdActivity().setOnBackPressedListener(null);
        }
        updateNumWindows();
    }

    void switchWindow(int index) {
        BrowserWindow topWindow = windows.get(windows.size() - 1);
        if (topWindow.getView() != null) {
            topWindow.getView().setVisibility(View.GONE);
        }
        BrowserWindow window = windows.get(index);
        windows.remove(index);
        windows.add(window);
        if (window.getView() != null) {
            window.getView().setVisibility(View.VISIBLE);
            getLMvdActivity().setOnBackPressedListener(window);
        }
        allWindows.getAdapter().notifyDataSetChanged();
    }

    void updateNumWindows() {
        for (BrowserWindow window : windows) {
            window.updateNumWindows(windows.size());
        }
    }

    public View getAllWindows() {
        return allWindows;
    }

    public void hideCurrentWindow() {
        if (windows.size() > 0) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow.getView() != null) {
                topWindow.getView().setVisibility(View.GONE);
            }
        }
    }

    public void unhideCurrentWindow() {
        if (windows.size() > 0) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow.getView() != null) {
                topWindow.getView().setVisibility(View.VISIBLE);
                getLMvdActivity().setOnBackPressedListener(topWindow);
            }
        } else {
            getLMvdActivity().setOnBackPressedListener(null);
        }
    }

    @Deprecated
    public void updateAdFilters() {
        if (adBlocker != null) {
            adBlocker.update(getActivity());
        } else {
            setupAdBlocker();
            if (adBlocker != null) {
                adBlocker.update(getActivity());
            } else {
                File file = new File(getActivity().getFilesDir(), "ad_filters.dat");
                if (file.exists()) {
                    if (file.delete()) {
                        setupAdBlocker();
                        if (adBlocker != null) {
                            adBlocker.update(getActivity());
                        }
                    }
                }
            }
        }
    }

    @Deprecated
    public boolean checkUrlIfAds(String url) {
        return adBlocker.checkThroughFilters(url);
    }

    public boolean isUrlAd(String url) {
        Log.i("loremarTest", "Finding ad in url: " + url);
        boolean isAd = adblock.checkThroughFilters(url);
        if (isAd) {
            Log.i("loremarTest", "Detected ad: " + url);
        }
        return isAd;
    }

    private class AllWindowsAdapter extends RecyclerView.Adapter<WindowItem> {

        @NonNull
        @Override
        public WindowItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View item = inflater.inflate(R.layout.all_windows_popup_item, parent, false);
            return new WindowItem(item);
        }

        @Override
        public void onBindViewHolder(@NonNull WindowItem holder, int position) {
            holder.bind(windows.get(position).getWebView());
        }

        @Override
        public int getItemCount() {
            return windows.size() - 1;
        }
    }

    private class WindowItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView windowTitle;
        ImageView favicon;

        WindowItem(View itemView) {
            super(itemView);
            windowTitle = itemView.findViewById(R.id.windowTitle);
            favicon = itemView.findViewById(R.id.favicon);
            itemView.setOnClickListener(this);
        }

        void bind(WebView webView) {
            windowTitle.setText(webView.getTitle());
            favicon.setImageBitmap(webView.getFavicon());
        }

        @Override
        public void onClick(View v) {
            switchWindow(getAdapterPosition());
        }
    }
}
