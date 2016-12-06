package fretx.version4.paging.chords;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import fretx.version4.BluetoothClass;
import fretx.version4.ChordView;
import fretx.version4.Util;
import fretx.version4.activities.BluetoothActivity;
import fretx.version4.Config;
import fretx.version4.activities.MainActivity;
import fretx.version4.ObservableVideoView;
import fretx.version4.R;
import fretx.version4.paging.play.PlayFragmentYoutubeFragment;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.FretboardPosition;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * Created by Misho on 2/4/2016.
 */

public class ChordFragment extends Fragment
{
    //the images to display
//    Integer[] imageIDs = {
//            R.drawable.dor,
//            R.drawable.re,
//            R.drawable.mi,
//            R.drawable.fa,
//            R.drawable.so,
//            R.drawable.la,
//            R.drawable.si
//    };
//    Integer[] imageBackgroundIDs = {
//            R.drawable.backone,
//            R.drawable.backtwo,
//            R.drawable.backthree,
//            R.drawable.backfour,
//            R.drawable.backfive,
//            R.drawable.backsix,
//            R.drawable.backseven
//    };
    ObservableVideoView vvMain;
    Uri[] videoUri = new Uri[7];
    ArrayList<byte[]> musicArray = new ArrayList<>(7);
    ImageView imgBack;

	Chord currentChord;

    MainActivity mActivity;

    View rootView;
	ChordView chordView;

	HashMap<String,FingerPositions> chordFingerings;



    public ChordFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (MainActivity) getActivity();

        rootView = inflater.inflate(R.layout.chord_fragment, container, false);

//        vvMain = (ObservableVideoView)rootView.findViewById(R.id.vvMain);

//        imgBack = (ImageView)rootView.findViewById(R.id.imgBackground);

//	    imgBack.setImageResource(imageBackgroundIDs[0]);

        // input data file
//        videoUri[0] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.cmajor);
//        videoUri[1] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.dmajor);
//        videoUri[2] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.emajor);
//        videoUri[3] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.fmajor);
//        videoUri[4] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.gmajor);
//        videoUri[5] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.amajor);
//        videoUri[6] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.bmajor);


        // input text file

//        byte[] array1 = data2array(R.raw.cmajortxt);
//        byte[] array2 = data2array(R.raw.dmajortxt);
//        byte[] array3 = data2array(R.raw.emajortxt);
//        byte[] array4 = data2array(R.raw.fmajortxt);
//        byte[] array5 = data2array(R.raw.gmajortxt);
//        byte[] array6 = data2array(R.raw.amajortxt);
//        byte[] array7 = data2array(R.raw.bmajortxt);

//        musicArray.add(array1);
//        musicArray.add(array2);
//        musicArray.add(array3);
//        musicArray.add(array4);
//        musicArray.add(array5);
//        musicArray.add(array6);
//        musicArray.add(array7);

        // init gallery
//        Gallery gallery = (Gallery) rootView.findViewById(R.id.gallery1);
//        gallery.setAdapter(new ImageAdapter(mActivity));
//        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Toast.makeText(mActivity.getBaseContext(), "pic" + (position + 1) + " selected", Toast.LENGTH_SHORT).show();
//                playChord(position);
//            }
//        });
        return  rootView;
    }
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		chordView = (ChordView) mActivity.findViewById(R.id.chordView);
		chordFingerings = MusicUtils.parseChordDb();

		ConnectThread connectThread = new ConnectThread(Util.str2array("{0}"));
		connectThread.run();

		String[] rootNotes = {"C","C#","D","Eb","E","F","F#","G","G#","A","Bb","B"};
		String [] chordTypes = {"maj","m","maj7","m7","sus2","sus4","dim","dim7","aug",};

		LinearLayout rootNoteView = (LinearLayout) mActivity.findViewById(R.id.chordPickerRootNoteView);
		LinearLayout chordTypeView = (LinearLayout) mActivity.findViewById(R.id.chordPickerTypeView);

		//TODO: do proper, unrepeated code dammit
		TextView tmpTextView;
		for (String str :rootNotes) {
			tmpTextView = new TextView(mActivity);
			tmpTextView.setText(str);
			tmpTextView.setTextSize(50);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			rootNoteView.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.chordPickerRootNoteView);
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.secondary_text));
						}
					}
					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.primary_text));
					updateCurrentChord(((TextView) view).getText().toString(),currentChord.type);
				}
			});
		}
		tmpTextView = new TextView(chordTypeView.getContext());
		for (String str : chordTypes) {
			tmpTextView = new TextView(mActivity);
			tmpTextView.setText(str);
			tmpTextView.setTextSize(50);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			chordTypeView.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.chordPickerTypeView);
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.secondary_text));
						}
					}
					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.primary_text));
					updateCurrentChord(currentChord.root,((TextView) view).getText().toString());
				}
			});
		}

		TextView initialRoot = (TextView) rootNoteView.getChildAt(0);
		TextView initialType = (TextView) chordTypeView.getChildAt(0);
		initialRoot.setTextColor(mActivity.getResources().getColor(R.color.primary_text));
		initialType.setTextColor(mActivity.getResources().getColor(R.color.primary_text));
		updateCurrentChord(initialRoot.getText().toString(),initialType.getText().toString());


	}

	private void updateCurrentChord(String root , String type){
		currentChord = new Chord(root,type);
		Log.d("Chord Selector",currentChord.toString());
//		FingerPositions fp = chordFingerings.get(currentChord.toString());

		byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(currentChord.toString(),chordFingerings);

		chordView.setFingerPositions(chordFingerings.get(currentChord.toString()));
//		int[] chordNotes = currentChord.getNotes();
//		byte[] bluetoothArray = new byte[chordNotes.length+1];
//		//TODO: gotta take care of the exceptions here, or somewhere this will probably create wrong fingerings for some chords
//		for (int i = 0; i < chordNotes.length; i++) {
//			FretboardPosition fb = MusicUtils.midiNoteToFretboardPosition(  chordNotes[i]);
//			bluetoothArray[i] = Byte.valueOf(Integer.toString(fb.getFret()*10 + fb.getString()));
//		}
//		bluetoothArray[bluetoothArray.length-1] = Byte.valueOf("0");

		Log.d("Chord picker BT","sending :" + bluetoothArray.toString());

		ConnectThread connectThread = new ConnectThread(bluetoothArray);
		connectThread.run();

	}
    // data convert to array
//    public byte[] data2array(int resID){
//        String str= readRawTextFile(mActivity.getBaseContext(), resID);
//        String[] strArrTemp = str.split(" ");
//        String strText = strArrTemp[1].replaceAll("\n", "");     // This is text of that strTime.
//        return str2array(strText);
//    }
    // string convert to array
    public byte[] str2array(String string){
        String strSub = string.replaceAll("[{}]", "");
        String[] parts = strSub.split(",");
        byte[] array = new byte[parts.length];
        for (int i = 0; i < parts.length; i ++)
        {
            array[i] = Byte.valueOf(parts[i]);
        }
        return array;
    }
    // this function read form raw data.
//    public static String readRawTextFile(Context ctx, int resId) {
//        InputStream inputStream = ctx.getResources().openRawResource(resId);
//
//        InputStreamReader inputreader = new InputStreamReader(inputStream);
//        BufferedReader buffreader = new BufferedReader(inputreader);
//        String line;
//        StringBuilder text = new StringBuilder();
//
//        try {
//            while ((line = buffreader.readLine()) != null) {
//                text.append(line);
//                text.append('\n');
//            }
//        } catch (IOException e) {
//            return null;
//        }
//        return text.toString();
//    }

//    public void playChord(int i){
//        vvMain.setVideoURI(videoUri[i]);
//        vvMain.start();
//        imgBack.setImageResource(imageBackgroundIDs[i]);
//        startViaData(i);
//
//    }
//    public class ImageAdapter extends BaseAdapter {
//        private Context context;
//        private int itemBackground;
//        public ImageAdapter(Context c)
//        {
//            context = c;
//            // sets a grey background; wraps around the images
//            TypedArray a =mActivity.obtainStyledAttributes(R.styleable.MyGallery);
//            itemBackground = a.getResourceId(R.styleable.MyGallery_android_galleryItemBackground, 0);
//            a.recycle();
//        }
//        // returns the number of images
//        public int getCount() {
//            return imageIDs.length;
//        }
//        // returns the ID of an item
//        public Object getItem(int position) {
//            return position;
//        }
//        // returns the ID of an item
//        public long getItemId(int position) {
//            return position;
//        }
//        // returns an ImageView view
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ImageView imageView = new ImageView(context);
//            imageView.setImageResource(imageIDs[position]);
//            imageView.setLayoutParams(new Gallery.LayoutParams(300, 450));
//            imageView.setBackgroundResource(itemBackground);
//            return imageView;
//        }
//    }
//    // to send byte array data for playing
//    public void startViaData(int index) {
//        if(Config.bBlueToothActive == true) {
//            BluetoothActivity.mHandler.obtainMessage(BluetoothActivity.FRET, musicArray.get(index)).sendToTarget();
//        }
//    }
//    // to turn off light
//    public void stopViaData() {
//        if(Config.bBlueToothActive == true) {
//            byte[] array = new byte[]{0};
//            BluetoothActivity.mHandler.obtainMessage(BluetoothActivity.FRET, array).sendToTarget();
//        }
//    }



	/////////////////////////////////BlueToothConnection/////////////////////////
	static private class ConnectThread extends Thread {
		byte[] array;

		public ConnectThread(byte[] tmp) {
			array = tmp;
		}

		public void run() {
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				Util.startViaData(array);
			} catch (Exception connectException) {
				Log.i(BluetoothClass.tag, "connect failed");
				// Unable to connect; close the socket and get out
				try {
					BluetoothClass.mmSocket.close();
				} catch (IOException closeException) {
					Log.e(BluetoothClass.tag, "mmSocket.close");
				}
				return;
			}
			// Do work to manage the connection (in a separate thread)
			if (BluetoothClass.mHandler == null)
				Log.v("debug", "mHandler is null @ obtain message");
			else
				Log.v("debug", "mHandler is not null @ obtain message");
		}
	}

}


