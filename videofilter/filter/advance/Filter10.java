package com.metime.videofilter.filter.advance;

import android.content.Context;

import com.metime.videofilter.filter.base.OpenGlUtils;

public class Filter10 extends BaseFilter {


    public Filter10(Context context) {
        super(context);
    }

    @Override
    protected int getInputTexture() {
        return OpenGlUtils.loadTexture(mContext, "filter/filter_10.png");
    }

}
