package com.cht.iot.chtiotapp.other;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016/10/26.
 */

public class MyData {

    //Fake Data to demonstrate on RecycleView
    private static final String[] deviceName = {"device-HTC", "device-I-Phone", "device-SONY"};

    private static final int[] icons =
                    {android.R.drawable.picture_frame,
                    android.R.drawable.ic_media_ff,
                    android.R.drawable.ic_media_play};

    public static List<ListItem> getListData(){
        List<ListItem> data = new ArrayList<>();

        for (int x = 0; x < 5; x++)
        {
            for (int i = 0; i< deviceName.length && i < icons.length; i++)
            {
                ListItem item = new ListItem();
                item.setDeviceName(deviceName[i]);
                item.setImgSource(icons[i]);
                data.add(item);
            }
        }
        return data;
    }
}
