package me.marc_himmelberger.musicinterpreter.ui;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.widget.ProgressBar;

import me.marc_himmelberger.musicinterpreter.R;

class ViewPagerLock implements ViewPager.OnPageChangeListener {
    int screenUnlocked = 0;

    private final Activity mActivity;

    public ViewPagerLock(Activity parent) {
        mActivity = parent;
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position >= screenUnlocked && positionOffset > 0.25f) {
            ViewPager mViewPager = ((ViewPager) mActivity.findViewById(R.id.pager));
            mViewPager.setCurrentItem(position);
            mViewPager.setEnabled(false);

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
        if (position > screenUnlocked)
            ((ViewPager) mActivity.findViewById(R.id.pager)).setCurrentItem(position-1);

        if (position == 2 && screenUnlocked == 2) {
            if (!((ProgressBar) mActivity.findViewById(R.id.param_idleBar)).isIndeterminate())
                ((WaveformPreview) mActivity.findViewById(R.id.waveform_preview)).update();
        }
    }
}