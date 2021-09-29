package com.metime.videofilter.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.metime.R;
import com.metime.videofilter.filter.helper.FilterTypeHelper;
import com.metime.videofilter.filter.helper.MagicFilterType;
import com.metime.videofilter.utils.ConfigUtils;

import static com.metime.videofilter.filter.helper.MagicFilterType.FILTER_1;
import static com.metime.videofilter.filter.helper.MagicFilterType.FILTER_10;
import static com.metime.videofilter.filter.helper.MagicFilterType.FILTER_11;
import static com.metime.videofilter.filter.helper.MagicFilterType.FILTER_12;
import static com.metime.videofilter.filter.helper.MagicFilterType.FILTER_13;
import static com.metime.videofilter.filter.helper.MagicFilterType.FILTER_3;
import static com.metime.videofilter.filter.helper.MagicFilterType.FILTER_4;
import static com.metime.videofilter.filter.helper.MagicFilterType.FILTER_5;
import static com.metime.videofilter.filter.helper.MagicFilterType.FILTER_6;
import static com.metime.videofilter.filter.helper.MagicFilterType.FILTER_7;
import static com.metime.videofilter.filter.helper.MagicFilterType.FILTER_8;
import static com.metime.videofilter.filter.helper.MagicFilterType.FILTER_9;


public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder> {
    private MagicFilterType[] mFilters;
    private Context mContext;
    private int mSelected = 0;

    private final MagicFilterType[] types = new MagicFilterType[]{
            FILTER_1,
            FILTER_3,
            FILTER_4,
            FILTER_5,
            FILTER_6,
            FILTER_7,
            FILTER_8,
            FILTER_9,
            FILTER_10,
            FILTER_11,
            FILTER_12,
            FILTER_13
    };

    public FilterAdapter(Context context) {
        this.mFilters = types;
        this.mContext = context;
        for (int i = 0; i < types.length; i++) {
            if (types[i] == ConfigUtils.getInstance().getMagicFilterType()) {
                mSelected = i;
            }
        }
    }

    @Override
    public FilterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.filter_item_layout, parent, false);


        return new FilterHolder(view);
    }

    @Override
    public void onBindViewHolder(FilterHolder holder, final int position) {
        holder.onBindView();
    }

    @Override
    public int getItemCount() {
        return mFilters == null ? 0 : mFilters.length;
    }


    class FilterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mThumbImage;
        TextView mFilterName;
        View vSelected;


        public FilterHolder(View itemView) {
            super(itemView);

            mThumbImage = (ImageView) itemView
                    .findViewById(R.id.filter_thumb_image);
            mFilterName = (TextView) itemView
                    .findViewById(R.id.filter_thumb_name);
            vSelected = (View) itemView
                    .findViewById(R.id.vSelected);
        }

        void onBindView() {
            int position = getAdapterPosition();
            mThumbImage.setImageResource(FilterTypeHelper.FilterType2Thumb(mFilters[position]));
            mFilterName.setText(FilterTypeHelper.FilterType2Name(mFilters[position]));

            if (position == mSelected) {
                vSelected.setVisibility(View.VISIBLE);
            } else {
                vSelected.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (mSelected == position) {
                if (mOnFilterChangeListener != null) {
                    mOnFilterChangeListener.onNoChanged(position);
                }
                return;
            }
            int lastSelected = mSelected;
            mSelected = position;
            notifyItemChanged(lastSelected);
            notifyItemChanged(position);
            if (mOnFilterChangeListener != null) {
                mOnFilterChangeListener.onFilterChanged(mFilters[position]);
            }
        }
    }

    public interface onFilterChangeListener {
        void onFilterChanged(MagicFilterType filterType);

        void onNoChanged(int pos);
    }

    private onFilterChangeListener mOnFilterChangeListener;

    public void setOnFilterChangeListener(onFilterChangeListener onFilterChangeListener) {
        this.mOnFilterChangeListener = onFilterChangeListener;
    }
}
