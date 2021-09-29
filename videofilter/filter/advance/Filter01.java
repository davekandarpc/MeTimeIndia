package com.metime.videofilter.filter.advance;


import android.content.Context;

import com.metime.videofilter.filter.base.OpenGlUtils;

public class Filter01 extends BaseFilter {


    public Filter01(Context context) {
        super(context);
    }

    @Override
    protected int getInputTexture() {
        return OpenGlUtils.loadTexture(mContext, "filter/filter_01.png");
    }


}
