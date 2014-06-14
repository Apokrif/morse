package com.unilead;

import com.unilead.Reflection.MethodBuilder;

import android.content.Context;

//import static com.mopub.mobileads.AdTypeTranslator.CustomEventType.ADMOB_INTERSTITIAL;
//import static com.mopub.mobileads.AdTypeTranslator.CustomEventType.GOOGLE_PLAY_BANNER;
//import static com.mopub.mobileads.AdTypeTranslator.CustomEventType.GOOGLE_PLAY_INTERSTITIAL;
//import static com.mopub.mobileads.util.Reflection.MethodBuilder;

public class AdTypeTranslator {
    private static final int GOOGLE_PLAY_SUCCESS_CODE = 0;

    public enum CustomEventType {
        ADMOB_BANNER("admob_native_banner", "com.mopub.mobileads.GoogleAdMobBanner"),
        ADMOB_INTERSTITIAL("admob_full_interstitial", "com.mopub.mobileads.GoogleAdMobInterstitial"),
        GOOGLE_PLAY_BANNER("google_play_banner", "com.mopub.mobileads.GooglePlayServicesBanner"),
        GOOGLE_PLAY_INTERSTITIAL("google_play_interstitial", "com.mopub.mobileads.GooglePlayServicesInterstitial"),
        MILLENNIAL_BANNER("millennial_native_banner", "com.mopub.mobileads.MillennialBanner"),
        MILLENNIAL_INTERSTITIAL("millennial_full_interstitial", "com.mopub.mobileads.MillennialInterstitial"),
        MRAID_BANNER("mraid_banner", "com.mopub.mobileads.MraidBanner"),
        MRAID_INTERSTITIAL("mraid_interstitial", "com.mopub.mobileads.MraidInterstitial"),
        HTML_BANNER("html_banner", "com.mopub.mobileads.HtmlBanner"),
        HTML_INTERSTITIAL("html_interstitial", "com.mopub.mobileads.HtmlInterstitial"),
        VAST_VIDEO_INTERSTITIAL("vast_interstitial", "com.mopub.mobileads.VastVideoInterstitial"),

        UNSPECIFIED("", null);

        private final String mKey;
        private final String mClassName;

        private CustomEventType(String key, String className) {
            mKey = key;
            mClassName = className;
        }

        private static CustomEventType fromString(String key) {
            for (CustomEventType customEventType : CustomEventType.values()) {
                if (customEventType.mKey.equals(key)) {
                    return customEventType;
                }
            }

            return UNSPECIFIED;
        }

        @Override
        public String toString() {
            return mClassName;
        }
    }

    static String getAdNetworkType(String adType, String fullAdType) {
        String adNetworkType = "interstitial".equals(adType) ? fullAdType : adType;
        return adNetworkType != null ? adNetworkType : "unknown";
    }

    static String getCustomEventNameForAdType(UnileadView moPubView, String adType, String fullAdType) {
        CustomEventType customEventType;

        if ("html".equals(adType) || "mraid".equals(adType)) {
            customEventType = (isInterstitial(moPubView))
                    ? CustomEventType.fromString(adType + "_interstitial")
                    : CustomEventType.fromString(adType + "_banner");
        } else {
            customEventType = ("interstitial".equals(adType))
                    ? CustomEventType.fromString(fullAdType + "_interstitial")
                    : CustomEventType.fromString(adType + "_banner");

            if (moPubView != null) {
                customEventType = convertAdMobToGooglePlay(moPubView.getContext(), customEventType);
            }
        }

        return customEventType.toString();
    }
  
    private static boolean isInterstitial(UnileadView moPubView) {
        return moPubView instanceof MoPubInterstitial.MoPubInterstitialView;
    }

    private static CustomEventType convertAdMobToGooglePlay(Context context, CustomEventType customEventType) {
        // In both cases, only check if GooglePlayServices is available if absolutely necessary
        if (customEventType == CustomEventType.ADMOB_BANNER &&
                classFound(CustomEventType.GOOGLE_PLAY_BANNER) &&
                isGooglePlayServicesAvailable(context)) {
            return CustomEventType.GOOGLE_PLAY_BANNER;
        } else if (customEventType == CustomEventType.ADMOB_INTERSTITIAL &&
                classFound(CustomEventType.GOOGLE_PLAY_INTERSTITIAL) &&
                isGooglePlayServicesAvailable(context)) {
            return CustomEventType.GOOGLE_PLAY_INTERSTITIAL;
        }

        return customEventType;
    }

    private static boolean classFound(CustomEventType customEventType) {
        try {
            Class.forName(customEventType.toString());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean isGooglePlayServicesAvailable(Context context) {
        try {
            MethodBuilder methodBuilder = new MethodBuilder(null, "isGooglePlayServicesAvailable")
                    .setStatic(Class.forName("com.google.android.gms.common.GooglePlayServicesUtil"))
                    .addParam(Context.class, context);

            Object result = methodBuilder.execute();

            return (result != null && (Integer) result == GOOGLE_PLAY_SUCCESS_CODE);
        } catch (Exception exception) {
            return false;
        }
    }
}
