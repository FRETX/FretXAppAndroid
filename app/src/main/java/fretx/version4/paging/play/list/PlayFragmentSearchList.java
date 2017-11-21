package fretx.version4.paging.play.list;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.fragment.YoutubeListener;
import fretx.version4.fragment.YoutubeTutorial;
import fretx.version4.fretxapi.song.SongCallback;
import fretx.version4.fretxapi.song.SongItem;
import fretx.version4.fretxapi.song.SongList;
import fretx.version4.paging.play.player.PlayYoutubeFragment;
import fretx.version4.paging.play.preview.PlayPreview;
import fretx.version4.utils.Preference;
import fretx.version4.utils.Prefs;
import fretx.version4.utils.bluetooth.BluetoothAnimator;
import fretx.version4.utils.firebase.Analytics;
import fretx.version4.utils.firebase.FirebaseConfig;

import static fretx.version4.activities.MainActivity.setGreyed;
import static fretx.version4.activities.MainActivity.setNonGreyed;

public class PlayFragmentSearchList extends Fragment implements SongCallback,
        SearchView.OnQueryTextListener, YoutubeListener {
    private static final String TAG = "KJKP6_PLAYFRAGMENT_LIST";
    private MainActivity mActivity;
    private final ArrayList<SongItem> rawData = new ArrayList<>();
    private final ArrayList<SongItem> filteredData = new ArrayList<>();
    private PlaySongGridViewAdapter adapter;
    private MenuItem searchItem;
    private MenuItem previewItem;
    private SongItem selectedItem;
    private boolean previewEnabled = true;
    private GridView listView;
    private Button retry;
    private ProgressBar progressBar;
    private String youtubeId = "";

    /*----------------------------------- LIFECYCLE ----------------------------------------------*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        Analytics.getInstance().logSelectEvent("TAB", "Play");
        previewEnabled = Preference.getInstance().isSongPreview();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.paging_play_searchlist, container, false);
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
                selectedItem = filteredData.get(position);

                if (Preference.getInstance().needPlayTutorial()) {
                    Log.d(TAG, "need to display video");
                    youtubeId = FirebaseConfig.getInstance().getPlayUrl();
                    Log.d(TAG, "video id: " + youtubeId);
                    startTutorial();
                } else {
                    start();
                }
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
    public void onResume() {
        super.onResume();
        BluetoothAnimator.getInstance().stringFall();
        updateMenu();
    }

    /*----------------------------------- OPTION MENU --------------------------------------------*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "");
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_search, menu);
        previewItem = menu.findItem(R.id.action_preview);
        searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        final SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        updateMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_preview:
                if(previewEnabled){
                    setGreyed(item);
                    previewEnabled = false;
                } else {
                    setNonGreyed(item);
                    previewEnabled = true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        filterList(query);
        searchItem.getActionView().clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filterList(newText);
        return true;
    }

    /*--------------------------------------- CALLBACKSS -----------------------------------------*/

    @Override
    public void onUpdate(boolean requesting, JSONArray index) {
        if (requesting) {
            Log.d(TAG, "requesting song list");
            retry.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else if (index == null){
            Log.d(TAG, "song list update failed");
            progressBar.setVisibility(View.INVISIBLE);
            retry.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "song list update succeeded");
            retry.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            refreshData();
        }
    }

    /*--------------------------------------- UTILS ----------------------------------------------*/

    private void refreshData() {
        rawData.clear();
        rawData.addAll(SongList.getSongItems());

        Collections.sort(rawData, new Comparator<SongItem>() {
            @Override
            public int compare(SongItem o1, SongItem o2) {
                if (o1 == null)
                    return 1;
                else if (o2 == null)
                    return -1;
                else
                    return o2.updated_at.compareTo(o1.updated_at);
            }
        });

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

    private void start() {
        if (previewEnabled)
            startSongPreview();
        else
            startSong();
    }

    private void startSongPreview() {
        //Launch exercise with sequence of chords
        final PlayPreview fragmentChordExercise = PlayPreview.newInstance(selectedItem);
        mActivity.fragNavController.pushFragment(fragmentChordExercise);
    }

    private void startSong() {
        final PlayYoutubeFragment fragment = PlayYoutubeFragment.newInstance(selectedItem);
        mActivity.fragNavController.pushFragment(fragment);
    }

    private void startTutorial() {
        if (youtubeId.isEmpty()) {
            start();
        } else {
            Log.d(TAG, "display the video");
            final YoutubeTutorial fragment = YoutubeTutorial.newInstance(this, youtubeId);
            mActivity.fragNavController.pushFragment(fragment);
        }
    }

    private void updateMenu() {
        if (previewItem != null) {
            if (previewEnabled) {
                setNonGreyed(previewItem);
            } else {
                setGreyed(previewItem);
            }
        }
    }

    @Override
    public void onVideoEnded() {
        final Prefs prefs = new Prefs.Builder().setPlayTutorial("false").build();
        Preference.getInstance().save(prefs);
        start();
    }
}