package au.com.xandar.admob.millennial;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import com.google.ads.AdSize;
import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.customevent.CustomEventBanner;
import com.google.ads.mediation.customevent.CustomEventBannerListener;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMAdViewSDK;

import java.util.Hashtable;

/**
 * Class to allow loading of Millennial banner ads unless MM-SDK-4.6.0 AND device is JellyBean AND targetSDK=17 in which case it returns no ad.
 * This is because Millennial 4.6.0 does not process clicks when targetSDK=17 on JellyBean devices (ie it can't run native JellyBean).
 *
 * Because this class is loaded by the Admob AdView via reflection you will need to include an exception for it in your Proguard config.
 * Eg -keep class au.com.xandar.admob.millennial.CustomMillennialAd
 */
public final class CustomMillennialAd implements CustomEventBanner {

    private static final String TAG = "AdmobCE.CustomMillennialAd";

    @Override
    public void requestBannerAd(final CustomEventBannerListener mediationListener,
                                Activity activity,
                                String label,
                                String millennialAppId,
                                AdSize adSize,
                                MediationAdRequest mediationAdRequest,
                                Object customEventExtra) {

        final boolean deviceIsJellyBeanOrGreater = (Build.VERSION.SDK_INT >= 16);
        Log.d(TAG, "#requestBannerAd androidVersion=" + Build.VERSION.SDK_INT + " deviceIsJellyBeanOrGreater=" + deviceIsJellyBeanOrGreater);

        // Only fails if targetSDK=17 (or greater?) AND device is Version=17 (or 16? ie all JellyBean)
        final boolean targetSDKIs17OrGreater = activity.getApplicationInfo().targetSdkVersion >= 17;
        Log.d(TAG, "#requestBannerAd targetSDK=" + activity.getApplicationInfo().targetSdkVersion + " targetSDKis17OrGreater=" + targetSDKIs17OrGreater);

        // THis is a problem with MM-4.6.0 - hopefully it will be fixed in a later version.
        final String mmSdkVersion = MMAdViewSDK.SDKVER.substring(0, 5);
        final int compareToVersion = mmSdkVersion.compareTo("4.6.0");
        final boolean millennialVersionIsFourPointSixPointZero = (compareToVersion <= 0);
        Log.d(TAG, "#requestBannerAd MillennialSDKVersion=" + MMAdViewSDK.SDKVER + " versionIs4.6.0(OrLower)=" + millennialVersionIsFourPointSixPointZero);

        // Barf now if AndroidVersion is JellyBean (16) or greater,
        // AND targetSDK is JellyBean (16) or greater
        // AND we are using MillennialSDK 4.6.0 or lower.

        if (deviceIsJellyBeanOrGreater && targetSDKIs17OrGreater && millennialVersionIsFourPointSixPointZero) {
            Log.d(TAG, "#requestBannerAd JellyBean or greater device - bailing now");
            mediationListener.onFailedToReceiveAd();
            return;
        }

        // Apply appropriate sizing to the ad.
        Log.d(TAG, "#requestBannerAd AdSize=" + adSize + " height=" + adSize.getHeight() + " width=" + adSize.getWidth());
        final String adHeight = Integer.toString(adSize.getHeight());
        final String adWidth = Integer.toString(adSize.getWidth());
        final Hashtable<String, String> metaMap = new Hashtable<String, String>();

        final MMAdView adView = new MMAdView(activity, millennialAppId, MMAdView.BANNER_AD_RECTANGLE, MMAdView.REFRESH_INTERVAL_OFF, metaMap, false);
        adView.setHeight(adHeight);
        adView.setWidth(adWidth);

        adView.setListener(new MMAdView.MMAdListener() {
            @Override
            public void MMAdCachingCompleted(MMAdView mmAdView, boolean b) {
                Log.d(TAG, "#cachingCompleted - nothing to do");
            }

            @Override
            public void MMAdReturned(MMAdView mmAdView) {
                Log.d(TAG, "#returned height=" + mmAdView.getHeight() + " width=" + mmAdView.getWidth() + " layoutParams#width=" + mmAdView.getLayoutParams().width + " layoutParams#height=" + mmAdView.getLayoutParams().height);

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
        adView.callForAd();
        Log.d(TAG, "#requestBannerAd after callForAd");
    }

    @Override
    public void destroy() {
        Log.d(TAG, "#destroy");
    }
}
