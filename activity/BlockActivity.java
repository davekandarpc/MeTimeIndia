package com.metime.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metime.R;
import com.metime.adapter.BlockedItemAdapter;
import com.metime.model.BlockedModel;
import com.metime.model.payload.UnBlockedPayloadModel;
import com.metime.retrofit.ApiManager;
import com.metime.utils.PreferenceHelper;
import com.metime.utils.Utils;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlockActivity extends Activity {

    private RecyclerView recyclerView;
    private ImageButton imgbtn_bakcarrow;
    private LinearLayout llNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_block);
        imgbtn_bakcarrow = (ImageButton) findViewById(R.id.backarrow_block);
        llNoData = (LinearLayout) findViewById(R.id.llNoData);


        imgbtn_bakcarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        blockedUsers();

    }

    /**
     * blocked user method
     * to get all blocked user list from calling Blocked API*/
    private void blockedUsers() {
        Utils.showProgressDialog(this);
        Call<List<BlockedModel>> call = ApiManager.shared(this).service.blocked(PreferenceHelper.getInstance(this).getObjectId());

        call.enqueue(new Callback<List<BlockedModel>>() {
            @Override
            public void onResponse(Call<List<BlockedModel>> call, Response<List<BlockedModel>> response) {
                Utils.hideProgressDialog();
                if (response.body() != null && response.body().size() != 0) {
                    llNoData.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    recyclerView.setLayoutManager(new LinearLayoutManager(BlockActivity.this));
                    recyclerView.setAdapter(new BlockedItemAdapter(response.body(), BlockActivity.this, new BlockedItemAdapter.OnBlockedListeners() {
                        @Override
                        public void onBlockedUnBlocked(BlockedModel blockedModel) {
                            //if(blockedModel.getRelationType().equalsIgnoreCase("Blocked")){
                                unBlocked(blockedModel.getByUserId(), blockedModel.getToUserId());
                            //}
                        }
                    }));
                } else {
                    llNoData.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<BlockedModel>> call, Throwable t) {
                Utils.hideProgressDialog();
                t.printStackTrace();
            }
        });
    }

    private void unBlocked(String byUserId, String userId) {

        UnBlockedPayloadModel unBlockedPayloadModel = new UnBlockedPayloadModel();
        unBlockedPayloadModel.setBlockByUserId(byUserId);
        unBlockedPayloadModel.setBlockedUserId(userId);

        Utils.showProgressDialog(this);
        Call<ResponseBody> call = ApiManager.shared(this).service.blockedRemove(PreferenceHelper.getInstance(this).getObjectId(), unBlockedPayloadModel);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgressDialog();
                Utils.showToast(BlockActivity.this, "user unblocked successfully.");
                blockedUsers();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgressDialog();
                t.printStackTrace();
            }
        });
    }
}