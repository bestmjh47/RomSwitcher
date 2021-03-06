package com.grarak.romswitcher.activities;

/*
 * Copyright (C) 2014 The RomSwitcher Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created by grarak's kitten (meow) on 31.03.14.
 */

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.grarak.romswitcher.R;
import com.grarak.romswitcher.fragments.CreditsFragment;
import com.grarak.romswitcher.fragments.DownloadFragment;
import com.grarak.romswitcher.fragments.InstallationFragment;
import com.grarak.romswitcher.fragments.RomFragment;
import com.grarak.romswitcher.services.AppUpdaterService;
import com.grarak.romswitcher.utils.Constants;
import com.grarak.romswitcher.utils.Utils;
import com.stericson.RootTools.RootTools;

import java.util.ArrayList;
import java.util.List;

public class RomSwitcherActivity extends Activity implements ActionBar.TabListener, Constants {

    private ViewPager mViewPager;

    private List<Fragment> fragments = new ArrayList<Fragment>();
    private List<String> fragmentsname = new ArrayList<String>();

    private static MenuItem progressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Batman is coming nananananana
         * Let's check for new application version
         */
        startService(new Intent(getApplicationContext(), AppUpdaterService.class));

        // Clear fragments otherwise fragments will be spammed with items after recreation
        fragments.clear();
        fragmentsname.clear();

        /*
         * Check if root or busybox is available
         * Disable check if nonroot is set to true
         */
        if ((!RootTools.isAccessGiven() || !RootTools.isBusyboxAvailable()) && !nonroot) {
            new Utils().toast(getString(!RootTools.isAccessGiven() ? R.string.noroot : R.string.nobusybox), getApplicationContext());
            exit();
        }

        setContentView(R.layout.activity_romswitcher);

        // Add fragments to list
        fragments.add(new DownloadFragment());
        fragmentsname.add(getString(R.string.download));

        if (new Utils().isSupported()) {
            fragments.add(new InstallationFragment());
            fragmentsname.add(getString(R.string.installation));

            for (int i = 1; i <= new Utils().getRomNumber(); i++) {
                fragments.add(RomFragment.newInstance(i));
                fragmentsname.add(getString(R.string.rom, i));
            }
        }

        fragments.add(new CreditsFragment());
        fragmentsname.add(getString(R.string.credits));

        final ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (actionBar != null) actionBar.setSelectedNavigationItem(position);
            }
        });

        // Load all fragments once
        mViewPager.setOffscreenPageLimit(fragments.size());

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++)
            if (actionBar != null)
                actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));

        /*
         * Initialize ProgressDialog in Utils just in case
         * nasty hack to avoid ProgressDialog disappear after recreation (rotation).
         * That shouldn't happen anymore.
         */
        new Utils().createProgressDialog(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        progressItem = menu.findItem(R.id.action_menu_progress);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentsname.get(position);
        }
    }

    private void exit() {
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
        finish();
    }

    public static void showProgress(boolean show) {
        if (progressItem != null) progressItem.setVisible(show);
    }
}
