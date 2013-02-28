package au.com.xandar.admob;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import au.com.xandar.admob.common.CustomEventConsts;
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
        CustomEventConsts.DEBUG = true;
    }

    private static final String TAG = "AdmobCE.BannerActivity";

    private ViewGroup adLayoutContainer;
    private AdView admobAdView;

    private InterstitialAd interstitialAd;
    private Button loadInterstitial;
    private Button showInterstitial;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.v(TAG, "#onCreate - start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);
        adLayoutContainer = (ViewGroup) findViewById(R.id.adBanner);
        loadInterstitial = (Button) findViewById(R.id.loadInterstitial);
        showInterstitial = (Button) findViewById(R.id.showInterstitial);

        Log.v(TAG, "#onCreate create Admob AdView - start");
        admobAdView = new AdView(this, AdSize.BANNER, MyAdmobConfig.ADMOB_BANNER_MEDIATION_ID); // "enter-your-Admob-mediation-id-here";
        Log.v(TAG, "#onCreate adding Admob AdListener");

        admobAdView.setAdListener(new AdListener() {
            @Override
            public void onReceiveAd(Ad ad) {
                Log.v(TAG, "Banner#onReceiveAd : " + ad + " adView#width=" + admobAdView.getWidth() + " adView#height=" + admobAdView.getHeight());
            }
            @Override
            public void onFailedToReceiveAd(Ad ad, AdRequest.ErrorCode errorCode) {
                Log.v(TAG, "Banner#onFailedToReceiveAd errorCode=" + errorCode + " : " + ad);
            }
            @Override
            public void onPresentScreen(Ad ad) {
                Log.v(TAG, "Banner#onPresentScreen : " + ad);
            }
            @Override
            public void onDismissScreen(Ad ad) {
                Log.v(TAG, "Banner#onDismissScreen : " + ad);
            }
            @Override
            public void onLeaveApplication(Ad ad) {
                Log.v(TAG, "Banner#onLeaveApplication : " + ad);
            }
        });
        Log.v(TAG, "#onCreate added Admob AdListener");
        adLayoutContainer.addView(admobAdView);

        Log.v(TAG, "#onCreate loading BannerAd");
        admobAdView.loadAd(new AdRequest());
        Log.v(TAG, "#onCreate loaded BannerAd");

        // Interstitial Ads
        interstitialAd = new InterstitialAd(this, MyAdmobConfig.ADMOB_INTERSTITIAL_MEDIATION_ID);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onReceiveAd(Ad ad) {
                Log.v(TAG, "Interstitial#onReceivedAd : " + ad);
                showInterstitial.setEnabled(true);
            }

            @Override
            public void onFailedToReceiveAd(Ad ad, AdRequest.ErrorCode errorCode) {
                Log.v(TAG, "Interstitial#onFailedToReceiveAd errorCode=" + errorCode);
                showInterstitial.setEnabled(false);
            }

            @Override
            public void onPresentScreen(Ad ad) {
                Log.v(TAG, "Interstitial#onPresentScreen : " + ad);
                showInterstitial.setEnabled(false);
            }

            @Override
            public void onDismissScreen(Ad ad) {
                Log.v(TAG, "Interstitial#onDismissScreen : " + ad);
                showInterstitial.setEnabled(false);
            }

            @Override
            public void onLeaveApplication(Ad ad) {
                Log.v(TAG, "Interstitial#onLeaveApplication : " + ad);
            }
        });

        loadInterstitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interstitialAd.loadAd(new AdRequest());
            }
        });

        showInterstitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "showInterstitial#onClick isReady : " + interstitialAd.isReady());
                if (interstitialAd.isReady()) {
                    interstitialAd.show();
                }
            }
        });

        Log.v(TAG, "#onCreate - finish");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "#onResume - start");
        super.onResume();
        Log.d(TAG, "#onResume - finish");
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "#onPause - start isFinishing:" + isFinishing() + " changingConfigurations:" + getChangingConfigurations());
        super.onPause();
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

        interstitialAd.setAdListener(null);
        interstitialAd = null;

        Log.v(TAG, "(ads) Stopping and removing all AdViews - finish");

        super.onDestroy();
        Log.v(TAG, "#onDestroy - finish");
    }
}