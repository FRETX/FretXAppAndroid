package fretx.version4;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT =4;
    private String titles[] = new String[]{"    Play     ", "    Learn    ", "    Chords   ", "    Tuner    "};

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new PlayFragment();
            case 1:
                return new LearnFragment();
            case 2:
                return new ChordFragment();
            case 3:
                return new TunerFragment();
        }
        return null;
    }

    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}