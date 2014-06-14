package com.unilead;

import android.content.Context;
import android.os.Handler;
import java.util.*;

import com.unilead.CustomEventInterstitial.CustomEventInterstitialListener;
//import com.unilead.CustomEventInterstitial.Factory;

public class CustomEventInterstitialAdapter implements CustomEventInterstitialListener {
    public static final int DEFAULT_INTERSTITIAL_TIMEOUT_DELAY = 30000;

    private final MoPubInterstitial mMoPubInterstitial;
    private boolean mInvalidated;
    private CustomEventInterstitialAdapterListener mCustomEventInterstitialAdapterListener;
    private CustomEventInterstitial mCustomEventInterstitial;
    private Context mContext;
    private Map<String, Object> mLocalExtras;
    private Map<String, String> mServerExtras;
    private final Handler mHandler;
    private final Runnable mTimeout;

    public CustomEventInterstitialAdapter(MoPubInterstitial moPubInterstitial, String className, String jsonParams) {
        mHandler = new Handler();
        mMoPubInterstitial = moPubInterstitial;
        mServerExtras = new HashMap<String, String>();
        mLocalExtras = new HashMap<String, Object>();
        mContext = moPubInterstitial.getActivity();
        mTimeout = new Runnable() {
            @Override
            public void run() {
                Logger.d("Third-party network timed out.");
                onInterstitialFailed(ErrorCode.NETWORK_TIMEOUT);
                invalidate();
            }
        };

        Logger.d("Attempting to invoke custom event: " + className);
        try {
            mCustomEventInterstitial = CustomEventInterstitial.Factory.create(className);
        } catch (Exception exception) {
            Logger.d("Couldn't locate or instantiate custom event: " + className + ".");
            if (mCustomEventInterstitialAdapterListener != null) mCustomEventInterstitialAdapterListener.onCustomEventInterstitialFailed(ErrorCode.ADAPTER_NOT_FOUND);
        }
        
        // Attempt to load the JSON extras into mServerExtras.
        try {
            mServerExtras = Json.jsonStringToMap(jsonParams);
        } catch (Exception exception) {
            Logger.d("Failed to create Map from JSON: " + jsonParams);
        }
        
        mLocalExtras = moPubInterstitial.getLocalExtras();
        if (moPubInterstitial.getLocation() != null) {
            mLocalExtras.put("location", moPubInterstitial.getLocation());
        }

        Engine adViewController = moPubInterstitial.getMoPubInterstitialView().getAdViewController();
        if (adViewController != null) {
            mLocalExtras.put(AdFetcher.AD_CONFIGURATION_KEY, adViewController.getAdConfiguration());
        }
    }
    
    void loadInterstitial() {
        if (isInvalidated() || mCustomEventInterstitial == null) {
            return;
        }

        if (getTimeoutDelayMilliseconds() > 0) {
            mHandler.postDelayed(mTimeout, getTimeoutDelayMilliseconds());
        }

        mCustomEventInterstitial.loadInterstitial(mContext, this, mLocalExtras, mServerExtras);
    }
    
    void showInterstitial() {
        if (isInvalidated() || mCustomEventInterstitial == null) return;
        
        mCustomEventInterstitial.showInterstitial();
    }

    void invalidate() {
        if (mCustomEventInterstitial != null) mCustomEventInterstitial.onInvalidate();
        mCustomEventInterstitial = null;
        mContext = null;
        mServerExtras = null;
        mLocalExtras = null;
        mCustomEventInterstitialAdapterListener = null;
        mInvalidated = true;
    }

    boolean isInvalidated() {
        return mInvalidated;
    }

    void setAdapterListener(CustomEventInterstitialAdapterListener listener) {
        mCustomEventInterstitialAdapterListener = listener;
    }

    private void cancelTimeout() {
        mHandler.removeCallbacks(mTimeout);
    }

    private int getTimeoutDelayMilliseconds() {
        if (mMoPubInterstitial == null
                || mMoPubInterstitial.getAdTimeoutDelay() == null
                || mMoPubInterstitial.getAdTimeoutDelay() < 0) {
            return DEFAULT_INTERSTITIAL_TIMEOUT_DELAY;
        }

        return mMoPubInterstitial.getAdTimeoutDelay() * 1000;
    }

    interface CustomEventInterstitialAdapterListener {
        void onCustomEventInterstitialLoaded();
        void onCustomEventInterstitialFailed(ErrorCode errorCode);
        void onCustomEventInterstitialShown();
        void onCustomEventInterstitialClicked();
        void onCustomEventInterstitialDismissed();
    }

    /*
     * CustomEventInterstitial.Listener implementation
     */
    @Override
    public void onInterstitialLoaded() {
        if (isInvalidated()) {
            return;
        }

        cancelTimeout();

        if (mCustomEventInterstitialAdapterListener != null) {
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialLoaded();
        }
    }

    @Override
    public void onInterstitialFailed(ErrorCode errorCode) {
        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitialAdapterListener != null) {
            if (errorCode == null) {
                errorCode = ErrorCode.UNSPECIFIED;
            }
            cancelTimeout();
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialFailed(errorCode);
        }
    }

    @Override
    public void onInterstitialShown() {
        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitialAdapterListener != null) {
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialShown();
        }
    }

    @Override
    public void onInterstitialClicked() {
        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitialAdapterListener != null) {
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialClicked();
        }
    }

    @Override
    public void onLeaveApplication() {
        onInterstitialClicked();
    }

    @Override
    public void onInterstitialDismissed() {
        if (isInvalidated()) return;

        if (mCustomEventInterstitialAdapterListener != null) mCustomEventInterstitialAdapterListener.onCustomEventInterstitialDismissed();
    }

    @Deprecated
    void setCustomEventInterstitial(CustomEventInterstitial interstitial) {
        mCustomEventInterstitial = interstitial;
    }
    public static class Factory {
        protected static Factory instance = new Factory();

        @Deprecated // for testing
        public static void setInstance(Factory factory) {
            instance = factory;
        }

        public static CustomEventInterstitialAdapter create(MoPubInterstitial moPubInterstitial, String className, String classData) {
            return instance.internalCreate(moPubInterstitial, className, classData);
        }

        protected CustomEventInterstitialAdapter internalCreate(MoPubInterstitial moPubInterstitial, String className, String classData) {
            return new CustomEventInterstitialAdapter(moPubInterstitial, className, classData);
        }
    }

}
