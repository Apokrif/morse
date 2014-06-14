package com.unilead;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.webkit.WebView;
//import com.mopub.mobileads.util.DateAndTime;
//import com.mopub.mobileads.util.VersionCode;
import org.apache.http.HttpResponse;

import java.io.*;
import java.util.*;

public class AdConfiguration implements Serializable {
    private static final int MINIMUM_REFRESH_TIME_MILLISECONDS = 10000;
    private static final int DEFAULT_REFRESH_TIME_MILLISECONDS = 60000;
    private static final String mPlatform = "Android";
    private final String mSdkVersion;

    private final String mHashedUdid;
    private final String mUserAgent;
    private final String mDeviceLocale;
    private final String mDeviceModel;
    private final int mPlatformVersion;

    private String mResponseString;
    private String mAdUnitId;

    private String mAdType;
    private String mNetworkType;
    private String mRedirectUrl;
    private String mClickthroughUrl;
    private String mFailUrl;
    private String mImpressionUrl;
    private long mTimeStamp;
    private int mWidth;
    private int mHeight;
    private Integer mAdTimeoutDelay;
    private int mRefreshTimeMilliseconds;
    private String mDspCreativeId;

    static AdConfiguration extractFromMap(Map<String,Object> map) {
        if (map == null) {
            return null;
        }

        Object adConfiguration = map.get(AdFetcher.AD_CONFIGURATION_KEY);

        if (adConfiguration instanceof AdConfiguration) {
            return (AdConfiguration) adConfiguration;
        }

        return null;
    }

    AdConfiguration(final Context context) {
        setDefaults();

        if (context != null) {
            String udid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            mHashedUdid = Utils.sha1((udid != null) ? udid : "");

            mUserAgent = new WebView(context).getSettings().getUserAgentString();
            mDeviceLocale = context.getResources().getConfiguration().locale.toString();
        } else {
            mHashedUdid = null;
            mUserAgent = null;
            mDeviceLocale = null;
        }

        mDeviceModel = Build.MANUFACTURER + " " + Build.MODEL;
        mPlatformVersion = VersionCode.currentApiLevel().getApiLevel();
        mSdkVersion = Utils.SDK_VERSION;
    }

    void cleanup() {
        setDefaults();
    }

    void addHttpResponse(final HttpResponse httpResponse) {
        mAdType = HttpResponses.extractHeader(httpResponse, ResponseHeader.AD_TYPE);

        // Set the network type of the ad.
        mNetworkType = HttpResponses.extractHeader(httpResponse, ResponseHeader.NETWORK_TYPE);

        // Set the redirect URL prefix: navigating to any matching URLs will send us to the browser.
        mRedirectUrl = HttpResponses.extractHeader(httpResponse, ResponseHeader.REDIRECT_URL);

        // Set the URL that is prepended to links for click-tracking purposes.
        mClickthroughUrl = HttpResponses.extractHeader(httpResponse, ResponseHeader.CLICKTHROUGH_URL);

        // Set the fall-back URL to be used if the current request fails.
        mFailUrl = HttpResponses.extractHeader(httpResponse, ResponseHeader.FAIL_URL);

        // Set the URL to be used for impression tracking.
        mImpressionUrl = HttpResponses.extractHeader(httpResponse, ResponseHeader.IMPRESSION_URL);

        // Set the time stamp used for Ad Alert Reporting.
        mTimeStamp = DateAndTime.now().getTime();

        // Set the width and height.
        mWidth = HttpResponses.extractIntHeader(httpResponse, ResponseHeader.WIDTH, 0);
        mHeight = HttpResponses.extractIntHeader(httpResponse, ResponseHeader.HEIGHT, 0);

        // Set the allowable amount of time an ad has before it automatically fails.
        mAdTimeoutDelay = HttpResponses.extractIntegerHeader(httpResponse, ResponseHeader.AD_TIMEOUT);

        // Set the auto-refresh time. A timer will be scheduled upon ad success or failure.
        if (!httpResponse.containsHeader(ResponseHeader.REFRESH_TIME.getKey())) {
            mRefreshTimeMilliseconds = 0;
        } else {
            mRefreshTimeMilliseconds = HttpResponses.extractIntHeader(httpResponse, ResponseHeader.REFRESH_TIME, 0) * 1000;
            mRefreshTimeMilliseconds = Math.max(
                    mRefreshTimeMilliseconds,
                    MINIMUM_REFRESH_TIME_MILLISECONDS);
        }

        // Set the unique identifier for the creative that was returned.
        mDspCreativeId = HttpResponses.extractHeader(httpResponse, ResponseHeader.DSP_CREATIVE_ID);
    }

    /*
     * View
     */

    String getAdUnitId() {
        return mAdUnitId;
    }

    void setAdUnitId(String adUnitId) {
        mAdUnitId = adUnitId;
    }

    String getResponseString() {
        return mResponseString;
    }

    void setResponseString(String responseString) {
        mResponseString = responseString;
    }

    /*
     * HttpResponse
     */

    String getAdType() {
        return mAdType;
    }

    String getNetworkType() {
        return mNetworkType;
    }

    String getRedirectUrl() {
        return mRedirectUrl;
    }

    String getClickthroughUrl() {
        return mClickthroughUrl;
    }

    @Deprecated
    void setClickthroughUrl(String clickthroughUrl) {
        mClickthroughUrl = clickthroughUrl;
    }

    String getFailUrl() {
        return mFailUrl;
    }

    void setFailUrl(String failUrl) {
        mFailUrl = failUrl;
    }

    String getImpressionUrl() {
        return mImpressionUrl;
    }

    long getTimeStamp() {
        return mTimeStamp;
    }

    int getWidth() {
        return mWidth;
    }

    int getHeight() {
        return mHeight;
    }

    Integer getAdTimeoutDelay() {
        return mAdTimeoutDelay;
    }

    int getRefreshTimeMilliseconds() {
        return mRefreshTimeMilliseconds;
    }

    @Deprecated
    void setRefreshTimeMilliseconds(int refreshTimeMilliseconds) {
        mRefreshTimeMilliseconds = refreshTimeMilliseconds;
    }

    String getDspCreativeId() {
        return mDspCreativeId;
    }

    /*
     * Context
     */

    String getHashedUdid() {
        return mHashedUdid;
    }

    String getUserAgent() {
        return mUserAgent;
    }

    String getDeviceLocale() {
        return mDeviceLocale;
    }

    String getDeviceModel() {
        return mDeviceModel;
    }

    int getPlatformVersion() {
        return mPlatformVersion;
    }

    String getPlatform() {
        return mPlatform;
    }

    /*
     * Misc.
     */

    String getSdkVersion() {
        return mSdkVersion;
    }

    private void setDefaults() {
        mAdUnitId = null;
        mResponseString = null;
        mAdType = null;
        mNetworkType = null;
        mRedirectUrl = null;
        mClickthroughUrl = null;
        mImpressionUrl = null;
        mTimeStamp = DateAndTime.now().getTime();
        mWidth = 0;
        mHeight = 0;
        mAdTimeoutDelay = null;
        mRefreshTimeMilliseconds = DEFAULT_REFRESH_TIME_MILLISECONDS;
        mFailUrl = null;
        mDspCreativeId = null;
    }
}
