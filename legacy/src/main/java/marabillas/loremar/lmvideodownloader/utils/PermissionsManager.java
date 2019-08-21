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

package marabillas.loremar.lmvideodownloader.utils;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import marabillas.loremar.lmvideodownloader.LMvdActivity;

public abstract class PermissionsManager implements ActivityCompat
        .OnRequestPermissionsResultCallback {
    private Activity activity;
    private boolean grantedPermissions;
    private String[] permissions;
    private int requestCode;

    protected PermissionsManager(Activity activity) {
        this.activity = activity;
        ((LMvdActivity) activity).setOnRequestPermissionsResultListener(this);
    }

    private boolean notGrantedPermission(String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) != PackageManager
                .PERMISSION_GRANTED;
    }

    public void checkPermissions(String permission, int requestCode) {
        checkPermissions(new String[]{permission}, requestCode);
    }

    public void checkPermissions(String[] permissions, int requestCode) {
        this.permissions = permissions;
        this.requestCode = requestCode;
        for (String permission : permissions) {
            if (notGrantedPermission(permission)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    showRequestPermissionRationale();
                } else {
                    requestPermissions();
                }
                break;
            } else grantedPermissions = true;
        }
        if (grantedPermissions) onPermissionsGranted();
    }

    public void requestPermissions() {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        permissions[i])) {
                    grantedPermissions = false;
                    requestDisallowedAction();
                } else {
                    grantedPermissions = false;
                    onPermissionsDenied();
                }
                break;
            } else grantedPermissions = true;
        }
        if (grantedPermissions) onPermissionsGranted();
    }

    /**
     * add code here to tell users what permissions you need granted and why you need each
     * permission. Should call requestPermissions() after showing rationale.
     */
    public abstract void showRequestPermissionRationale();

    /**
     * add code here when permissions can't be requested. Either disable feature, direct user to
     * settings to allow user to set permissions, ask user to uninstall, or etc.
     */
    public abstract void requestDisallowedAction();

    public abstract void onPermissionsGranted();

    public abstract void onPermissionsDenied();
}
