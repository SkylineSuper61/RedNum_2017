package com.example.administrator.rednum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
 * Created by Administrator on 2017/7/6.
 */

public class InnerWebView extends Activity {
    private WebView webView;
    private ImageView imageView;
    private Intent intent;
    private String url;
    private EditText searchEditText;
    private TextView searchButton;
    private ProgressBar progressBar;

    private String homePage;
    private Gson gson;
    private LinkedList<HashMap<String, Object>> data;
    private LinkedList<HashMap<String, Object>> data2;
    private String classBufferStr = null;

    private Handler handler = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.inner_webview_layout);

        intent = getIntent();
        url = intent.getStringExtra("itemURL");



        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 201:
                        Intent intent = new Intent(InnerWebView.this, SearchResultActivity.class);
                        RedNumApplication redNum = (RedNumApplication) getApplication();
                        redNum.setFilterSearchList(data);
                        startActivity(intent);

                        break;

                    case 202:
                        Toast.makeText(getApplicationContext(), "指定内容不存在", Toast.LENGTH_SHORT).show();

                        break;

                    case 199:
                        Toast.makeText(getApplicationContext(), "无网络连接", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }
            }
        };

        data = new LinkedList<>();
        data2 = new LinkedList<>();



        webView = findViewById(R.id.webview);
        searchEditText = findViewById(R.id.inner_search);
        progressBar = findViewById(R.id.progress);
        searchEditText = findViewById(R.id.inner_search);
        searchButton = findViewById(R.id.conduct_search);

        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String searchContent = searchEditText.getText().toString();
                if(!"".equals(searchContent)){
                    getSearchData(searchContent);
                    SearchActivity.getSearchKeyWord(searchContent);
                }
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);

        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO Auto-generated method stub
                // 当页面中出现内容时就可以取消Progressbar的显示，故设置进度值为60而非100
                if (newProgress <= 60) {
                    // 加载中
                    Log.i("web", "网页正在加载中");
                    progressBar.setVisibility(View.VISIBLE);

                } else {
                    progressBar.setVisibility(View.GONE);
                }

            }
        });


        imageView = findViewById(R.id.back_to_main);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InnerWebView.this.finish();
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();//返回上一页面
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }



    private void getSearchData(String keyword) {
        classBufferStr = keyword;
        final String key = keyword;

        Log.i("key", "key1=" + key);

        gson = new Gson();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (NetworkProcessor.isNetworkAvailable(InnerWebView.this)) {
                    homePage = NetworkProcessor.getHomePage();
                    if("".equals(homePage) || homePage == null){
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

