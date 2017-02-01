package fretx.version4.paging.learn;

		import android.content.Context;
		import android.graphics.Canvas;
		import android.graphics.Paint;
		import android.os.Bundle;
		import android.os.CountDownTimer;
		import android.util.AttributeSet;
		import android.util.Log;
		import android.view.View;
		import android.view.ViewGroup;
		import android.widget.FrameLayout;
		import android.widget.LinearLayout;
		import android.widget.ListView;
		import android.widget.RelativeLayout;
		import android.widget.TextView;

		import java.io.IOException;
		import java.util.ArrayList;
		import java.util.HashMap;

		import fretx.version4.BluetoothClass;
		import fretx.version4.FretboardView;
		import fretx.version4.R;
		import fretx.version4.Util;
		import fretx.version4.activities.MainActivity;
		import fretx.version4.paging.chords.ChordFragment;
		import rocks.fretx.audioprocessing.AudioAnalyzer;
		import rocks.fretx.audioprocessing.Chord;
		import rocks.fretx.audioprocessing.FingerPositions;
		import rocks.fretx.audioprocessing.MusicUtils;



/**
 * Created by onurb_000 on 15/12/16.
 */


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



