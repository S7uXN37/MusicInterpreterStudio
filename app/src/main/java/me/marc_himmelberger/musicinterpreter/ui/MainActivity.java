package me.marc_himmelberger.musicinterpreter.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import me.marc_himmelberger.musicinterpreter.R;
import me.marc_himmelberger.musicinterpreter.interpretation.Interpreter;

public class MainActivity extends FragmentActivity {
	public static final int GET_FILE_REQ_CODE = 0;
	
	private Interpreter mInterpreter;
	private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private ViewPagerLock mViewPagerLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mToolbar.setTitle(R.string.app_name);

		mInterpreter = new Interpreter();

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), getResources());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPagerLock = new ViewPagerLock(this);
        mViewPager.addOnPageChangeListener(mViewPagerLock);
	}

    // called when a file was selected -> put uri into filePath TextView, unlock screen no. 1
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GET_FILE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                // read out Uri
                Uri uri = data.getData();

                // display Uri
                ((TextView) findViewById(R.id.filePath)).setText(uri.toString());

                // unlock next screen
                mViewPagerLock.screenUnlocked = 1;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
	}
}