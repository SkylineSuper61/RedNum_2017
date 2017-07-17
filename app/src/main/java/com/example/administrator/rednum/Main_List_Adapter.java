package com.example.administrator.rednum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.rednum.NetworkSpider.NetworkProcessor;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/6/28.
 */

public class Main_List_Adapter extends BaseAdapter {

    private Context context;
    private LinkedList<HashMap<String, Object>> list;

    private View page1, page2, page3;
    private ViewPager viewPager;
    private List<View> viewList;
    private LayoutInflater inflater;
    private RedNum_PagerAdapter pagerAdapter;

    private int current_Point = 0;//记录当前点的位置

    //        private Handler handler;
    private Timer timer;
    //    private ViewPagerTask viewPagerTask;
    private DisplayImageOptions options;  // ListView上异步加载图片的框架
//    public ImageHandler handler = new ImageHandler(/*new WeakReference(context)*/);

//    private ImageHandler handler = new ImageHandler();

    public Main_List_Adapter(final Context context, LinkedList list) {
        this.context = context;
        this.list = list;

        inflater = LayoutInflater.from(context);
        page1 = inflater.inflate(R.layout.viewpager_layout1, null);
        page2 = inflater.inflate(R.layout.viewpager_layout2, null);
        page3 = inflater.inflate(R.layout.viewpager_layout3, null);

        viewList = new ArrayList<>();
        pagerAdapter = new RedNum_PagerAdapter(context, viewList);
        viewList.add(page1);
        viewList.add(page2);
        viewList.add(page3);

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        ViewHolder holder; // 作用为避免在复用convertView时进行树查找操作
        if (i == 0) {
            Log.i("shunxu", "########> 绘制第" + i + "个ViewPager");

            convertView = inflater.inflate(R.layout.first_item_layout, null);  // ===> 此处对应ViewPager的布局
            final ViewPager viewPager2 = convertView.findViewById(R.id.viewpager);
            viewPager = viewPager2;

            final List<ImageView> mPoints = new ArrayList<>();

            ImageView firstPoint = convertView.findViewById(R.id.dot_first);
            ImageView secondPoint = convertView.findViewById(R.id.dot_second);
            ImageView thirdPoint = convertView.findViewById(R.id.dot_thrid);
            mPoints.add(firstPoint);
            mPoints.add(secondPoint);
            mPoints.add(thirdPoint);
            mPoints.get(current_Point % 3).setImageResource(R.drawable.dot_focused);

            viewPager.setAdapter(pagerAdapter);
            viewPager.setCurrentItem(Integer.MAX_VALUE / 2);  // 默认在中间，使用户看不到边界
            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                //配合Adapter的currentItem字段进行设置。
                @Override
                public void onPageSelected(int arg0) {
//                    handler.sendMessage(Message.obtain(handler, ImageHandler.MSG_PAGE_CHANGED, arg0, 0));  // ========> 当手动选中时，执行此方法
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    // 此方法内只切换指示点的变化，不负责ViewPager上的图片切换
                    mPoints.get(current_Point % 3).setImageResource(R.drawable.dot_normal);
                    mPoints.get(position % 3).setImageResource(R.drawable.dot_focused);
                    current_Point = position;
                }

                //覆写该方法实现轮播效果的暂停和恢复
                @Override
                public void onPageScrollStateChanged(int arg0) {
                    switch (arg0) {
                        case ViewPager.SCROLL_STATE_DRAGGING:
//                            handler.sendEmptyMessage(ImageHandler.MSG_KEEP_SILENT);
                            break;

                        case ViewPager.SCROLL_STATE_IDLE:
//                            handler.sendEmptyMessageDelayed(ImageHandler.MSG_UPDATE_IMAGE, ImageHandler.MSG_DELAY);
                            break;
                        default:
                            break;
                    }
                }
            });
//            handler.sendEmptyMessageDelayed(ImageHandler.MSG_UPDATE_IMAGE, ImageHandler.MSG_DELAY);
//            Log.i("handle", "===> 延迟更新消息已经发送");

        } else {
            if (convertView == null) {
                // 当item未满一屏时，每次构建一个item都需要新建，从xml文件映射到View为IO耗时操作，应尽量避免
                // 对于每个新建View，都创建一个ViewHolder对象，并且将View中的子控件赋给ViewHolder对象的属性值
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.itemlayout, null);

                holder.title = convertView.findViewById(R.id.title);
                holder.content = convertView.findViewById(R.id.describe);
                holder.author = convertView.findViewById(R.id.author);
                holder.date = convertView.findViewById(R.id.date);
                holder.category = convertView.findViewById(R.id.category);
                holder.level = convertView.findViewById(R.id.level);
                holder.imageView = convertView.findViewById(R.id.item_image);

                convertView.setTag(holder);  // 将ViewHolder绑定到View上
            } else {
                holder = (ViewHolder) convertView.getTag();
                if (holder == null) {
                    holder = new ViewHolder();
                    convertView = inflater.inflate(R.layout.itemlayout, null);

                    holder.title = convertView.findViewById(R.id.title);
                    holder.content = convertView.findViewById(R.id.describe);
                    holder.author = convertView.findViewById(R.id.author);
                    holder.date = convertView.findViewById(R.id.date);
                    holder.category = convertView.findViewById(R.id.category);
                    holder.level = convertView.findViewById(R.id.level);
                    holder.imageView = convertView.findViewById(R.id.item_image);

                    convertView.setTag(holder);  // 将ViewHolder绑定到View上
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
            }
            HashMap<String, Object> map = list.get(i - 1);  // 第一项用于显示图片，所以索引值要减１
            // ViewHolder的属性title, describe分别代表着View中的子控件
            holder.title.setText((String) map.get("title"));  // ==> convertView.findViewById(R.id.title).setText(map.get("key"))
            holder.content.setText((String) map.get("content"));
            holder.author.setText((String) map.get("author"));
            holder.date.setText((String) map.get("pubtime"));
            holder.category.setText((String) map.get("keyWords"));
            holder.level.setText((String) map.get("level"));
            String picURL = (String) map.get("picURL");
            ImageLoader.getInstance().displayImage(picURL, holder.imageView, options); // 异步加载图片框架
        }

        return convertView;
    }

    private class ViewHolder {
        TextView title;
        TextView content;
        TextView author;
        TextView date;
        TextView category;
        TextView level;
        ImageView imageView;
    }


//    private int currentItem = 0;
//
//    class ImageHandler extends Handler {
//        /**
//         * 请求更新显示的View。
//         */
//        protected static final int MSG_UPDATE_IMAGE = 1;
//        /**
//         * 请求暂停轮播。
//         */
//        protected static final int MSG_KEEP_SILENT = 2;
//        /**
//         * 请求恢复轮播。
//         */
//        protected static final int MSG_BREAK_SILENT = 3;
//        /**
//         * 记录最新的页号，当用户手动滑动时需要记录新页号，否则会使轮播的页面出错。
//         * 例如当前如果在第一页，本来准备播放的是第二页，而这时候用户滑动到了末页，
//         * 则应该播放的是第一页，如果继续按照原来的第二页播放，则逻辑上有问题。
//         */
//        protected static final int MSG_PAGE_CHANGED = 4;
//
//        //轮播间隔时间
//        protected static final long MSG_DELAY = 3000;
//
//        //使用弱引用避免Handler泄露.这里的泛型参数可以不是Activity，也可以是Fragment等
////        public WeakReference<Context> weakReference;
//
//
////        protected ImageHandler(WeakReference wk){
////            weakReference = wk;
////        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//
//            Log.i("handle", "~~~~~~~~~~~~~~~~~~~~~~ msg.what = " + msg.what);
//
//            //检查消息队列并移除未发送的消息，这主要是避免在复杂环境下消息出现重复等问题。
//            if (handler.hasMessages(MSG_UPDATE_IMAGE)) {
//                handler.removeMessages(MSG_UPDATE_IMAGE);
//            }
//
//            switch (msg.what) {
//                case MSG_UPDATE_IMAGE:  // ====> 1
//                    currentItem++;
//                    viewPager.setCurrentItem(2);
//                    Log.i("handle", "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@> 更新 MSG_UPDATE_IMAGE，最新的索引为=" + currentItem);
//                    //准备下次播放
//                    handler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
//
//                    break;
//                case MSG_KEEP_SILENT:   // ====> 2
//                    //只要不发送消息就暂停了
//                    break;
//                case MSG_BREAK_SILENT:
//                    handler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
//                    break;
//                case MSG_PAGE_CHANGED:  // ===> 4
//                    //记录当前的页号，避免播放的时候页面显示不正确。
//                    currentItem = msg.arg1 % 3;
//                    Log.i("handle", "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& 当前记住的索引为: " + currentItem);
//                    break;
//
//                default:
//                    break;
//            }
//        }
//    }
}
