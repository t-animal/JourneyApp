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
import android.view.View;
import android.widget.ToggleButton;

public class Journey extends ActionBarActivity implements TabListener {

	private JourneyFragmentPagerAdapter adapter;
	private ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

	public void onToggleLocationService(View view) {
		if (((ToggleButton) view).isChecked()) {
			startService(new Intent(this, LocationService.class));
		} else {
			stopService(new Intent(this, LocationService.class));
		}
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
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
	}

}
