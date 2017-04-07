package fretx.version4.paging.learn;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.paging.learn.custom.builder.LearnCustomBuilderFragment;
import fretx.version4.paging.learn.guided.LearnGuidedChordExerciseListFragment;
import fretx.version4.paging.learn.scale.LearnScaleExerciseFragment;

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
	    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Learn");
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "TAB");
	    mActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        rootView = inflater.inflate(R.layout.paging_learn_buttons, container, false);
        btExerciseOne = (CardView)rootView.findViewById(R.id.btExerciseOne);
        btExerciseOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
	            mActivity.fragNavController.pushFragment(new LearnCustomBuilderFragment());
	            mActivity.audio.disablePitchDetector();
	            mActivity.audio.disableNoteDetector();
	            mActivity.audio.enableChordDetector();

            }
        });
        btExerciseTwo = (CardView)rootView.findViewById(R.id.btExerciseTwo);
	    btExerciseTwo.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    mActivity.fragNavController.pushFragment(new LearnScaleExerciseFragment());
			    mActivity.audio.enablePitchDetector();
			    mActivity.audio.enableNoteDetector();
			    mActivity.audio.disableChordDetector();
		    }
	    });

	    btGuidedChordExercise = (CardView)rootView.findViewById(R.id.btGuidedChordExercises);
	    btGuidedChordExercise.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    mActivity.fragNavController.pushFragment(new LearnGuidedChordExerciseListFragment());
			    mActivity.audio.disablePitchDetector();
			    mActivity.audio.disableNoteDetector();
			    mActivity.audio.enableChordDetector();
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