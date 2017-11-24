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
package com.orange.essentials.otb;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.orange.essentials.otb.event.EventType;
import com.orange.essentials.otb.logger.Logger;
import com.orange.essentials.otb.manager.BadgeListener;
import com.orange.essentials.otb.manager.TrustBadgeManager;
import com.orange.essentials.otb.model.Term;
import com.orange.essentials.otb.model.TrustBadgeElement;
import com.orange.essentials.otb.model.type.GroupType;
import com.orange.essentials.otb.ui.OtbContainerFragment;
import com.orange.essentials.otb.ui.OtbDataFragment;
import com.orange.essentials.otb.ui.OtbTermsFragment;
import com.orange.essentials.otb.ui.OtbUsageFragment;

import java.io.Serializable;
import java.util.List;


/**
 * OtbActivity
 * Main activity for the lib
 */

public class OtbActivity extends AppCompatActivity implements OtbContainerFragment.OtbFragmentListener, BadgeListener {

    private static final String TAG = "OtbActivity";
    private static final String LOGS_KEY = "LogsKey";
    private static final String BADGES_KEY = "BadgesKey";
    private static final String TERMS_KEY = "TermsKey";

    private static boolean isMasterDetail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.otb_activity);

        /** Manage toolbar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.otb_app_name);
        }

        /** Calling the fragment */
        if (savedInstanceState == null) {
            isMasterDetail = useMasterDetail();
            initFragments();
        } else {
            Logger.v(TAG, "savedInstanceState != null");
            Logger.v(TAG, "useMasterDetail: " + useMasterDetail());
            if (useMasterDetail() != isMasterDetail) {
                Logger.v(TAG, "popBackstack");
                //restore from scratch
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                initFragments();
                isMasterDetail = useMasterDetail();
            }
        }

        TrustBadgeManager.INSTANCE.addBadgeListener(this);
    }

    private void initFragments() {
        Fragment otbContainerFrag = getSupportFragmentManager().findFragmentByTag(OtbContainerFragment.FRAG_TAG);
        if (otbContainerFrag == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.lightfragment_container, OtbContainerFragment.newInstance(), OtbContainerFragment.FRAG_TAG)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .attach(otbContainerFrag)
                    .commit();
        }

        // Landscape mode
        if (useMasterDetail()) {
            Logger.d(TAG, "Landscape mode - add child fragment");
            Fragment frag = null;
            String tag = null;
            if (TrustBadgeManager.INSTANCE.hasData()) {
                Logger.d(TAG, "Landscape mode - add data fragment");
                frag = new OtbDataFragment();
                tag = OtbDataFragment.FRAG_TAG;
            } else if (TrustBadgeManager.INSTANCE.hasUsage()) {
                Logger.d(TAG, "Landscape mode - add usage fragment");
                frag = new OtbUsageFragment();
                tag = OtbUsageFragment.FRAG_TAG;
            } else if (TrustBadgeManager.INSTANCE.hasTerms()) {
                Logger.d(TAG, "Landscape mode - add terms fragment");
                frag = new OtbTermsFragment();
                tag = OtbTermsFragment.FRAG_TAG;
            } else {
                Logger.d(TAG, "Landscape mode - No item found, add data fragment by default");
                frag = new OtbDataFragment();
                tag = OtbDataFragment.FRAG_TAG;
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.lightfragment_detail, frag, tag)
                    .commit();

        }
    }

    // region OtbContainerFragment.ContainerFragmentListener

    @Override
    public void onDataClick() {
        Logger.d(TAG, "onDataClick");
        displayDetail(new OtbDataFragment(), OtbDataFragment.FRAG_TAG);
    }

    @Override
    public void onUsageClick() {
        Logger.d(TAG, "onUsageClick");
        displayDetail(new OtbUsageFragment(), OtbUsageFragment.FRAG_TAG);
    }

    @Override
    public void onTermsClick() {
        Logger.d(TAG, "onTermsClick");
        displayDetail(new OtbTermsFragment(), OtbTermsFragment.FRAG_TAG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // app icon in action bar clicked; goto parent activity.
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            } else {
                finish();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        TrustBadgeManager.INSTANCE.getEventTagger().tag(EventType.TRUSTBADGE_LEAVE);
        super.onDestroy();
    }

    // endregion


    // region Private methods
    private boolean useMasterDetail() {
        return findViewById(R.id.lightfragment_detail) != null;
    }

    private void displayDetail(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (useMasterDetail()) {
            transaction.replace(R.id.lightfragment_detail, fragment, tag);
        } else {
            transaction.replace(R.id.lightfragment_container, fragment, tag);
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    public void onBadgeChange(TrustBadgeElement trustBadgeElement, boolean value, AppCompatActivity callingActivity) {
        Logger.d(TAG, "onChange trustBadgeElement=" + trustBadgeElement + " value=" + value);
        if (null != trustBadgeElement) {
            if (GroupType.IMPROVEMENT_PROGRAM.equals(trustBadgeElement.getGroupType())) {
                TrustBadgeManager.INSTANCE.setUsingImprovementProgram(value);
                Fragment frag;
                if (useMasterDetail()) {
                    frag = getSupportFragmentManager().findFragmentById(R.id.lightfragment_detail);
                } else {
                    frag = getSupportFragmentManager().findFragmentById(R.id.lightfragment_container);
                }
                if (frag instanceof OtbDataFragment) {
                    ((OtbDataFragment) frag).refreshPermission();
                }
            }
        }
        //No action defined for other badges
    }
    // endregion


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Logger.d(TAG, "Saving Factory");
        super.onSaveInstanceState(outState);
        outState.putBoolean(LOGS_KEY, Logger.isLoggingAllowed());
        outState.putSerializable(BADGES_KEY, (Serializable) TrustBadgeManager.INSTANCE.getTrustBadgeElements());
        outState.putSerializable(TERMS_KEY, (Serializable) TrustBadgeManager.INSTANCE.getTerms());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Logger.d(TAG, "Restoring factory");
        super.onRestoreInstanceState(savedInstanceState);
        restoreFactory(savedInstanceState);
    }

    private void restoreFactory(Bundle savedInstanceState) {
        if (null != savedInstanceState && null != savedInstanceState.getSerializable(BADGES_KEY)) {
            Logger.d(TAG, "Restoring factory from instanceState");
            boolean logsEnabled = savedInstanceState.getBoolean(LOGS_KEY);
            List<TrustBadgeElement> badges = (List<TrustBadgeElement>) savedInstanceState.getSerializable(BADGES_KEY);
            List<Term> terms = (List<Term>) savedInstanceState.getSerializable(TERMS_KEY);
            TrustBadgeManager.INSTANCE.initialize(getApplicationContext(), logsEnabled, badges, terms);
        }
    }
}
