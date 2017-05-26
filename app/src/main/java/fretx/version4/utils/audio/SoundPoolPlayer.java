package fretx.version4.utils.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;

import fretx.version4.R;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 09/05/17 15:42.
 */

public class SoundPoolPlayer {
    private SoundPool mShortPlayer= null;
    private HashMap mSounds = new HashMap();

    public SoundPoolPlayer(Context pContext)
    {
        // setup Soundpool
        this.mShortPlayer = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        mSounds.put(R.raw.chime_bell_ding, this.mShortPlayer.load(pContext, R.raw.chime_bell_ding, 1));
    }

    public void playShortResource(int piResource) {
        int iSoundId = (Integer) mSounds.get(piResource);
        if (mShortPlayer != null)
            mShortPlayer.play(iSoundId, 0.99f, 0.99f, 0, 0, 1);
    }

    // Cleanup
    public void release() {
        // Cleanup
        if (mShortPlayer != null)
            this.mShortPlayer.release();
        this.mShortPlayer = null;
    }
}
