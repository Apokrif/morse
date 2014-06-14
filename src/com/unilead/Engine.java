package com.unilead;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executor;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.unilead.PostTask;
import org.unilead.openrtb.common.api.Bid;

import com.mopub.mobileads.factories.HttpClientFactory;
import com.mopub.mobileads.util.Dips;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.unilead.Reflection.MethodBuilder;
import com.unilead.UnileadView.LocationAwareness;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static com.mopub.mobileads.MoPubView.DEFAULT_LOCATION_PRECISION;
	
public class Engine {
    static final int MINIMUM_REFRESH_TIME_MILLISECONDS = 10000;
    static final int DEFAULT_REFRESH_TIME_MILLISECONDS = 60000;
    private static final FrameLayout.LayoutParams WRAP_AND_CENTER_LAYOUT_PARAMS =
            new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER);
    private static WeakHashMap<View,Boolean> sViewShouldHonorServerDimensions = new WeakHashMap<View, Boolean>();

    private final Context mContext;
    private UnileadView mView;
    private final AdUrlGenerator mUrlGenerator;
    private AdFetcher mAdFetcher;
    private AdConfiguration mAdConfiguration;
    private final Runnable refreshRunnable;

    private boolean mIsDestroyed;
    private Handler mHandler;
    private boolean mIsLoading;
    private String mUrl;

    private Map<String, Object> mLocalExtras = new HashMap<String, Object>();
    private boolean mAutoRefreshEnabled = true;
    private String mKeywords;
    private Location mLocation;
    private LocationAwareness mLocationAwareness = LocationAwareness.NORMAL;
    private int mLocationPrecision = DEFAULT_LOCATION_PRECISION;
    private boolean mIsFacebookSupported = true;
    private boolean mIsTesting;
    
	public ImageLoader imageLoader = ImageLoader.getInstance();
	private PostTask postTask;
	public Bid bid;

    protected static void setShouldHonorServerDimensions(View view) {
        sViewShouldHonorServerDimensions.put(view, true);
    }

    private static boolean getShouldHonorServerDimensions(View view) {
        return sViewShouldHonorServerDimensions.get(view) != null;
    }

    public Engine(Context context, UnileadView view) {
        mContext = context;
        mView = view;

        mUrlGenerator = new AdUrlGenerator(context);
        mAdConfiguration = new AdConfiguration(mContext);

        mAdFetcher = AdFetcher.Factory.create(this, mAdConfiguration.getUserAgent());

        refreshRunnable = new Runnable() {
            public void run() {
                loadAd();
            }
        };

        mHandler = new Handler();
	
		postTask = new PostTask();
		postTask.initialize(this);

        imageLoader = ImageLoader.getInstance();
		imageLoader.init(ImageLoaderConfiguration.createDefault(context));
    }

    public UnileadView getMoPubView() {
        return mView;
    }

    public void loadAd() {
        if (mAdConfiguration.getAdUnitId() == null) {
            Logger.d("Can't load an ad in this ad view because the ad unit ID is null. " +
                    "Did you forget to call initilize()?");
            return;
        }

        if (!isNetworkAvailable()) {
            Logger.d("Can't load an ad because there is no network connectivity.");
            scheduleRefresh();
            return;
        }

        if (mLocation == null) {
            mLocation = getLastKnownLocation();
        }

        // tested (remove me when the rest of this is tested)
        //String adUrl = generateAdUrl();
        //
        String adUrl = "http://dsp.unileadmedia.com/images/320x50-3.jpg";
        loadNonJavascript(adUrl);
        
        try {
			safeExecuteOnExecutor(postTask);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    void loadNonJavascript(String url) {
        if (url == null) return;

        Logger.d("Loading url: " + url);
        if (mIsLoading) {
            if (mAdConfiguration.getAdUnitId() != null) {
                Logger.i("Already loading an ad for " + mAdConfiguration.getAdUnitId() + ", wait to finish.");
            }
            return;
        }

        mUrl = url;
        mAdConfiguration.setFailUrl(null);
        mIsLoading = true;

        fetchAd(mUrl);
    }

    public void reload() {
        Logger.d("Reload ad: " + mUrl);
        loadNonJavascript(mUrl);
    }

    void loadFailUrl(ErrorCode errorCode) {
        mIsLoading = false;

        Logger.v("ErrorCode: " + (errorCode == null ? "" : errorCode.toString()));

        if (mAdConfiguration.getFailUrl() != null) {
            Logger.d("Loading failover url: " + mAdConfiguration.getFailUrl());
            loadNonJavascript(mAdConfiguration.getFailUrl());
        } else {
            // No other URLs to try, so signal a failure.
            adDidFail(ErrorCode.NO_FILL);
        }
    }

    void setFailUrl(String failUrl) {
        mAdConfiguration.setFailUrl(failUrl);
    }

    void setNotLoading() {
        this.mIsLoading = false;
    }

    public String getKeywords() {
        return mKeywords;
    }

    public void setKeywords(String keywords) {
        mKeywords = keywords;
    }

    public boolean isFacebookSupported() {
        return mIsFacebookSupported;
    }

    public void setFacebookSupported(boolean enabled) {
        mIsFacebookSupported = enabled;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public String getAdUnitId() {
        return mAdConfiguration.getAdUnitId();
    }

    public void setAdUnitId(String adUnitId) {
        mAdConfiguration.setAdUnitId(adUnitId);
    }

    public void setTimeout(int milliseconds) {
        if (mAdFetcher != null) {
            mAdFetcher.setTimeout(milliseconds);
        }
    }

    public int getAdWidth() {
        return mAdConfiguration.getWidth();
    }

    public int getAdHeight() {
        return mAdConfiguration.getHeight();
    }

    public String getClickthroughUrl() {
        return mAdConfiguration.getClickthroughUrl();
    }

    @Deprecated
    public void setClickthroughUrl(String clickthroughUrl) {
        mAdConfiguration.setClickthroughUrl(clickthroughUrl);
    }

    public String getRedirectUrl() {
        return mAdConfiguration.getRedirectUrl();
    }

    public String getResponseString() {
        return mAdConfiguration.getResponseString();
    }

    public boolean getAutorefreshEnabled() {
        return mAutoRefreshEnabled;
    }

    public void setAutorefreshEnabled(boolean enabled) {
        mAutoRefreshEnabled = enabled;

		if (mAdConfiguration.getAdUnitId() != null) {
			Logger.d("Automatic refresh for " + mAdConfiguration + " set to: "
					+ enabled + ".");
		}

		if (mAutoRefreshEnabled) {
			scheduleRefresh();
		} else {
			cancelRefreshTimer();
		}
    }

    public boolean getTesting() {
        return mIsTesting;
    }

    public void setTesting(boolean enabled) {
        mIsTesting = enabled;
    }

    int getLocationPrecision() {
        return mLocationPrecision;
    }

    void setLocationPrecision(int precision) {
        mLocationPrecision = Math.max(0, precision);
    }

    AdConfiguration getAdConfiguration() {
        return mAdConfiguration;
    }

    boolean isDestroyed() {
        return mIsDestroyed;
    }

    /*
     * Clean up the internal state of the AdViewController.
     */
    void cleanup() {
        if (mIsDestroyed) {
            return;
        }

        setAutorefreshEnabled(false);
        cancelRefreshTimer();

        // WebView subclasses are not garbage-collected in a timely fashion on Froyo and below,
        // thanks to some persistent references in WebViewCore. We manually release some resources
        // to compensate for this "leak".

        mAdFetcher.cleanup();
        mAdFetcher = null;

        mAdConfiguration.cleanup();

        mView = null;

        // Flag as destroyed. LoadUrlTask checks this before proceeding in its onPostExecute().
        mIsDestroyed = true;
    }

    void configureUsingHttpResponse(final HttpResponse response) {
        mAdConfiguration.addHttpResponse(response);
    }

    Integer getAdTimeoutDelay() {
        return mAdConfiguration.getAdTimeoutDelay();
    }

    int getRefreshTimeMilliseconds() {
        return mAdConfiguration.getRefreshTimeMilliseconds();
    }

    @Deprecated
    void setRefreshTimeMilliseconds(int refreshTimeMilliseconds) {
        mAdConfiguration.setRefreshTimeMilliseconds(refreshTimeMilliseconds);
    }

    void trackImpression() {
        new Thread(new Runnable() {
            public void run () {
                if (mAdConfiguration.getImpressionUrl() == null) return;

                DefaultHttpClient httpClient = HttpClientFactory.create();
                try {
                    HttpGet httpget = new HttpGet(mAdConfiguration.getImpressionUrl());
                    httpget.addHeader("User-Agent", mAdConfiguration.getUserAgent());
                    httpClient.execute(httpget);
                } catch (Exception e) {
                    Logger.d("Impression tracking failed : " + mAdConfiguration.getImpressionUrl(), e);
                } finally {
                    httpClient.getConnectionManager().shutdown();
                }
            }
        }).start();
    }

    void registerClick() {
        new Thread(new Runnable() {
            public void run () {
                if (mAdConfiguration.getClickthroughUrl() == null) return;

                DefaultHttpClient httpClient = HttpClientFactory.create();
                try {
                    Logger.d("Tracking click for: " + mAdConfiguration.getClickthroughUrl());
                    HttpGet httpget = new HttpGet(mAdConfiguration.getClickthroughUrl());
                    httpget.addHeader("User-Agent", mAdConfiguration.getUserAgent());
                    httpClient.execute(httpget);
                } catch (Exception e) {
                    Logger.d("Click tracking failed: " + mAdConfiguration.getClickthroughUrl(), e);
                } finally {
                    httpClient.getConnectionManager().shutdown();
                }
            }
        }).start();
    }

    void fetchAd(String mUrl) {
        if (mAdFetcher != null) {
            mAdFetcher.fetchAdForUrl(mUrl);
        }
    }

    void forceRefresh() {
        setNotLoading();
        loadAd();
    }

    String generateAdUrl() {
        return mUrlGenerator
                .withAdUnitId(mAdConfiguration.getAdUnitId())
                .withKeywords(mKeywords)
                .withFacebookSupported(mIsFacebookSupported)
                .withLocation(mLocation)
                .generateUrlString(getServerHostname());
    }

    void adDidFail(ErrorCode errorCode) {
        Logger.i("Ad failed to load." + errorCode.toString());
        setNotLoading();
        scheduleRefresh();
        getMoPubView().adFailed(errorCode);
    }

    /**
     * Schedule refresh timer if enabled
     */
    void scheduleRefresh() {
        cancelRefreshTimer();
        if (mAutoRefreshEnabled && mAdConfiguration.getRefreshTimeMilliseconds() > 0) {
            mHandler.postDelayed(refreshRunnable, mAdConfiguration.getRefreshTimeMilliseconds());
        }
    }

    void setLocalExtras(Map<String, Object> localExtras) {
        mLocalExtras = (localExtras != null)
                ? new HashMap<String,Object>(localExtras)
                : new HashMap<String,Object>();
    }

    Map<String, Object> getLocalExtras() {
        return (mLocalExtras != null)
                ? new HashMap<String,Object>(mLocalExtras)
                : new HashMap<String,Object>();
    }

    private void cancelRefreshTimer() {
        mHandler.removeCallbacks(refreshRunnable);
    }

    private String getServerHostname() {
        return mIsTesting ? UnileadView.HOST_FOR_TESTING : UnileadView.HOST;
    }

    private boolean isNetworkAvailable() {
        // If we don't have network state access, just assume the network is up.
        int result = mContext.checkCallingPermission(ACCESS_NETWORK_STATE);
        if (result == PackageManager.PERMISSION_DENIED) return true;

        // Otherwise, perform the connectivity check.
        ConnectivityManager cm
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    void setAdContentView(final View view) {
        // XXX: This method is called from the WebViewClient's callbacks, which has caused an error on a small portion of devices
        // We suspect that the code below may somehow be running on the wrong UI Thread in the rare case.
        // see: http://stackoverflow.com/questions/10426120/android-got-calledfromwrongthreadexception-in-onpostexecute-how-could-it-be
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                UnileadView moPubView = getMoPubView();
                if (moPubView == null) {
                    return;
                }
                moPubView.removeAllViews();
                moPubView.addView(view, getAdLayoutParams(view));
            }
        });
    }

    private FrameLayout.LayoutParams getAdLayoutParams(View view) {
        int width = mAdConfiguration.getWidth();
        int height = mAdConfiguration.getHeight();

        if (getShouldHonorServerDimensions(view) && width > 0 && height > 0) {
            int scaledWidth = Dips.asIntPixels(width, mContext);
            int scaledHeight = Dips.asIntPixels(height, mContext);

            return new FrameLayout.LayoutParams(scaledWidth, scaledHeight, Gravity.CENTER);
        } else {
            return WRAP_AND_CENTER_LAYOUT_PARAMS;
        }
    }

    /**
     * Returns the last known location of the device using its GPS and network location providers.
     * 
     * <p>May be null if:
     * - Location permissions are not requested in the Android manifest file
     * - The location providers don't exist
     * - Location awareness is disabled in the parent UnileadView</p>
     */
    private Location getLastKnownLocation() {
        Location result;

        if (mLocationAwareness == LocationAwareness.DISABLED) {
            return null;
        }

        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Location gpsLocation = null;
        try {
            gpsLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Logger.d("Failed to retrieve GPS location: access appears to be disabled.");
        } catch (IllegalArgumentException e) {
            Logger.d( "Failed to retrieve GPS location: device has no GPS provider.");
        }

        Location networkLocation = null;
        try {
            networkLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            Logger.d("Failed to retrieve network location: access appears to be disabled.");
        } catch (IllegalArgumentException e) {
            Logger.d("Failed to retrieve network location: device has no network provider.");
        }

        if (gpsLocation == null && networkLocation == null) {
            return null;
        }
        else if (gpsLocation != null && networkLocation != null) {
            if (gpsLocation.getTime() > networkLocation.getTime()) result = gpsLocation;
            else result = networkLocation;
        }
        else if (gpsLocation != null) result = gpsLocation;
        else result = networkLocation;

        // Truncate latitude/longitude to the number of digits specified by locationPrecision.
        if (mLocationAwareness == LocationAwareness.TRUNCATED) {
            double lat = result.getLatitude();
            double truncatedLat = BigDecimal.valueOf(lat)
                    .setScale(mLocationPrecision, BigDecimal.ROUND_HALF_DOWN)
                    .doubleValue();
            result.setLatitude(truncatedLat);

            double lon = result.getLongitude();
            double truncatedLon = BigDecimal.valueOf(lon)
                    .setScale(mLocationPrecision, BigDecimal.ROUND_HALF_DOWN)
                    .doubleValue();
            result.setLongitude(truncatedLon);
        }

        return result;
    }

    @Deprecated
    public void customEventDidLoadAd() {
        setNotLoading();
        trackImpression();
        scheduleRefresh();
    }

    @Deprecated
    public void customEventDidFailToLoadAd() {
        loadFailUrl(ErrorCode.UNSPECIFIED);
    }

    @Deprecated
    public void customEventActionWillBegin() {
        registerClick();
    }

    public static <P> void safeExecuteOnExecutor(AsyncTask<P, ?, ?> asyncTask,
			P... params) throws Exception {
		if (asyncTask == null) {
			throw new IllegalArgumentException(
					"Unable to execute null AsyncTask.");
		}
	
		if (VersionCode.currentApiLevel().isAtLeast(
				VersionCode.ICE_CREAM_SANDWICH)) {
			Executor threadPoolExecutor = (Executor) AsyncTask.class.getField(
					"THREAD_POOL_EXECUTOR").get(AsyncTask.class);
	
			new Reflection.MethodBuilder(asyncTask, "executeOnExecutor")
					.addParam(Executor.class, threadPoolExecutor)
					.addParam(Object[].class, params).execute();
		} else {
			asyncTask.execute(params);
		}
	}

	public static class Factory {
        protected static Factory instance = new Factory();

        @Deprecated // for testing
        public static void setInstance(Factory factory) {
            instance = factory;
        }

        public static Engine create(Context context, UnileadView moPubView) {
            return instance.internalCreate(context, moPubView);
        }

        protected Engine internalCreate(Context context, UnileadView moPubView) {
            return new Engine(context, moPubView);
        }
    }

}//UnileadViewController
