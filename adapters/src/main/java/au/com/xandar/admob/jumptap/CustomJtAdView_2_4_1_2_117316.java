package au.com.xandar.admob.jumptap;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager;
import au.com.xandar.admob.common.Consts;
import com.jumptap.adtag.JtAdView;
import com.jumptap.adtag.JtAdWidgetSettings;
import com.jumptap.adtag.utils.JtException;

/**
 * JtAdView that attempts to mitigate the errors of version 2.4.1.2
 * See https://docs.google.com/spreadsheet/ccc?key=0AjAjgkxDofpudEJoQzFaQTR1WU85Mm42OWtvclJfY1E&usp=sharing
 */
final class CustomJtAdView_2_4_1_2_117316 extends JtAdView {

    private static final String TAG = "AdmobCE.CustomJtAdView";

    public CustomJtAdView_2_4_1_2_117316(Context context) throws JtException {
        super(context);
    }

    public CustomJtAdView_2_4_1_2_117316(Context context, AttributeSet attrs) throws JtException {
        super(context, attrs);
    }

    public CustomJtAdView_2_4_1_2_117316(Context context, AttributeSet attrs, int defStyle) throws JtException {
        super(context, attrs, defStyle);
    }

    public CustomJtAdView_2_4_1_2_117316(Context context, JtAdWidgetSettings widgetSettings) throws JtException {
        super(context, widgetSettings);
    }

    @Override
    public void handleClicks(String url) {

        // This hopefully handles BadTokenException: Unable to add window -- token android.os.BinderProxy@405fcbd0 is not valid; is your activity running?
        // https://www.bugsense.com/dashboard/project/2c1e8d5f#error/106494857

        if (super.context instanceof Activity) {
            final Activity activity = (Activity) super.context;
            if (activity.isFinishing()) {
                if (Consts.DEBUG) Log.d(TAG, "#handleClicks activity#isFinishing - not handling click");
                return;
            } else if (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed()) { // Activity#isDestroyed was only added in SDK#17 so need to check version first
                if (Consts.DEBUG) Log.d(TAG, "#handleClicks activity#isDestroyed - not handling click");
                return;
            }
        }

        if (Consts.DEBUG) Log.d(TAG, "#handleClicks - Context : " + context);
        try {
            super.handleClicks(url);
        } catch (WindowManager.BadTokenException e) {
            throw new IllegalStateException("#handleClicks context not valid : " + context.getClass(), e);
        }
    }

/* If I could access progressDialog then I could use a subclass of ProgressDialog to stop instances of
    IllegalArgumentException: View not attached to window manager
    https://www.bugsense.com/dashboard/project/2c1e8d5f#error/106108614

    // The code in JtAdView#handleClicks
    private void handleClicksPrivately(String url) {
        progressDialog = ProgressDialog.show(context, "", "", true, true);
        Runnable performActionRunnable = new PerformActionHandler(url, this);
        Thread th = new Thread(performActionRunnable);
        th.start();
    }
*/
}
