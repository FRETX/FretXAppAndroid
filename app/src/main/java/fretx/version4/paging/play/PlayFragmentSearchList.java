package fretx.version4.paging.play;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import org.json.JSONArray;

import java.util.ArrayList;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.fretxapi.song.SongCallback;
import fretx.version4.fretxapi.song.SongItem;
import fretx.version4.fretxapi.song.SongList;
import fretx.version4.utils.bluetooth.BluetoothLE;

public class PlayFragmentSearchList extends Fragment {
    private static final String TAG = "KJKP6_PLAYFRAGMENT_LIST";
    private final ArrayList<SongItem> rawData = new ArrayList<>();
    private final ArrayList<SongItem> filteredData = new ArrayList<>();
    //// TODO: 05/05/17 handle update on query
    private PlaySongGridViewAdapter adapter;

    private SearchView searchBox;
    private GridView listView;
    private Button retry;
    private ProgressBar progressBar;

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

        SongList.setListener(new SongCallback() {
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
        });

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retry.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                SongList.getIndexFromServer();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        BluetoothLE.getInstance().clearMatrix();
    }

    private boolean refreshData() {
        rawData.clear();
        for(int i = 0; i< SongList.length(); ++i) {
            final SongItem item = SongList.getSongItem(i);
            if (item != null && item.published) {
                rawData.add(item);
            }
        }

        filteredData.clear();
        filteredData.addAll(rawData);
        adapter.notifyDataSetChanged();
        return true;
    }

    //// TODO: 05/05/17 handle onUpdate with a search on going
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
}