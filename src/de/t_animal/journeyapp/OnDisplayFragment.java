package de.t_animal.journeyapp;

public interface OnDisplayFragment {

	/**
	 * Is called when the Fragment is made visible to the user, but only if this is after it was resumed (i.e. isResumed
	 * is true)
	 */
	void onDisplay();
}
