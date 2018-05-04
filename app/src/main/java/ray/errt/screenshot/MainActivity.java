package ray.errt.screenshot;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private WindowManager mWindowManager;
    private DisplayMetrics mDisplayMetrics;
    private int mScreenWidth, mScreenHeight;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mDisplayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getRealMetrics(mDisplayMetrics);
        mScreenWidth = mDisplayMetrics.widthPixels;
        mScreenHeight = mDisplayMetrics.heightPixels;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = screenShot(new Rect(), mScreenWidth, mScreenHeight,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE, false,
                        Surface.ROTATION_0);
                if (bitmap != null) {
                    saveBitmap(bitmap);
                }
            }
        });

    }

    private Bitmap screenShot(Rect sourceCrop, int width, int height, int minLayer,
            int maxLayer, boolean useIdentityTransform, int rotation) {
        try {
            final Class<?> surfaceControlCls = Class.forName("android.view.SurfaceControl");
            final Method screenShot = surfaceControlCls.getMethod("screenshot", Rect.class,
                    int.class,
                    int.class, int.class, int.class, boolean.class, int.class);
            return (Bitmap) screenShot.invoke(null, sourceCrop, width, height, minLayer,
                    maxLayer, useIdentityTransform, rotation);
        } catch (Exception e) {
            //This should never happen
            Log.d(TAG, "SurfaceControl: screenShot: " + e);
            return null;
        }
    }

    private void saveBitmap(Bitmap bitmap) {
        String mountState = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(mountState)) {
            return;
        }
        File rootDir = Environment.getExternalStorageDirectory();
        File filePath = new File(rootDir + "/" + "test.jpg");
        if (filePath.exists()) {
            filePath.delete();
        }
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
        } catch (Exception e) {
            Log.d(TAG, "saveBitmap: " + e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
