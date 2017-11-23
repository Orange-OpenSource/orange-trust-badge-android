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


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orange.essentials.otb.R;
import com.orange.essentials.otb.logger.Logger;
import com.orange.essentials.otb.manager.TrustBadgeManager;
import com.orange.essentials.otb.model.TrustBadgeElement;
import com.orange.essentials.otb.model.type.AppUsesPermission;
import com.orange.essentials.otb.model.type.ElementType;
import com.orange.essentials.otb.model.type.GroupType;
import com.orange.essentials.otb.model.type.UserPermissionStatus;

import java.util.List;

/**
 * Main fragment to display otb
 */
public class OtbContainerFragment extends android.support.v4.app.Fragment {

    public static final String FRAG_TAG = "OtbContainerFragment";
    private static final String TAG = "OtbContainerFragment";
    private static final String KEY_MAIN_SELECTED = "key_main_selected";
    private static final int DATA_SELECTED = 0;
    private static final int USAGE_SELECTED = 1;
    private static final int TERM_SELECTED = 2;

    private OtbFragmentListener mListener;
    private int mFragmentSelected = DATA_SELECTED;

    public static OtbContainerFragment newInstance() {
        return new OtbContainerFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_MAIN_SELECTED, mFragmentSelected);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.otb_home, container, false);
        TextView headerTv = (TextView) view.findViewById(R.id.otb_header_tv_text);
        headerTv.setText(R.string.otb_home_header_title);
        return view;
    }

    @Override
    public void onResume() {
        Logger.d(TAG, "Resuming Fragment");
        super.onResume();

        /** Manage ActionBar title*/
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.otb_home_title);
        }
        /** refreshing data */
        TrustBadgeManager.INSTANCE.refreshTrustBadgePermission(getContext());
        buildCards();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** Setting complementary infos about Accessibility */
        /** data */
        setTitleAndLabel(R.id.otb_home_data_card,
                R.id.otb_home_data_card_tv_title,
                R.id.otb_home_data_card_tv_content,
                R.string.otb_home_data_title,
                R.string.otb_home_data_content,
                R.string.otb_accessibility_title_description
        );
        /** Usage */
        setTitleAndLabel(R.id.otb_home_usage_card,
                R.id.otb_home_data_card_tv_title,
                R.id.otb_home_data_card_tv_content,
                R.string.otb_home_usage_title,
                R.string.otb_home_usage_content,
                R.string.otb_accessibility_title_description
        );

        /** terms */
        setTitleAndLabel(R.id.otb_home_terms_card,
                R.id.otb_home_terms_card_tv_commitment_title,
                0,
                R.string.otb_home_terms_title,
                0,
                R.string.otb_accessibility_title_description
        );
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            initListeners(savedInstanceState.getInt(KEY_MAIN_SELECTED));
        } else {
            initListeners(DATA_SELECTED);
        }

        if (getActivity() instanceof OtbFragmentListener) {
            mListener = (OtbFragmentListener) getActivity();
        } else {
            throw new RuntimeException("Activity must implements ContainerFragmentListener");
        }


    }

    /**
     * *******************************************************************************************
     * /***********************    PRIVATE METHOD DATA INITIALISATION     *****************************
     * /********************************************************************************************
     */

    /**
     * Convenient method used to fill information title, label and accessbility string
     */
    private void setTitleAndLabel(int viewId, int titleId, int contentId, int titleRes, int contentRes, int accessibilityRes) {
        TextView dataTitle = null;
        TextView dataDetail = null;
        View container = this.getActivity().findViewById(viewId);
        if (null != container) {
            dataTitle = (TextView) container.findViewById(titleId);
            dataDetail = (TextView) container.findViewById(contentId);
        }
        if (dataTitle == null) {
            dataTitle = (TextView) getActivity().findViewById(viewId);
        }
        if (null != dataTitle) {
            dataTitle.setText(titleRes);
            dataTitle.setContentDescription(getString(accessibilityRes) + "  " + getString(titleRes));
        }
        if (null != dataDetail) {
            dataDetail.setText(contentRes);
        }
    }

    /**
     * Add the policy buttons ont appropriate layout
     */
    private void buildCards() {
        Logger.d(TAG, "buildCards");
        LinearLayout dataLayout = null;
        LinearLayout usageLayout = null;
        View containerData = this.getActivity().findViewById(R.id.otb_home_data_card);
        if (null != containerData) {
            containerData.setVisibility(TrustBadgeManager.INSTANCE.hasData() ? View.VISIBLE : View.GONE);
            dataLayout = (LinearLayout) containerData.findViewById(R.id.otb_home_data_card_ll_container);
        }
        View containerUsage = this.getActivity().findViewById(R.id.otb_home_usage_card);
        if (null != containerUsage) {
            containerUsage.setVisibility(TrustBadgeManager.INSTANCE.hasUsage() ? View.VISIBLE : View.GONE);
            usageLayout = (LinearLayout) containerUsage.findViewById(R.id.otb_home_data_card_ll_container);
        }
        View containerTerms = this.getActivity().findViewById(R.id.otb_home_terms_card);
        if (null != containerTerms) {
            containerTerms.setVisibility(TrustBadgeManager.INSTANCE.hasTerms() ? View.VISIBLE : View.GONE);
        }

        if (dataLayout != null && usageLayout != null) {
            dataLayout.removeAllViews();
            usageLayout.removeAllViews();

            List<TrustBadgeElement> trustBadgeElements = TrustBadgeManager.INSTANCE.getTrustBadgeElements();
            if (trustBadgeElements != null) {
                for (TrustBadgeElement trustBadgeElement : trustBadgeElements) {
                    Logger.d(TAG, trustBadgeElement.toString());

                    if (trustBadgeElement.getElementType() == ElementType.MAIN || trustBadgeElement.getElementType() == ElementType.USAGE) {

                        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View view = inflater.inflate(R.layout.otb_custom_toggle_button, null);

                        ImageView icon = (ImageView) view.findViewById(R.id.otb_custom_toggle_button_iv_id);
                        icon.setContentDescription(trustBadgeElement.getNameKey());

                        TextView buttonText = (TextView) view.findViewById(R.id.otb_custom_toggle_button_tv_id);
                        if (trustBadgeElement.getGroupType() != GroupType.PEGI) {
                            if ((trustBadgeElement.getAppUsesPermission() == AppUsesPermission.TRUE) ||
                                    (trustBadgeElement.getAppUsesPermission() == AppUsesPermission.NOT_SIGNIFICANT)) {
                                if (trustBadgeElement.getUserPermissionStatus() == UserPermissionStatus.GRANTED ||
                                        trustBadgeElement.getUserPermissionStatus() == UserPermissionStatus.MANDATORY) {

                                    buttonText.setText(getResources().getString(R.string.otb_toggle_button_granted));
                                    buttonText.setTextColor(getResources().getColor(R.color.colorAccent));
                                    icon.setImageDrawable(getResources().getDrawable(trustBadgeElement.getEnabledIconId()));
                                } else {
                                    buttonText.setText(getResources().getString(R.string.otb_toggle_button_not_granted));
                                    buttonText.setTextColor(getResources().getColor(R.color.otb_black));
                                    icon.setImageDrawable(getResources().getDrawable(trustBadgeElement.getDisabledIconId()));
                                }
                            } else {
                                buttonText.setText(getResources().getString(R.string.otb_toggle_button_not_granted));
                                icon.setImageDrawable(getResources().getDrawable(trustBadgeElement.getDisabledIconId()));
                            }
                        } else {
                            icon.setImageDrawable(getResources().getDrawable(trustBadgeElement.getEnabledIconId()));
                            buttonText.setText(TrustBadgeManager.INSTANCE.getPegiAge(trustBadgeElement).concat(getString(R.string.otb_home_usage_pegi_age)));
                            buttonText.setTextColor(getResources().getColor(R.color.otb_black));
                        }

                        if (trustBadgeElement.getElementType() == ElementType.MAIN) {
                            dataLayout.addView(view);
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mListener.onDataClick();
                                }
                            });
                        } else {
                            usageLayout.addView(view);
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mListener.onUsageClick();
                                }
                            });
                        }
                    }
                }
            } else {
                Logger.d(TAG, "TrustBadgeElements is null, please add TrustBadgeElements to list");
            }
        }
        Logger.d(TAG, "buildCards");
    }

    /***********************************************************************************************
     * /***********************    PRIVATE METHOD FOR PERMISSIONS Listener     ************************
     * /
     * ********************************************************************************************
     *
     * @param fragmentSelected the selected fragment identifier
     */
    private void initListeners(int fragmentSelected) {
        final View dataCardLayout = getActivity().findViewById(R.id.otb_home_data_card);
        final View usageCardLayout = getActivity().findViewById(R.id.otb_home_usage_card);
        final View termsCardLayout = getActivity().findViewById(R.id.otb_home_terms_card);
        if (dataCardLayout instanceof TextView) {
            switch (fragmentSelected) {
                case USAGE_SELECTED:
                    usageCardLayout.setSelected(true);
                    mFragmentSelected = USAGE_SELECTED;
                    break;
                case TERM_SELECTED:
                    termsCardLayout.setSelected(true);
                    mFragmentSelected = TERM_SELECTED;
                    break;
                case DATA_SELECTED:
                default:
                    dataCardLayout.setSelected(true);
                    mFragmentSelected = DATA_SELECTED;
            }
        }
        dataCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataCardLayout instanceof TextView) {
                    mFragmentSelected = DATA_SELECTED;
                    dataCardLayout.setSelected(true);
                    usageCardLayout.setSelected(false);
                    termsCardLayout.setSelected(false);
                }
                mListener.onDataClick();
            }
        });
        usageCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataCardLayout instanceof TextView) {
                    mFragmentSelected = USAGE_SELECTED;
                    dataCardLayout.setSelected(false);
                    usageCardLayout.setSelected(true);
                    termsCardLayout.setSelected(false);
                }
                mListener.onUsageClick();
            }
        });
        termsCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataCardLayout instanceof TextView) {
                    mFragmentSelected = TERM_SELECTED;
                    dataCardLayout.setSelected(false);
                    usageCardLayout.setSelected(false);
                    termsCardLayout.setSelected(true);
                }
                mListener.onTermsClick();
            }
        });
    }

    /**
     * Interface of "ContainerFragment" listener
     * this listener notify the ContainerFragments events to host application
     */
    public interface OtbFragmentListener {

        void onDataClick();

        void onUsageClick();

        void onTermsClick();
    }

}
