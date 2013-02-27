package au.com.xandar.admob.millennial;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import au.com.xandar.admob.common.Consts;
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
public class MillennialBannerAd implements CustomEventBanner {

    private static final String TAG = "AdmobCE.MillennialBanner";

    @Override
    public void requestBannerAd(final CustomEventBannerListener mediationListener,
                                Activity activity,
                                String label,
                                String millennialAppId,
                                AdSize adSize,
                                MediationAdRequest mediationAdRequest,
                                Object customEventExtra) {

        final boolean deviceIsJellyBeanOrGreater = (Build.VERSION.SDK_INT >= 16);
        if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd androidVersion=" + Build.VERSION.SDK_INT + " deviceIsJellyBeanOrGreater=" + deviceIsJellyBeanOrGreater);

        // Only fails if targetSDK=17 (or greater?) AND device is Version=17 (or 16? ie all JellyBean)
        final boolean targetSDKIs17OrGreater = activity.getApplicationInfo().targetSdkVersion >= 17;
        if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd targetSDK=" + activity.getApplicationInfo().targetSdkVersion + " targetSDKis17OrGreater=" + targetSDKIs17OrGreater);

        // THis is a problem with MM-4.6.0 - hopefully it will be fixed in a later version.
        final String mmSdkVersion = MMAdViewSDK.SDKVER.substring(0, 5);
        final int compareToVersion = mmSdkVersion.compareTo("4.6.0");
        final boolean millennialVersionIsFourPointSixPointZero = (compareToVersion <= 0);
        if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd MillennialSDKVersion=" + MMAdViewSDK.SDKVER + " versionIs4.6.0(OrLower)=" + millennialVersionIsFourPointSixPointZero);

        // Barf now if AndroidVersion is JellyBean (16) or greater,
        // AND targetSDK is JellyBean (16) or greater
        // AND we are using MillennialSDK 4.6.0 or lower.

        if (deviceIsJellyBeanOrGreater && targetSDKIs17OrGreater && millennialVersionIsFourPointSixPointZero) {
            if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd JellyBean or greater device - Millennial-4.6.0 doesn't handle that well - bailing now");
            mediationListener.onFailedToReceiveAd();
            return;
        }

        final Hashtable<String, String> metaMap = new Hashtable<String, String>();
        final MMAdView adView = new MMAdView(activity, millennialAppId, MMAdView.BANNER_AD_RECTANGLE, MMAdView.REFRESH_INTERVAL_OFF, metaMap, false);
        adView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Apply appropriate sizing to the ad.
        if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd AdSize=" + adSize + " width=" + adSize.getWidth() + "dp height=" + adSize.getHeight() + "dp");
        if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd width=" + adSize.getWidthInPixels(activity) + "px height=" + adSize.getHeightInPixels(activity) + "px");

        final LinearLayout.LayoutParams wrappedLayoutParams = new LinearLayout.LayoutParams(adSize.getWidthInPixels(activity), adSize.getHeightInPixels(activity));
        wrappedLayoutParams.gravity = Gravity.CENTER;

        final LinearLayout wrappedAdView = new LinearLayout(activity);
        wrappedAdView.setLayoutParams(wrappedLayoutParams);
        wrappedAdView.addView(adView);

        adView.setListener(new MMAdView.MMAdListener() {
            @Override
            public void MMAdCachingCompleted(MMAdView mmAdView, boolean b) {
                if (Consts.DEBUG) Log.d(TAG, "#adCachingCompleted - nothing to do");
            }

            @Override
            public void MMAdReturned(MMAdView mmAdView) {
                // Hand back the wrapped view so we get the correct sizing and gravity
                if (Consts.DEBUG) Log.d(TAG, "#adReturned height=" + mmAdView.getHeight() + " width=" + mmAdView.getWidth() + " layoutParams#width=" + mmAdView.getLayoutParams().width + " layoutParams#height=" + mmAdView.getLayoutParams().height);
                mediationListener.onReceivedAd(wrappedAdView);
            }

            @Override
            public void MMAdFailed(MMAdView mmAdView) {
                if (Consts.DEBUG) Log.d(TAG, "#adFailed");
                mediationListener.onFailedToReceiveAd();
            }

            @Override
            public void MMAdClickedToOverlay(MMAdView mmAdView) {
                if (Consts.DEBUG) Log.d(TAG, "#adClickedToOverlay");
                mediationListener.onClick();
                mediationListener.onPresentScreen();
            }

            @Override
            public void MMAdOverlayLaunched(MMAdView mmAdView) {
                if (Consts.DEBUG) Log.d(TAG, "#adOverlayLaunched (automatically) - nothing to do");
            }

            @Override
            public void MMAdRequestIsCaching(MMAdView mmAdView) {
                if (Consts.DEBUG) Log.d(TAG, "#adRequestIsCaching - nothing to do");
            }
        });

        if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd before callForAd");
        adView.callForAd();
        if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd after callForAd");
    }

    @Override
    public void destroy() {
        if (Consts.DEBUG) Log.d(TAG, "#destroy");
    }
}
