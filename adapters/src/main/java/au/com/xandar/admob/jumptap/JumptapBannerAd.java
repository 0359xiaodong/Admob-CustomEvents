package au.com.xandar.admob.jumptap;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import au.com.xandar.admob.common.Consts;
import com.google.ads.AdSize;
import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.customevent.CustomEventBanner;
import com.google.ads.mediation.customevent.CustomEventBannerListener;
import com.jumptap.adtag.JtAdView;
import com.jumptap.adtag.JtAdViewListener;
import com.jumptap.adtag.JtAdWidgetSettings;
import com.jumptap.adtag.JtAdWidgetSettingsFactory;
import com.jumptap.adtag.utils.JtException;

/**
 * Class to allow loading of JumpTap banner ads from jumptap-2.4.1.2-117316 while dealing with as many of the errors from that library as possible.
 * See https://docs.google.com/spreadsheet/ccc?key=0AjAjgkxDofpudEJoQzFaQTR1WU85Mm42OWtvclJfY1E&usp=sharing
 *
 * Because this class is loaded by the Admob AdView via reflection you will need to include an exception for it in your Proguard config.
 * Eg -keep class au.com.xandar.admob.jumptap.JumptapBannerAd
 */
public final class JumptapBannerAd implements CustomEventBanner {

    private static final String TAG = "AdmobCE.JumptapBannerAd";

    private static class JumptapId {
        String publisherId;
        String siteId;
        String spotId;
    }

    @Override
    public void requestBannerAd(final CustomEventBannerListener mediationListener,
                                Activity activity,
                                String label,
                                String unparsedJumptapId,
                                AdSize adSize,
                                MediationAdRequest mediationAdRequest,
                                Object customEventExtra) {

        if (unparsedJumptapId == null || unparsedJumptapId.trim().equals("")) {
            if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd no JumptapId found - exiting");
            return;
        }

        // Split JumptapId into publisherId, siteId, spotId. Values should be separated by "/"
        final JumptapId jumptapId = parseJumptapId(unparsedJumptapId);

        final JtAdWidgetSettings settings = JtAdWidgetSettingsFactory.createWidgetSettings();
        settings.setPublisherId(jumptapId.publisherId);
        settings.setSpotId(jumptapId.siteId);
        settings.setSiteId(jumptapId.spotId);
        settings.setRefreshPeriod(0);

        //Copied from JumptapAdapter2.4.1.2.117316
        settings.setApplicationId("GWhirl Adapter");
        settings.setApplicationVersion("1.2.3");

        final CustomJtAdView_2_4_1_2_117316 adView;
        try {
            adView = new CustomJtAdView_2_4_1_2_117316(activity, settings);
        } catch (JtException e) {
            throw new IllegalStateException("Could not create JumptapAdView", e);
        }

        // Apply appropriate sizing to the ad.
        final int heightPixels = adSize.getHeightInPixels(activity);
        final int widthPixels = adSize.getWidthInPixels(activity);
        if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd AdSize=" + adSize + " width=" + adSize.getWidth() + "dp height=" + adSize.getHeight() + "dp");
        if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd width=" + widthPixels + "px height=" + heightPixels + "px");

        final LinearLayout.LayoutParams wrappedLayoutParams = new LinearLayout.LayoutParams(adSize.getWidth(), adSize.getHeight()); // use AdSize to determine width and height.
        wrappedLayoutParams.gravity = Gravity.CENTER;

        final LinearLayout wrappedAdView = new LinearLayout(activity);
        wrappedAdView.setLayoutParams(wrappedLayoutParams);
        wrappedAdView.addView(adView);

        adView.setAdViewListener(new JtAdViewListener() {

            @Override
            public void onNoAdFound(JtAdView jtAdView, int i) {
                if (Consts.DEBUG) Log.d(TAG, "#onNoAdFound");
                mediationListener.onFailedToReceiveAd();
            }

            @Override
            public void onFocusChange(JtAdView jtAdView, int i, boolean b) {
                if (Consts.DEBUG) Log.d(TAG, "#onFocusChange");
            }

            @Override
            public void onNewAd(JtAdView jtAdView, int i, String s) {
                // Hand back the wrapped view so we get the correct sizing and gravity
                if (Consts.DEBUG) Log.d(TAG, "#onNewAd height=" + jtAdView.getHeight() + " width=" + jtAdView.getWidth() + " layoutParams#width=" + jtAdView.getLayoutParams().width + " layoutParams#height=" + jtAdView.getLayoutParams().height);
                mediationListener.onReceivedAd(wrappedAdView);
            }

            @Override
            public void onAdError(JtAdView jtAdView, int i1, int i2) {
                if (Consts.DEBUG) Log.d(TAG, "#onAdError i1=" + i1 + " i2=" + i2 + " jtAdView=" + jtAdView);
            }

            @Override
            public void onInterstitialDismissed(JtAdView jtAdView, int i) {
                if (Consts.DEBUG) Log.d(TAG, "#onInterstitualDismissed");
                mediationListener.onDismissScreen();
            }

            @Override
            public void onBeginAdInteraction(JtAdView jtAdView, int i) {
                if (Consts.DEBUG) Log.d(TAG, "#onBeingAdInteraction");
                mediationListener.onPresentScreen();
            }

            @Override
            public void onEndAdInteraction(JtAdView jtAdView, int i) {
                if (Consts.DEBUG) Log.d(TAG, "#onEndAdInteraction");
                mediationListener.onDismissScreen();
            }

            @Override
            public void onHide(JtAdView jtAdView, int i) {
                if (Consts.DEBUG) Log.d(TAG, "#onHide");
            }

            @Override
            public void onExpand(JtAdView jtAdView, int i) {
                if (Consts.DEBUG) Log.d(TAG, "#onExpand");
            }

            @Override
            public void onContract(JtAdView jtAdView, int i) {
                if (Consts.DEBUG) Log.d(TAG, "#onContract");
            }

            @Override
            public void onBannerClicked(JtAdView jtAdView, int i) {
                if (Consts.DEBUG) Log.d(TAG, "#onBannerClicked");
                mediationListener.onClick();
            }

            @Override
            public void onLaunchActivity(JtAdView jtAdView, int i) {
                if (Consts.DEBUG) Log.d(TAG, "#onLaunchActivity");
                mediationListener.onPresentScreen();
            }

            @Override
            public void onReturnFromActivity(JtAdView jtAdView, int i) {
                if (Consts.DEBUG) Log.d(TAG, "#onReturnFromActivity");
                mediationListener.onDismissScreen();
            }
        });

        if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd before callForAd");
        adView.refreshAd();
        if (Consts.DEBUG) Log.d(TAG, "#requestBannerAd after callForAd");
    }

    @Override
    public void destroy() {
        if (Consts.DEBUG) Log.d(TAG, "#destroy");
    }

    private JumptapId parseJumptapId(String unparsedId) {
        final JumptapId jumptapId = new JumptapId();

        if (Consts.DEBUG) Log.d(TAG, "#parseJumptapId unparsedId='" + unparsedId + "'");

        // publisherId
        int stopToken;
        stopToken = unparsedId.indexOf("/");
        if (stopToken == -1) {
            jumptapId.publisherId = unparsedId;
            if (Consts.DEBUG) Log.d(TAG, "#parseJumptapId stopToken=" + stopToken + " publisherId=" + jumptapId.publisherId);
        } else {
            jumptapId.publisherId = unparsedId.substring(0, stopToken);
            if (Consts.DEBUG) Log.d(TAG, "#parseJumptapId stopToken=" + stopToken + " publisherId=" + jumptapId.publisherId);
            unparsedId = unparsedId.substring(stopToken + 1);

            // SiteId
            stopToken = unparsedId.indexOf("/");
            if (stopToken == -1) {
                jumptapId.siteId = unparsedId;
                if (Consts.DEBUG) Log.d(TAG, "#parseJumptapId stopToken=" + stopToken + " siteId=" + jumptapId.siteId);
            } else {
                jumptapId.siteId = unparsedId.substring(0, stopToken);
                if (Consts.DEBUG) Log.d(TAG, "#parseJumptapId stopToken=" + stopToken + " siteId=" + jumptapId.siteId);
                unparsedId = unparsedId.substring(stopToken + 1);
                jumptapId.spotId = unparsedId;
                if (Consts.DEBUG) Log.d(TAG, "#parseJumptapId spotId=" + jumptapId.spotId);
            }
        }

        return jumptapId;
    }
}
