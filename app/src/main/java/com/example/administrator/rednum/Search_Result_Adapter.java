package com.example.administrator.rednum;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Administrator on 2017/7/14.
 */

public class Search_Result_Adapter extends BaseAdapter {
    private Context context;
    private LinkedList<HashMap<String, Object>> list;
    private ViewHolder holder;
    private LayoutInflater inflater;
    private DisplayImageOptions options;

    public Search_Result_Adapter(Context context, LinkedList<HashMap<String, Object>> list){
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);

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
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
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
        }

        HashMap<String, Object> map = list.get(i);
        // ViewHolder的属性title, describe分别代表着View中的子控件
        holder.title.setText((String) map.get("title"));  // ==> convertView.findViewById(R.id.title).setText(map.get("key"))
        holder.content.setText((String) map.get("content"));
        holder.author.setText((String) map.get("author"));
        holder.date.setText((String) map.get("pubtime"));
        holder.category.setText((String) map.get("keyWords"));
        holder.level.setText((String) map.get("level"));
        String picURL = (String) map.get("picURL");
        ImageLoader.getInstance().displayImage(picURL, holder.imageView, options); // 异步加载图片框架

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
}
