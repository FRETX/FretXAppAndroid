package fretx.version4.paging.learn;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fretx.version4.R;
import rocks.fretx.audioprocessing.Chord;

/**
 * Created by pandor on 3/7/17.
 */

public class LearnCustomChordExerciseDialog extends DialogFragment
{
    private Dialog dialog;
    ArrayList<Sequence> sequences;
    SpinnerSequenceArrayAdapter spinnerAdapter;
    ListViewSequenceArrayAdapter listViewAdapter;
    int unsavedSequencePosition = -1;
    private final String DEFAULT_NAME = "New Exercise";
    private final String SEQUENCE_FILENAME = "sequences";

    public static LearnCustomChordExerciseDialog newInstance(ArrayList<Chord> chords) {
        LearnCustomChordExerciseDialog dialog = new LearnCustomChordExerciseDialog();
        Bundle args = new Bundle();
        //args.putInt("title", title); MODIFY CHORDS TO BE PARCELABLE
        dialog.setArguments(args);
        return dialog;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.chord_custom_sequence_dialog);

        sequences = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ArrayList<Chord> chords = new ArrayList<>();
            chords.add(new Chord(Chord.ALL_ROOT_NOTES[i], "maj"));
            chords.add(new Chord(Chord.ALL_ROOT_NOTES[i], "maj"));
            chords.add(new Chord(Chord.ALL_ROOT_NOTES[i], "maj"));
            chords.add(new Chord(Chord.ALL_ROOT_NOTES[i], "maj"));
            chords.add(new Chord(Chord.ALL_ROOT_NOTES[i], "maj"));
            sequences.add(new Sequence("sequence" + i, chords));
        }

        //if (chordSequence.size() > 0) {
        //    sequences.add(0, new Sequence(DEFAULT_NAME, chordSequence));
        //    unsavedSequencePosition = 0;
        //}

        final Spinner spinner = (Spinner) dialog.findViewById(R.id.sequence_selection);
        spinnerAdapter = new SpinnerSequenceArrayAdapter(getActivity(), sequences);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        final ListView listview = (ListView) dialog.findViewById(R.id.chords_listview);
        listViewAdapter = new ListViewSequenceArrayAdapter(getActivity(), new ArrayList<Chord>());
        listview.setAdapter(listViewAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Sequence selected = (Sequence)spinner.getSelectedItem();
                listViewAdapter.setSpinnerPosition(spinner.getSelectedItemPosition());
                listViewAdapter.clear();
                listViewAdapter.addAll(selected.getChords());
                listViewAdapter.notifyDataSetChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                listViewAdapter.clear();
                listViewAdapter.notifyDataSetChanged();
            }
        });

        ImageButton deleteSequence = (ImageButton) dialog.findViewById(R.id.delete_button);
        deleteSequence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sequence toDelete = (Sequence)spinner.getSelectedItem();
                if (toDelete != null) {
                    String name = toDelete.getName();
                    Toast.makeText(getContext(), name, Toast.LENGTH_SHORT).show();
                    spinnerAdapter.remove(toDelete);
                    spinnerAdapter.notifyDataSetChanged();
                }
            }
        });

        Button save = (Button) dialog.findViewById(R.id.save_button);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Save", Toast.LENGTH_SHORT).show();
            }
        });

        Button play = (Button) dialog.findViewById(R.id.play_button);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Play", Toast.LENGTH_SHORT).show();
            }
        });

        return dialog;
    }

    public class SpinnerSequenceArrayAdapter extends ArrayAdapter<Sequence> {
        private final Context context;
        private final ArrayList<Sequence> values;

        SpinnerSequenceArrayAdapter(Context context, ArrayList<Sequence> values) {
            super(context, android.R.layout.simple_spinner_item, values);
            this.context = context;
            this.values = values;
        }

        @Override
        @NonNull
        public TextView getView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            v.setText(values.get(position).getName());
            return v;
        }

        @Override
        @NonNull
        public TextView getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            v.setText(values.get(position).getName());
            return v;
        }
    }

    public class ListViewSequenceArrayAdapter extends ArrayAdapter<Chord> {
        private final Context context;
        private final ArrayList<Chord> values;
        private int spinnerPosition;

        ListViewSequenceArrayAdapter(Context context, ArrayList<Chord> values) {
            super(context, R.layout.chord_custom_sequence_dialog_item, values);
            this.context = context;
            this.values = values;
        }

        void setSpinnerPosition(int position) {
            spinnerPosition = position;
        }

        @Override
        @NonNull
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.chord_custom_sequence_dialog_item, parent, false);
            TextView textViewName = (TextView) rowView.findViewById(R.id.chordNameTextview);
            textViewName.setText(values.get(position).toString());
            ImageView image = (ImageView) rowView.findViewById(R.id.deleteImageView);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Click on item " + position, Toast.LENGTH_SHORT).show();
                    sequences.get(spinnerPosition).removeChord(position);
                    listViewAdapter.clear();
                    listViewAdapter.addAll(sequences.get(spinnerPosition).getChords());
                    listViewAdapter.notifyDataSetChanged();
                }
            });
            return rowView;
        }
    }

    private class Sequence {
        private String name;
        private ArrayList<Chord> chords;

        Sequence(String name, ArrayList<Chord> chords) {
            this.name = name;
            this.chords = chords;
        }

        public String getName() {
            return name;
        }

        public ArrayList<Chord> getChords() {
            return chords;
        }

        void removeChord(int position) {
            chords.remove(position);
        }
    }
}
