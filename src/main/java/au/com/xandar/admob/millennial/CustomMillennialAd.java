package au.com.xandar.admob.millennial;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import com.google.ads.AdSize;
import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.customevent.CustomEventBanner;
import com.google.ads.mediation.customevent.CustomEventBannerListener;
import com.millennialmedia.android.MMAdView;

import java.lang.reflect.Field;

/**
 * Class to allow loading of Millennial banner ads but only if device is on a version less than JellyBean.
 * This is because Millennial 4.6.0 does not process clicks when targetSDK=17 on JellyBean devices (ie it can't run native JellyBean).
 *
 * Because this class is loaded by the Admob AdView via reflection you will need to include an exception for it in your Proguard config.
 * Eg -keep class au.com.xandar.admob.millennial.CustomMillennialAd
 */
public final class CustomMillennialAd implements CustomEventBanner {

    private static final String TAG = "AdmobCE.CustomMillennialAd";

    private MMAdView adView;

    @Override
    public void requestBannerAd(final CustomEventBannerListener mediationListener,
                                Activity activity,
                                String label,
                                String millennialAppId,
                                AdSize adSize,
                                MediationAdRequest mediationAdRequest,
                                Object customEventExtra) {

        final int apiLevel = getAPILevel();
        Log.d(TAG, "#requestBannerAd androidVersion=" + apiLevel);

        // Barf now if AndroidVersion > 16
        if (apiLevel > 16) {
            Log.d(TAG, "#requestBannerAd JellyBean or greater device - bailing now");
            mediationListener.onFailedToReceiveAd();
            return;
        }

        this.adView = new MMAdView(activity, millennialAppId, MMAdView.BANNER_AD_RECTANGLE, MMAdView.REFRESH_INTERVAL_OFF, null, false);
        this.adView.setListener(new MMAdView.MMAdListener() {
            @Override
            public void MMAdCachingCompleted(MMAdView mmAdView, boolean b) {
                Log.d(TAG, "#cachingCompleted - nothing to do");
            }

            @Override
            public void MMAdReturned(MMAdView mmAdView) {
                Log.d(TAG, "#returned");
                mediationListener.onReceivedAd(mmAdView);
            }

            @Override
            public void MMAdFailed(MMAdView mmAdView) {
                Log.d(TAG, "#failed");
                mediationListener.onFailedToReceiveAd();
            }

            @Override
            public void MMAdClickedToOverlay(MMAdView mmAdView) {
                Log.d(TAG, "#clickedToOverlay");
                mediationListener.onClick();
                mediationListener.onPresentScreen();
            }

            @Override
            public void MMAdOverlayLaunched(MMAdView mmAdView) {
                Log.d(TAG, "#overlayLaunched (automatically) - nothing to do");
            }

            @Override
            public void MMAdRequestIsCaching(MMAdView mmAdView) {
                Log.d(TAG, "#requestIsCaching - nothing to do");
            }
        });

        Log.d(TAG, "#requestBannerAd before callForAd");
        this.adView.callForAd();
        Log.d(TAG, "#requestBannerAd after callForAd");
    }

    @Override
    public void destroy() {
        Log.d(TAG, "#destroy");
    }

    private int getAPILevel() {
        try {
            // This field has been added in Android 1.6 (API level 4)
            final Field SDK_INT = Build.VERSION.class.getField("SDK_INT");
            return SDK_INT.getInt(null);
        } catch (SecurityException e) {
            return Integer.parseInt(Build.VERSION.SDK);
        } catch (NoSuchFieldException e) {
            return Integer.parseInt(Build.VERSION.SDK);
        } catch (IllegalArgumentException e) {
            return Integer.parseInt(Build.VERSION.SDK);
        } catch (IllegalAccessException e) {
            return Integer.parseInt(Build.VERSION.SDK);
        }
    }
}
