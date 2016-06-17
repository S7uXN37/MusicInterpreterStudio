package me.marc_himmelberger.musicinterpreter.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import me.marc_himmelberger.musicinterpreter.R;

public class PagerAdapter extends FragmentPagerAdapter {
    private Resources resources;

    public PagerAdapter (FragmentManager fragMan, Resources res) {
        super(fragMan);
        resources = res;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new InterpreterFragment();

        Bundle args = new Bundle();
        args.putInt(InterpreterFragment.ARG_ID_KEY, position);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        int strId;

        switch (position) {
            case 0:
                strId = R.string.open_file_title;
                break;
            case 1:
                strId = R.string.read_file_title;
                break;
            case 2:
                strId = R.string.analysis_title;
                break;
            case 3:
                strId = R.string.results_title;
                break;
            default:
                return super.getPageTitle(position);
        }

        return resources.getString(strId);
    }
}