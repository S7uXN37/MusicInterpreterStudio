package me.marc_himmelberger.musicinterpreter.ui;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;

import me.marc_himmelberger.musicinterpreter.R;

public class ViewPagerLock implements ViewPager.OnPageChangeListener {
    public int screenUnlocked = 0;

    private Activity mActivity;

    public ViewPagerLock(Activity parent) {
        mActivity = parent;
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position >= screenUnlocked && positionOffset > 0.25f) {
            ((ViewPager) mActivity.findViewById(R.id.pager)).setCurrentItem(position);

            Snackbar.make(
                    mActivity.findViewById(R.id.pager),
                    R.string.scroll_block_msg,
                    Snackbar.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageSelected(int position) {
    }
}