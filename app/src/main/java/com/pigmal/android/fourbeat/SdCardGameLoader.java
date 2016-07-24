package com.pigmal.android.fourbeat;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SdCardGameLoader {

    public static final String FOURBEAT_MARKER_FILE_NAME = "put_the_apps_for_fourbeat_here";
    public String mLoadedGameName = "";

    public Uri getUriCurrentGameFilePath(String filePath) {
        String path = "file://" + getGameRootPath(mLoadedGameName) + filePath;
        return Uri.parse(path);
    }

    public Uri getCurrentGameFilePath(String filePath) {
        String path = getGameRootPath(mLoadedGameName) + filePath;
        return Uri.parse(path);
    }

    public String getTopPageData() {
        StringBuffer htmlDoc = new StringBuffer();
        htmlDoc.append("<html><head><script type=\"text/javascript\" src=\"js/fourbeat.js\"></script><script type=\"text/javascript\" src=\"main.js\"></script><script type=\"text/javascript\" src=\"js/sdcardgameloader.js\"></script></head>" + "<body><h3>Apps on External Storage</h3>");


        ArrayList<String> games = getGameList();
        for (String game : games) {
            htmlDoc.append(getGameLink(game));
        }
        htmlDoc.append("<br><br>Put your app under /com.pigmal.android.fourbeat/apps<br>otherwise search " + FOURBEAT_MARKER_FILE_NAME + "file in the storage.");
        htmlDoc.append("</body></html>");

        Log.i("Apps", "html file : " + htmlDoc.toString());

        return htmlDoc.toString();
    }

    private String getGameLink(String name) {

        String urlPath = "<a name=\"app\" id=\"" + name + "\" onclick=\"loadgame(this.id);\" " + " href=\"\"#\"" + "\">" + name + "</a><br>";
        return urlPath;
    }

    public String getGameFilePath(String name) {
        String path = "file://" + getGameRootPath(name) + "index.html";
        Log.i("Apps", "file path : " + path);
        return path;
    }

    private String getGameRootPath(String name) {
        String path = getGamesDir() + File.separator + name + File.separator;
        return path;
    }

    private ArrayList<String> getGameList() {
        ArrayList<String> games = new ArrayList<String>();
        File f = new File(getGamesDir());
        File test = new File(f.getAbsolutePath() + File.separator + FOURBEAT_MARKER_FILE_NAME);
        try {
            if (!test.exists()) {
                test.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File[] files = f.listFiles();
        if (files != null) {
            for (File inFile : files) {
                if (inFile.isDirectory()) {
                    String name = inFile.getName();
                    if (!name.equals("js")) {
                        games.add(name);
                    }
                }
            }
        }
        return games;
    }

    private String getGamesDir() {

        return Environment.getExternalStorageDirectory() + File.separator
                + "com.pigmal.android.fourbeat" + File.separator + "apps";
    }
}
