package fretx.version4.fretxapi;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class AppCache {

    private static File cacheDir;

    public static void initialize(Context context) { cacheDir = context.getCacheDir(); }

    public static String getFromCache(String path) {

        try {
            int i; String buff = "";
            File file = getFile( path );
            FileInputStream fis = new FileInputStream(file);
            while ( ( i = fis.read() ) != -1 ) { buff += (char) i; }
            fis.close();
            Log.d( "APP CACHE", String.format( "Got From Cache %s", path ) );
            return buff;
        }

        catch (Exception e) {
            Log.d( "APP CACHE", String.format( "Failed Getting From Cache %s\r\n%s", path, e.toString() ) );
            return "";
        }

    }

    public static void saveToCache(String path, byte[] body) {

        try {
            File file = getFile( path );
            if( file.exists() ) file.delete();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(body);
            fos.close();
            Log.d( "APP CACHE", String.format( "Saved To Cache %s", path ) );
        }

        catch (Exception e) {
            Log.d( "APP CACHE", String.format( "Failed Saving To Cache %s\r\n%s", path, e.toString() ) );
        }

    }

    public static File    getFile      ( String path ) { return new File( cacheDir, path); }
    public static Boolean exists       ( String path ) { return getFile( path ).exists(); }
    public static long    last_modified( String path ) { return getFile( path ).lastModified(); }

}
