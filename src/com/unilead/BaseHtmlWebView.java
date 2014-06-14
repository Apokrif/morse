package com.unilead;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.unilead.ViewGestureDetector.UserClickListener;

public class BaseHtmlWebView extends BaseWebView implements UserClickListener {
    private final ViewGestureDetector mViewGestureDetector;
    private boolean mClicked;

    public BaseHtmlWebView(Context context, AdConfiguration adConfiguration) {
        super(context);

        disableScrollingAndZoom();
        getSettings().setJavaScriptEnabled(true);

        mViewGestureDetector = new ViewGestureDetector(context, this, adConfiguration);
        mViewGestureDetector.setUserClickListener(this);

        if (VersionCode.currentApiLevel().isAtLeast(VersionCode.ICE_CREAM_SANDWICH)) {
            enablePlugins(true);
        }
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void init(boolean isScrollable) {
        initializeOnTouchListener(isScrollable);
    }

    @Override
    public void loadUrl(String url) {
        if (url == null) return;

        Log.d("MoPub", "Loading url: " + url);
        if (url.startsWith("javascript:")) {
            super.loadUrl(url);
        }
    }

    private void disableScrollingAndZoom() {
        setHorizontalScrollBarEnabled(false);
        setHorizontalScrollbarOverlay(false);
        setVerticalScrollBarEnabled(false);
        setVerticalScrollbarOverlay(false);
        getSettings().setSupportZoom(false);
    }

    void loadHtmlResponse(String htmlResponse) {
        loadDataWithBaseURL("http://ads.mopub.com/", htmlResponse, "text/html", "utf-8", null);
    }

    void initializeOnTouchListener(final boolean isScrollable) {
        setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mViewGestureDetector.sendTouchEvent(event);

                // We're not handling events if the current action is ACTION_MOVE
                return (event.getAction() == MotionEvent.ACTION_MOVE) && !isScrollable;
            }
        });
    }

    @Override
    public void onUserClick() {
        mClicked = true;
    }

    @Override
    public void onResetUserClick() {
        mClicked = false;
    }

    @Override
    public boolean wasClicked() {
        return mClicked;
    }
}
