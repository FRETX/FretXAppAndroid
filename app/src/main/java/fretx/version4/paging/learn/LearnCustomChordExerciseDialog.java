package fretx.version4.paging.learn;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
    public static final String DEFAULT_SEQUENCE_NAME = "New Exercise";
    private SpinnerSequenceArrayAdapter spinnerAdapter;
    private ListViewSequenceArrayAdapter listViewAdapter;
    private int unsavedCurrentSequencePosition = -1;

    public static LearnCustomChordExerciseDialog newInstance(ArrayList<Chord> chords) {
        LearnCustomChordExerciseDialog dialog = new LearnCustomChordExerciseDialog();
        Bundle args = new Bundle();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.chord_custom_sequence_dialog);

        //retrieved data from internal memory
        ArrayList<Sequence> sequences = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ArrayList<Chord> chords = new ArrayList<>();
            chords.add(new Chord(Chord.ALL_ROOT_NOTES[i], "maj"));
            chords.add(new Chord(Chord.ALL_ROOT_NOTES[i], "maj"));
            chords.add(new Chord(Chord.ALL_ROOT_NOTES[i], "maj"));
            chords.add(new Chord(Chord.ALL_ROOT_NOTES[i], "maj"));
            chords.add(new Chord(Chord.ALL_ROOT_NOTES[i], "maj"));
            sequences.add(new Sequence("sequence " + i, chords));
        }

        //current sequence
        ArrayList<Chord> chords = new ArrayList<>();
        chords.add(new Chord(Chord.ALL_ROOT_NOTES[7], "maj"));
        chords.add(new Chord(Chord.ALL_ROOT_NOTES[7], "maj"));
        chords.add(new Chord(Chord.ALL_ROOT_NOTES[7], "maj"));
        chords.add(new Chord(Chord.ALL_ROOT_NOTES[7], "maj"));
        chords.add(new Chord(Chord.ALL_ROOT_NOTES[7], "maj"));
        sequences.add(0, new Sequence(DEFAULT_SEQUENCE_NAME, chords));
        unsavedCurrentSequencePosition = 0;

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
                listViewAdapter.clear();
                listViewAdapter.addAll(new ArrayList<>(selected.getChords()));
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
                    deleteConfirmationAlertDialogBuilder(spinner).show();
                }
            }
        });

        Button save = (Button) dialog.findViewById(R.id.save_button);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Save", Toast.LENGTH_SHORT).show();
                Sequence toSave = (Sequence)spinner.getSelectedItem();
                if (toSave != null) {
                    if (spinner.getSelectedItemPosition() == unsavedCurrentSequencePosition) {
                        NameSelectionAlertDialogBuilder(spinner).show();
                    } else {
                        toSave.setChords(listViewAdapter.getModifiedChords());
                    }
                }
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

    private AlertDialog deleteConfirmationAlertDialogBuilder(final Spinner spinner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you want to delete " + ((Sequence)spinner.getSelectedItem()).getName())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (spinner.getSelectedItemPosition() == unsavedCurrentSequencePosition) {
                            unsavedCurrentSequencePosition = -1;
                        }
                        String name = ((Sequence)spinner.getSelectedItem()).getName();
                        Toast.makeText(getContext(), name, Toast.LENGTH_SHORT).show();
                        spinnerAdapter.remove((Sequence)spinner.getSelectedItem());
                        spinnerAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }

    private Dialog NameSelectionAlertDialogBuilder(final Spinner spinner) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.chord_custom_sequence_name_dialog);

        final EditText nameEditText = (EditText) dialog.findViewById(R.id.name);
        final Button save = (Button) dialog.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                if (name.equals("")) {
                    nameEditText.setError("Specify a name");
                } else {
                    Sequence toSave = (Sequence)spinner.getSelectedItem();
                    toSave.setName(nameEditText.getText().toString());
                    unsavedCurrentSequencePosition = -1;
                    dialog.dismiss();
                }
            }
        });
        return dialog;
    }

    private class SpinnerSequenceArrayAdapter extends ArrayAdapter<Sequence> {
        private final ArrayList<Sequence> values;

        SpinnerSequenceArrayAdapter(Context context, ArrayList<Sequence> values) {
            super(context, android.R.layout.simple_spinner_item, values);
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

    private class ListViewSequenceArrayAdapter extends ArrayAdapter<Chord> {
        private final Context context;
        private final ArrayList<Chord> chords;

        ListViewSequenceArrayAdapter(Context context, ArrayList<Chord> chords) {
            super(context, R.layout.chord_custom_sequence_dialog_item, chords);
            this.context = context;
            this.chords = chords;
        }

        @Override
        @NonNull
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.chord_custom_sequence_dialog_item, parent, false);
            TextView textViewName = (TextView) rowView.findViewById(R.id.chordNameTextview);
            textViewName.setText(chords.get(position).toString());
            ImageView image = (ImageView) rowView.findViewById(R.id.deleteImageView);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Click on item " + position, Toast.LENGTH_SHORT).show();
                    chords.remove(position);
                    notifyDataSetChanged();
                }
            });
            return rowView;
        }

        ArrayList<Chord> getModifiedChords() {
            return chords;
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

        public void setChords (ArrayList<Chord> chords) {
            this.chords = new ArrayList<>(chords);
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
