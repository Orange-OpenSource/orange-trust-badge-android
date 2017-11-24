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
package com.orange.essentials.otb.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orange.essentials.otb.R;
import com.orange.essentials.otb.event.EventType;
import com.orange.essentials.otb.logger.Logger;
import com.orange.essentials.otb.manager.TrustBadgeManager;
import com.orange.essentials.otb.model.TrustBadgeElement;
import com.orange.essentials.otb.model.type.AppUsesPermission;
import com.orange.essentials.otb.model.type.ElementType;
import com.orange.essentials.otb.model.type.UserPermissionStatus;
import com.orange.essentials.otb.ui.utils.ViewHelper;

import java.util.ArrayList;

/**
 * <p/>
 * File name:   OtbDataFragment
 * <p/>
 * Fragment used to display the details for badges other than usage badges
 */
public class OtbDataFragment extends Fragment {

    public static final String FRAG_TAG = "OtbDataFragment";
    private static final String TAG = "OtbDataFragment";
    private boolean ignoreCheckedChange = false;
    private LinearLayout mMainll, mOtherll;
    private View mNoOtherLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.otb_data, container, false);
        TextView headerTv = (TextView) view.findViewById(R.id.otb_header_tv_text);
        headerTv.setText(R.string.otb_home_data_content);

        TextView keyDataTitleText = (TextView) view.findViewById(R.id.otb_data_tv_main_data_title);
        keyDataTitleText.setContentDescription(getContext().getString(R.string.otb_accessibility_category_description) + "  " + getContext().getString(R.string.otb_main_data_title));


        mMainll = (LinearLayout) view.findViewById(R.id.otb_data_ll_main_data);
        mOtherll = (LinearLayout) view.findViewById(R.id.otb_data_ll_other_data);
        mNoOtherLayout = view.findViewById(R.id.otb_data_tv_no_other_data);


        /** Adding listener on bottom button */
        Button bottomButton = (Button) view.findViewById(R.id.otb_data_bt_parameter);
        bottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoPermissions();
            }
        });

        return view;
    }


    @Override
    public void onResume() {
        Logger.d(TAG, "onResume");
        super.onResume();
        /** Manage ActionBar */
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.otb_home_data_title);
        }
        TrustBadgeManager.INSTANCE.getEventTagger().tag(EventType.TRUSTBADGE_PERMISSION_ENTER);
        //Refreshes the view according to permissions status
        refreshPermission();
    }


    /**
     * provide access to App permission screen (intent out of the fragment)
     */
    private void gotoPermissions() {
        /** Code to access to the details settings of the package name */
        Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + TrustBadgeManager.INSTANCE.getApplicationPackageName()));
        TrustBadgeManager.INSTANCE.getEventTagger().tag(EventType.TRUSTBADGE_GO_TO_SETTINGS);
        getContext().startActivity(i);
    }


    private String getToggleContentDescription(String name, boolean value) {
        return name + " " + (
                value ?
                        getContext().getString(R.string.otb_accessibility_item_is_used_description) :
                        getContext().getString(R.string.otb_accessibility_item_not_used_description));
    }

    public void refreshPermission() {
        TrustBadgeManager.INSTANCE.refreshTrustBadgePermission(getContext());
        ArrayList<TrustBadgeElement> datas = TrustBadgeManager.INSTANCE.getElementsForDataCollected();
        mMainll.removeAllViews();
        mOtherll.removeAllViews();
        boolean isOtherTitleAdded = false;
        for (final TrustBadgeElement data : datas) {
            View usageView = View.inflate(getContext(), R.layout.otb_data_usage_item, null);
            ViewHelper.INSTANCE.buildView(usageView, data, getContext());
            if (data.getElementType() == ElementType.MAIN) {
                mMainll.addView(usageView);
            } else {
                mOtherll.addView(usageView);
                isOtherTitleAdded = true;
            }

            if (data.isToggable()) {
                final SwitchCompat switchCompat = (SwitchCompat) usageView.findViewById(R.id.otb_data_usage_item_sc_switch);
                switchCompat.setVisibility(View.VISIBLE);
                switchCompat.setContentDescription(getToggleContentDescription(data.getNameKey(), switchCompat.isChecked()));
                if (data.getAppUsesPermission() == AppUsesPermission.TRUE) {
                    switchCompat.setEnabled(true);
                    switchCompat.setChecked(data.getUserPermissionStatus() == UserPermissionStatus.GRANTED);
                }

                switchCompat.setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                switchCompat.setContentDescription(getToggleContentDescription(data.getNameKey(), isChecked));
                                if (ignoreCheckedChange) {
                                    return;
                                }
                                TrustBadgeManager.INSTANCE.getEventTagger().tagElement(EventType.TRUSTBADGE_ELEMENT_TOGGLED, data);
                                TrustBadgeManager.INSTANCE.badgeChanged(data, isChecked, (AppCompatActivity) getActivity());
                                TrustBadgeManager.INSTANCE.badgeChanged(data.getGroupType(), isChecked, (AppCompatActivity) getActivity());
                            }
                        }
                );
            }
        }

        if (!isOtherTitleAdded) {
            mOtherll.setVisibility(View.GONE);
            mNoOtherLayout.setVisibility(View.VISIBLE);
            /** Accessibility : Adding "other data" section */
            TextView otherDataTitleText = (TextView) mNoOtherLayout.findViewById(R.id.otb_data_tv_other_data_title);
            if (otherDataTitleText != null) {
                otherDataTitleText.setContentDescription(getContext().getString(R.string.otb_accessibility_category_description) + "  " + getContext().getString(R.string.otb_other_data_title));
            }
        } else {
            mOtherll.setVisibility(View.VISIBLE);
            mNoOtherLayout.setVisibility(View.GONE);

        }
    }

}
