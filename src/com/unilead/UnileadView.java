package com.unilead;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebViewDatabase;
import android.widget.FrameLayout;
import java.util.*;

public class UnileadView extends FrameLayout {

    public interface BannerAdListener {
        public void onBannerLoaded(UnileadView banner);
        public void onBannerFailed(UnileadView banner, ErrorCode errorCode);
        public void onBannerClicked(UnileadView banner);
        public void onBannerExpanded(UnileadView banner);
        public void onBannerCollapsed(UnileadView banner);
    }

    /**
     * The ability to determine geographical position
     * @author Apocrypha
     *
     */
    public enum LocationAwareness {
        NORMAL, TRUNCATED, DISABLED
    }

    public static final String HOST = "ads.unilead.com";
    public static final String HOST_FOR_TESTING = "testing.ads.unilead.com";
    public static final String AD_HANDLER = "/m/ad";
    public static final int DEFAULT_LOCATION_PRECISION = 6;

    protected Engine engine;
    protected CustomEventBannerAdapter mCustomEventBannerAdapter;

    private Context mContext;
    private BroadcastReceiver mScreenStateReceiver;
    private boolean mIsInForeground;
    private LocationAwareness mLocationAwareness;
    private boolean mPreviousAutorefreshSetting = false;
    
    private BannerAdListener mBannerAdListener;
    
    /*
    private OnAdWillLoadListener mOnAdWillLoadListener;
    private OnAdLoadedListener mOnAdLoadedListener;
    private OnAdFailedListener mOnAdFailedListener;
    private OnAdPresentedOverlayListener mOnAdPresentedOverlayListener;
    private OnAdClosedListener mOnAdClosedListener;
    private OnAdClickedListener mOnAdClickedListener;
	*/
    
    public UnileadView(Context context) {
        this(context, null);
    }

    public UnileadView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mIsInForeground = (getVisibility() == VISIBLE);
        mLocationAwareness = LocationAwareness.NORMAL;

        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);

        // There is a rare bug in Froyo/2.2 where creation of a WebView causes a
        // NullPointerException. (http://code.google.com/p/android/issues/detail?id=10789)
        // It happens when the WebView can't access the local file store to make a cache file.
        // Here, we'll work around it by trying to create a file store and then just go inert
        // if it's not accessible.
        if (WebViewDatabase.getInstance(context) == null) {
            Logger.e("Disabling Unilead SDK. Local cache file is inaccessible so SDK will " +
                    "fail if we try to create a WebView. Details of this Android bug found at:" +
                    "http://code.google.com/p/android/issues/detail?id=10789");
            return;
        }

        engine = Engine.Factory.create(context, this);
        registerScreenStateBroadcastReceiver();
    }

    private void registerScreenStateBroadcastReceiver() {
        if (engine == null) return;

        mScreenStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    if (mIsInForeground) {
                        Logger.d("Screen sleep with ad in foreground, disable refresh");
                        if (engine != null) {
                            mPreviousAutorefreshSetting = engine.getAutorefreshEnabled();
                            engine.setAutorefreshEnabled(false);
                        }
                    } else {
                        Logger.d("Screen sleep but ad in background; " +
                                "refresh should already be disabled");
                    }
                } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    if (mIsInForeground) {
                        Logger.d("Screen wake / ad in foreground, reset refresh");
                        if (engine != null) {
                            engine.setAutorefreshEnabled(mPreviousAutorefreshSetting);
                        }
                    } else {
                        Logger.d("Screen wake but ad in background; don't enable refresh");
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mScreenStateReceiver, filter);
    }

    private void unregisterScreenStateBroadcastReceiver() {
        try {
            mContext.unregisterReceiver(mScreenStateReceiver);
        } catch (Exception IllegalArgumentException) {
            Logger.d("Failed to unregister screen state broadcast receiver (never registered).");
        }
    }

	/**
	 * Load content
	 */
	public void loadAd() {
		if (engine != null)
			engine.loadAd();
	}

    /*
     * Tears down the ad view: no ads will be shown once this method executes. The parent
     * Activity's onDestroy implementation must include a call to this method.
     */
    public void destroy() {
        unregisterScreenStateBroadcastReceiver();
        removeAllViews();

        if (engine != null) {
            engine.cleanup();
            engine = null;
        }

        if (mCustomEventBannerAdapter != null) {
            mCustomEventBannerAdapter.invalidate();
            mCustomEventBannerAdapter = null;
        }
    }

    Integer getAdTimeoutDelay() {
        return (engine != null) ? engine.getAdTimeoutDelay() : null;
    }

    protected void loadFailUrl(ErrorCode errorCode) {
        if (engine != null) engine.loadFailUrl(errorCode);
    }

    protected void loadCustomEvent(Map<String, String> paramsMap) {
        if (paramsMap == null) {
            Logger.d("Couldn't invoke custom event because the server did not specify one.");
            loadFailUrl(ErrorCode.ADAPTER_NOT_FOUND);
            return;
        }

        if (mCustomEventBannerAdapter != null) {
            mCustomEventBannerAdapter.invalidate();
        }

        Logger.d("Loading custom event adapter.");

        mCustomEventBannerAdapter = CustomEventBannerAdapter.Factory.create(
                this,
                paramsMap.get(ResponseHeader.CUSTOM_EVENT_NAME.getKey()),
                paramsMap.get(ResponseHeader.CUSTOM_EVENT_DATA.getKey()));
        mCustomEventBannerAdapter.loadAd();
    }

    protected void registerClick() {
        if (engine != null) {
            engine.registerClick();

            // Let any listeners know that an ad was clicked
            adClicked();
        }
    }

    protected void trackNativeImpression() {
        Logger.d("Tracking impression for native adapter.");
        if (engine != null) engine.trackImpression();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (engine == null) return;

        if (visibility == VISIBLE) {
            Logger.d("Ad Unit ("+ engine.getAdUnitId()+") going visible: enabling refresh");
            mIsInForeground = true;
            engine.setAutorefreshEnabled(true);
        }
        else {
            Logger.d("Ad Unit ("+ engine.getAdUnitId()+") going invisible: disabling refresh");
            mIsInForeground = false;
            engine.setAutorefreshEnabled(false);
        }
    }

    protected void adLoaded() {
        Logger.d("adLoaded");
        
        if (mBannerAdListener != null) {
            mBannerAdListener.onBannerLoaded(this);
        } 
    }

    protected void adFailed(ErrorCode errorCode) {
        if (mBannerAdListener != null) {
            mBannerAdListener.onBannerFailed(this, errorCode);
        } 
    }

    protected void adPresentedOverlay() {
        if (mBannerAdListener != null) {
            mBannerAdListener.onBannerExpanded(this);
        } 
    }

    protected void adClosed() {
        if (mBannerAdListener != null) {
            mBannerAdListener.onBannerCollapsed(this);
        } 
    }

    protected void adClicked() {
        if (mBannerAdListener != null) {
            mBannerAdListener.onBannerClicked(this);
        } 
    }

    protected void nativeAdLoaded() {
        if (engine != null) engine.scheduleRefresh();
        adLoaded();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public String getAdUnitId() {
        return (engine != null) ? engine.getAdUnitId() : null;
    }

    public void setKeywords(String keywords) {
        if (engine != null) engine.setKeywords(keywords);
    }

    public String getKeywords() {
        return (engine != null) ? engine.getKeywords() : null;
    }

    public void setFacebookSupported(boolean enabled) {
        if (engine != null) engine.setFacebookSupported(enabled);
    }

    public boolean isFacebookSupported() {
        return (engine != null) ? engine.isFacebookSupported() : false;
    }

    public void setLocation(Location location) {
        if (engine != null) engine.setLocation(location);
    }

    public Location getLocation() {
        return (engine != null) ? engine.getLocation() : null;
    }

    public void setTimeout(int milliseconds) {
        if (engine != null) engine.setTimeout(milliseconds);
    }

    public int getAdWidth() {
        return (engine != null) ? engine.getAdWidth() : 0;
    }

    public int getAdHeight() {
        return (engine != null) ? engine.getAdHeight() : 0;
    }

    public String getResponseString() {
        return (engine != null) ? engine.getResponseString() : null;
    }

    public void setClickthroughUrl(String url) {
        if (engine != null) engine.setClickthroughUrl(url);
    }

    public String getClickthroughUrl() {
        return (engine != null) ? engine.getClickthroughUrl() : null;
    }

    public Activity getActivity() {
        return (Activity) mContext;
    }

    public void setBannerAdListener(BannerAdListener listener) {
        mBannerAdListener = listener;
    }

    public BannerAdListener getBannerAdListener() {
        return mBannerAdListener;
    }

    public void setLocationAwareness(LocationAwareness awareness) {
        mLocationAwareness = awareness;
    }

    public LocationAwareness getLocationAwareness() {
        return mLocationAwareness;
    }

    public void setLocationPrecision(int precision) {
        if (engine != null) {
            engine.setLocationPrecision(precision);
        }
    }

    public int getLocationPrecision() {
        return (engine != null) ? engine.getLocationPrecision() : 0;
    }

    public void setLocalExtras(Map<String, Object> localExtras) {
        if (engine != null) engine.setLocalExtras(localExtras);
    }

    public Map<String, Object> getLocalExtras() {
        if (engine != null) return engine.getLocalExtras();
        return Collections.emptyMap();
    }

    public void setAutorefreshEnabled(boolean enabled) {
        if (engine != null) engine.setAutorefreshEnabled(enabled);
    }

    public boolean getAutorefreshEnabled() {
        if (engine != null) return engine.getAutorefreshEnabled();
        else {
            Logger.d("Can't get autorefresh status for destroyed MoPubView. " +
                    "Returning false.");
            return false;
        }
    }

    public void setAdContentView(View view) {
        if (engine != null) engine.setAdContentView(view);
    }

    public void setTesting(boolean testing) {
        if (engine != null) engine.setTesting(testing);
    }

	public boolean getTesting() {
		if (engine != null)
			return engine.getTesting();
		else {
			Logger.d("Can't get testing status for destroyed MoPubView. "
					+ "Returning false.");
			return false;
		}
    }

    public void forceRefresh() {
        if (mCustomEventBannerAdapter != null) {
            mCustomEventBannerAdapter.invalidate();
            mCustomEventBannerAdapter = null;
        }

        if (engine != null) engine.forceRefresh();
    }

    Engine getAdViewController() {
        return engine;
    }

	public void initialize(String adSlotId) {
		if (engine == null)
			return;
		engine.setAdUnitId(adSlotId);
	}
}
