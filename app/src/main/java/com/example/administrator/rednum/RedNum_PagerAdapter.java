package com.example.administrator.rednum;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/5.
 */

public class RedNum_PagerAdapter extends PagerAdapter {

    private Context context;
    private List<View> viewList; // View数据源
    private String[] bannerURL = {
            "http://www.cnblogs.com/kamong/p/6099914.html",
            "http://blog.csdn.net/dehu_zhou/article/details/53102772",
            "http://www.cnblogs.com/blueel/archive/2013/01/06/2847842.html"
    };

    public RedNum_PagerAdapter(Context context, List<View> mViews) {
        this.context = context;
        this.viewList = mViews;
    }

    @Override
    public int getCount() {
//        return viewList.size();
        //设置成最大，使用户看不到边界
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == viewList.get(Integer.parseInt(o.toString()));
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        int currentPosition = position;
        currentPosition %= viewList.size();
        if (currentPosition < 0){
            currentPosition = viewList.size() + currentPosition;
        }

        if(viewList.get(currentPosition).getParent() != null){
            ((ViewGroup)viewList.get(currentPosition).getParent()).removeView(viewList.get(currentPosition));

            Log.i("container", "====> delete view from viewGroup, and the index is:" + currentPosition);
        }

        final int finalCurrentPosition = currentPosition;
        viewList.get(currentPosition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, InnerWebView.class);
                intent.putExtra("itemURL", bannerURL[finalCurrentPosition]);
                context.startActivity(intent);
            }
        });

        container.addView(viewList.get(currentPosition));
        return currentPosition;  // 返回当前View所对应position作为key值
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        container.removeView(viewList.get(position));
    }
}
