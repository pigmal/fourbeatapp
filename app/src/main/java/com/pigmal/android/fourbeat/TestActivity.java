package com.pigmal.android.fourbeat;


import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class TestActivity extends FourBeatBaseActivity implements OnClickListener {
    static final String TAG = "Main";

    private TextView[] mTextViews;

    private SoundPool mSoundPool;
    private int[] mSoundIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        findViewById(R.id.button_red).setOnClickListener(this);
        findViewById(R.id.button_blue).setOnClickListener(this);
        findViewById(R.id.button_yellow).setOnClickListener(this);
        findViewById(R.id.button_green).setOnClickListener(this);

        mTextViews = new TextView[4];
        mTextViews[0] = (TextView) findViewById(R.id.textView1);
        mTextViews[1] = (TextView) findViewById(R.id.textView2);
        mTextViews[2] = (TextView) findViewById(R.id.textView3);
        mTextViews[3] = (TextView) findViewById(R.id.textView4);

        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        mSoundIds = new int[4];
        mSoundIds[0] = mSoundPool.load(this, R.raw.se_maoudamashii_system41, 1);
        mSoundIds[1] = mSoundPool.load(this, R.raw.se_maoudamashii_system44, 1);
        mSoundIds[2] = mSoundPool.load(this, R.raw.se_maoudamashii_system47, 1);
        mSoundIds[3] = mSoundPool.load(this, R.raw.se_maoudamashii_system49, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_red:
                handleButton(0, 1);
                break;
            case R.id.button_blue:
                handleButton(1, 1);
                break;
            case R.id.button_yellow:
                handleButton(2, 1);
                break;
            case R.id.button_green:
                handleButton(3, 1);
                break;
            default:
                break;
        }
    }

    void handleButton(final int id, final int state) {
        if (state == 1) {
            final int count = Integer.valueOf(mTextViews[id].getText().toString());
            if (count < 100) {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViews[id].setText(String.valueOf(count + 1));
                    }
                });

            } else {
                finish();
            }

            mSoundPool.play(mSoundIds[id], 1.0f, 1.0f, 1, 0, 1);
        } else {
            // Nothing to do
        }
    }

    @Override
    protected void onFourBeatStateChange(int id, int state) {
        handleButton(id, state);
    }

    @Override
    protected void onFourBeatConnected() {
        // TODO Auto-generated method stub
    }
}
