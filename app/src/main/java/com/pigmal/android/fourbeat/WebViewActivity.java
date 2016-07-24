package com.pigmal.android.fourbeat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.pigmal.android.hardware.fourbeat.Protocol;

import org.json.JSONException;
import org.json.JSONObject;

public class WebViewActivity extends FourBeatBaseActivity {
    private static final String JS_COMMAND_FOURBEAT_EVENT = "javascript:nativeFourBeatEvent";

    private WebView mWebView;
    private Button mConnectButton;
    private JavaScriptInterface mJavaScriptInterface;
    private SdCardGameLoader mSdcardGameLoader = new SdCardGameLoader();
    private MediaPlayer mMediaPlayer;
    private SoundPool mSoundPool;
    private Activity mActivity;

    private static final int NUM_SOUND = 20;
    private int mSoundIndex;
    private int[] mSoundIds;

    @TargetApi(19)
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_activity);
        mActivity = this;

        mConnectButton = (Button) findViewById(R.id.button_connect);
        mConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                bindFourBeat();
            }
        });

        // WebView
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setVerticalScrollbarOverlay(true);
        mJavaScriptInterface = new JavaScriptInterface();
        mWebView.addJavascriptInterface(mJavaScriptInterface, "FbNativeInterface");

        // Enable HTML5
        WebSettings ws = mWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setSupportMultipleWindows(true);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setAppCachePath("/data/data/" + getPackageName() + "/cache/");
        ws.setAppCacheEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE))
            { WebView.setWebContentsDebuggingEnabled(true); }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        initSound();

        String url = "file:///android_asset/html/index.html";
        String param = this.getIntent().getStringExtra("param");
        if (param != null && !param.equals("")) {
            url = param;
            mWebView.loadUrl(url);
        } else {
            // loadData cannot be used due to restriction of "file" scheme;
            mWebView.loadDataWithBaseURL(url, mSdcardGameLoader.getTopPageData(), "text/html", null, null);
            //mWebView.loadUrl(url);
        }
    }

    @Override
    protected void onPause() {
        mJavaScriptInterface.stopMusic();
        mWebView.clearCache(true);
        super.onPause();
    }

    private Handler mHandler = new Handler();

    public void transportEventToJs(Protocol.BUTTON_ID button, Protocol.BUTTON_STATE change) {
        Log.d(TAG, "Event : " + button + " , " + change);

        JSONObject json = new JSONObject();
        try {
            json.put("eventType", "button");

            switch (button) {
                case RED:
                    json.put("color", "RED");
                    break;
                case BLUE:
                    json.put("color", "BLUE");
                    break;
                case YELLOW:
                    json.put("color", "YELLOW");
                    break;
                case GREEN:
                    json.put("color", "GREEN");
                    break;
                default:
                    break;
            }

            switch (change) {
                case ON:
                    json.put("state", "PRESS");
                    break;
                case OFF:
                    json.put("state", "RELEASE");
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String jsCommand = JS_COMMAND_FOURBEAT_EVENT + "(" + json.toString() + ")";
        mWebView.loadUrl(jsCommand);
    }

    void initSound() {
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }

        mSoundPool = new SoundPool(NUM_SOUND, AudioManager.STREAM_MUSIC, 0);
        mSoundIds = new int[NUM_SOUND];
    }

    public class JavaScriptInterface {
        public JavaScriptInterface() {
        }

        @JavascriptInterface
        public void finishActivity() {
            Log.v(TAG, "finishActivity");
            WebViewActivity.this.finish();
        }

        @JavascriptInterface
        public void loadGame(final String name) {
            Log.v(TAG, "Selected game : " + name);
            mSdcardGameLoader.mLoadedGameName = name;
            mHandler.post(new Runnable() {
                public void run() {
                    mWebView.loadUrl(mSdcardGameLoader.getGameFilePath(name));
                }
            });
        }

        @JavascriptInterface
        public void preloadSound(final String path) {
            Log.v(TAG, "preloadSound " + path);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSoundIds[mSoundIndex++] = mSoundPool.load(mSdcardGameLoader.getCurrentGameFilePath(path).toString(), 0);
                    JSONObject json = new JSONObject();
                    try {
                        json.put("eventType", "callback");
                        json.put("functionName", "preloadSound");
                        json.put("result", "ok");
                        json.put("id", mSoundIndex);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String jsCommand = JS_COMMAND_FOURBEAT_EVENT + "(" + json.toString() + ")";
                    Log.v(TAG, jsCommand);
                    mWebView.loadUrl(jsCommand);
                }
            });
        }

        /**
         * 0: up 1: down 2: left 3: right
         *
         * @param id
         */
        @JavascriptInterface
        public void playDefaultSound(int id) {
            Log.v(TAG, "playDefaultSound");
            switch (id) {
                case 0:
                    mWebView.playSoundEffect(SoundEffectConstants.NAVIGATION_UP);
                    break;
                case 1:
                    mWebView.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
                    break;
                case 2:
                    mWebView.playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
                    break;
                case 3:
                    mWebView.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
                    break;
                default:
                    break;
            }
        }

        @JavascriptInterface
        public void playSound(int index) {
            Log.v(TAG, "playSound index = " + index);
            mSoundPool.play(mSoundIds[index], 1.0f, 1.0f, 0, 0, 1.0f);
        }

        /*
         *  Temporary, play the resource file only
         */
        @JavascriptInterface
        public void playMusic(int index) {
            Log.v(TAG, "playBgm");

            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                stopMusic();
            }
            int resid = 0;
            switch (index) {
                case 0:
                    resid = R.raw.game_maoudamashii_4_field09;
                    break;
                case 1:
                    resid = R.raw.game_maoudamashii_1_battle37;
                    break;
                case 2:
                    resid = R.raw.game_maoudamashii_9_jingle05;
                    break;
                default:
                    break;
            }

            mMediaPlayer = MediaPlayer.create(WebViewActivity.this, resid);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setVolume(0.5f, 0.5f);
            mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        }

        @JavascriptInterface
        public void stopMusic() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }

        // For SD Card games
        @JavascriptInterface
        public void playMusic2(String filePath) {
            Log.v(TAG, "playBgm");

            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                stopMusic();
            }

            Uri uri = mSdcardGameLoader.getUriCurrentGameFilePath(filePath);
            Log.e("games", "URI path : " + uri);
            mMediaPlayer = MediaPlayer.create(WebViewActivity.this, uri);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        }
    }

    @Override
    protected void onFourBeatStateChange(final int id, final int state) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                transportEventToJs(Protocol.BUTTON_ID.values()[id],
                        Protocol.BUTTON_STATE.values()[state]);
            }
        });
    }

    @Override
    protected void onFourBeatConnected() {
        mConnectButton.setVisibility(View.GONE);
    }
}
