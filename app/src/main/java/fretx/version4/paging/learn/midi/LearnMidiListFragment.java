package fretx.version4.paging.learn.midi;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.EXTRA_ALLOW_MULTIPLE;

public class LearnMidiListFragment extends Fragment {
	private static final String TAG = "KJKP6_MIDI_LIST";
    private static final int REQUEST_CODE = 1;
    private final ArrayList<File> files = new ArrayList<>();
    private GridView listView;
    private TextView intro;
    private MidiGridViewAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.paging_learn_midi_list, container, false);

        listView = (GridView) rootView.findViewById(R.id.list);
        intro = (TextView) rootView.findViewById(R.id.intro);
        adapter = new MidiGridViewAdapter((MainActivity) getActivity(),
                R.layout.paging_learn_midi_list_item, files);
        listView.setAdapter(adapter);
        updateFileList();

        final FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.plus);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/midi");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(EXTRA_ALLOW_MULTIPLE, "true");

                try {
                    startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), REQUEST_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return rootView;
	}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Fragment fragment = MidiExercise.newInstance(files.get(position));
                ((MainActivity) getActivity()).fragNavController.pushFragment(fragment);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    String filename;
                    Uri uri = data.getData();
                    Log.d(TAG, "result: " + uri.toString());

                    String mimeType = getActivity().getContentResolver().getType(uri);
                    if (mimeType == null) {
                        Log.d(TAG, "MINETYPE IS NULL");
                        filename = "";
                    } else {
                        Cursor returnCursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        returnCursor.moveToFirst();
                        filename = returnCursor.getString(nameIndex);
                        Log.d(TAG, "FILENAME IS: " + filename);
                    }
                    String sourcePath = getActivity().getExternalFilesDir(null).toString();
                    Log.d(TAG, "SOURCE PATH IS: " + sourcePath);
                    try {
                        copyFile(new File(sourcePath + "/" + filename), uri, getActivity());
                        updateFileList();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void copyFile(File dest, Uri uri, Context context) throws IOException {
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFileList() {
        String path = getActivity().getExternalFilesDir(null).toString();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        files.clear();
        files.addAll(Arrays.asList(directory.listFiles()));
        Log.d(TAG, "Size: "+ files.size());
        adapter.notifyDataSetChanged();
        if (files.isEmpty()) {
            intro.setVisibility(View.VISIBLE);
        } else {
            intro.setVisibility(View.GONE);
        }
    }
}