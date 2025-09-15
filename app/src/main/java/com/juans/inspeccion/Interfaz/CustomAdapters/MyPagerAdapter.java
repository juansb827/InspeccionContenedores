package com.juans.inspeccion.Interfaz.CustomAdapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Juan on 29/05/2015.
 */
public class MyPagerAdapter extends FragmentPagerAdapter implements
        ViewPager.OnPageChangeListener {

    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private List<String> mTabTitles = new ArrayList<String>();  // Store tab titles
    private ViewPager mPager;
    
    private Fragment mPrimaryItem;

    public MyPagerAdapter(FragmentManager fm, ViewPager vp) {
        super(fm);
        mPager = vp;
        mPager.setAdapter(this);
        mPager.setOnPageChangeListener(this);
    }

    public void addTab(Fragment frag, String title) {
        mFragments.add(frag);
        mTabTitles.add(title);
        notifyDataSetChanged();  // Notify adapter of changes
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // This method provides titles for TabLayout if you use it
        return mTabTitles.get(position);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mPrimaryItem = (Fragment) object;
    }

    @Override
    public int getItemPosition(Object object) {
        if (object == mPrimaryItem) {
            return POSITION_UNCHANGED;
        }
        return POSITION_NONE;
    }

    // ViewPager.OnPageChangeListener methods
    @Override
    public void onPageScrollStateChanged(int arg0) { }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) { }

    @Override
    public void onPageSelected(int position) {
        // Handle page selection if needed
        // You can add custom logic here instead of ActionBar code
    }

    /**
     * This method removes all pages from ViewPager
     */
    public void removePages(ViewPager vp, androidx.viewpager.widget.PagerAdapter adapter) {
        // Remove all fragments and titles
        mFragments.clear();
        mTabTitles.clear();
        
        // Update ViewPager
        vp.removeAllViews();
        vp.setAdapter(null);
        vp.setAdapter(adapter);
    }
    
    public Fragment removePage(ViewPager vp, androidx.viewpager.widget.PagerAdapter adapter, int position) {
        if ((position < 0) || (position >= mFragments.size()) || (mFragments.size() <= 1)) {
            return null;
        } else {
            // Handle current item adjustment
            if (position == mPager.getCurrentItem()) {
                if (position == (mFragments.size() - 1)) {
                    mPager.setCurrentItem(position - 1);
                } else if (position == 0) {
                    mPager.setCurrentItem(1);
                }
            }
            
            // Remove fragment and title
            Fragment removedFragment = mFragments.remove(position);
            mTabTitles.remove(position);
            
            // Update adapter
            vp.setAdapter(null);
            vp.setAdapter(adapter);
            
            return removedFragment;
        }
    }
}