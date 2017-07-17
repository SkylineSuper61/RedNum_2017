package com.example.administrator.rednum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.administrator.rednum.NetworkSpider.NetworkProcessor;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/14.
 */

public class SearchResultActivity extends Activity {
    private ListView listView;
    private String homePage;
    private Gson gson;
    private LinkedList<HashMap<String, Object>> data;
    private LinkedList<HashMap<String, Object>> data2;
    private Handler handler = null;
    private Search_Result_Adapter adapter;
    private LinearLayout back;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.search_result_activity);


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 301:
                        adapter.notifyDataSetChanged();
                        break;

                    case 302:
                        Toast.makeText(getApplicationContext(), "未找到数据", Toast.LENGTH_SHORT).show();
                        break;

                    case 399:
                        Toast.makeText(getApplicationContext(), "无网络连接", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }
            }
        };


        back = findViewById(R.id.back_to_search_activity);
        listView = findViewById(R.id.search_result_list);

        intent = getIntent();
        String key = intent.getStringExtra("keyword");
        Log.i("null", "key1=" + key);
        if(key == null){
            RedNumApplication redNum = (RedNumApplication) getApplication();
            data =  redNum.getFilterSearchList();
        }else{
            Log.i("red", "====> getintent不为空, 传过来的值等于：" + key);
            data = new LinkedList<>();
            data2 = new LinkedList<>();
            getSearchData(key);
        }

        adapter = new Search_Result_Adapter(SearchResultActivity.this, data);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(SearchResultActivity.this, InnerWebView.class);
                String itemURL = "http://rednum.cn/ViewListAction?method=detail&dataid=" + data.get(i).get("urlID") + "&classfyid=1";
                intent.putExtra("itemURL", itemURL);
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void getSearchData(String keyword) {
        Log.i("null", "key2=" + keyword);
        final String key = keyword;
        gson = new Gson();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (NetworkProcessor.isNetworkAvailable(SearchResultActivity.this)) {
                    homePage = NetworkProcessor.getHomePage();
                    if("".equals(homePage) || homePage == null){
                        handler.sendEmptyMessage(399);
                        return;
                    }
                    List<HashMap<String, Object>> list = gson.fromJson(homePage, List.class);

                    for (Object obj : list) {
                        LinkedTreeMap map = (LinkedTreeMap) obj;
                        String title = (String) map.get("title");  // 主标题
                        Log.i("null", "key3=" + key);

                        if (title.toUpperCase().indexOf(key.toUpperCase()) != -1) {
                            Log.i("key", "key2===> 进入到for-> if");
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
                    }

                    data.clear();
                    data.addAll(data2);
                    if (data.size() > 0) {
                        handler.sendEmptyMessage(301);
                    } else {
                        handler.sendEmptyMessage(302);
                    }
                } else {
                    handler.sendEmptyMessage(399);
                }
            }
        }).start();
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
}
