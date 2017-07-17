package com.example.administrator.rednum;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Administrator on 2017/7/14.
 */

public class RedNumApplication extends Application {
    private LinkedList<HashMap<String, Object>> filterSearchList;

    @Override
    public void onCreate() {
        super.onCreate();

        //创建默认的ImageLoader配置参数
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);

        //Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(configuration);
    }


    public void setFilterSearchList(LinkedList<HashMap<String, Object>> data) {
        this.filterSearchList = data;
    }

    public LinkedList<HashMap<String, Object>> getFilterSearchList() {
        return filterSearchList;
    }
}
