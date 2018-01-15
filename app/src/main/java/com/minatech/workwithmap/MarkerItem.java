package com.minatech.workwithmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by dustu on 1/16/2018.
 */

public class MarkerItem implements ClusterItem {

    private LatLng mLatlng;
    private String mTitle;
    private String mSnippet;

    public MarkerItem(LatLng mLatlng, String mTitle, String mSnippet) {
        this.mLatlng = mLatlng;
        this.mTitle = mTitle;
        this.mSnippet = mSnippet;
    }

    @Override
    public LatLng getPosition() {
        return mLatlng;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }
}
