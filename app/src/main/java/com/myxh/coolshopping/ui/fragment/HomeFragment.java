package com.myxh.coolshopping.ui.fragment;

import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.myxh.coolshopping.R;
import com.myxh.coolshopping.common.AppConstant;
import com.myxh.coolshopping.entity.FilmInfo;
import com.myxh.coolshopping.entity.GoodsInfo;
import com.myxh.coolshopping.entity.HomeGridInfo;
import com.myxh.coolshopping.listener.ViewPagerListener;
import com.myxh.coolshopping.network.CallServer;
import com.myxh.coolshopping.network.HttpListener;
import com.myxh.coolshopping.ui.adapter.BannerPagerAdapter;
import com.myxh.coolshopping.ui.adapter.GoodsListAdapter;
import com.myxh.coolshopping.ui.adapter.GridAdapter;
import com.myxh.coolshopping.ui.adapter.HeadPageAdapter;
import com.myxh.coolshopping.ui.base.BaseFragment;
import com.myxh.coolshopping.ui.widget.Indicator;
import com.myxh.coolshopping.util.ToastUtil;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2016/8/27.
 */
public class HomeFragment extends BaseFragment implements HttpListener<String> {

    public static final int GOOD_REQUEST = 0x01;
    public static final int FILM_REQUEST = 0x02;
    public static final int SCAN_QR_REQUEST = 103;
    private int[] imgRes = new int[]{R.drawable.banner01,R.drawable.banner02,R.drawable.banner03};
    private Handler mHandler = new Handler();

    private ViewPager bannerPager;
    private Indicator bannerIndicator;
    private View mView;

    private List<HomeGridInfo> pageOneData = new ArrayList<>();
    private List<HomeGridInfo> pageTwoData = new ArrayList<>();
    private ListView mListView;
    private List<View> mViewList = new ArrayList<>();

    private List<GoodsInfo.ResultBean.GoodlistBean> mGoodlist = new ArrayList<>();
    private List<FilmInfo.ResultBean> mFilmList = new ArrayList<>();
    private LinearLayout mFilmLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_home,null);
        initData();
        initViews(mView);
        antoScroll();
        return mView;
    }

    private void initData() {
        String[] gridTitles = getResources().getStringArray(R.array.home_bar_labels);
        TypedArray typedArray = getResources().obtainTypedArray(R.array.home_bar_icon);
        for (int i = 0; i < gridTitles.length; i++) {
            if (i < 8) {
                pageOneData.add(new HomeGridInfo(typedArray.getResourceId(i,0),gridTitles[i]));
            } else {
                pageTwoData.add(new HomeGridInfo(typedArray.getResourceId(i,0),gridTitles[i]));
            }
        }

        Request<String> goodRequest = NoHttp.createStringRequest(AppConstant.RECOMMEND_URL, RequestMethod.GET);
        CallServer.getInstance().add(getActivity(), GOOD_REQUEST, goodRequest, this, true, true);

        Request<String> filmRequest = NoHttp.createStringRequest(AppConstant.HOT_FILM_URL,RequestMethod.GET);
        CallServer.getInstance().add(getActivity(), FILM_REQUEST, filmRequest, this, true, true);
    }

    private void antoScroll() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int currentItem = bannerPager.getCurrentItem();
                bannerPager.setCurrentItem(currentItem+1,true);
                mHandler.postDelayed(this,2000);
            }
        },2000);
    }

    private void initViews(View view) {
        //titleBar
        View titleView = view.findViewById(R.id.home_titlebar);
        initTitlebar(titleView);
        mListView = (ListView) view.findViewById(R.id.home_listView);

        //广告条
        /*View bannerView = LayoutInflater.from(getActivity()).inflate(R.layout.home_banner,null);
        bannerPager = (ViewPager) bannerView.findViewById(R.id.home_banner_pager);
        bannerIndicator = (Indicator) bannerView.findViewById(R.id.home_banner_indicator);
        bannerPager.setAdapter(new BannerPagerAdapter(getChildFragmentManager(),imgRes));
        bannerPager.addOnPageChangeListener(new ViewPagerListener(bannerIndicator));*/

        //header头部
        View headView = LayoutInflater.from(getActivity()).inflate(R.layout.home_head_page,null);
        //banner
        View bannerView = headView.findViewById(R.id.home_head_include_banner);
        bannerPager = (ViewPager) bannerView.findViewById(R.id.home_banner_pager);
        bannerIndicator = (Indicator) bannerView.findViewById(R.id.home_banner_indicator);
        bannerPager.setAdapter(new BannerPagerAdapter(getChildFragmentManager(),imgRes));
        bannerPager.addOnPageChangeListener(new ViewPagerListener(bannerIndicator));
        ViewPager headPager = (ViewPager) headView.findViewById(R.id.home_head_pager);
        Indicator headIndicator = (Indicator) headView.findViewById(R.id.home_head_indicator);
        //第一页
        View pageOne = LayoutInflater.from(getActivity()).inflate(R.layout.home_gridview,null);
        GridView gridView1 = (GridView) pageOne.findViewById(R.id.home_gridView);
        gridView1.setAdapter(new GridAdapter(getActivity(),pageOneData));
        gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        //第二页
        View pageTwo = LayoutInflater.from(getActivity()).inflate(R.layout.home_gridview,null);
        GridView gridView2 = (GridView) pageTwo.findViewById(R.id.home_gridView);
        gridView2.setAdapter(new GridAdapter(getActivity(),pageTwoData));
        gridView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        mViewList.add(pageOne);
        mViewList.add(pageTwo);
        headPager.setAdapter(new HeadPageAdapter(mViewList));
        headPager.addOnPageChangeListener(new ViewPagerListener(headIndicator));
        //热门电影
        View filmView = headView.findViewById(R.id.home_head_include_film);
        mFilmLayout = (LinearLayout) filmView.findViewById(R.id.home_film_ll);

//        mListView.addHeaderView(bannerView);
        mListView.addHeaderView(headView);
        mListView.setHeaderDividersEnabled(false);
    }

    private void initTitlebar(View view) {
        ImageView scanQR = (ImageView) view.findViewById(R.id.titleBar_scan_img);
        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CaptureActivity.class);
                getActivity().startActivityForResult(intent,SCAN_QR_REQUEST);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_QR_REQUEST) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    ToastUtil.show(getActivity(),"解析结果:"+result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    ToastUtil.show(getActivity(),"解析二维码失败");
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSucceed(int what, Response<String> response) {
        switch (what) {
            case GOOD_REQUEST:
                Gson gson = new Gson();
                GoodsInfo goodsInfo = gson.fromJson(response.get(),GoodsInfo.class);
                List<GoodsInfo.ResultBean.GoodlistBean> goodlistBeen = goodsInfo.getResult().getGoodlist();
                mGoodlist.addAll(goodlistBeen);

                int headerViewsCount = mListView.getHeaderViewsCount();
                GoodsListAdapter goodsListAdapter = new GoodsListAdapter(getActivity(),mGoodlist,headerViewsCount);
                mListView.setAdapter(goodsListAdapter);
                break;
            case FILM_REQUEST:
                Gson filmGson = new Gson();
                FilmInfo filmInfo = filmGson.fromJson(response.get(),FilmInfo.class);
                List<FilmInfo.ResultBean> filmList = filmInfo.getResult();
                mFilmList.addAll(filmList);

                for (int i = 0; i < mFilmList.size(); i++) {
                    View filmItemView = LayoutInflater.from(getActivity()).inflate(R.layout.item_film,null);
                    SimpleDraweeView filmIcon = (SimpleDraweeView) filmItemView.findViewById(R.id.home_film_icon);
                    TextView filmTitle = (TextView) filmItemView.findViewById(R.id.home_film_title);
                    TextView filmGrade = (TextView) filmItemView.findViewById(R.id.home_film_grade);
                    filmIcon.setImageURI(Uri.parse(mFilmList.get(i).getPosterUrl()));
                    filmTitle.setText(mFilmList.get(i).getFilmName());
                    filmGrade.setText(mFilmList.get(i).getGrade()+"分");
                    mFilmLayout.addView(filmItemView);
                }
        }
    }

    @Override
    public void onFailed(int what, Response<String> response) {

    }
}
