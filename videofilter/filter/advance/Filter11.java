package com.metime.videofilter.filter.advance;


import android.content.Context;

import com.metime.videofilter.filter.base.OpenGlUtils;

public class Filter11 extends BaseFilter {


    public Filter11(Context context) {
        super(context);
    }

    @Override
    protected int getInputTexture() {
        return OpenGlUtils.loadTexture(mContext, "filter/filter_11.png");
    }


}
