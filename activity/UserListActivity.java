package com.metime.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metime.BaseActivity;
import com.metime.R;
import com.metime.adapter.SearchUserItemAdapter;
import com.metime.model.UserListModel;
import com.metime.model.UserListPayloadModel;
import com.metime.model.payload.FollowRequestsAddPayloadModel;
import com.metime.retrofit.ApiManager;
import com.metime.utils.PreferenceHelper;
import com.metime.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends BaseActivity {

    private RecyclerView recyclerView;

    SearchUserItemAdapter searchUserItemAdapter;
    private RelativeLayout rlSearch;
    private EditText edtSearch;
    private LinearLayout llDataNotFound;
    private ImageView ivback;


    UserListPayloadModel userListPayloadModel;

    LinearLayoutManager layoutManager;
    List<UserListModel> data = new ArrayList<UserListModel>();
    boolean isLoading = false;
    boolean isLastPage = false;
    boolean inCresePage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        // initToolbarWithBackButton("Songs");

        this.recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        this.rlSearch = (RelativeLayout) findViewById(R.id.rlSearch);
        this.edtSearch = (EditText) findViewById(R.id.edtSearch);
        this.llDataNotFound = (LinearLayout) findViewById(R.id.llDataNotFound);
        this.ivback = (ImageView) findViewById(R.id.ivback);

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    data.clear();
                    performSearch();
                    return true;
                }
                return false;
            }
        });
        rlSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.clear();
                performSearch();
            }
        });

        ivback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        userListPayloadModel = new UserListPayloadModel();
        userListPayloadModel.setSearchTerm("");
        userListPayloadModel.setPageSize(10);
        userListPayloadModel.setPageNumber(1);
        setupRecycler();

        userSearch();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return (super.onOptionsItemSelected(item));
    }

    private void userSearch() {

        if(inCresePage == false){
            Utils.showProgressDialog(this);
        }else{
            Utils.showProgressDialogBottom(this);
        }

        Call<List<UserListModel>> call = ApiManager.shared(this).service.userSearch(userListPayloadModel);

        call.enqueue(new Callback<List<UserListModel>>() {
            @Override
            public void onResponse(Call<List<UserListModel>> call, Response<List<UserListModel>> response) {
                if(inCresePage == false){
                    Utils.hideProgressDialog();
                    Log.e("Sanjay","when increas search false"+inCresePage);
                }else{
                    Utils.hideProgressDiloagBottom();
                    Log.e("Sanjay","when increas search true"+inCresePage);
                }
                isLoading = false;
                setData(response.body());
            }

            @Override
            public void onFailure(Call<List<UserListModel>> call, Throwable t) {
                if(inCresePage == false){
                    Utils.hideProgressDialog();
                    Log.e("Sanjay","when increas search false"+inCresePage);
                }else{
                    Utils.hideProgressDiloagBottom();
                    Log.e("Sanjay","when increas search true"+inCresePage);
                }
                t.printStackTrace();
            }
        });
    }

    private void performSearch() {
        userListPayloadModel.setSearchTerm(edtSearch.getText().toString().trim());
        userListPayloadModel.setPageNumber(1);
        isLastPage = false;
        userSearch();
    }

    void setupRecycler() {
        layoutManager = new LinearLayoutManager(UserListActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        searchUserItemAdapter = new SearchUserItemAdapter(data, UserListActivity.this, new SearchUserItemAdapter.OnFollowListeners() {
            @Override
            public void onFollow(UserListModel userListModel) {
                followRequestsAdd(userListModel.getObjectId());
            }
        });
        recyclerView.setAdapter(searchUserItemAdapter);

        // Pagination
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
    }

    void setData(List<UserListModel> newData) {

        if (newData != null) {
            data.addAll(newData);
        }

        if (newData == null || newData.size() == 0)
            isLastPage = true;

        if (data != null && data.size() > 0) {
            llDataNotFound.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (searchUserItemAdapter == null)
                setupRecycler();
            else
                searchUserItemAdapter.notifyDataSetChanged();


        } else {
            llDataNotFound.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }


    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= 10) {

                    isLoading = true;
                    inCresePage = true;
                    userListPayloadModel.setPageNumber((userListPayloadModel.getPageNumber() + 1));
                    userSearch();
                }
            }
        }
    };

    private void followRequestsAdd(String toUserId) {

        FollowRequestsAddPayloadModel followRequestsAddPayloadModel = new FollowRequestsAddPayloadModel();
        followRequestsAddPayloadModel.setRequestedUserId(toUserId);
        followRequestsAddPayloadModel.setUserId(PreferenceHelper.getInstance(this).getObjectId());

        Utils.showProgressDialog(this);
        Call<ResponseBody> call = ApiManager.shared(this).service.followRequestsAdd(PreferenceHelper.getInstance(this).getObjectId(), followRequestsAddPayloadModel);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgressDialog();
                Utils.showToast(UserListActivity.this, "Request send successfully.");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgressDialog();
                t.printStackTrace();
            }
        });
    }
}
