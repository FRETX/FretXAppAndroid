package fretx.version4.paging.play;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;

import java.util.ArrayList;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.fretxapi.SongItem;
import fretx.version4.fretxapi.Songlist;
import fretx.version4.utils.bluetooth.BluetoothLE;

public class PlayFragmentSearchList extends Fragment {

    public  ArrayList<SongItem> mainData = new ArrayList<>();
    public  SearchView          searchBox;
    public  GridView            listView;

    private MainActivity        context;
    private View                rootView;
    private ImageView           refreshBtn;
    private ProgressDialog      dialog;

    ///////////////////////////////////// LIFECYCLE EVENTS /////////////////////////////////////////////////////////////////

    @Override public void onResume() { super.onResume();
        BluetoothLE.getInstance().clearMatrix(); }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflateView(inflater, container);
        initVars();
        setEventListeners();
        setListData(mainData);
        return rootView;
    }



    ///////////////////////////////////// LIFECYCLE EVENTS /////////////////////////////////////////////////////////////////


    ////////////////////////////////////////// SETUP ///////////////////////////////////////////////////////////////////////

    private void initVars() {
        mainData   = new ArrayList<>();
        context    = (MainActivity) getActivity();
        searchBox  = (SearchView)   rootView.findViewById(R.id.svSongs);
//        refreshBtn = (ImageView)    rootView.findViewById(R.id.fresh);
        listView   = (GridView)     rootView.findViewById(R.id.lvSongList);
    }

    private void setEventListeners() {
        searchBox.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                searchBox.setIconified(false);
            }
        });
        searchBox.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { filterList(query); return false; }
            @Override public boolean onQueryTextChange(String query) { filterList(query); return false; }
        });

//        refreshBtn.setOnClickListener( new View.OnClickListener() {
//            @Override public void onClick(View view) {
//                if( Network.isConnected() ) { Songlist.initialize(); }
//                else                        { Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show(); }
//            }
//        });

        Songlist.setListener( new Songlist.Callback() {
            @Override public void onBusy()  { showBusy(); }
            @Override public void onReady() { initData(); }
        });
    }

    private void initData() {
        mainData.clear();
        for(int i = 0; i< Songlist.length(); i++) { mainData.add(Songlist.getSongItem(i)); }
        setListData(mainData);
        hideBusy();
    }

    private View inflateView(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(R.layout.paging_play_searchlist, container, false);
        return rootView;
    }

    ////////////////////////////////////////// SETUP ///////////////////////////////////////////////////////////////////////


    /////////////////////////////////////// SEARCH LIST  ///////////////////////////////////////////////////////////////////

    public void filterList(String query) {
        //lvListNews.setVisibility(View.VISIBLE);
        if (query == null ) { setListData(mainData); return; }

        ArrayList<SongItem> filterList = new ArrayList<>();
        for ( int i = 0; i < mainData.size(); i++ ) {
            String title = mainData.get(i).title.toLowerCase();
            if( title.contains(query.toLowerCase()) ) { filterList.add(mainData.get(i)); }
        }
        setListData(filterList);
    }

    public void setListData( ArrayList<SongItem> data ) {
        listView.setAdapter( new PlaySongGridViewAdapter( context, R.layout.paging_play_searchlist_item, data) );
    }

    /////////////////////////////////////// SEARCH LIST  ///////////////////////////////////////////////////////////////////

    public void showBusy() { if(getActivity() != null ) dialog = ProgressDialog.show(context, getString(R.string.refreshing), getString(R.string.please_wait)); }
    public void hideBusy() { if( dialog != null ) dialog.dismiss(); }

}