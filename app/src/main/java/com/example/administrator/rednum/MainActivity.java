package com.example.administrator.rednum;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.rednum.NetworkSpider.NetworkProcessor;
import com.example.administrator.rednum.PullToRefresh.PullToRefreshBase;
import com.example.administrator.rednum.PullToRefresh.PullToRefreshScrollView;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends Activity {
    private ListView listView;
    private TextView searchText;
    private LinkedList<HashMap<String, Object>> data;
    private LinkedList<HashMap<String, Object>> data2;
    private Main_List_Adapter adapter;

    private ScrollView mScrollView;
    private PullToRefreshScrollView mPullScrollView;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");


    private Handler handler = null;
    private String homePage;
    private Gson gson;

    private BroadcastReceiver networkChange;
    private IntentFilter networkChangeFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 101:
                        adapter.notifyDataSetChanged();
                        break;

                    case 99:
                        Toast.makeText(getApplicationContext(), "无网络连接", Toast.LENGTH_SHORT).show();
                        break;

                    case 2000:
                        Toast.makeText(getApplicationContext(), "已更新到最新", Toast.LENGTH_SHORT).show();
                        break;

                    case 1009:
                        Toast.makeText(getApplicationContext(), "已经到底啦", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }
            }
        };

        networkChangeFilter = new IntentFilter();
        networkChangeFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        networkChange = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (NetworkProcessor.isNetworkAvailable(MainActivity.this) && NetworkProcessor.isUseMobile(MainActivity.this)) {
                    Toast.makeText(getApplicationContext(), "当前使用手机流量，请注意流量使用情况", Toast.LENGTH_SHORT).show();
                }
            }
        };

        registerReceiver(networkChange, networkChangeFilter);


        mPullScrollView = new PullToRefreshScrollView(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mPullScrollView);

        mPullScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                new GetDataTask().execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                new PullUpToRefresh().execute();
            }
        });

        mScrollView = mPullScrollView.getRefreshableView();


        // ++++++++++++++++++++++++


        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View activity_view = inflater.inflate(R.layout.activity_main, null);


        searchText = activity_view.findViewById(R.id.search);
        listView = activity_view.findViewById(R.id.mainlist);

        data = new LinkedList<>();
        data2 = new LinkedList<>();

        adapter = new Main_List_Adapter(MainActivity.this, data);
        listView.setVerticalScrollBarEnabled(false); // 隐藏右侧滚动条
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, InnerWebView.class);
                String itemURL = "http://rednum.cn/ViewListAction?method=detail&dataid=" + data.get(i-1).get("urlID") + "&classfyid=1";
                intent.putExtra("itemURL", itemURL);
                startActivity(intent);
            }
        });

        searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.new_enter_from_bottom, R.anim.old_exit_to_bottom);
            }
        });


        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // getChildCount() : listView当前显示出来的item数量，也就是在屏幕上可以看到的数量
                // getChildAt(int pos) : listView当前显示出来的item中的第pos项，也就是说索引是从显示出来的第一项开始计算的，如果屏幕上方有n个item看不见，那么这个函数的返回值就是n
                // getFirstVisiblePosition() : 当前可以看到的第一个item，即使只能看到一部分也算，不需要完全可见
                // getTop() : View相对于它的父控件的top值，对于item而言，如果item只有一半显示出来（下半部分），那么返回值应该是 - 这个item的高度
//                Log.i("yang", "--->相对于它的父控件的top值：" + listView.getChildAt(0).getTop());
//                if (absListView.getChildCount() > 0 && absListView.getFirstVisiblePosition() == 0 && absListView.getChildAt(0).getTop() >= 0) {
//                    PullToRefreshBase.FIRST_ITEM_VISIBLE = true;
//                } else {
//                    PullToRefreshBase.FIRST_ITEM_VISIBLE = false;
//                }

                if ((visibleItemCount > 0) && (firstVisibleItem == 0)) {
                    if (absListView.getChildAt(0).getTop() >= 0) {
                        Log.i("yang5", "===>顶部");
                        PullToRefreshBase.FIRST_ITEM_VISIBLE = true;
                    } else {
                        Log.i("yang5", "===>non顶部");
                        PullToRefreshBase.FIRST_ITEM_VISIBLE = false;
                    }
                }


                int currentHeight = absListView.getHeight();
                Log.i("yang5", "currentHeight=" + currentHeight);

                if ((totalItemCount > 0) && (absListView.getLastVisiblePosition() == totalItemCount - 1)) {
                    if (currentHeight == absListView.getChildAt(absListView.getChildCount() - 1).getBottom()) {
                        Log.i("yang5", "absListView.getChildAt(absListView.getChildCount()-1).getBottom()=" + absListView.getChildAt(absListView.getChildCount() - 1).getBottom());
                        Log.i("yang5", "===>底部");
                        PullToRefreshBase.LAST_ITEM_VISIBLE = true;
                    } else {
                        Log.i("yang5", "===>非底部");
                        PullToRefreshBase.LAST_ITEM_VISIBLE = false;
                    }
                }
            }
        });


        mScrollView.addView(activity_view);

        setLastUpdateTime();

        // ===> 进行联网的子线程
        gson = new Gson();
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (NetworkProcessor.isNetworkAvailable(MainActivity.this)) {
                    homePage = NetworkProcessor.getHomePage();

                    Log.i("content", "正常刷新找到的内容为：" + homePage);

                    if ("".equals(homePage) || homePage == null) {
                        handler.sendEmptyMessage(99);
                        return;
                    }

                    List<HashMap<String, Object>> list = gson.fromJson(homePage, List.class);

                    for (Object obj : list) {
                        LinkedTreeMap map = (LinkedTreeMap) obj;
                        String title = (String) map.get("title");  // 主标题
                        Log.i("content23", "title=" + title);
                        BigDecimal bd = new BigDecimal((Double) map.get("pubtime"));
                        String pubtime = formatPubtime(bd.toPlainString());  // 发布时间
                        String keyWords = map.get("description").toString(); // 关键字信息
                        String author = map.get("AUTHOR").toString();  // 作者
                        String content = map.get("content").toString();  // 正文描述信息
                        String level = formetLevel((new Double((Double) map.get("LEVEL"))).intValue());  // 级别
                        String picURL = map.get("logourl").toString();  // 图片
                        int urlID = (int) Float.parseFloat(map.get("ID").toString());

                        HashMap<String, Object> itemMap = new HashMap<>();
                        itemMap.put("title", title);
                        itemMap.put("pubtime", pubtime);
                        itemMap.put("keyWords", keyWords);
                        itemMap.put("author", author);
                        itemMap.put("content", content);
                        itemMap.put("level", level);
                        itemMap.put("urlID", urlID);
                        Log.i("picURL", "MainActivity中->picURL=" + picURL);
                        itemMap.put("picURL", picURL);

                        data2.add(itemMap);
                    }

                    data.clear();
                    data.addAll(data2);
                    handler.sendEmptyMessage(101);
                } else {
                    handler.sendEmptyMessage(99);
                }
            }
        }).start();


        // ++++++++++++++++++++++++
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            try {
                if (NetworkProcessor.isNetworkAvailable(MainActivity.this)) {
                    String homePage2 = NetworkProcessor.getHomePage();

                    if (!"".equals(homePage2) && homePage2 != null) {


                        if (homePage2.equals(homePage2)) {
                            Log.i("simple", "=====> 内容相同");
                            handler.sendEmptyMessage(2000);
                        } else {
                            List<HashMap<String, Object>> list = gson.fromJson(homePage2, List.class);
                            for (Object obj : list) {
                                LinkedTreeMap map = (LinkedTreeMap) obj;
                                String title = (String) map.get("title");  // 主标题
                                BigDecimal bd = new BigDecimal((Double) map.get("pubtime"));
                                String pubtime = formatPubtime(bd.toPlainString());  // 发布时间
                                String keyWords = map.get("description").toString(); // 关键字信息
                                String author = map.get("AUTHOR").toString();  // 作者
                                String content = map.get("content").toString();  // 正文描述信息
                                String level = formetLevel((new Double((Double) map.get("LEVEL"))).intValue());  // 级别
                                String picURL = map.get("logourl").toString();  // 图片
                                int urlID = (int) Float.parseFloat(map.get("ID").toString());

                                HashMap<String, Object> itemMap = new HashMap<>();
                                itemMap.put("title", title);
                                itemMap.put("pubtime", pubtime);
                                itemMap.put("keyWords", keyWords);
                                itemMap.put("author", author);
                                itemMap.put("content", content);
                                itemMap.put("level", level);
                                itemMap.put("urlID", urlID);
                                itemMap.put("picURL", picURL);

                                data2.add(itemMap);
                            }
                            data.clear();
                            data.addAll(data2);
                            handler.sendEmptyMessage(101);
                        }
                    } else {
                        handler.sendEmptyMessage(99);
                    }
                } else {
                    handler.sendEmptyMessage(99);
                }
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            mPullScrollView.onPullDownRefreshComplete();
            setLastUpdateTime();

            super.onPostExecute(result);
        }

    }


    private class PullUpToRefresh extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
                handler.sendEmptyMessage(1009);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            mPullScrollView.onPullUpRefreshComplete();
            super.onPostExecute(result);
        }

    }




    private void setLastUpdateTime() {
        String text = formatDateTime(System.currentTimeMillis());
        mPullScrollView.setLastUpdatedLabel(text);
    }

    private String formatDateTime(long time) {
        if (0 == time) {
            return "";
        }
        return mDateFormat.format(new Date(time));
    }


    // 将Json中的时间戳转换为时间
    private String formatPubtime(String timeStamp) {
        String time;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        long lt = Long.parseLong(timeStamp);
        Date date = new Date(lt * 1000);
        time = simpleDateFormat.format(date);

        return time;
    }

    private String formetLevel(int level) {
        String levelStr;
        switch (level) {
            case 1:
                levelStr = "初级";
                break;

            case 2:
                levelStr = "进阶";
                break;

            case 3:
                levelStr = "职业";
                break;

            case 4:
                levelStr = "商业";
                break;

            case 5:
                levelStr = "黑客";
                break;

            default:
                levelStr = "出现异常";
                break;
        }
        return levelStr;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChange);  // 注销接收网络状况变化的广播接收器
    }
}
