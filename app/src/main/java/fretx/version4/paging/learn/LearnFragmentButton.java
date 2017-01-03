package fretx.version4.paging.learn;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;
//import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class LearnFragmentButton extends Fragment {

    MainActivity mActivity;
    View rootView = null;

    Button btExerciseOne;
    Button btExerciseTwo;
	Button btGuidedChordExercise;
    Button btExerciseThree;
    Button btExerciseFour;

	private String SHOWCASE_CHORD_ID = "ShowcaseChordId";

    public LearnFragmentButton(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (MainActivity)getActivity();

        rootView = inflater.inflate(R.layout.learn_fragment_buttons, container, false);
        btExerciseOne = (Button)rootView.findViewById(R.id.btExerciseOne);
        btExerciseOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.learn_container, new LearnFragmentChordExercise());
	            fragmentTransaction.replace(R.id.learn_container, new LearnFragmentCustomChordExercise());
	            //TODO: back stack isn't working here
                fragmentTransaction.addToBackStack(null);

	            mActivity.audio.disablePitchDetector();
	            mActivity.audio.disableNoteDetector();
	            mActivity.audio.enableChordDetector();

                fragmentTransaction.commit();

            }
        });
        btExerciseTwo = (Button)rootView.findViewById(R.id.btExerciseTwo);
	    btExerciseTwo.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
			    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			    fragmentTransaction.replace(R.id.learn_container, new LearnFragmentScaleExercise());
			    //TODO: back stack isn't working here
			    fragmentTransaction.addToBackStack(null);

			    mActivity.audio.enablePitchDetector();
			    mActivity.audio.enableNoteDetector();
			    mActivity.audio.disableChordDetector();

			    fragmentTransaction.commit();

		    }
	    });

	    btGuidedChordExercise = (Button)rootView.findViewById(R.id.btGuidedChordExercises);
	    btGuidedChordExercise.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
			    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			    fragmentTransaction.replace(R.id.learn_container, new LearnFragmentGuidedChordExercise());
			    //TODO: back stack isn't working here
			    fragmentTransaction.addToBackStack("guidedChordExercise");

			    mActivity.audio.disablePitchDetector();
			    mActivity.audio.disableNoteDetector();
			    mActivity.audio.enableChordDetector();

			    fragmentTransaction.commit();

		    }
	    });




//        btExerciseTwo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.learn_container, LearnFragmentEx.newInstance(2));
//                fragmentTransaction.commit();
//
//            }
//        });
//        btExerciseThree = (Button)rootView.findViewById(R.id.btExerciseThree);
//        btExerciseThree.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.learn_container, LearnFragmentEx.newInstance(3));
//                fragmentTransaction.commit();
//
//            }
//        });
//        btExerciseFour = (Button)rootView.findViewById(R.id.btExerciseFour);
//        btExerciseFour.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.learn_container, LearnFragmentEx.newInstance(4));
//                fragmentTransaction.commit();
//
//            }
//        });
//
//        btExerciseTwo.setEnabled(true);
//        btExerciseThree.setEnabled(true);
//        btExerciseFour.setEnabled(true);

        /*userHistory = Util.checkUserHistory(mActivity);
        int highestExercise = 0;
        if(userHistory != null){
            highestExercise = (Integer)userHistory.get("highestExercise");
        }
        if(highestExercise < 1){
            btExerciseTwo.setEnabled(false);
            btExerciseTwo.setBackgroundColor(Color.GRAY);
        }
        if(highestExercise < 2){
            btExerciseThree.setEnabled(false);
            btExerciseThree.setBackgroundColor(Color.GRAY);
        }
        if(highestExercise < 3){
            btExerciseFour.setEnabled(false);
            btExerciseFour.setBackgroundColor(Color.GRAY);
        }*/
        return rootView;
        //return null;
    }

	@Override public void onViewCreated(View v, Bundle savedInstanceState){
//		new MaterialShowcaseView.Builder(mActivity)
//				.setTarget((Button) getActivity().findViewById(R.id.btExerciseOne))
//				.setDismissText("LET'S GO!")
//				.setContentText("Start the chord exercise here")
//				.setDelay(200) // optional but starting animations immediately in onCreate can make them choppy
//				.singleUse(SHOWCASE_CHORD_ID) // provide a unique ID used to ensure it is only shown once
//				.setMaskColour(getResources().getColor(R.color.showcaseOverlay))
//				.setShapePadding(20)
//				.show();
	}

}