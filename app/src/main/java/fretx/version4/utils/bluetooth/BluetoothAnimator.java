package fretx.version4.utils.bluetooth;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 23/05/17 11:29.
 */

public class BluetoothAnimator {
    private static final String TAG = "KJKP6_BLE_ANIM";
    private static final byte[] F0 = new byte[] {1, 2, 3, 4, 5, 6, 0};
    private static final byte[] F1 = new byte[] {11, 12, 13, 14, 15, 16, 0};
    private static final byte[] F2 = new byte[] {21, 22, 23, 24, 25, 26, 0};
    private static final byte[] F3 = new byte[] {31, 32, 33, 34, 35, 36, 0};
    private static final byte[] F4 = new byte[] {41, 42, 43, 44, 45, 46, 0};
    private static final byte[] S1 = new byte[] {1, 11, 21, 31, 41, 0};
    private static final byte[] S2 = new byte[] {2, 12, 22, 32, 42, 0};
    private static final byte[] S3 = new byte[] {3, 13, 23, 33, 43, 0};
    private static final byte[] S4 = new byte[] {4, 14, 24, 34, 44, 0};
    private static final byte[] S5 = new byte[] {5, 15, 25, 35, 45, 0};
    private static final byte[] S6 = new byte[] {6, 16, 26, 36, 46, 0};
    private static final byte[] S1_NO_F0 = new byte[] {11, 21, 31, 41, 0};
    private static final byte[] S2_NO_F0 = new byte[] {12, 22, 32, 42, 0};
    private static final byte[] S3_NO_F0 = new byte[] {13, 23, 33, 43, 0};
    private static final byte[] S4_NO_F0 = new byte[] {14, 24, 34, 44, 0};
    private static final byte[] S5_NO_F0 = new byte[] {15, 25, 35, 45, 0};
    private static final byte[] S6_NO_F0 = new byte[] {16, 26, 36, 46, 0};
    private static final byte[] BLANK = new byte[] {0};

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
