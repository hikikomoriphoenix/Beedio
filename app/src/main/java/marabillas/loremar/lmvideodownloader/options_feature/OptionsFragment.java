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

package marabillas.loremar.lmvideodownloader.options_feature;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import marabillas.loremar.lmvideodownloader.R;

public final class OptionsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.options, container, false);
        Switch adBlockSwitch = view.findViewById(R.id.options_block_ads);
        Switch autoVideoDetectSwitch = view.findViewById(R.id.options_auto_video_detection);

        final SharedPreferences prefs = inflater.getContext().getSharedPreferences("settings", 0);
        boolean adBlockOn = prefs.getBoolean(getString(R.string.adBlockON), true);
        boolean autoVideoDetect = prefs.getBoolean(getString(R.string.autoVideoDetect), true);

        adBlockSwitch.setChecked(adBlockOn);
        autoVideoDetectSwitch.setChecked(autoVideoDetect);

        adBlockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(getString(R.string.adBlockON), isChecked).apply();
            }
        });
        autoVideoDetectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(getString(R.string.autoVideoDetect), isChecked).apply();
            }
        });

        Toolbar toolbar = view.findViewById(R.id.options_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View root = container != null ? container.getRootView() : null;
                DrawerLayout drawer = root != null ? (DrawerLayout) root.findViewById(R.id.drawer) : null;
                if (drawer != null) {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });
        int density = (int) getResources().getDisplayMetrics().density;
        toolbar.setPadding(8 * density, 16 * density, 8 * density, 16 * density);

        toolbar.setTitle("Options");
        toolbar.setTitleMarginStart(16 * density);
        toolbar.setTitleTextAppearance(inflater.getContext(), android.R.style.TextAppearance_Medium);

        return view;
    }
}
