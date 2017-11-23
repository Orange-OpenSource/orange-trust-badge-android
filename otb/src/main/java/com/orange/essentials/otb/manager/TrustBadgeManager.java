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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.orange.essentials.otb.event.EventTagger;
import com.orange.essentials.otb.event.EventType;
import com.orange.essentials.otb.logger.Logger;
import com.orange.essentials.otb.model.Term;
import com.orange.essentials.otb.model.TrustBadgeElement;
import com.orange.essentials.otb.model.type.ElementType;
import com.orange.essentials.otb.model.type.GroupType;
import com.orange.essentials.otb.model.type.RatingType;
import com.orange.essentials.otb.model.type.UserPermissionStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * <p/>
 * File name:   TrustBadgeManager
 * Main singleton handling the trustbadge elements
 */
public enum TrustBadgeManager {
    INSTANCE;

    /**
     * Log TAG
     */
    private static final String TAG = "TrustBadgeManager";
    /**
     * mApplicationName
     * Host application name
     */
    private String mApplicationName;
    /**
     * mApplicationPackageName
     * Host application packageName
     */
    private String mApplicationPackageName;

    /**
     * Provides a way to tag events
     */
    private EventTagger mEventTagger;

    /**
     * Store all informations about Main Data, Complementary Data, and Usage Data
     */
    private List<TrustBadgeElement> mTrustBadgeElements = new ArrayList<>();

    /**
     * Stores all the listeners who should act when a toggable badge element value is switched
     */
    private List<TrustBadgeElementListener> mTrustBadgeElementListeners = new ArrayList<>();
    /**
     * Stores all the listeners who should act when a toggable badge element value is switched
     */
    private List<BadgeListener> mBadgeListeners = new ArrayList<>();

    /**
     * List of terms and conditions that should be displayed in "terms and conditions" section
     */
    private List<Term> mTerms = new ArrayList<>();
    /**
     * Boolean indicating if app checked improvement program
     */
    private boolean mUsingImprovementProgram = true;
    /**
     * boolean letting us know if badges are initialized, false by default
     */
    private boolean mInitialized = false;

    /** METHODS ********************************************************************************* */

    /** KEYS DATAS */

    /**
     * Return the host application name
     *
     * @return Host application name from AndroidManifest.xml
     */
    private String extractApplicationName(@NonNull Context context) {
        String appName = context.getPackageName();
        Logger.d(TAG, "extractApplicationName");
        CharSequence csName = context.getApplicationInfo().loadLabel(context.getPackageManager());
        if (null != csName) {
            appName = csName.toString();
        }
        Logger.d(TAG, "extractApplicationName");
        return appName;
    }

    /**
     * provide init with default BadgeFactory. Logs are disabled.
     *
     * @param context : Context providing needed data to retrieve permissions infos
     */
    public void initialize(@NonNull Context context) {
        PermissionManager.INSTANCE.initPermissionList(context);
        initialize(context, false, TrustBadgeElementFactory.getDefaultElements(context), new ArrayList<Term>());
    }


    /**
     * initialize
     *
     * @param context            : Activity providing needed data to retrieve permissions infos
     * @param enableLogs         : enable logs in otb, they are disabled by default.
     * @param trustBadgeElements : the list of trust badge that will be used into the app
     */
    public void initialize(@NonNull Context context, boolean enableLogs, List<TrustBadgeElement> trustBadgeElements, List<Term> terms) {

        if (enableLogs) Logger.allowLogging();
        Logger.d(TAG, "TrustBadgeManager initialize");
        PermissionManager.INSTANCE.initPermissionList(context);
        mApplicationName = extractApplicationName(context);
        if (!mApplicationName.isEmpty()) {
            Logger.d(TAG, "mApplicationName is NOT empty");
            mApplicationPackageName = context.getPackageName();
            if (mApplicationPackageName != null && mApplicationPackageName.length() != 0) {
                Logger.d(TAG, "mApplicationPackageName is NOT empty");
                /** Initialisation of TrustBadgeElements to 15 permissions */
                setTrustBadgeElements(trustBadgeElements);
                setTerms(terms);
            } else {
                Logger.d(TAG, "Context does not provide PackageName.");
            }
        } else {
            Logger.d(TAG, "Context does not provide ApplicationName.");
        }
        mInitialized = true;
    }

    /**
     * retrieve list of permissions about kids
     */
    public ArrayList<TrustBadgeElement> getElementsForUsage() {
        ArrayList<TrustBadgeElement> resultPermissionList = new ArrayList<>();
        for (int i = 0; i < mTrustBadgeElements.size(); i++) {
            TrustBadgeElement trustBadgeElement = mTrustBadgeElements.get(i);
            if (null != trustBadgeElement && trustBadgeElement.getElementType() == ElementType.USAGE) {
                resultPermissionList.add(trustBadgeElement);
            }
        }
        return resultPermissionList;
    }

    /**
     * retrieve list of  permissions
     */
    public ArrayList<TrustBadgeElement> getElementsForDataCollected() {
        Logger.d(TAG, "getElementsForDataCollected");
        ArrayList<TrustBadgeElement> resultPermissionList = new ArrayList<>();
        if (mTrustBadgeElements != null) {
            for (TrustBadgeElement trustBadgeElement : mTrustBadgeElements) {
                Logger.d(TAG, "getElementsForDataCollected  : " + trustBadgeElement.getGroupType() + ", " + trustBadgeElement.getElementType() + ", " + trustBadgeElement.getAppUsesPermission() + ", " + trustBadgeElement.getUserPermissionStatus());
                if (trustBadgeElement.getElementType() != ElementType.USAGE) {
                    resultPermissionList.add(trustBadgeElement);
                    Logger.d(TAG, trustBadgeElement.getNameKey() + " ==> ADDED");
                }
            }
        } else {
            Logger.d(TAG, "Empty mTrustBadgeElements.");
        }
        return resultPermissionList;
    }

    /**
     * retrieve a specific badge according to type
     */
    public TrustBadgeElement getSpecificPermission(GroupType groupType) {
        Logger.d(TAG, "getSpecificPermission");
        TrustBadgeElement result = null;
        if (null != mTrustBadgeElements) {
            for (TrustBadgeElement trustBadgeElement : mTrustBadgeElements) {
                if (trustBadgeElement.getGroupType() == groupType) {
                    Logger.d(TAG, "trustBadgeElement.getElementType() equals " + ElementType.USAGE.toString());
                    result = trustBadgeElement;
                }
            }
        }
        return result;
    }

    /**
     * GETTERS & SETTERS************************************************************************
     */
    /**
     * Gets the array of trustbadge elements
     *
     * @return the list of TrustBadgeElements that will be displayed to the user
     */
    public List<TrustBadgeElement> getTrustBadgeElements() {
        return mTrustBadgeElements;
    }

    public void setTrustBadgeElements(List<TrustBadgeElement> trustBadgeElements) {
        mTrustBadgeElements = trustBadgeElements;
    }

    public void addTrustBadgeElement(TrustBadgeElement trustBadgeElement) {
        if (null != trustBadgeElement) {
            if (null == mTrustBadgeElements) {
                mTrustBadgeElements = new ArrayList<>();
            }
            mTrustBadgeElements.add(trustBadgeElement);
        }
    }

    /**
     * Remove all element from the SparseArray and the array
     */
    public void clearTrustBadgeElements() {
        mTrustBadgeElements.clear();
    }


    /**
     * Refresh the badges according to the permissions granted by the app
     */
    public void refreshTrustBadgePermission(@NonNull Context context) {
        Logger.d(TAG, "refreshTrustBadgePermission...");
        PermissionManager.INSTANCE.initPermissionList(context);
        for (int i = 0; i < mTrustBadgeElements.size(); i++) {
            TrustBadgeElement trustBadgeElement = mTrustBadgeElements.get(i);
            if (trustBadgeElement.isShouldBeAutoConfigured()) {
                GroupType type = trustBadgeElement.getGroupType();
                if (type.isSystemPermission()) {
                    trustBadgeElement.setUserPermissionStatus(PermissionManager.INSTANCE.doesUserAlreadyAcceptPermission(context, trustBadgeElement.getGroupType()));
                }
                if (type.equals(GroupType.IMPROVEMENT_PROGRAM)) {
                    trustBadgeElement.setUserPermissionStatus(mUsingImprovementProgram ? UserPermissionStatus.GRANTED : UserPermissionStatus.NOT_GRANTED);
                }
            }

            Logger.d(TAG, "refresh = " + trustBadgeElement);
        }

    }

    public String getApplicationName() {
        return mApplicationName;
    }

    /**
     * Method returns true if at least one badge is of type DATA or OTHER
     *
     * @return a boolean indicating if data badges are available
     */
    public boolean hasData() {
        boolean data = getElementsForDataCollected() != null && getElementsForDataCollected().size() > 0;
        Logger.d(TAG, "hasData : " + data);
        return data;
    }

    /**
     * Method returns true if at least one badge is of type USAGE
     *
     * @return a boolean indicating if usage badges are available
     */
    public boolean hasUsage() {
        boolean usage = getElementsForUsage() != null && getElementsForUsage().size() > 0;
        Logger.d(TAG, "hasUsage : " + usage);
        return usage;
    }

    /**
     * Method returns true if at least one term is available
     *
     * @return a boolean indicating if a term is available
     */
    public boolean hasTerms() {
        boolean terms = getTerms() != null && getTerms().size() > 0;
        Logger.d(TAG, "hasTerms : " + terms);
        return terms;
    }

    /**
     * getPegiAge : Retrieve permission pegi age rating
     *
     * @param trustBadgeElement : th badge to retrieve pegi age value text
     * @return String age permission
     */
    public String getPegiAge(TrustBadgeElement trustBadgeElement) {
        String ageInNumber = "";
        String ageRating = trustBadgeElement.getDescriptionKey();
        if (ageRating.contains(RatingType.THREE.name())) {
            ageInNumber = Integer.toString(RatingType.THREE.getAge());
        }
        if (ageRating.contains(RatingType.SEVEN.name())) {
            ageInNumber = Integer.toString(RatingType.SEVEN.getAge());
        }
        if (ageRating.contains(RatingType.TWELVE.name())) {
            ageInNumber = Integer.toString(RatingType.TWELVE.getAge());
        }
        if (ageRating.contains(RatingType.SIXTEEN.name())) {
            ageInNumber = Integer.toString(RatingType.SIXTEEN.getAge());
        }
        if (ageRating.contains(RatingType.EIGHTEEN.name())) {
            ageInNumber = Integer.toString(RatingType.EIGHTEEN.getAge());
        }
        return ageInNumber;
    }

    public String getApplicationPackageName() {
        return mApplicationPackageName;
    }

    public List<Term> getTerms() {
        return mTerms;
    }

    public void setTerms(List<Term> terms) {
        mTerms = terms;
    }

    public EventTagger getEventTagger() {
        if (null == mEventTagger) {
            //Provides a default eventtagger if none has been provided
            mEventTagger = new EventTagger() {
                @Override
                public void tag(EventType eventType) {
                    Logger.d(TAG, "tagging event " + eventType);
                }

                @Override
                public void tagElement(EventType eventType, TrustBadgeElement element) {
                    Logger.d(TAG, "tagging event " + eventType + " for element " + element);
                }
            };
        }
        return mEventTagger;
    }

    public void setEventTagger(EventTagger eventTagger) {
        mEventTagger = eventTagger;
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public boolean isUsingImprovementProgram() {
        return mUsingImprovementProgram;
    }

    public void setUsingImprovementProgram(boolean usingImprovementProgram) {
        mUsingImprovementProgram = usingImprovementProgram;
        TrustBadgeElement improvementProgram = TrustBadgeManager.INSTANCE.getSpecificPermission(GroupType.IMPROVEMENT_PROGRAM);
        if (null != improvementProgram) {
            improvementProgram.setUserPermissionStatus(usingImprovementProgram ? UserPermissionStatus.GRANTED : UserPermissionStatus.NOT_GRANTED);
        }
    }

    @Deprecated
    public void addTrustBadgeElementListener(TrustBadgeElementListener trustBadgeElementListener) {
        if (null != mTrustBadgeElementListeners && !mTrustBadgeElementListeners.contains(trustBadgeElementListener)) {
            mTrustBadgeElementListeners.add(trustBadgeElementListener);
        }
    }

    @Deprecated
    public void clearTrustBadgeElementListener() {
        if (null != mTrustBadgeElementListeners) {
            mTrustBadgeElementListeners.clear();
        }
    }


    @Deprecated
    public List<TrustBadgeElementListener> getTrustBadgeElementListeners() {
        return mTrustBadgeElementListeners;
    }

    public void badgeChanged(TrustBadgeElement trustBadgeElement, boolean toggled, AppCompatActivity callingActivity) {
        if (null != mBadgeListeners) {
            for (int i = 0; i < mBadgeListeners.size(); i++) {
                mBadgeListeners.get(i).onBadgeChange(trustBadgeElement, toggled, callingActivity);
            }
        }
        //Change the UserPermissionStatus as it has been switched by user
        trustBadgeElement.setUserPermissionStatus(toggled ? UserPermissionStatus.GRANTED : UserPermissionStatus.NOT_GRANTED);
    }

    public List<BadgeListener> getBadgeListeners() {
        return mBadgeListeners;
    }

    public void addBadgeListener(BadgeListener badgeListener) {
        if (null != mBadgeListeners && !mBadgeListeners.contains(badgeListener)) {
            mBadgeListeners.add(badgeListener);
        }
    }

    public void clearBadgeListener() {
        if (null != mBadgeListeners) {
            mBadgeListeners.clear();
        }
    }

    /**
     * @deprecated use rather the interface BadgeListener, called by
     * badgeChanged(TrustBadgeElement trustBadgeElement, boolean toggled, AppCompatActivity callingActivity)
     */
    @Deprecated
    public void badgeChanged(GroupType groupType, boolean toggled, AppCompatActivity callingActivity) {
        if (null != mTrustBadgeElementListeners) {
            for (int i = 0; i < mTrustBadgeElementListeners.size(); i++) {
                mTrustBadgeElementListeners.get(i).onBadgeChange(groupType, toggled, callingActivity);
            }
        }
    }


}

