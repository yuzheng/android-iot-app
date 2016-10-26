package com.cht.iot.chtiotapp.other;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cht.iot.chtiotapp.R;

import java.util.List;

/**
 * Created by user on 2016/10/26.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder>{

    private List<ListItem> listData;
    private LayoutInflater inflater;

    public MyAdapter(List<ListItem> listData, Context context)
    {
        this.listData = listData;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        ListItem item = listData.get(position);
        holder.tv_DeviceName.setText(item.getDeviceName());
        holder.img_Icon.setImageResource(item.getImgSource());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        private TextView tv_DeviceName;
        private ImageView img_Icon;
        private View container;

        public MyHolder(View itemView) {
            super(itemView);

            tv_DeviceName = (TextView) itemView.findViewById(R.id.tv_DeviceName);
            img_Icon = (ImageView) itemView.findViewById(R.id.iv_Icon);
            container = itemView.findViewById(R.id.root);
        }
    }

}
