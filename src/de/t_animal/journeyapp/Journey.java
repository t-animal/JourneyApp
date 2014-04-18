package de.t_animal.journeyapp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;

public class Journey extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_journey);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.journey, menu);
		return true;
	}
	
	public void onToggleLocationService(View view){
		if(((ToggleButton) view).isChecked()){
			startService(new Intent(this, LocationService.class));
		}else{
			stopService(new Intent(this, LocationService.class));
		}
	}

}
