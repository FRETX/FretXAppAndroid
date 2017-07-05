package fretx.version4.utils.bluetooth;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import static fretx.version4.utils.bluetooth.Bluetooth.BLANK;
import static fretx.version4.utils.bluetooth.Bluetooth.F0;
import static fretx.version4.utils.bluetooth.Bluetooth.F1;
import static fretx.version4.utils.bluetooth.Bluetooth.F2;
import static fretx.version4.utils.bluetooth.Bluetooth.F3;
import static fretx.version4.utils.bluetooth.Bluetooth.F4;
import static fretx.version4.utils.bluetooth.Bluetooth.S1;
import static fretx.version4.utils.bluetooth.Bluetooth.S1_NO_F0;
import static fretx.version4.utils.bluetooth.Bluetooth.S2;
import static fretx.version4.utils.bluetooth.Bluetooth.S2_NO_F0;
import static fretx.version4.utils.bluetooth.Bluetooth.S3;
import static fretx.version4.utils.bluetooth.Bluetooth.S3_NO_F0;
import static fretx.version4.utils.bluetooth.Bluetooth.S4;
import static fretx.version4.utils.bluetooth.Bluetooth.S4_NO_F0;
import static fretx.version4.utils.bluetooth.Bluetooth.S5;
import static fretx.version4.utils.bluetooth.Bluetooth.S5_NO_F0;
import static fretx.version4.utils.bluetooth.Bluetooth.S6;
import static fretx.version4.utils.bluetooth.Bluetooth.S6_NO_F0;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 23/05/17 11:29.
 */

public class BluetoothAnimator {
    private static final String TAG = "KJKP6_BLE_ANIM";
    private static final int DEFAULT_DELAY_MS = 500;

    private final Handler handler = new Handler();
    private final ArrayList<AnimationStep> animations = new ArrayList<>();
    private int animationSize;
    private int index;

    /* = = = = = = = = = = = = = = = = = SINGLETON PATTERN = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final BluetoothAnimator instance = new BluetoothAnimator();
    }

    private BluetoothAnimator() {
    }

    public static BluetoothAnimator getInstance() {
        return Holder.instance;
    }

    /* = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = */
    private class AnimationStep {
        byte[] bluetoothArray;
        int delayMs;

        AnimationStep(byte[] bluetoothArray, int delayMs) {
            this.bluetoothArray = bluetoothArray;
            this.delayMs = delayMs;
        }

        AnimationStep(byte[] bluetoothArray) {
            this.bluetoothArray = bluetoothArray;
            this.delayMs = DEFAULT_DELAY_MS;
        }
    }

    private final Runnable playAnimation = new Runnable() {
        @Override
        public void run() {
            final AnimationStep anim = animations.get(index);
            Bluetooth.getInstance().setMatrix(anim.bluetoothArray);
            //Log.v(TAG, "set matrix");
            handler.postDelayed(this, anim.delayMs);
            ++index;
            if (index == animationSize)
                index = 0;
        }
    };

    /* = = = = = = = = = = = = = = = = = = = ANIMATIONS = = = = = = = = = = = = = = = = = = = = = */
    public void fretFall() {
        //stop playing animation
        handler.removeCallbacksAndMessages(null);
        if (!Bluetooth.getInstance().isEnabled())
            return;
        Bluetooth.getInstance().clearMatrix();
        //build new animation
        Log.v(TAG, "fret fall");
        animations.clear();
        animations.add(new AnimationStep(F0));
        animations.add(new AnimationStep(F1));
        animations.add(new AnimationStep(F2));
        animations.add(new AnimationStep(F3));
        animations.add(new AnimationStep(F4));
        animationSize = animations.size();
        //play new animation
        index = 0;
        handler.post(playAnimation);
    }

    public void fretFall(int delayMs) {
        //stop playing animation
        handler.removeCallbacksAndMessages(null);
        if (!Bluetooth.getInstance().isEnabled())
            return;
        Bluetooth.getInstance().clearMatrix();
        //build new animation
        Log.v(TAG, "fret fall custom delay");
        animations.clear();
        animations.add(new AnimationStep(F0, delayMs));
        animations.add(new AnimationStep(F1, delayMs));
        animations.add(new AnimationStep(F2, delayMs));
        animations.add(new AnimationStep(F3, delayMs));
        animations.add(new AnimationStep(F4, delayMs));
        animationSize = animations.size();
        //play new animation
        index = 0;
        handler.post(playAnimation);
    }

    public void stringFall() {
        //stop playing animation
        handler.removeCallbacksAndMessages(null);
        if (!Bluetooth.getInstance().isEnabled())
            return;
        Bluetooth.getInstance().clearMatrix();
        //build new animation
        Log.v(TAG, "string fall");
        animations.clear();
        animations.add(new AnimationStep(S1));
        animations.add(new AnimationStep(S2));
        animations.add(new AnimationStep(S3));
        animations.add(new AnimationStep(S4));
        animations.add(new AnimationStep(S5));
        animations.add(new AnimationStep(S6));
        animationSize = animations.size();
        //play new animation
        index = 0;
        handler.post(playAnimation);
    }

    public void stringFall(int delayMs) {
        //stop playing animation
        handler.removeCallbacksAndMessages(null);
        if (!Bluetooth.getInstance().isEnabled())
            return;
        Bluetooth.getInstance().clearMatrix();
        //build new animation
        Log.v(TAG, "string fall custom delay");
        animations.clear();
        animations.add(new AnimationStep(S1, delayMs));
        animations.add(new AnimationStep(S2, delayMs));
        animations.add(new AnimationStep(S3, delayMs));
        animations.add(new AnimationStep(S4, delayMs));
        animations.add(new AnimationStep(S5, delayMs));
        animations.add(new AnimationStep(S6, delayMs));
        animationSize = animations.size();
        //play new animation
        index = 0;
        handler.post(playAnimation);
    }

    public void stringFallNoF0() {
        //stop playing animation
        handler.removeCallbacksAndMessages(null);
        if (!Bluetooth.getInstance().isEnabled())
            return;
        Bluetooth.getInstance().clearMatrix();
        //build new animation
        Log.v(TAG, "string fall no F0");
        animations.clear();
        animations.add(new AnimationStep(S1_NO_F0));
        animations.add(new AnimationStep(S2_NO_F0));
        animations.add(new AnimationStep(S3_NO_F0));
        animations.add(new AnimationStep(S4_NO_F0));
        animations.add(new AnimationStep(S5_NO_F0));
        animations.add(new AnimationStep(S6_NO_F0));
        animationSize = animations.size();
        //play new animation
        index = 0;
        handler.post(playAnimation);
    }

    public void stringFallNoF0(int delayMs) {
        //stop playing animation
        handler.removeCallbacksAndMessages(null);
        if (!Bluetooth.getInstance().isEnabled())
            return;
        Bluetooth.getInstance().clearMatrix();
        //build new animation
        Log.v(TAG, "string fall no F0 custom delay");
        animations.clear();
        animations.add(new AnimationStep(S1_NO_F0, delayMs));
        animations.add(new AnimationStep(S2_NO_F0, delayMs));
        animations.add(new AnimationStep(S3_NO_F0, delayMs));
        animations.add(new AnimationStep(S4_NO_F0, delayMs));
        animations.add(new AnimationStep(S5_NO_F0, delayMs));
        animations.add(new AnimationStep(S6_NO_F0, delayMs));
        animationSize = animations.size();
        //play new animation
        index = 0;
        handler.post(playAnimation);
    }

    public void blinkF0() {
        //stop playing animation
        handler.removeCallbacksAndMessages(null);
        if (!Bluetooth.getInstance().isEnabled())
            return;
        Bluetooth.getInstance().clearMatrix();
        //build new animation
        Log.v(TAG, "blink F0");
        animations.clear();
        animations.add(new AnimationStep(F0));
        animations.add(new AnimationStep(BLANK));
        animationSize = animations.size();
        //play new animation
        index = 0;
        handler.post(playAnimation);
    }

    public void blinkF0(int delayMs) {
        //stop playing animation
        handler.removeCallbacksAndMessages(null);
        if (!Bluetooth.getInstance().isEnabled())
            return;
        Bluetooth.getInstance().clearMatrix();
        //build new animation
        Log.v(TAG, "blink F0 custom delay");
        animations.clear();
        animations.add(new AnimationStep(F0, delayMs));
        animations.add(new AnimationStep(BLANK, delayMs));
        animationSize = animations.size();
        //play new animation
        index = 0;
        handler.post(playAnimation);
    }

    public void blink(byte[] bluetoothArray, int delayMs) {
        //stop playing animation
        handler.removeCallbacksAndMessages(null);
        if (!Bluetooth.getInstance().isEnabled())
            return;
        Bluetooth.getInstance().clearMatrix();
        //build new animation
        Log.v(TAG, "blink F0 custom");
        animations.clear();
        animations.add(new AnimationStep(bluetoothArray, delayMs));
        animations.add(new AnimationStep(BLANK, delayMs));
        animationSize = animations.size();
        //play new animation
        index = 0;
        handler.post(playAnimation);
    }

    public void stopAnimation() {
        handler.removeCallbacksAndMessages(null);
    }

}
