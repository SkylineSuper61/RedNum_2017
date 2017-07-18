package com.example.administrator.rednum;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Administrator on 2017/7/7.
 */

public class Search_History_Adapter extends BaseAdapter {
    private Context context;
    private LinkedList<String> data;

    public Search_History_Adapter(Context context, LinkedList data){
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.search_history_item_layout, null);

            holder.searchHistoryItem = convertView.findViewById(R.id.search_history_item);
            holder.delete = convertView.findViewById(R.id.search_history_delete);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.searchHistoryItem.setText(data.get(i));
        final int index = i;
        holder.searchHistoryItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!SearchActivity.isSearching){
                    SearchActivity.progressBar.setVisibility(View.VISIBLE);
                    SearchActivity.isSearching = true;
                    Intent intent = new Intent(context, SearchResultActivity.class);
                    String contnet = (String) ((TextView)view).getText();
                    intent.putExtra("keyword", contnet);
                    context.startActivity(intent);

                    SearchActivity.progressBar.setVisibility(View.GONE);
                    SearchActivity.isSearching = false;

                    SearchActivity.getSearchKeyWord(contnet);
                }

            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.remove(index);
                Search_History_Adapter.this.notifyDataSetChanged();
            }
        });
        return convertView;
    }

    class ViewHolder{
        TextView searchHistoryItem;
        ImageView delete;
    }
}
