package com.example.framgia.soundclound_01.ui.audioresult;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.framgia.soundclound_01.R;
import com.example.framgia.soundclound_01.data.model.Category;
import com.example.framgia.soundclound_01.data.model.Track;
import com.example.framgia.soundclound_01.ui.adapter.AudioOnlineAdapter;
import com.example.framgia.soundclound_01.utils.Const;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.framgia.soundclound_01.utils.Const.APIConst.VALUE_LIMIT;
import static com.example.framgia.soundclound_01.utils.Const.IntentKey.EXTRA_CATEGORY;
import static com.example.framgia.soundclound_01.utils.Const.IntentKey.EXTRA_QUERY;
import static com.example.framgia.soundclound_01.utils.Const.IntentKey.EXTRA_TITLE;

public class AudioResultActivity extends AppCompatActivity
    implements AudioResultContract.View, SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.recycler_view_audio)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_load_more)
    ProgressBar mProgressLoadMore;
    @BindView(R.id.swipe_to_refresh)
    SwipeRefreshLayout mSwifeToRefresh;
    private int mPastVisiblesItems;
    private int mVisibleItemCount;
    private int mTotalItemCount;
    private int mOffSet;
    private String mCategory;
    private String mQuery;
    private String mTitle;
    private boolean mCanLoadMore = true;
    private boolean userScrolled = true;
    private AudioResultContract.Presenter mAudioResultPresenter;
    private AudioOnlineAdapter mAudioOnlineAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private List<Track> mTracks = new ArrayList<>();

    public static Intent getAudioFromCategory(Context context, Category
        category) {
        Intent intent =
            new Intent(context, AudioResultActivity.class);
        intent.putExtra(EXTRA_CATEGORY, category.getCategoryParam());
        intent.putExtra(EXTRA_TITLE, category.getCategoryTitle());
        return intent;
    }

    public static Intent getAudioFromQuery(Context context, String query) {
        android.content.Intent intent =
            new android.content.Intent(context, AudioResultActivity.class);
        intent.putExtra(EXTRA_QUERY, query);
        intent.putExtra(EXTRA_TITLE, query);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_audio);
        setPresenter(new AudioResultPresenter(this));
        ButterKnife.bind(this);
        mAudioResultPresenter.start();
    }

    @Override
    public void setPresenter(AudioResultContract.Presenter presenter) {
        mAudioResultPresenter = presenter;
    }

    @Override
    public void start() {
        initView();
        getIntentData();
        setupActionBar();
        mAudioResultPresenter.getAudio(mCategory, mQuery, mCanLoadMore, mOffSet);
    }

    private void initView() {
        mSwifeToRefresh.setOnRefreshListener(this);
        mAudioOnlineAdapter = new AudioOnlineAdapter(mTracks, this);
        mRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAudioOnlineAdapter);
        implementScrollListener();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        mCategory = intent.getStringExtra(Const.IntentKey.EXTRA_CATEGORY);
        mQuery = intent.getStringExtra(Const.IntentKey.EXTRA_QUERY);
        mTitle = intent.getStringExtra(Const.IntentKey.EXTRA_TITLE);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setTitle(mTitle);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onRefresh() {
        mOffSet = 0;
        mCanLoadMore = true;
        mSwifeToRefresh.setRefreshing(false);
        mAudioResultPresenter.getAudio(mCategory, mQuery, mCanLoadMore, mOffSet);
    }

    @Override
    public void showAudio(List<Track> list, String nextHref) {
        if (list == null) return;
        if (nextHref == null) mCanLoadMore = false;
        if (mOffSet == 0) mTracks.clear();
        mTracks.addAll(list);
        mOffSet += Integer.parseInt(VALUE_LIMIT);
        mAudioOnlineAdapter.notifyDataSetChanged();
    }

    @Override
    public void showGetAudioError() {
        Toast.makeText(this, R.string.get_audio_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showProgress(boolean show) {
        if (show) mProgressLoadMore.setVisibility(View.VISIBLE);
        else mProgressLoadMore.setVisibility(View.GONE);
    }

    private void implementScrollListener() {
        mRecyclerView
            .addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView,
                                                 int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        userScrolled = true;
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx,
                                       int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    mVisibleItemCount = mLinearLayoutManager.getChildCount();
                    mTotalItemCount = mLinearLayoutManager.getItemCount();
                    mPastVisiblesItems = mLinearLayoutManager
                        .findFirstVisibleItemPosition();
                    if (userScrolled
                        && (mVisibleItemCount + mPastVisiblesItems) == mTotalItemCount) {
                        userScrolled = false;
                        mAudioResultPresenter.getAudio(mCategory, mQuery, mCanLoadMore, mOffSet);
                    }
                }
            });
    }
}