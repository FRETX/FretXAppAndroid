package fretx.version4.paging.learn;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.paging.learn.chords.ChordFragment;
import fretx.version4.paging.learn.custom.LearnCustomBuilderFragment;
import fretx.version4.paging.learn.guided.LearnGuidedListFragment;
import fretx.version4.paging.learn.midi.LearnMidiListFragment;
import fretx.version4.paging.learn.scale.LearnScaleExerciseFragment;
import fretx.version4.utils.bluetooth.BluetoothAnimator;
import fretx.version4.utils.firebase.Analytics;

public class LearnPage extends Fragment {
    CardView btCustomChordExercise;
	CardView btScaleChordExercise;
    CardView btGuidedChordExercise;
    CardView btChord;

    public LearnPage(){}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.getInstance().logSelectEvent("TAB", "Learn");
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.paging_learn_buttons, container, false);

        btGuidedChordExercise = (CardView)rootView.findViewById(R.id.btGuidedChordExercises);
        btCustomChordExercise = (CardView)rootView.findViewById(R.id.btCustomChordExercise);
        btScaleChordExercise = (CardView)rootView.findViewById(R.id.btScaleChordExercise);
        btChord = (CardView)rootView.findViewById(R.id.btChord);

        return rootView;
    }

	@Override public void onViewCreated(View v, Bundle savedInstanceState){
        btGuidedChordExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).fragNavController.pushFragment(new LearnGuidedListFragment());
            }
        });
        btCustomChordExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).fragNavController.pushFragment(new LearnCustomBuilderFragment());
            }
        });
        btScaleChordExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).fragNavController.pushFragment(new LearnScaleExerciseFragment());
            }
        });
        btChord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).fragNavController.pushFragment(new LearnMidiListFragment());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        BluetoothAnimator.getInstance().stringFall();
    }
}