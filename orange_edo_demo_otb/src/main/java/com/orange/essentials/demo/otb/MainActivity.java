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
/* Orange Trust Badge library demo app
 *
 * Version:     1.0
 * Created:     2016-03-15 by Aurore Penault, Vincent Boesch, and Giovanni Battista Accetta
 */
package com.orange.essentials.demo.otb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.orange.essentials.demo.otb.dialog.OtbImprovementDialogFragment;
import com.orange.essentials.otb.OtbActivity;
import com.orange.essentials.otb.manager.BadgeListener;
import com.orange.essentials.otb.manager.TrustBadgeManager;
import com.orange.essentials.otb.model.TrustBadgeElement;
import com.orange.essentials.otb.model.type.GroupType;
import com.orange.essentials.otb.model.type.UserPermissionStatus;


public class MainActivity extends AppCompatActivity implements BadgeListener {

    public static final String TAG = "MainActivity";
    public static final String PREF_IMPROVEMENT = "improvement";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new_brand);
        setTitle("");
    }

    public void startDemOTB(View v) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        CustomBadgeFactory customBadgeFactory = new CustomBadgeFactory(getApplicationContext());
        TrustBadgeManager.INSTANCE.initialize(getApplicationContext(), true, customBadgeFactory.getTrustBadgeElements(), customBadgeFactory.getTerms());

        //We can add a badge listener. Make sure to do not have multiple copy of the same listener
        //You can also clear existing listener using: TrustBadgeManager.INSTANCE.clearBadgeListener();
        if (!TrustBadgeManager.INSTANCE.getBadgeListeners().contains(this)) {
            TrustBadgeManager.INSTANCE.addBadgeListener(this);
        }

        //retrieving toggle status from Shared Preference
        //in this case we threat differently the improvement program since we have a dialog on the disable case
        boolean improvement = sp.getBoolean(PREF_IMPROVEMENT, true);
        Log.d(TAG, "Improvement Program?" + improvement);
        TrustBadgeManager.INSTANCE.setUsingImprovementProgram(improvement);

        // than we do the same for all the other elements which are toggable
        for (TrustBadgeElement element : TrustBadgeManager.INSTANCE.getTrustBadgeElements()) {
            if (element.isToggable() && element.getGroupType() != GroupType.IMPROVEMENT_PROGRAM) {
                boolean granted = sp.getBoolean(element.getNameKey(), true);
                element.setUserPermissionStatus(granted ? UserPermissionStatus.GRANTED : UserPermissionStatus.NOT_GRANTED);
            }
        }

        Intent intent = new Intent(MainActivity.this, OtbActivity.class);
        this.startActivity(intent);
    }

    @Override
    public void onBadgeChange(TrustBadgeElement trustBadgeElement, boolean value, AppCompatActivity callingActivity) {
        Log.d(TAG, "onBadgeChange trustBadgeElement =" + trustBadgeElement + " value=" + value);
        if (null != trustBadgeElement) {
            // in this example we threat differently the improvement program
            if (GroupType.IMPROVEMENT_PROGRAM.equals(trustBadgeElement.getGroupType())) {
                if (!value) {
                    showDialog(callingActivity);
                } else {
                    //This is an example of what could be done
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean(PREF_IMPROVEMENT, value);
                    editor.apply();
                }
                //else for all other toggable elements
            } else {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(trustBadgeElement.getNameKey(), value);
                editor.apply();
            }
        }
    }

    private void showDialog(AppCompatActivity callingActivity) {
        FragmentTransaction ft = callingActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = callingActivity.getSupportFragmentManager().findFragmentByTag(callingActivity.getLocalClassName());
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment dialogFragment = OtbImprovementDialogFragment.newInstance();
        dialogFragment.show(ft, callingActivity.getLocalClassName());
    }
}
