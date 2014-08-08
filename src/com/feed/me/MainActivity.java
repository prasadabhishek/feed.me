package com.feed.me;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.viewpagerindicator.CirclePageIndicator;

public class MainActivity extends FragmentActivity {

	private ViewPager mPager;
	private ScreenSlidePagerAdapter mPagerAdapter;
	private static final int NUM_PAGES = 5;
	private CirclePageIndicator mIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setOffscreenPageLimit(2);
		mPager.setCurrentItem(0);
		mPager.setPageTransformer(true, new DepthPageTransformer());

		mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0)
				return new PageOneFragment();
			else if (position == 1)
				return new PageTwoFragment();
			else if (position == 2)
				return new PageThreeFragment();
			else if (position == 3)
				return new PageFourFragment();
			else
				return new PageFiveFragment();
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}

	public class DepthPageTransformer implements ViewPager.PageTransformer {
		private static final float MIN_SCALE = 0.75f;

		public void transformPage(View view, float position) {
			int pageWidth = view.getWidth();

			if (position < -1) { // [-Infinity,-1)
				// This page is way off-screen to the left.
				view.setAlpha(0);

			} else if (position <= 0) { // [-1,0]
				// Use the default slide transition when moving to the left page
				view.setAlpha(1);
				view.setTranslationX(0);
				view.setScaleX(1);
				view.setScaleY(1);

			} else if (position <= 1) { // (0,1]
				// Fade the page out.
				view.setAlpha(1 - position);

				// Counteract the default slide transition
				view.setTranslationX(pageWidth * -position);

				// Scale the page down (between MIN_SCALE and 1)
				float scaleFactor = MIN_SCALE + (1 - MIN_SCALE)
						* (1 - Math.abs(position));
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

			} else { // (1,+Infinity]
				// This page is way off-screen to the right.
				view.setAlpha(0);
			}
		}
	}
}
