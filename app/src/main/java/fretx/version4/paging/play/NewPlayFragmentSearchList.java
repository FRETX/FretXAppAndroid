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
import android.widget.Toast;

import java.util.ArrayList;

import fretx.version4.Config;
import fretx.version4.Constants;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.Util;
import fretx.version4.fretxapi.Network;
import fretx.version4.fretxapi.SongItem;
import fretx.version4.fretxapi.Songlist;

public class NewPlayFragmentSearchList extends Fragment implements SearchView.OnQueryTextListener {

    private MainActivity mActivity;

    private View       rootView   = null;
    public  SearchView svNews     = null;
    private ImageView  refresh    = null;
    public  GridView   lvListNews = null;
    private ProgressDialog dialog;

    public ArrayList<SongItem> mainData;
    public ArrayList<SongItem> Data;

    public NewPlayFragmentSearchList(){}

    private void getGuiReferences() {
        svNews     = (SearchView) rootView.findViewById(R.id.svSongs);
        refresh    = (ImageView)  rootView.findViewById(R.id.fresh);
        lvListNews = (GridView)   rootView.findViewById(R.id.lvSongList);
    }

    private void setGuiEventListeners() {
        svNews.setOnQueryTextListener(this);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Network.isConnected()) { initData(); }
                else                      { Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show(); }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (MainActivity) getActivity();
        rootView  = inflater.inflate(R.layout.play_fragment_search_list, container, false);

        getGuiReferences();
        setGuiEventListeners();

        if (Constants.refreshed) {
            Data = Constants.savedData;
            lvListNews.setAdapter(new CustomGridViewAdapter(mActivity, R.layout.play_fragment_search_list_row_item, Data));
            return rootView;
        }

        if( ! Network.isConnected() ) {
            Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            return rootView;
        }

        mainData = new ArrayList<SongItem>();
        lvListNews.setAdapter(new CustomGridViewAdapter(mActivity, R.layout.play_fragment_search_list_row_item, mainData));
        //initData();
        Constants.refreshed = true;

        Songlist.setListener(new Songlist.Callback() {
            @Override public void onBusy()  { showBusy(); }
            @Override public void onReady() { initData(); }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        stop_led();
    }

    public void initData(){
        for(int i = 0; i< Songlist.length(); i++) {
            mainData.add(Songlist.getSongItem(i));
        }
        lvListNews.setAdapter(new CustomGridViewAdapter(mActivity, R.layout.play_fragment_search_list_row_item, mainData));
        hideBusy();

        //mainData = new ArrayList<SongItem>();
        //String accessFolder = Util.checkS3Access(mActivity);
        //input = accessFolder;
        //new GetFileListTask().execute(accessFolder);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        lvListNews.setVisibility(View.VISIBLE);
        if (!query.equals(null)){
            ArrayList<SongItem> arrResultTemp = new ArrayList<SongItem>();
            for (int i = 0; i < mainData.size(); i ++){
                if(mainData.get(i).songName.toLowerCase().contains(query.toLowerCase())){
                    arrResultTemp.add(mainData.get(i));
                }
                lvListNews.setAdapter(new CustomGridViewAdapter(mActivity,R.layout.play_fragment_search_list_row_item, arrResultTemp));
            }
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        lvListNews.setVisibility(View.VISIBLE);
        if (newText.equals(null)){
            lvListNews.setAdapter(new CustomGridViewAdapter(mActivity,R.layout.play_fragment_search_list_row_item, mainData));
        }else{
            ArrayList<SongItem> arrResultTemp = new ArrayList<SongItem>();
            for (int i = 0; i < mainData.size(); i ++){
                if(mainData.get(i).songName.toLowerCase().contains(newText.toLowerCase())) {
                    arrResultTemp.add(mainData.get(i));
                }
                lvListNews.setAdapter(new CustomGridViewAdapter(mActivity,R.layout.play_fragment_search_list_row_item, arrResultTemp));
            }
        }
        return false;
    }

    public void stop_led() { if(Config.bBlueToothActive) { Util.stopViaData(); } }
    public void showBusy() { dialog = ProgressDialog.show(mActivity, getString(R.string.refreshing), getString(R.string.please_wait)); }
    public void hideBusy() { if( dialog != null ) dialog.dismiss(); }
}