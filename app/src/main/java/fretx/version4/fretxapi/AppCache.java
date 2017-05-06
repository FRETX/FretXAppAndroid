package fretx.version4.fretxapi;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class AppCache {
    private static final String TAG = "KJKP6_APPCACHE";
    private static File cacheDir;


    public static void initialize(Context context) {
        cacheDir = context.getCacheDir();
    }

    public static String getFromCache(String path) {
        try {
            final File file = getFile( path );
            final StringBuffer contents = new StringBuffer();
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            String text;
            while ((text = reader.readLine()) != null) {
                contents.append(text).append(System.getProperty("line.separator"));
            }
            reader.close();
            return contents.toString();
        } catch (Exception e) {
            Log.d(TAG, String.format( "Failed Getting From Cache %s\n%s", path, e.toString() ) );
            return "";
        }

    }

    public static void saveToCache(String path, byte[] body) {
        try {
            final File file = getFile(path);
            if(file.exists())
                file.delete();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(body);
            fos.close();
        } catch (Exception e) {
            Log.d(TAG, String.format( "Failed Saving To Cache %s\n%s", path, e.toString() ) );
        }

    }

    private static File getFile(String path) {
        return new File( cacheDir, path);
    }

    public static Boolean exists(String path) {
        return getFile(path).exists();
    }

    public static long last_modified (String path) {
        return getFile(path).lastModified();
    }
}
