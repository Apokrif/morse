package com.unilead;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

//import com.mopub.mobileads.factories.CustomEventInterstitialAdapterFactory;
import com.unilead.UnileadView.LocationAwareness;

import java.util.*;

public class MoPubInterstitial implements CustomEventInterstitialAdapter.CustomEventInterstitialAdapterListener {

    private enum InterstitialState {
        CUSTOM_EVENT_AD_READY,
        NOT_READY;

        boolean isReady() {
            return this != InterstitialState.NOT_READY;
        }
    }

    private MoPubInterstitialView mInterstitialView;
    private CustomEventInterstitialAdapter mCustomEventInterstitialAdapter;
    private InterstitialAdListener mInterstitialAdListener;
    private Activity mActivity;
    private String mAdUnitId;
    private InterstitialState mCurrentInterstitialState;
    private boolean mIsDestroyed;

    public interface InterstitialAdListener {
        public void onInterstitialLoaded(MoPubInterstitial interstitial);
        public void onInterstitialFailed(MoPubInterstitial interstitial, ErrorCode errorCode);
        public void onInterstitialShown(MoPubInterstitial interstitial);
        public void onInterstitialClicked(MoPubInterstitial interstitial);
        public void onInterstitialDismissed(MoPubInterstitial interstitial);
    }

    private MoPubInterstitialListener mListener;

    @Deprecated
    public interface MoPubInterstitialListener {
        public void OnInterstitialLoaded();
        public void OnInterstitialFailed();
    }

    public MoPubInterstitial(Activity activity, String id) {
        mActivity = activity;
        mAdUnitId = id;

        mInterstitialView = new MoPubInterstitialView(mActivity);
        mInterstitialView.initialize(mAdUnitId);

        mCurrentInterstitialState = InterstitialState.NOT_READY;

    }

    public void load() {
        resetCurrentInterstitial();
        mInterstitialView.loadAd();
    }

    public void forceRefresh() {
        resetCurrentInterstitial();
        mInterstitialView.forceRefresh();
    }

    private void resetCurrentInterstitial() {
        mCurrentInterstitialState = InterstitialState.NOT_READY;

        if (mCustomEventInterstitialAdapter != null) {
            mCustomEventInterstitialAdapter.invalidate();
            mCustomEventInterstitialAdapter = null;
        }

        mIsDestroyed = false;
    }

    public boolean isReady() {
        return mCurrentInterstitialState.isReady();
    }

    boolean isDestroyed() {
        return mIsDestroyed;
    }

    public boolean show() {
        switch (mCurrentInterstitialState) {
            case CUSTOM_EVENT_AD_READY:
                showCustomEventInterstitial();
                return true;
        }
        return false;
    }

    private void showCustomEventInterstitial() {
        if (mCustomEventInterstitialAdapter != null) mCustomEventInterstitialAdapter.showInterstitial();
    }

    Integer getAdTimeoutDelay() {
        return mInterstitialView.getAdTimeoutDelay();
    }

    MoPubInterstitialView getMoPubInterstitialView() {
        return mInterstitialView;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void setKeywords(String keywords) {
        mInterstitialView.setKeywords(keywords);
    }

    public String getKeywords() {
        return mInterstitialView.getKeywords();
    }

    public void setFacebookSupported(boolean enabled) {
        mInterstitialView.setFacebookSupported(enabled);
    }

    public boolean isFacebookSupported() {
        return mInterstitialView.isFacebookSupported();
    }

    public Activity getActivity() {
        return mActivity;
    }

    public Location getLocation() {
        return mInterstitialView.getLocation();
    }

    public void destroy() {
        mIsDestroyed = true;

        if (mCustomEventInterstitialAdapter != null) {
            mCustomEventInterstitialAdapter.invalidate();
            mCustomEventInterstitialAdapter = null;
        }

        mInterstitialView.setBannerAdListener(null);
        mInterstitialView.destroy();
    }

    public void setInterstitialAdListener(InterstitialAdListener listener) {
        mInterstitialAdListener = listener;
    }

    public InterstitialAdListener getInterstitialAdListener() {
        return mInterstitialAdListener;
    }

    public void setLocationAwareness(LocationAwareness awareness) {
        mInterstitialView.setLocationAwareness(awareness);
    }

    public LocationAwareness getLocationAwareness() {
        return mInterstitialView.getLocationAwareness();
    }

    public void setLocationPrecision(int precision) {
        mInterstitialView.setLocationPrecision(precision);
    }

    public int getLocationPrecision() {
        return mInterstitialView.getLocationPrecision();
    }

    public void setTesting(boolean testing) {
        mInterstitialView.setTesting(testing);
    }

    public boolean getTesting() {
        return mInterstitialView.getTesting();
    }

    public void setLocalExtras(Map<String, Object> extras) {
        mInterstitialView.setLocalExtras(extras);
    }

    public Map<String, Object> getLocalExtras() {
        return mInterstitialView.getLocalExtras();
    }

    /*
     * Implements CustomEventInterstitialAdapter.CustomEventInterstitialListener
     */

    @Override
    public void onCustomEventInterstitialLoaded() {
        if (mIsDestroyed) return;

        mCurrentInterstitialState = InterstitialState.CUSTOM_EVENT_AD_READY;

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialLoaded(this);
        } else if (mListener != null) {
            mListener.OnInterstitialLoaded();
        }
    }

    @Override
    public void onCustomEventInterstitialFailed(ErrorCode errorCode) {
        if (isDestroyed()) return;

        mCurrentInterstitialState = InterstitialState.NOT_READY;
        mInterstitialView.loadFailUrl(errorCode);
    }

    @Override
    public void onCustomEventInterstitialShown() {
        if (isDestroyed()) return;

        mInterstitialView.trackImpression();

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialShown(this);
        }
    }

    @Override
    public void onCustomEventInterstitialClicked() {
        if (isDestroyed()) return;

        mInterstitialView.registerClick();

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialClicked(this);
        }
    }

    @Override
    public void onCustomEventInterstitialDismissed() {
        if (isDestroyed()) return;

        mCurrentInterstitialState = InterstitialState.NOT_READY;

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialDismissed(this);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public class MoPubInterstitialView extends UnileadView {

        public MoPubInterstitialView(Context context) {
            super(context);
            setAutorefreshEnabled(false);
        }

        @Override
        protected void loadCustomEvent(Map<String, String> paramsMap) {
            if (paramsMap == null) {
                Logger.d("Couldn't invoke custom event because the server did not specify one.");
                loadFailUrl(ErrorCode.ADAPTER_NOT_FOUND);
                return;
            }

            if (mCustomEventInterstitialAdapter != null) {
                mCustomEventInterstitialAdapter.invalidate();
            }

            Logger.d("Loading custom event interstitial adapter.");

            mCustomEventInterstitialAdapter = CustomEventInterstitialAdapter.Factory.create(
                    MoPubInterstitial.this,
                    paramsMap.get(ResponseHeader.CUSTOM_EVENT_NAME.getKey()),
                    paramsMap.get(ResponseHeader.CUSTOM_EVENT_DATA.getKey()));
            mCustomEventInterstitialAdapter.setAdapterListener(MoPubInterstitial.this);
            mCustomEventInterstitialAdapter.loadInterstitial();
        }

        protected void trackImpression() {
            Logger.d("Tracking impression for interstitial.");
            if (engine != null) engine.trackImpression();
        }

        @Override
        protected void adFailed(ErrorCode errorCode) {
            if (mInterstitialAdListener != null) {
                mInterstitialAdListener.onInterstitialFailed(MoPubInterstitial.this, errorCode);
            }
        }
    }

    @Deprecated // for testing
    void setInterstitialView(MoPubInterstitialView interstitialView) {
        mInterstitialView = interstitialView;
    }

    @Deprecated
    public void setListener(MoPubInterstitialListener listener) {
        mListener = listener;
    }

    @Deprecated
    public MoPubInterstitialListener getListener() {
        return mListener;
    }

    @Deprecated
    public void customEventDidLoadAd() {
        if (mInterstitialView != null) mInterstitialView.trackImpression();
    }

    @Deprecated
    public void customEventDidFailToLoadAd() {
        if (mInterstitialView != null) mInterstitialView.loadFailUrl(ErrorCode.UNSPECIFIED);
    }

    @Deprecated
    public void customEventActionWillBegin() {
        if (mInterstitialView != null) mInterstitialView.registerClick();
    }
}
