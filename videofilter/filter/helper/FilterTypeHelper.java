package com.metime.videofilter.filter.helper;


import android.content.Context;

import com.metime.R;
import  com.metime.videofilter.filter.advance.*;
import  com.metime.videofilter.filter.base.GPUImageFilter;
import  com.metime.videofilter.utils.ConfigUtils;

public class FilterTypeHelper {

    public static GPUImageFilter getFilter(Context context) {
        MagicFilterType filterType = ConfigUtils.getInstance().getMagicFilterType();
        return getFilter(filterType, context);
    }

    private static GPUImageFilter getFilter(MagicFilterType filterType, Context context) {
        switch (filterType) {
            case FILTER_1:
                return new Filter01(context);
            case FILTER_2:
                return new Filter02(context);
            case FILTER_3:
                return new Filter03(context);
            case FILTER_4:
                return new Filter04(context);
            case FILTER_5:
                return new Filter05(context);
            case FILTER_6:
                return new Filter06(context);
            case FILTER_7:
                return new Filter07(context);
            case FILTER_8:
                return new Filter08(context);
            case FILTER_9:
                return new Filter09(context);
            case FILTER_10:
                return new Filter10(context);
            case FILTER_11:
                return new Filter11(context);
            case FILTER_12:
                return new Filter12(context);
            case FILTER_13:
                return new FIlter13(context);
            default:
                return null;
        }
    }


    public static int FilterType2Name(MagicFilterType filterType) {
        switch (filterType) {
            case FILTER_1:
                return R.string.filter_one;
            case FILTER_2:
                return R.string.filter_two;
            case FILTER_3:
                return R.string.filter_three;
            case FILTER_4:
                return R.string.filter_four;
            case FILTER_5:
                return R.string.filter_five;
            case FILTER_6:
                return R.string.filter_six;
            case FILTER_7:
                return R.string.filter_seven;
            case FILTER_8:
                return R.string.filter_eight;
            case FILTER_9:
                return R.string.filter_nine;
            case FILTER_10:
                return R.string.filter_ten;
            case FILTER_11:
                return R.string.filter_eleven;
            case FILTER_12:
                return R.string.filter_twelve;
            case FILTER_13:
                return R.string.filter_thirteen;
            default:
                return R.string.filter_none;
        }
    }

    public static int FilterType2Color(MagicFilterType filterType) {
        switch (filterType) {
            case NONE:
                return R.color.filter_category_greenish_dummy;
            default:
                return R.color.filter_category_greenish_normal;
        }
    }

    public static int FilterType2Thumb(MagicFilterType filterType) {
        switch (filterType) {
            case NONE:
                return R.drawable.user;
            default:
                return R.drawable.user;
        }
    }
}
