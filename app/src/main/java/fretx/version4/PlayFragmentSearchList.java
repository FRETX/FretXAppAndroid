package fretx.version4;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class PlayFragmentSearchList extends Fragment implements SearchView.OnQueryTextListener {

    private MainActivity mActivity;

    private View rootView = null;
    private ImageView refresh;

    public SearchView svNews = null;

    public ConnectivityManager connectivityManager;
    public boolean state;
    public Context contexts;

    public GridView lvListNews = null;
    public ArrayList<SongItem> mainData;
    public ArrayList<SongItem> Data;
    private AmazonS3Client client;
    private List<S3ObjectSummary> s3Obj;
    String input;
    public PlayFragmentSearchList(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (MainActivity) getActivity();

        rootView = inflater.inflate(R.layout.play_fragment_search_list, container, false);

        svNews = (SearchView) rootView.findViewById(R.id.svSongs);
        svNews.setOnQueryTextListener(this);
        refresh = (ImageView) rootView.findViewById(R.id.fresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state = isNetworkAvailable(getActivity());
                if(state == true) {
                    initData();
                }
                else
                {
                    Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (Constants.refreshed == true) {
            Data = Constants.savedData;
            lvListNews = (GridView)rootView.findViewById(R.id.lvSongList);

            lvListNews.setAdapter(new CustomGridViewAdapter(mActivity, R.layout.play_fragment_search_list_row_item, Data));
            //stop_led();

    }

        if (Constants.refreshed == false) {
            state = isNetworkAvailable(getActivity());
            if(state == true) {
                mainData = new ArrayList<SongItem>();
                lvListNews = (GridView)rootView.findViewById(R.id.lvSongList);

                lvListNews.setAdapter(new CustomGridViewAdapter(mActivity, R.layout.play_fragment_search_list_row_item, mainData));
                initData();
                Constants.refreshed = true;
            }
            else
            {
                Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        stop_led();
    }

    public void initData(){
        mainData = new ArrayList<SongItem>();
        String accessFolder = Util.checkS3Access(mActivity);
        input = accessFolder;
        new GetFileListTask().execute(accessFolder);
        /*mainData.add(Util.setSongItem("The Beatles - Come Together",        "eTNitq77Utg",  R.raw.one, homeIcon));
        mainData.add(Util.setSongItem("The Beatles - Here Comes The Sun",   "Y6GNEEi7x4c",  R.raw.two,homeIcon));
        mainData.add(Util.setSongItem("Oasis - Wonderwall",                 "SLZ7uzFIMoY",  R.raw.three,homeIcon));
        mainData.add(Util.setSongItem("Led Zeppelin - Immigrant Song", "TlmrQfSTmiY", R.raw.four,homeIcon));*/
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

    private class GetFileListTask extends AsyncTask<String, Void, Void> {
        // The list of objects we find in the S3 bucket
        private List<S3ObjectSummary> s3ObjList;
        // A dialog to let the user know we are retrieving the files
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(mActivity,
                    getString(R.string.refreshing),
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(String... inputs) {
            // Queries files in the bucket from S3.
            AmazonS3Client s3 = Util.getS3Client(mActivity);
            //AmazonS3Client client = Util.getS3Client(mActivity);
            client = s3;
            s3ObjList = s3.listObjects(inputs[0]).getObjectSummaries();
            s3Obj = s3ObjList;
            mainData.clear();
            //int total = 26;
            int count = 0;
            //int total = s3ObjList.size();
            for (S3ObjectSummary summary : s3ObjList) {
                //if(count >= total)
                if(count >= summary.getSize())                     /** This value can increase and decrease the number of items in the grid show **/
                {
                    break;
                }
                if(!(new File(mActivity.getFilesDir().toString() + "/" + summary.getKey()).isFile())) {
                    Util.downloadFile(mActivity, inputs[0], summary.getKey());
                }
                String keySplit[] = summary.getKey().split("\\.");
                Drawable drawable = Util.LoadImageFromWeb("http://img.youtube.com/vi/" + keySplit[1] + "/0.jpg");
                if(drawable != null) {
                    mainData.add(Util.setSongItem(keySplit[0], keySplit[1], summary.getKey(), drawable));
                }else{
                    mainData.add(Util.setSongItem(keySplit[0], keySplit[1], summary.getKey(), ContextCompat.getDrawable(mActivity.getApplicationContext(), R.drawable.defaultthumb)));
                }
                count++;
            }
            Constants.savedData = mainData;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            lvListNews.setAdapter(new CustomGridViewAdapter(mActivity, R.layout.play_fragment_search_list_row_item, mainData));
        }
    }

    public void stop_led()
    {
        if(Config.bBlueToothActive == true)
        {
            Util.stopViaData();
        }
    }

    public boolean isNetworkAvailable(final Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }
}