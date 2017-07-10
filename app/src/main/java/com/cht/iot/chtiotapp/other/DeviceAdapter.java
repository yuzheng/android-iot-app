package com.cht.iot.chtiotapp.other;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.MyHolder>{

    private List<DeviceItem> listData;
    private LayoutInflater inflater;

    private ItemClickCallBack itemClickCallBack;

    public interface ItemClickCallBack{
        void onItemClick(int position);
    }

    public void setItemClickCallBack (final ItemClickCallBack itemClickCallBack)
    {
        this.itemClickCallBack = itemClickCallBack;
    }

    public DeviceAdapter(List<DeviceItem> listData, Context context)
    {
        this.listData = listData;
        inflater = LayoutInflater.from(context);

        Log.d("DeviceAdapter", Integer.toString(listData.size()));
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        DeviceItem item = listData.get(position);
        holder.tv_DeviceName.setText(item.getDeviceName());
        holder.tv_DeviceDesc.setText(item.getDeviceDesc());
        holder.img_Icon.setImageResource(item.getImgSource());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView tv_DeviceName;
        private TextView tv_DeviceDesc;
        private ImageView img_Icon;
        private View container;

        public MyHolder(View itemView) {
            super(itemView);

            tv_DeviceName = (TextView) itemView.findViewById(R.id.tv_DeviceName);
            tv_DeviceDesc = (TextView) itemView.findViewById(R.id.tv_DeviceDesc);
            img_Icon = (ImageView) itemView.findViewById(R.id.iv_Icon);
            container = itemView.findViewById(R.id.root);
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.root)
            {
                itemClickCallBack.onItemClick(getAdapterPosition());
            }
            else
            {
                Log.d("Click", "onClick error");
            }
        }
    }

}
