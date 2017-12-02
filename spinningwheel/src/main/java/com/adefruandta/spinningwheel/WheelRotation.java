package com.adefruandta.spinningwheel;

import android.os.CountDownTimer;

/**
 * Created by adefruandta on 3/12/17.
 */

class WheelRotation extends CountDownTimer {

    private final static float SLOW_FACTOR = 2f / 3f;

    private final float ROTATE_SCALE_FACTOR = 2;

    private float maxAngle;

    private float angle = 1;

    private long thresholdSlow;

    private RotationListener rotationListener;

    private long duration;

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public WheelRotation(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        thresholdSlow = (long) (millisInFuture * SLOW_FACTOR);
        duration = millisInFuture;
    }

    public static WheelRotation init(long millisInFuture, long countDownInterval) {
        return new WheelRotation(millisInFuture, countDownInterval);
    }

    public WheelRotation setMaxAngle(float maxAngle) {
        this.maxAngle = maxAngle;
        return this;
    }

    public WheelRotation setListener(RotationListener l) {
        this.rotationListener = l;
        return this;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (rotationListener == null) {
            return;
        }

        if (millisUntilFinished <= thresholdSlow) {
            angle = maxAngle * ((float) millisUntilFinished / (float) duration);
            rotationListener.onRotate(angle);
        }
        else if (angle < maxAngle) {
            rotationListener.onRotate(angle);

            angle *= ROTATE_SCALE_FACTOR;

            if (angle > maxAngle) {
                angle = maxAngle;
            }
        }
        else {
            rotationListener.onRotate(angle);
        }
    }

    @Override
    public void onFinish() {
        if (rotationListener == null) {
            return;
        }

        rotationListener.onStop();
    }

    public interface RotationListener {

        void onRotate(float angle);

        void onStop();
    }
}
