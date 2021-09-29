package com.metime.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metime.BaseActivity;
import com.metime.R;
import com.metime.adapter.HashTagItemAdapter;
import com.metime.model.HashTagModel;
import com.metime.model.HashTagPayloadModel;
import com.metime.retrofit.ApiManager;
import com.metime.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddHashTagActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private TextView tvNext;
    HashTagItemAdapter hashTagItemAdapter;

    private LinearLayout llDataNotFound;
    private ImageView ivback;
    private List<HashTagModel> selectedHashTagData;

    HashTagPayloadModel hashTagPayloadModel;
    LinearLayoutManager layoutManager;
    List<HashTagModel> data = new ArrayList<HashTagModel>();
    boolean isLoading = false;
    boolean isLastPage = false;

    MultiAutoCompleteTextView multiAutoCompleteTextView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hash_tag);

        //selectedHashTagData = getIntent().getParcelableArrayListExtra("data");

        this.multiAutoCompleteTextView = (MultiAutoCompleteTextView) findViewById(R.id.multiAutoCompleteTextView);
        this.multiAutoCompleteTextView.setThreshold(1);
        this.multiAutoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);


        this.recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        this.tvNext = (TextView) findViewById(R.id.tvNext);

        this.llDataNotFound = (LinearLayout) findViewById(R.id.llDataNotFound);
        this.ivback = (ImageView) findViewById(R.id.ivback);

        ivback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        hashTagPayloadModel = new HashTagPayloadModel();
        hashTagPayloadModel.setSearchTerm("");
        hashTagPayloadModel.setPageSize(50);
        hashTagPayloadModel.setPageNumber(1);
        //setupRecycler();
        //getHashTagList();

        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putParcelableArrayListExtra("data", (ArrayList<HashTagModel>) hashTagItemAdapter.getSelectedHashTag());
                setResult(RESULT_OK, data);
                finish();

            }
        });


        multiAutoCompleteTextView.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = multiAutoCompleteTextView.getText().toString();
                String[] data = str.split(",");
                String searchQuery = data[data.length - 1].trim();

                if(searchQuery.isEmpty())
                    return;

                hashTagPayloadModel.setSearchTerm(searchQuery);
                getHashTagList();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




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

    private void getHashTagList() {
        // display a progress dialog

        progressBar.setVisibility(View.VISIBLE);
        Call<List<HashTagModel>> call = ApiManager.shared(this).service.getHashTagList(hashTagPayloadModel);

        call.enqueue(new Callback<List<HashTagModel>>() {
            @Override
            public void onResponse(Call<List<HashTagModel>> call, Response<List<HashTagModel>> response) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                String [] data = new String[response.body().size()];
                //ArrayList<String> data = new ArrayList<>();
                for (int i = 0; i < response.body().size(); i++) {
                    //data.add(hash.getTagName());
                    data[i]=response.body().get(i).getTagName();
                }

                ArrayAdapter tagArray = new ArrayAdapter(AddHashTagActivity.this, R.layout.row_hash_tag_item_select, data);
                multiAutoCompleteTextView.setAdapter(tagArray);
                tagArray.notifyDataSetChanged();
                //setData(response.body());
        }

            @Override
            public void onFailure(Call<List<HashTagModel>> call, Throwable t) {
                t.printStackTrace();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    void setupRecycler() {
        layoutManager = new LinearLayoutManager(AddHashTagActivity.this, LinearLayoutManager.VERTICAL, false);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setLayoutManager(layoutManager);

        hashTagItemAdapter = new HashTagItemAdapter(data, AddHashTagActivity.this);

        recyclerView.setAdapter(hashTagItemAdapter);

        // Pagination
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
    }

    void setData(List<HashTagModel> newData) {

        if (newData != null) {
            data.addAll(newData);
        }

        if (newData == null || newData.size() == 0)
            isLastPage = true;

        List<HashTagModel> selectedData = new ArrayList<>();
        /*for (HashTagModel selectedH : selectedHashTagData) {
            for (HashTagModel dataH : newData) {
                if (selectedH.getTagId() == dataH.getTagId()) {
                    selectedData.add(dataH);
                    break;
                }
            }
        }*/
        hashTagItemAdapter.createSelectedList(selectedData);

        if (data != null && data.size() > 0) {
            llDataNotFound.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (hashTagItemAdapter == null)
                setupRecycler();
            else
                hashTagItemAdapter.notifyDataSetChanged();

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
                    hashTagPayloadModel.setPageNumber((hashTagPayloadModel.getPageNumber() + 1));
                    getHashTagList();
                }
            }
        }
    };
}
