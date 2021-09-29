package com.metime.videofilter.filter.advance;


import android.content.Context;

import com.metime.videofilter.filter.base.OpenGlUtils;

public class Filter12 extends BaseFilter {


    public Filter12(Context context) {
        super(context);
    }

    @Override
    protected int getInputTexture() {
        return OpenGlUtils.loadTexture(mContext, "filter/filter_12.png");
    }


}
