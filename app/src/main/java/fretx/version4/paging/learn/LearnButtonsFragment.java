package fretx.version4.paging.learn;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;

public class LearnButtonsFragment extends Fragment {

    MainActivity mActivity;
    View rootView = null;

    CardView btExerciseOne;
	CardView btExerciseTwo;
	CardView btGuidedChordExercise;

    public LearnButtonsFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (MainActivity)getActivity();


	    Bundle bundle = new Bundle();
	    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Learn Tab activated");
	    mActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        rootView = inflater.inflate(R.layout.learn_fragment_buttons, container, false);
        btExerciseOne = (CardView)rootView.findViewById(R.id.btExerciseOne);
        btExerciseOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.learn_container, new LearnChordExerciseFragment());
	            fragmentTransaction.replace(R.id.learn_container, new LearnCustomChordExerciseFragment());
	            //TODO: back stack isn't working here
                fragmentTransaction.addToBackStack("customChordExercise");

	            mActivity.audio.disablePitchDetector();
	            mActivity.audio.disableNoteDetector();
	            mActivity.audio.enableChordDetector();

                fragmentTransaction.commit();
	            MainActivity.displayBackStack(fragmentManager);

            }
        });
        btExerciseTwo = (CardView)rootView.findViewById(R.id.btExerciseTwo);
	    btExerciseTwo.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
			    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			    fragmentTransaction.replace(R.id.learn_container, new LearnScaleExerciseFragment());
			    //TODO: back stack isn't working here
			    fragmentTransaction.addToBackStack("scaleExercise");

			    mActivity.audio.enablePitchDetector();
			    mActivity.audio.enableNoteDetector();
			    mActivity.audio.disableChordDetector();

			    fragmentTransaction.commit();
			    MainActivity.displayBackStack(fragmentManager);

		    }
	    });

	    btGuidedChordExercise = (CardView)rootView.findViewById(R.id.btGuidedChordExercises);
	    btGuidedChordExercise.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
			    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			    fragmentTransaction.replace(R.id.learn_container, new LearnGuidedChordExerciseListFragment());
			    fragmentTransaction.addToBackStack("guidedChordExercise");

			    mActivity.audio.disablePitchDetector();
			    mActivity.audio.disableNoteDetector();
			    mActivity.audio.enableChordDetector();

			    fragmentTransaction.commit();
			    MainActivity.displayBackStack(fragmentManager);

		    }
	    });

        return rootView;
    }

	@Override public void onViewCreated(View v, Bundle savedInstanceState){
		showTutorial();
	}

	private void showTutorial(){

	}
}