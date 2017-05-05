package fretx.version4.paging.play;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SearchView;

import java.util.ArrayList;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.fretxapi.SongItem;
import fretx.version4.fretxapi.SongList;
import fretx.version4.utils.bluetooth.BluetoothLE;

public class PlayFragmentSearchList extends Fragment {

    private final ArrayList<SongItem> rawData = new ArrayList<>();
    private final ArrayList<SongItem> filteredData = new ArrayList<>();
    private PlaySongGridViewAdapter adapter;
    private SearchView searchBox;
    private GridView listView;
    private final ProgressDialog dialog = new ProgressDialog(getActivity());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.paging_play_searchlist, container, false);
        searchBox = (SearchView) rootView.findViewById(R.id.svSongs);
        listView = (GridView) rootView.findViewById(R.id.lvSongList);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new PlaySongGridViewAdapter((MainActivity) getActivity(),
                R.layout.paging_play_searchlist_item, filteredData);
        listView.setAdapter(adapter);

        dialog.setTitle(getString(R.string.refreshing));
        dialog.setMessage(getString(R.string.please_wait));

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

        //// TODO: 05/05/17 check if this piece of code run on UI thread
        SongList.setListener(new SongList.Callback() {
            @Override
            public void onBusy() {
                dialog.show();
            }

            @Override public void onReady() {
                rawData.clear();
                for(int i = 0; i< SongList.length(); ++i) {
                    final SongItem item = SongList.getSongItem(i);
                    if (item.published) {
                        rawData.add(item);
                    }
                }

                filteredData.clear();
                filteredData.addAll(rawData);
                adapter.notifyDataSetChanged();

                dialog.hide();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        BluetoothLE.getInstance().clearMatrix();
    }

    /////////////////////////////////////// SEARCH LIST  ///////////////////////////////////////////
    public void filterList(String query) {
        filteredData.clear();
        if (query == null ) {
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