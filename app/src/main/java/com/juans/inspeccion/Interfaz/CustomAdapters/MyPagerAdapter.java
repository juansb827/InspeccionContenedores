package com.juans.inspeccion.Interfaz.CustomAdapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Juan on 29/05/2015.
 */
public class MyPagerAdapter extends FragmentPagerAdapter implements
        ViewPager.OnPageChangeListener, ActionBar.TabListener {

    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private ViewPager mPager;
    private ActionBar mActionBar;

    private Fragment mPrimaryItem;

    public MyPagerAdapter(FragmentManager fm, ViewPager vp, ActionBar ab) {
        super(fm);
        mPager = vp;
        mPager.setAdapter(this);
        mPager.setOnPageChangeListener(this);
        mActionBar = ab;
    }

    public void addTab(Fragment frag,String title) {
        mFragments.add(frag);
        mActionBar.addTab(mActionBar.newTab().setTabListener(this).
                setText(title));
    }

    @Override
    public Fragment getItem(int position) {
      return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    /** (non-Javadoc)
     * @see android.support.v4.app.FragmentStatePagerAdapter#setPrimaryItem(android.view.ViewGroup, int, java.lang.Object)
     */
    @Override
    public void setPrimaryItem(ViewGroup container, int position,
                               Object object) {
        super.setPrimaryItem(container, position, object);
        mPrimaryItem = (Fragment) object;
    }

    /** (non-Javadoc)
     * @see android.support.v4.view.PagerAdapter#getItemPosition(java.lang.Object)
     */
    @Override
    public int getItemPosition(Object object) {
        if (object == mPrimaryItem) {
            return POSITION_UNCHANGED;
        }
        return POSITION_NONE;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) { }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) { }

    @Override
    public void onPageScrollStateChanged(int arg0) { }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) { }

    @Override
    public void onPageSelected(int position) {
        mActionBar.setSelectedNavigationItem(position);
    }

    /**
     * This method removes the pages from ViewPager
     */
    public void removePages(ViewPager vp, android.support.v4.view.PagerAdapter adapter) {
        mActionBar.removeAllTabs();

        //call to ViewPage to remove the pages
        vp.removeAllViews();
        mFragments.clear();

        //make this to update the pager
        vp.setAdapter(null);
        vp.setAdapter(adapter);
    }
    public Fragment removePage(ViewPager vp, android.support.v4.view.PagerAdapter adapter,int position) {
        if ((position < 0) || (position >= mFragments.size()) || (mFragments.size()<=1)) {
            return null ;
        }
        else
        {
            if (position == mPager.getCurrentItem()) {
                if(position == (mFragments.size()-1)) {
                    mPager.setCurrentItem(position-1);
                } else if (position == 0){
                    mPager.setCurrentItem(1);
                }
            }
            mActionBar.removeTabAt(position);
            Fragment view=mFragments.remove(position);
            vp.setAdapter(null);
            vp.setAdapter(adapter);
            return view;
        }
        }



        //call to ViewPage to remove the pages



        //make this to update the pager


}