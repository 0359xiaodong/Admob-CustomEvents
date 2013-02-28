package au.com.xandar.admob.greystripe;

import android.app.Activity;
import android.database.sqlite.SQLiteDiskIOException;
import android.os.Build;
import android.util.Log;
import au.com.xandar.admob.common.CustomEventConsts;
import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.customevent.CustomEventInterstitial;
import com.google.ads.mediation.customevent.CustomEventInterstitialListener;
import com.greystripe.sdk.GSAd;
import com.greystripe.sdk.GSAdErrorCode;
import com.greystripe.sdk.GSAdListener;
import com.greystripe.sdk.GSFullscreenAd;

/**
 * Provides a wrapper around a Greystripe interstitial so that it can be used with Admob mediation.
 */
public final class GreystripeInterstitialAd implements CustomEventInterstitial {

    private static final String TAG = "AdmobCE.GreystripeInterstitial";

    private GSFullscreenAd ad;

    @Override
    public void requestInterstitialAd(final CustomEventInterstitialListener mediationListener, Activity activity,
                                      String label, String greystripeApplicationId,
                                      MediationAdRequest mediationAdRequest, Object customEventExtra) {

        if (CustomEventConsts.DEBUG) Log.d(TAG, "#requestInterstitialAd greystripeApplicationId=" + greystripeApplicationId);

        // If less than the min API (7?) for Greystripe then bail immediately.
        final boolean deviceIsEclairOrGreater = (Build.VERSION.SDK_INT >= 7);
        if (!deviceIsEclairOrGreater) {
            mediationListener.onFailedToReceiveAd();
            return;
        }

        try {
            ad = new GSFullscreenAd(activity, greystripeApplicationId);
        } catch (SQLiteDiskIOException e) {

            // This catch was added because Greystripe-2.1 started occasionally throwing these errors for Gingerbread and up.
            // If configured to report the error then rethrow as an IllegalStateException. Default to rethrow.
            if (CustomEventConsts.THROW_GREYSTRIPE_INTERSTITIAL_CREATION_EXCEPTION) {
                throw new IllegalStateException("Could not construct GreystripeAd", e);
            }

            mediationListener.onFailedToReceiveAd();
            Log.e(TAG, "#requestInterstitialAd Could not construct GreystripeAd - bailing now", e);

            return;
        }

        final GSAdListener fullScreenAdListener = new GSAdListener() {
            @Override
            public void onFailedToFetchAd(GSAd gsAd, GSAdErrorCode errorCode) {
                if (CustomEventConsts.DEBUG) Log.d(TAG, "#onFailedToReceiveAd errorCode=" + errorCode);
                mediationListener.onFailedToReceiveAd();
            }

            @Override
            public void onFetchedAd(GSAd gsAd) {
                if (CustomEventConsts.DEBUG) Log.d(TAG, "#onFetchedAd");
                mediationListener.onReceivedAd();
            }

            @Override
            public void onAdClickthrough(GSAd gsAd) {
                if (CustomEventConsts.DEBUG) Log.d(TAG, "#onAdClickthrough");
            }

            @Override
            public void onAdDismissal(GSAd gsAd) {
                if (CustomEventConsts.DEBUG) Log.d(TAG, "#onAdDismissal");
                mediationListener.onDismissScreen();
            }

            @Override
            public void onAdExpansion(GSAd gsAd) {
                if (CustomEventConsts.DEBUG) Log.d(TAG, "#onAdExpansion");
                mediationListener.onPresentScreen();
            }

            @Override
            public void onAdCollapse(GSAd gsAd) {
                if (CustomEventConsts.DEBUG) Log.d(TAG, "#onAdCollapse");
            }
        };
        ad.addListener(fullScreenAdListener);

        if (CustomEventConsts.DEBUG) Log.d(TAG, "#requestIterstitialAd - fetch start");
        ad.fetch();
        if (CustomEventConsts.DEBUG) Log.d(TAG, "#requestInterstitialAd - fetch finish");
    }

    @Override
    public void showInterstitial() {
        if (ad == null) {
            if (CustomEventConsts.DEBUG) Log.d(TAG, "#showInterstitialAd - no ad was created - bailing");
        } else if (!ad.isAdReady()) {
            if (CustomEventConsts.DEBUG) Log.d(TAG, "#showInterstitialAd - no ad is ready - bailing");
        } else {
            if (CustomEventConsts.DEBUG) Log.d(TAG, "#showInterstitialAd - displaying ad");
            ad.display();
        }
    }

    @Override
    public void destroy() {
        ad = null;
    }
}
