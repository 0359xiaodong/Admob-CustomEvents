package au.com.xandar.admob;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import au.com.xandar.admob.common.Consts;
import au.com.xandar.admob.customevents.R;
import com.google.ads.*;

/**
 * Shows a banner ad.
 * <p/>
 * User: William
 * Date: 30/08/2010
 * Time: 8:40:17 PM
 */
public final class BannerActivity extends Activity {

    static {
        // Switch on debug for component
        Consts.DEBUG = true;
    }

    private static final String TAG = "AdmobCE.BannerActivity";

    private ViewGroup adLayoutContainer;
    private AdView admobAdView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.v(TAG, "#onCreate - start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);
        adLayoutContainer = (ViewGroup) findViewById(R.id.adBanner);

        Log.v(TAG, "(ads) Create Admob AdView - start");
        admobAdView = new AdView(this, AdSize.BANNER, MyAdmobConfig.ADMOB_MEDIATION_ID); // "enter-your-Admob-mediation-id-here";
        Log.v(TAG, "(ads) adding Admob AdListener");

        admobAdView.setAdListener(new AdListener() {
            @Override
            public void onReceiveAd(Ad ad) {
                Log.v(TAG, "Admob onReceiveAd : " + ad + " adView#width=" + admobAdView.getWidth() + " adView#height=" + admobAdView.getHeight());
            }
            @Override
            public void onFailedToReceiveAd(Ad ad, AdRequest.ErrorCode errorCode) {
                Log.v(TAG, "Admob onFailedToReceiveAd errorCode=" + errorCode + " : " + ad);
            }
            @Override
            public void onPresentScreen(Ad ad) {
                Log.v(TAG, "Admob onPresentScreen : " + ad);
            }
            @Override
            public void onDismissScreen(Ad ad) {
                Log.v(TAG, "Admob onDismissScreen : " + ad);
            }
            @Override
            public void onLeaveApplication(Ad ad) {
                Log.v(TAG, "Admob onLeaveApplication : " + ad);
            }
        });
        Log.v(TAG, "(ads) added Admob AdListener");
        adLayoutContainer.addView(admobAdView);

        Log.v(TAG, "#onCreate - finish");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "#onResume - start");
        super.onResume();

        Log.v(TAG, "(ads) Admob start loading - start");
        final AdRequest adRequest = new AdRequest();
        admobAdView.loadAd(adRequest);
        Log.v(TAG, "(ads) Admob start loading - finish");

        Log.d(TAG, "#onResume - finish");
    }

    /**
     * Invoked whenever another Activity comes in front of this Activity.
     * The Activity can be killed at any point after this.
     * <p>
     * This is where we will persist the Game state.
     * </p>
     */
    @Override
    protected void onPause() {
        Log.v(TAG, "#onPause - start isFinishing:" + isFinishing() + " changingConfigurations:" + getChangingConfigurations());
        super.onPause();

        Log.v(TAG, "(ads) - stop loading - start");
        admobAdView.stopLoading();
        Log.v(TAG, "(ads) - stop loading - finish");

        Log.d(TAG, "#onPause - finish");
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "#onStop - start");
        super.onStop();
        Log.v(TAG, "#onStop - finish");
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "#onDestroy - start");

        // Stop requesting ads.
        Log.v(TAG, "(ads) Stopping and removing all AdViews - start");
        admobAdView.setAdListener(null);
        admobAdView.destroy();
        admobAdView = null;
        adLayoutContainer.removeAllViews();

        Log.v(TAG, "(ads) Stopping and removing all AdViews - finish");

        super.onDestroy();
        Log.v(TAG, "#onDestroy - finish");
    }
}