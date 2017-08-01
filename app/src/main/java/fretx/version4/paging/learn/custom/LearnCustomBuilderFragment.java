package fretx.version4.paging.learn.custom;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import android.content.Context;
import android.widget.ArrayAdapter;

import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.view.FretboardView;
import fretx.version4.R;
import fretx.version4.activities.BaseActivity;
import fretx.version4.activities.MainActivity;
import fretx.version4.utils.firebase.Analytics;
import rocks.fretx.audioprocessing.Chord;

/**
 * Created by onurb_000 on 15/12/16.
 */

public class LearnCustomBuilderFragment extends Fragment {
    private static final String DEFAULT_SEQUENCE_NAME = "Unsaved Exercise";

	//view
	FretboardView fretboardView;
	TextView chordText;
    Spinner spinner;
    ListView listView;
	Button add;
	Button play;
    ImageView delete;
    ImageView save;

	//chords
    CustomListAdapter listAdapter;
    CustomSpinnerAdapter spinnerAdapter;
	Chord currentChord;
	ArrayList<Sequence> sequences;

	public LearnCustomBuilderFragment(){}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.getInstance().logSelectEvent("EXERCISE", "Custom Chord");
		sequences = LearnCustomBuilderJson.load(getContext());
		sequences.add(0, new Sequence(null, new ArrayList<Chord>()));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.paging_learn_custom_builder, container, false);
		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
		chordText = (TextView) rootView.findViewById(R.id.textChord);
		add = (Button) rootView.findViewById(R.id.add);
		play = (Button) rootView.findViewById(R.id.play);
        save = (ImageView) rootView.findViewById(R.id.save);
        delete = (ImageView) rootView.findViewById(R.id.delete);
        spinner = (Spinner) rootView.findViewById(R.id.spinner);
        listView = (ListView) rootView.findViewById(R.id.listview);
		return  rootView;
	}

	@Override
	public void onViewCreated(View v, Bundle b){
        populateChordPicker();

        spinnerAdapter = new CustomSpinnerAdapter(getActivity(), sequences);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(0);
        listAdapter = new CustomListAdapter(getActivity(), sequences.get(0));
        listView.setAdapter(listAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*
                if (selected != null) {}
                */
                final Sequence selected = (Sequence)spinner.getSelectedItem();
                listAdapter.clear();
                listAdapter.addAll(selected.getChords());
                listAdapter.notifyDataSetChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                listAdapter.clear();
                listAdapter.notifyDataSetChanged();
            }
        });

		add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(currentChord == null)
					return;
                final Sequence selected = (Sequence)spinner.getSelectedItem();
                selected.addChord(currentChord);
                listAdapter.clear();
                listAdapter.addAll(selected.getChords());
                listAdapter.notifyDataSetChanged();
			}
		});

		play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                ArrayList<Chord> chords = ((Sequence) spinner.getSelectedItem()).getChords();
                if(chords.size() < 1)
                    return;
                LearnCustomExercise fragmentChordExercise = new LearnCustomExercise();
                fragmentChordExercise.setChords(chords);
                ((MainActivity)getActivity()).fragNavController.pushFragment(fragmentChordExercise);
            }
		});

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sequence selected = (Sequence) spinner.getSelectedItem();
                if (selected != null) {
                    String name = selected.getName();
                    if (selected.getName() == null) {
                        NameSelectionAlertDialogBuilder().show();
                    } else {
                        final ArrayList<Sequence> save = LearnCustomBuilderJson.load(getContext());
                        for (int i = 0; i < save.size(); ++i) {
                            if (save.get(i).getName().equals(name)) {
                                save.get(i).setChords(selected.getChords());
                                LearnCustomBuilderJson.save(getContext(), save);
                                break;
                            }
                        }
                    }
                }
            }
        });
        save.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                NameSelectionAlertDialogBuilder().show();
                return false;
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinner.getSelectedItem() != null) {
                    deleteConfirmationAlertDialogBuilder().show();
                }
            }
        });
	}

    /* CHORD PICKER */
	private void updateCurrentChord(String root , String type){
		currentChord = new Chord(root,type);
		fretboardView.setFretboardPositions(currentChord.getFingerPositions());
		chordText.setText(currentChord.toString());
        Bluetooth.getInstance().setMatrix(currentChord);
	}

	private TextView populateChordPickerLine(String[] contents, @IdRes int idRes,
										 View.OnClickListener onClickListener) {
		LinearLayout linearLayout = (LinearLayout) BaseActivity.getActivity().findViewById(idRes);
		TextView tmpTextView;
		for (String str : contents) {
			tmpTextView = new TextView(BaseActivity.getActivity());
			tmpTextView.setText(str);
			tmpTextView.setTextSize(26);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			linearLayout.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setBackgroundColor(getResources().getColor(R.color.primary));
			tmpTextView.setTextColor(getResources().getColor(R.color.tertiaryText));
			tmpTextView.setOnClickListener(onClickListener);
		}
		TextView initial = (TextView) linearLayout.getChildAt(0);
		initial.setBackgroundResource(R.drawable.picker_text_background);
		initial.setTextColor(BaseActivity.getActivity().getResources().getColor(R.color.tertiaryText));
		return initial;
	}

	private void populateChordPicker(){
		String[] rootNotes = {"C","C#","D","Eb","E","F","F#","G","G#","A","Bb","B"};
		String [] chordTypes = {"maj","m","5","maj7","m7","sus2","sus4","dim","dim7","aug",};

		TextView initialRoot = populateChordPickerLine(rootNotes, R.id.chordPickerRootNoteView,
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						LinearLayout layout = (LinearLayout) BaseActivity.getActivity().findViewById(R.id.chordPickerRootNoteView);
						for (int i = 0; i < layout.getChildCount(); i++) {
							View v = layout.getChildAt(i);
							v.setBackgroundResource(0);
							v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
						}
						view.setBackgroundResource(R.drawable.picker_text_background);
						updateCurrentChord(((TextView) view).getText().toString(),currentChord.getType());
					}
				});

		TextView initialType = populateChordPickerLine(chordTypes, R.id.chordPickerTypeView,
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						LinearLayout layout = (LinearLayout) BaseActivity.getActivity().findViewById(R.id.chordPickerTypeView);
						for (int i = 0; i < layout.getChildCount(); i++) {
							View v = layout.getChildAt(i);
							v.setBackgroundResource(0);
							v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
						}
						view.setBackgroundResource(R.drawable.picker_text_background);
						updateCurrentChord(currentChord.getRoot(), ((TextView) view).getText().toString());
					}
				});

		updateCurrentChord(initialRoot.getText().toString(),initialType.getText().toString());
	}

    /* DIALOGS */
    private ArrayList<Sequence> isSequenceSavable(String name) {
        ArrayList<Sequence> save = LearnCustomBuilderJson.load(getContext());
        for (int i = 0; i < save.size(); ++i) {
            if (save.get(i).getName().equals(name)) {
                return null;
            }
        }
        return save;
    }

    private Dialog NameSelectionAlertDialogBuilder() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.paging_learn_custom_builder_dialog_name);

        final EditText nameEditText = (EditText) dialog.findViewById(R.id.name);
        final Button save = (Button) dialog.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Sequence> save;
                String name = nameEditText.getText().toString();
                if (name.equals("")) {
                    nameEditText.setError("Specify a name");
                } else if ((save = isSequenceSavable(name)) == null) {
                    nameEditText.setError("Name must be unique");
                } else {
                    final Sequence sequence = (Sequence) spinner.getSelectedItem();
                    Sequence newSequence = new Sequence(name, new ArrayList<>(sequence.getChords()));
                    sequences.add(newSequence);
                    save.add(newSequence);
                    if (sequence.getName() == null)
                        sequences.remove(sequence);
                    LearnCustomBuilderJson.save(getContext(), save);
                    spinner.setSelection(sequences.size() - 1);
                    spinnerAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });
        return dialog;
    }

    private AlertDialog deleteConfirmationAlertDialogBuilder() {
        final Sequence sequence = (Sequence) spinner.getSelectedItem();
        final String name = sequence.getName();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage("Are you sure you want to delete " + (name == null ? DEFAULT_SEQUENCE_NAME : name))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //remove in local cache
                        if (name != null) {
                            ArrayList<Sequence> save = LearnCustomBuilderJson.load(getContext());
                            for (int i = 0; i < save.size(); ++i) {
                                if (save.get(i).getName().equals(sequence.getName())) {
                                    save.remove(i);
                                    LearnCustomBuilderJson.save(getContext(), save);
                                    break;
                                }
                            }
                        }
                        //remove in spinner
                        spinnerAdapter.remove(sequence);
                        if(spinnerAdapter.getCount() == 0){
                            sequences.add(0, new Sequence(null, new ArrayList<Chord>()));
                            spinner.setSelection(0);
                        } else {
                            spinner.setSelection(sequences.size() - 1);
                        }
                        spinnerAdapter.notifyDataSetChanged();
                        //update list view
                        Sequence selected = (Sequence)spinner.getSelectedItem();
                        listAdapter.clear();
                        if (selected != null) {
                            listAdapter.addAll(selected.getChords());
                        }
                        listAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }

    /* CUSTOM ADAPTERS */
    private class CustomListAdapter extends ArrayAdapter<Chord> {
        private final Context context;
        private final Sequence sequence;

        CustomListAdapter(Context context, Sequence sequence) {
            super(context, R.layout.paging_learn_custom_builder_dialog_item, sequence.getChords());
            this.context = context;
            this.sequence = sequence;
        }

        @Override
        @NonNull
        public View getView(final int position, final View convertView, @NonNull ViewGroup parent) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View rowView = inflater.inflate(R.layout.paging_learn_custom_builder_dialog_item, parent, false);

            TextView textViewName = (TextView) rowView.findViewById(R.id.chordNameTextview);
            textViewName.setText(getItem(position).toString());
            ImageView image = (ImageView) rowView.findViewById(R.id.deleteImageView);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(getItem(position));
                    ((Sequence) spinner.getSelectedItem()).getChords().remove(position);
                }
            });
            return rowView;
        }
    }

    private class CustomSpinnerAdapter extends ArrayAdapter<Sequence> {
        private static final String DEFAULT_SEQUENCE_NAME = "Unsaved Exercise";

        CustomSpinnerAdapter(Context context, ArrayList<Sequence> values) {
            super(context, android.R.layout.simple_spinner_item, values);
        }

        @Override
        @NonNull
        public TextView getView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            String name = getItem(position).getName();
            v.setText(name == null ? DEFAULT_SEQUENCE_NAME : name);
            return v;
        }

        @Override
        @NonNull
        public TextView getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            String name = getItem(position).getName();
            v.setText(name == null ? DEFAULT_SEQUENCE_NAME : name);
            return v;
        }
    }
}