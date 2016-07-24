package com.pigmal.android.fourbeat;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pigmal.android.fourbeat.LaunchAppManager.Filter;
import com.pigmal.android.hardware.fourbeat.Protocol;
import java.util.ArrayList;

public class MainActivity extends FourBeatBaseActivity implements OnClickListener {
    static final String TAG = "Main";

    private TextView mTargetText;
    private ImageView mTargetImage;
    private ImageButton mLeftButton;
    private ImageButton mRightButton;
    private Button mConnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        findViewById(R.id.button_red).setOnClickListener(this);
        mLeftButton = (ImageButton) findViewById(R.id.button_blue);
        mLeftButton.setOnClickListener(this);
        mRightButton = (ImageButton) findViewById(R.id.button_yellow);
        mRightButton.setOnClickListener(this);
        findViewById(R.id.button_green).setOnClickListener(this);
        mTargetText = (TextView) findViewById(R.id.text_target);
        mTargetImage = (ImageView) findViewById(R.id.image_target);

        mConnectButton = (Button) findViewById(R.id.button_connect);
        mConnectButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupFbMenu();
    }

    @Override
    protected void onPause() {
        mConnectButton.setVisibility(View.VISIBLE);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_red:
                handleButton(Protocol.BUTTON_ID.RED, Protocol.BUTTON_STATE.ON);
                break;
            case R.id.button_blue:
                handleButton(Protocol.BUTTON_ID.BLUE, Protocol.BUTTON_STATE.ON);
                break;
            case R.id.button_yellow:
                handleButton(Protocol.BUTTON_ID.YELLOW, Protocol.BUTTON_STATE.ON);
                break;
            case R.id.button_green:
                handleButton(Protocol.BUTTON_ID.GREEN, Protocol.BUTTON_STATE.ON);
                break;
            case R.id.button_connect:
                bindFourBeat();
                break;
            default:
                break;
        }
    }

    private int mIndex;
    private int mMaxIndex;
    private FbItem[] mFbItems;

    protected void setupFbMenu() {
        ArrayList<FbItem> list;
        list = new ArrayList<FbItem>();

        String[] packages = LaunchAppManager.getInstance(this).getLaunchPackages(Filter.MAIN);
        for (int i = 0; i < packages.length; i++) {
            if (!packages[i].equals("")) {
                list.add(new FbItem(packages[i], "", 0));
            }
        }

        // set applications
        list.add(new FbItem(WebViewActivity.class, "External", null, R.drawable.folder2));
        list.add(new FbItem(TestActivity.class, "Test", null, R.drawable.test));
        list.add(new FbItem(WebViewActivity.class, "Bagworm Strap", "file:///android_asset/html/examples/sprint/index.html", R.drawable.ic_sprint));

        mFbItems = list.toArray(new FbItem[mIndex]);
        mMaxIndex = mFbItems.length - 1;
        refreshUiState();
    }

    protected void next() {
        mIndex = mIndex < mMaxIndex ? mIndex + 1 : mIndex;
        refreshUiState();
    }

    protected void prev() {
        mIndex = mIndex > 0 ? mIndex - 1 : mIndex;
        refreshUiState();
    }

    protected void ok() {
        switch (mFbItems[mIndex].getActionType()) {
            case ACTIVITY:
                Intent i = new Intent(this, mFbItems[mIndex].getActivityClass());
                i.putExtra("param", mFbItems[mIndex].getParameter());
                startActivity(i);
                break;
            case PACKAGE:
                startActivityByPackage(mFbItems[mIndex].getPackageName());
                break;
            default:
                break;
        }

    }

    protected void cancel() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "cancel", Toast.LENGTH_SHORT).show();
            }

        });
    }

    protected void refreshUiState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTargetIcon();
                setEnable();
            }
        });
    }

    void setEnable() {
        mLeftButton.setEnabled(mIndex > 0);
        mRightButton.setEnabled(mIndex < mMaxIndex);
    }

    void setTargetIcon() {
        mTargetText.setText(mFbItems[mIndex].getName());

        switch (mFbItems[mIndex].getActionType()) {
            case ACTIVITY:
                mTargetImage.setImageResource(mFbItems[mIndex].getIconResource());
                break;
            case PACKAGE:
                if (mFbItems[mIndex].getName().equals("")) {
                    String name = Util.getNameByPackage(this, mFbItems[mIndex].getPackageName());
                    mTargetText.setText(name);
                }

                if (mFbItems[mIndex].getIconResource() == 0) {
                    Drawable icon = Util.getIconByPackage(this, mFbItems[mIndex].getPackageName());
                    if (icon != null) {
                        mTargetImage.setImageDrawable(icon);
                    } else {
                        mTargetImage.setImageResource(R.drawable.no_icon_image);
                    }
                } else {
                    mTargetImage.setImageResource(mFbItems[mIndex].getIconResource());
                }
                break;
            default:
                break;
        }
    }

    void startActivityByPackage(String packageName) {
        PackageManager pm = getPackageManager();
        Intent intent = null;
        intent = pm.getLaunchIntentForPackage(packageName);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_preferences:
                Toast.makeText(this, "Preferences", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_cleardefault:
                PackageManager pm = getPackageManager();
                pm.clearPackagePreferredActivities("com.pigmal.android.fourbeat");
                return true;
            case R.id.menu_help:
                Toast.makeText(this, "Help", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void handleButton(Protocol.BUTTON_ID button, Protocol.BUTTON_STATE change) {
        if (change == Protocol.BUTTON_STATE.ON) {
            switch (button) {
                case RED:
                    ok();
                    break;
                case BLUE:
                    prev();
                    break;
                case YELLOW:
                    next();
                    break;
                case GREEN:
                    cancel();
                    break;
                default:
                    break;
            }
        } else {
            // Nothing done now
        }
    }

    @Override
    protected void onFourBeatStateChange(int id, int state) {
        handleButton(Protocol.BUTTON_ID.values()[id], Protocol.BUTTON_STATE.values()[state]);
    }

    @Override
    protected void onFourBeatConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectButton.setVisibility(View.GONE);
            }
        });
    }
}
