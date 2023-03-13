package com.takuchan.sensortoml;

import android.os.CountDownTimer;
import android.view.View;

import org.w3c.dom.CDATASection;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Callable;

public class CountDown extends CountDownTimer{

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */

    private final SimpleDateFormat dataFormat =
            new SimpleDateFormat("mm:ss.SSS", Locale.US);
    private Runnable onTaskFinish;

    public CountDown(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        MainActivity.countDownText.setText(dataFormat.format(millisUntilFinished));
    }

    @Override
    public void onFinish() {
        MainActivity.countDownText.setVisibility(View.GONE);
    }

}

