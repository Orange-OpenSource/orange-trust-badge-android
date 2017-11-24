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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orange.essentials.otb.R;
import com.orange.essentials.otb.event.EventType;
import com.orange.essentials.otb.logger.Logger;
import com.orange.essentials.otb.manager.TrustBadgeManager;
import com.orange.essentials.otb.model.TrustBadgeElement;
import com.orange.essentials.otb.model.type.AppUsesPermission;
import com.orange.essentials.otb.model.type.UserPermissionStatus;
import com.orange.essentials.otb.ui.utils.ViewHelper;

import java.util.ArrayList;

/**
 * <p/>
 * File name:   OtbUsageFragment
 * <p/>
 * Copyright (C) 2015 Orange
 * <p/>
 * Fragment used to display usage badges
 */
public class OtbUsageFragment extends Fragment {

    public static final String FRAG_TAG = "OtbUsageFragment";
    private static final String TAG = "OtbUsageFragment";
    private boolean ignoreCheckedChange = false;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View scrollView = inflater.inflate(R.layout.otb_usage, container, false);
        LinearLayout view = (LinearLayout) scrollView.findViewById(R.id.otb_data_usage_layout);

        View header = View.inflate(getContext(), R.layout.otb_header, null);
        TextView headerTv = (TextView) header.findViewById(R.id.otb_header_tv_text);
        headerTv.setText(R.string.otb_home_usage_content);
        view.addView(header);

        ArrayList<TrustBadgeElement> usages = TrustBadgeManager.INSTANCE.getElementsForUsage();
        Logger.v(TAG, "usages elements: " + usages.size());
        for (final TrustBadgeElement usage : usages) {
            View usageView = View.inflate(getContext(), R.layout.otb_data_usage_item, null);
            ViewHelper.INSTANCE.buildView(usageView, usage, getContext());
            Logger.v(TAG, "add usage view");
            view.addView(usageView);

            if (usage.isToggable()) {
                final SwitchCompat switchCompat = (SwitchCompat) usageView.findViewById(R.id.otb_data_usage_item_sc_switch);
                switchCompat.setVisibility(View.VISIBLE);
                switchCompat.setContentDescription(getToggleContentDescription(usage.getNameKey(), switchCompat.isChecked()));
                if (usage.getAppUsesPermission() == AppUsesPermission.TRUE) {
                    switchCompat.setEnabled(true);
                    switchCompat.setChecked(usage.getUserPermissionStatus() == UserPermissionStatus.GRANTED);
                }

                switchCompat.setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                switchCompat.setContentDescription(getToggleContentDescription(usage.getNameKey(), isChecked));
                                if (ignoreCheckedChange) {
                                    return;
                                }
                                TrustBadgeManager.INSTANCE.getEventTagger().tagElement(EventType.TRUSTBADGE_ELEMENT_TOGGLED, usage);
                                TrustBadgeManager.INSTANCE.badgeChanged(usage, isChecked, (AppCompatActivity) getActivity());
                                TrustBadgeManager.INSTANCE.badgeChanged(usage.getGroupType(), isChecked, (AppCompatActivity) getActivity());
                            }
                        }
                );
            }

        }

        return scrollView;
    }

    private String getToggleContentDescription(String name, boolean value) {
        return name + " " + (
                value ?
                        getContext().getString(R.string.otb_accessibility_item_is_used_description) :
                        getContext().getString(R.string.otb_accessibility_item_not_used_description));
    }

    @Override
    public void onResume() {
        super.onResume();
        /** Manage ActionBar */
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.otb_home_usage_title);
        }
        TrustBadgeManager.INSTANCE.getEventTagger().tag(EventType.TRUSTBADGE_USAGE_ENTER);

    }

}
