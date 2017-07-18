package com.example.administrator.rednum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.rednum.NetworkSpider.NetworkProcessor;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/7.
 */

public class SearchActivity extends Activity {
    private TextView conductSearch;
    private FlowLayout flowLayout;
    private TextView tv;

    private EditText search;
    protected static ProgressBar progressBar;
    protected static boolean isSearching = false;

    private String homePage;
    private Gson gson;
    private LinkedList<HashMap<String, Object>> data;
    private LinkedList<HashMap<String, Object>> data2;
    private Handler handler = null;

    // 每次此Activity结束时，都需要将搜索记录存入到SharedPreference
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String classBufferStr = null;

    private String[] texts = new String[]{
            "Java", "Python", "Objective-C", "Swift", "C", "C++", "PHP", "C#",
            "Perl", "Go", "JavaScript", "R", "Ruby", "MATLAB"
    };

    private String[] textBackgrounds = {
            "#FF6EB4", "#EE2C2C", "#8968CD", "#EE3A8C", "#CD8500", "#66CD00", "#8B7D6B", "#7B68EE", "#00868B", "#A0522D"
    };

    private ListView listView;
    private static LinkedList<String> searchHistory = new LinkedList<>();
    private static Search_History_Adapter adapter = null;


    public static void getSearchKeyWord(String search_Key_Word) {
        if (searchHistory.size() == 0 || (searchHistory.size() >= 1 && !search_Key_Word.equals(searchHistory.get(0)))) {
            searchHistory.add(0, search_Key_Word);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }


    int length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.search_activity_layout);

        preferences = getSharedPreferences("search_history", MODE_PRIVATE);
        editor = preferences.edit();

        length = texts.length;

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 201:
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
                        RedNumApplication redNum = (RedNumApplication) getApplication();
                        redNum.setFilterSearchList(data);
                        startActivity(intent);

                        getSearchKeyWord(classBufferStr);
                        isSearching = false;

                        break;

                    case 202:
                        progressBar.setVisibility(View.GONE);
                        isSearching = false;
                        Toast.makeText(getApplicationContext(), "指定内容不存在", Toast.LENGTH_SHORT).show();

                        break;

                    case 199:
                        progressBar.setVisibility(View.GONE);
                        isSearching = false;
                        Toast.makeText(getApplicationContext(), "无网络连接", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }
            }
        };

        data = new LinkedList<>();
        data2 = new LinkedList<>();

        search = findViewById(R.id.search);

        conductSearch = findViewById(R.id.conduct_search);
        progressBar = findViewById(R.id.progress);
        flowLayout = findViewById(R.id.flowlayout);

        listView = findViewById(R.id.search_history);

        conductSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSearching) {
                    String searchContent = search.getText().toString();
                    if (!"".equals(searchContent)) {
                        progressBar.setVisibility(View.VISIBLE);
                        getSearchData(searchContent);
                        isSearching = true;
                    }else{
                        Toast.makeText(getApplicationContext(), "搜索内容不能为空", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        for (int i = 0; i < length; i++) {
            int ranHeight = dip2px(this, 30);
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ranHeight);
            lp.setMargins(dip2px(this, 3), 0, dip2px(this, 3), 0);
            tv = new TextView(this);
            tv.setOnClickListener(new TextViewOnClickListener());
            tv.setPadding(dip2px(this, 15), 0, dip2px(this, 15), 0);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setText(texts[i]);
            tv.setGravity(Gravity.CENTER_VERTICAL);
            tv.setLines(1);
            tv.setBackgroundResource(R.drawable.bg_tag);
            String rgb = textBackgrounds[(int) (Math.random() * textBackgrounds.length)];
            tv.setBackgroundResource(R.drawable.bg_tag);
            GradientDrawable myGrad = (GradientDrawable) tv.getBackground();
            myGrad.setColor(Color.parseColor(rgb));
            tv.setBackgroundDrawable(myGrad);
            flowLayout.addView(tv, lp);
            flowLayout.relayoutToCompress();
        }

        if (preferences.contains("history_length") && preferences.getInt("history_length", 0) > 0) {
            for (int i = 0; i < preferences.getInt("history_length", 0); i++) {
                searchHistory.add(preferences.getString("history" + i, ""));
            }
            Log.i("preference", "从ShearedPreference中加载数据完毕");
        }

        adapter = new Search_History_Adapter(this, searchHistory);
        listView.setAdapter(adapter);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    class TextViewOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (!isSearching) {
                progressBar.setVisibility(View.VISIBLE);
                String searchContent = ((TextView) view).getText().toString();
                getSearchData(searchContent);
                isSearching = true;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 此Activity退出时，将搜索记录存入数据库
        editor.putInt("history_length", searchHistory.size());
        for (int i = 0; i < searchHistory.size(); i++) {
            editor.putString("history" + i, searchHistory.get(i));
        }
        editor.commit();
        Log.i("preference", "已经将数据存入SharedPreference");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        overridePendingTransition(R.anim.new_enter_from_alpha, R.anim.old_exit_from_top);
        return super.onKeyDown(keyCode, event);
    }

    private void getSearchData(String keyword) {
        classBufferStr = keyword;
        final String key = keyword;

        gson = new Gson();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (NetworkProcessor.isNetworkAvailable(SearchActivity.this)) {
                    homePage = NetworkProcessor.getHomePage();

                    if ("".equals(homePage) || homePage == null) {
                        handler.sendEmptyMessage(199);
                        return;
                    }

                    List<HashMap<String, Object>> list = gson.fromJson(homePage, List.class);

                    data2.clear();

                    for (Object obj : list) {
                        LinkedTreeMap map = (LinkedTreeMap) obj;
                        String title = (String) map.get("title");  // 主标题
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
                        handler.sendEmptyMessage(201);
                    } else {
                        handler.sendEmptyMessage(202);
                    }
                } else {
                    handler.sendEmptyMessage(199);
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
