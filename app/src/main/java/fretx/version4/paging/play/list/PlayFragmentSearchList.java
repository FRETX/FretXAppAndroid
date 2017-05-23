package fretx.version4.paging.play.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;

import fretx.version4.Config;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.fretxapi.song.SongCallback;
import fretx.version4.fretxapi.song.SongItem;
import fretx.version4.fretxapi.song.SongList;
import fretx.version4.paging.play.player.PlayOfflinePlayerFragment;
import fretx.version4.paging.play.player.PlayYoutubeFragment;
import fretx.version4.paging.play.preview.PlayPreview;
import fretx.version4.utils.bluetooth.BluetoothLE;
import fretx.version4.utils.firebase.Analytics;
import rocks.fretx.audioprocessing.Chord;

public class PlayFragmentSearchList extends Fragment implements SongCallback {
    private static final String TAG = "KJKP6_PLAYFRAGMENT_LIST";
    private MainActivity mActivity;
    private final ArrayList<SongItem> rawData = new ArrayList<>();
    private final ArrayList<SongItem> filteredData = new ArrayList<>();
    private PlaySongGridViewAdapter adapter;

    private SearchView searchBox;
    private GridView listView;
    private Button retry;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        Analytics.getInstance().logSelectEvent("TAB", "Play");
        BluetoothLE.getInstance().clearMatrix();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.paging_play_searchlist, container, false);
        searchBox = (SearchView) rootView.findViewById(R.id.svSongs);
        listView = (GridView) rootView.findViewById(R.id.lvSongList);
        retry = (Button) rootView.findViewById(R.id.retry);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress);

        retry.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new PlaySongGridViewAdapter((MainActivity) getActivity(),
                R.layout.paging_play_searchlist_item, filteredData);
        listView.setAdapter(adapter);
        refreshData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SongItem item = filteredData.get(position);

                searchBox.setQuery("", true);
                searchBox.clearFocus();

                if(mActivity.previewEnabled){
                    startSongPreview(item);
                } else {
                    startSong(item);
                }
            }
        });

        searchBox.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                searchBox.setIconified(false);
            }
        });
        searchBox.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                filterList(query);
                return false;
            }
        });

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retry.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                SongList.getIndexFromServer();
            }
        });

        SongList.setListener(this);
    }

    @Override
    public void onUpdate(boolean requesting, JSONArray index) {
        if (requesting) {
            retry.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else if (index == null){
            progressBar.setVisibility(View.INVISIBLE);
            retry.setVisibility(View.VISIBLE);
        } else {
            retry.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            refreshData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BluetoothLE.getInstance().clearMatrix();
    }

    private void refreshData() {
        rawData.clear();
        final int length = SongList.length();
        for(int i = 0; i < length; ++i) {
            final SongItem item = SongList.getSongItem(i);
            if (item != null && item.published) {
                rawData.add(item);
            }
        }

        filteredData.clear();
        filteredData.addAll(rawData);
        adapter.notifyDataSetChanged();
    }

    public void filterList(String query) {
        filteredData.clear();
        if (query == null) {
            filteredData.addAll(rawData);
        } else {
            final String lowercaseQuery = query.toLowerCase();
            for (SongItem item: rawData) {
                final String title = item.title.toLowerCase();
                if (title.contains(lowercaseQuery)) {
                    filteredData.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void startSongPreview(SongItem item) {
        //Launch exercise with sequence of chords
        ArrayList<Chord> chords = item.getChords();

        if (chords.size() < 1) {
            Toast.makeText(mActivity, "No chord data found for this song", Toast.LENGTH_SHORT).show();
            return;
        }

        PlayPreview fragmentChordExercise = new PlayPreview();
        fragmentChordExercise.setChords(chords);
        fragmentChordExercise.setSong(item);
        mActivity.fragNavController.pushFragment(fragmentChordExercise);
    }

    private void startSong(SongItem item) {
        boolean loadOfflinePlayer = false;
        if (Config.useOfflinePlayer) {
            String fileName = "fretx" + item.youtube_id.toLowerCase().replace("-", "_");
            int resourceIdentifier = getContext().getResources().getIdentifier(fileName, "raw", getContext().getPackageName());
            if(resourceIdentifier != 0){
                loadOfflinePlayer = true;
            }
        }
        if (loadOfflinePlayer) {
            PlayOfflinePlayerFragment offlinePlayerFragment = new PlayOfflinePlayerFragment();
            offlinePlayerFragment.setSong(item);
            mActivity.fragNavController.pushFragment(offlinePlayerFragment);
        } else {
            PlayYoutubeFragment youtubeFragment = new PlayYoutubeFragment();
            youtubeFragment.setSong(item);
            mActivity.fragNavController.pushFragment(youtubeFragment);
        }
    }
}