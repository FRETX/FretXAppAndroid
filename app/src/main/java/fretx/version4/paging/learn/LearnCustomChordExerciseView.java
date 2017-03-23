package fretx.version4.paging.learn;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import java.util.HashMap;
import fretx.version4.FretboardView;
import fretx.version4.activities.MainActivity;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;


public class LearnCustomChordExerciseView extends RelativeLayout {

	private fretx.version4.activities.MainActivity mActivity;
	private FrameLayout rootView;
	private FretboardView fretBoardView;

	private int width, height;

	private HashMap<String,FingerPositions> chordDb;
	private Chord currentChord;

	public LearnCustomChordExerciseView(Context context) {
		super(context);
		setWillNotDraw(false);
		invalidate();
	}
	public LearnCustomChordExerciseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
		invalidate();
	}
	public LearnCustomChordExerciseView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		invalidate();
	}

	public void setmActivity(MainActivity mActivity) {
		this.mActivity = mActivity;
	}
	public void setRootView(FrameLayout rv) { this.rootView = rv; }
	public void setFretBoardView(FretboardView fv) {this.fretBoardView = fv;}
	public void setChordDb(HashMap<String,FingerPositions> chordDb){ this.chordDb = chordDb; }


	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
	}

	public void setChord(Chord chord){
		this.currentChord = chord;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(currentChord==null) return;
		fretBoardView.setFretboardPositions(currentChord.getFingerPositions());
	}


}



