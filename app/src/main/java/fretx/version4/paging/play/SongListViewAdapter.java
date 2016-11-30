package fretx.version4.paging.play;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.fretxapi.SongItem;

/**
 * Created by ljk on 12/11/2015.
 */
public class SongListViewAdapter extends BaseAdapter {

    ArrayList<SongItem> marrList;
    private LayoutInflater layoutInflater;
    private MainActivity mActivity;

    public SongListViewAdapter(MainActivity context, ArrayList listData) {
        this.marrList = listData;
        layoutInflater = LayoutInflater.from(context);
        mActivity = context;
    }

    public int getCount() {
        return marrList.size();
    }
    @Override
    public Object getItem(int position) {
        return marrList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.song_list_item, null);
            holder = new ViewHolder();
            holder.tvName = (TextView) convertView.findViewById(R.id.tvSongName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final SongItem newsItem = marrList.get(position);

        holder.tvName.setText(newsItem.songName);

        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                PlayFragmentYoutubeFragment fragmentYoutubeFragment = new PlayFragmentYoutubeFragment();
                FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Bundle args = new Bundle();
                args.putString("URL", newsItem.songUrl);
                args.putString("RAW", newsItem.songTxt);
                fragmentYoutubeFragment.setArguments(args);
                fragmentTransaction.replace(R.id.play_container, fragmentYoutubeFragment);
                fragmentTransaction.commit();
            }
        });
        return convertView;
    }
    static class ViewHolder {
        TextView tvName;
    }

}
