/*
 * Copyright 2016 Orange
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* Orange Trust Badge library
 *
 * Module name: com.orange.essentials:otb
 * Version:     1.0
 * Created:     2016-03-15 by Aurore Penault, Vincent Boesch, and Giovanni Battista Accetta
 */
package com.orange.essentials.otb.manager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Binder;
import android.os.Build;

import com.orange.essentials.otb.logger.Logger;
import com.orange.essentials.otb.model.type.AppUsesPermission;
import com.orange.essentials.otb.model.type.GroupType;
import com.orange.essentials.otb.model.type.UserPermissionStatus;

import java.util.ArrayList;

/**
 * PermissionManager
 * This singleton is used to check app permission and affect the state of a group permission to the trustbadges
 */
public enum PermissionManager {
    INSTANCE;

    public static final String TAG = "PermissionManager";

    /**
     * mGroupPermissionDataList
     * Store Permission Informations from host app Manifest.xml
     */
    private ArrayList<String> mGroupNameList = new ArrayList<>();

    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public void initPermissionList(Context context) {
        Logger.d(TAG, "initPermissionList for context " + context);
        //Cleaning old stored groups
        mGroupNameList.clear();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_PERMISSIONS
            );

        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, "PackageManagerName NOT found.", e);
        }
        if (null != pkgInfo) {
            //getting Host app permissions declared in Manifest.xml
            String[] requestedPermissions = pkgInfo.requestedPermissions;
            if (requestedPermissions != null) {
                Logger.d(TAG, "requestedPermissions is not null");
                for (int i = 0; i < requestedPermissions.length; i++) {
                    Logger.d(TAG, "Adding permission : " + requestedPermissions[i]);
                    mGroupNameList.add(requestedPermissions[i]);
                    try {
                        Logger.d(TAG, "Looking group for permission " + requestedPermissions[i]);
                        PermissionInfo pinfo = context.getPackageManager().getPermissionInfo(requestedPermissions[i], PackageManager.GET_META_DATA);
                        if (pinfo.group != null) {
                            Logger.d(TAG, "Adding permission group " + pinfo.group);
                            mGroupNameList.add(pinfo.group);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        Logger.d(TAG, "PackageManagerName NOT found. Adding permission name " + requestedPermissions[i] + ", " + e.getMessage());
                    }
                }
            }
            initialized = true;
        } else {
            Logger.d(TAG, "pkgInfo not found");
        }
    }


    /**
     * Return the user status about a specific permission
     *
     * @param groupType : permission to be check
     * @return Values.USER_STATUS
     */
    public UserPermissionStatus doesUserAlreadyAcceptPermission(Context context, GroupType groupType) {
        Logger.d(TAG, "doesUserAlreadyAcceptPermissionGroup " + groupType);
        UserPermissionStatus result;

        if (groupType == GroupType.IMPROVEMENT_PROGRAM || groupType == GroupType.IDENTITY) {
            result = UserPermissionStatus.MANDATORY;
        } else {
            int currentapiVersion = Build.VERSION.SDK_INT;
            if (currentapiVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
                Logger.d(TAG, "currentapiVersion >= Build.VERSION_CODES.M =true");
                int permission = context.checkPermission(findInAppPermission(context, groupType), android.os.Process.myPid(), Binder.getCallingUid());
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    result = UserPermissionStatus.NOT_GRANTED;
                } else {
                    result = UserPermissionStatus.GRANTED;
                }
            } else {
                if (!((findInAppPermission(context, groupType)).isEmpty())) {
                    result = UserPermissionStatus.GRANTED;
                } else {
                    result = UserPermissionStatus.NOT_GRANTED;

                }

            }
        }
        Logger.d(TAG, "doesUserAlreadyAcceptPermissionGroup " + groupType + " is " + result);
        return result;
    }


    /**
     * Retrieve the permissionGroupName of a specific permission
     *
     * @param groupType : permission to find in GroupPermissionList
     * @return String containing the permissionGroupName
     */
    public String findInAppPermission(Context context, GroupType groupType) {
        String result = "";
        try {
            PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_PERMISSIONS
            );
            String[] requestedPermissions = pkgInfo.requestedPermissions;
            for (String permissionData : requestedPermissions) {
                if (groupType.matchPermission(permissionData)) {
                    result = permissionData;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, "findInAppPermission, package manager not found", e);
        }
        Logger.d(TAG, "finInAppPermission for groupType " + groupType + " returns " + result);
        return result;
    }

    public String getGroupNameForGroupType(GroupType groupType) {
        String result = null;
        Logger.d(TAG, "getGroupName " + groupType + " in " + mGroupNameList.size());
        for (String data : mGroupNameList) {
            if (groupType.matchPermission(data)) {
                result = data;
                break;
            }
        }
        return result;
    }


    public AppUsesPermission getAppUsesPermissionForGroupType(GroupType groupType) {
        AppUsesPermission appUsesPermission;
        if (getGroupNameForGroupType(groupType) != null) {
            appUsesPermission = AppUsesPermission.TRUE;
        } else {
            if (groupType.isSystemPermission()) {
                appUsesPermission = AppUsesPermission.FALSE;
            } else {
                appUsesPermission = AppUsesPermission.NOT_SIGNIFICANT;
            }
        }
        return appUsesPermission;
    }
}
