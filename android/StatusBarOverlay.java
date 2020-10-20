package jk.cordova.plugin.kiosk;

import org.apache.cordova.LOG;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

// from https://github.com/ngocdaothanh/cordova-plugin-unswipable-android-status-bar
// http://stackoverflow.com/questions/25284233/prevent-status-bar-for-appearing-android-modified
public class StatusBarOverlay extends ViewGroup {

    private static final int OVERLAY_PERMISSION_REQ_CODE = 4545;
    private static final String TAG = "KioskPlugin";

    public StatusBarOverlay(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    static StatusBarOverlay create(Activity activity) {

        WindowManager manager = ((WindowManager) activity.getApplicationContext()
            .getSystemService(Context.WINDOW_SERVICE));

        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = 
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |// this is to enable the notification to recieve touch events
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;// Draws over status bar

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        //https://stackoverflow.com/questions/1016896/get-screen-dimensions-in-pixels
        int resId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int result = 0;
        if (resId > 0) {
            result = activity.getResources().getDimensionPixelSize(resId);
        }

        localLayoutParams.height = result;

        localLayoutParams.format = PixelFormat.TRANSPARENT;

        StatusBarOverlay view = new StatusBarOverlay(activity);

        //manager.addView(view, localLayoutParams);

        LOG.d(TAG, "Creating StatusBarOverlay");
        return view;
    }
    
    static StatusBarOverlay createOrObtainPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // added in API level 23
            if (! Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
                activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                return null;
            } else {
                return create(activity);
                //return null;
            }
        } else {
            return create(activity);
            //return null;
        }
    }
    
    void destroy(Activity activity) {
        WindowManager manager = ((WindowManager) activity
                .getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));
        manager.removeView(this);
        LOG.d(TAG, "Removing StatusBarOverlay");
    }
}