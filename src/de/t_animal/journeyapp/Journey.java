package de.t_animal.journeyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

public class Journey extends ActionBarActivity implements TabListener {

	JourneyFragmentPagerAdapter adapter;
	private ViewPager viewPager;

	public static final int THEME_CHASER = 0x00;
	public static final int THEME_RUNNER = 0x01;

	/**
	 * Sets the theme according to the value "Theme" passed in the start intent's extras
	 */
	private void setJourneyTheme() {
		int theme = getIntent().getIntExtra("Theme", THEME_RUNNER);

		switch (theme) {
		case THEME_CHASER:
			setTheme(R.style.Theme_Chaser);
			break;
		case THEME_RUNNER:
		default:
			setTheme(R.style.Theme_Runner);
			break;
		}
	}

	/**
	 * Restarts the activity using the supplied theme
	 * 
	 * @param theme
	 *            either Journey.THEME_CHASER or Journey.THEME_RUNNER
	 */
	private void restartWithTheme(int theme) {
		if (theme != THEME_CHASER && theme != THEME_RUNNER)
			return;

		Intent intent = new Intent(this, Journey.class);
		intent.putExtra("Theme", theme);
		startActivity(intent);
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setJourneyTheme();
		setContentView(R.layout.activity_journey);

		adapter = new JourneyFragmentPagerAdapter(getSupportFragmentManager());

		viewPager = (ViewPager) findViewById(R.id.mainContainer);
		viewPager.setAdapter(adapter);

		final ActionBar actionBar = getSupportActionBar();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);

		actionBar.addTab(actionBar.newTab()
				.setText(R.string.tab_map_name)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab()
				.setText(R.string.tab_info_name)
				.setTabListener(this));

		/*
		 * Select correct tab, when the viewpager is swiped
		 */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page select correct tab
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		viewPager.setCurrentItem(tab.getPosition());
	}

	public class JourneyFragmentPagerAdapter extends FragmentPagerAdapter {

		private Fragment tabs[] = new Fragment[2];

		public JourneyFragmentPagerAdapter(android.support.v4.app.FragmentManager fm) {
			super(fm);
			tabs[0] = new MapFragment();
			tabs[1] = InformationFragment.newInstance();
		}

		@Override
		public Fragment getItem(int i) {
			try {
				return tabs[i];
			} catch (ArrayIndexOutOfBoundsException e) {
				return tabs[0];
			}
		}

		@Override
		public int getCount() {
			return tabs.length;
		}

		MapFragment getMapFragment() {
			return (MapFragment) tabs[0];
		}
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
	}

}
