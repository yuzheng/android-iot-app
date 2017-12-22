package com.cht.iot.chtiotapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cht.iot.chtiotapp.R;
import com.cht.iot.chtiotapp.activity.MainActivity;
import com.cht.iot.chtiotapp.other.DividerItemDecoration;
import com.cht.iot.chtiotapp.other.IoTServer;
import com.cht.iot.chtiotapp.other.IoTServer;
import com.cht.iot.chtiotapp.other.SensorAdapter;
import com.cht.iot.chtiotapp.other.SensorItem;
import com.cht.iot.persistence.entity.api.ISensor;
import com.cht.iot.persistence.entity.data.HeartBeat;
import com.cht.iot.persistence.entity.data.Rawdata;
import com.cht.iot.service.api.OpenMqttClient;
import com.cht.iot.service.api.OpenRESTfulClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SensorFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SensorFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 *  SensorFragment 是用來呈現 Sensor 的清單列表
 *
 */
public class SensorFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView tv_UpdateTime;

    private OnFragmentInteractionListener mListener;

    // add mqtt
    OpenMqttClient mqtt;

    public SensorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SensorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SensorFragment newInstance(String param1, String param2) {
        SensorFragment fragment = new SensorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        if (mqtt != null) {
            mqtt.stop();
        }
        super.onDetach();

    }

    /**
     *   以上Code都為Fragment預設或自動所產生...
     */

    // SensorFragment會承接從DeviceFragment傳來的Device資訊，故用以下變數儲存
    private String device_name;
    private String device_desc;
    private String device_id;

    // 對IoT平台進行連線請求，會需要前一個畫面(MainActivity)登入成功後的API Key
    private String ApiKey;
    private ISensor[] sensors;

    private RecyclerView recyclerView;      // 官方推薦使用的高效能清單View(取代以往的ListView)
    private SensorAdapter adapter;          // 轉接器，負責將資料配置給recycleView
    private View view;                      // 用來代表RecycleView上的單一列
    private Context context;                // 獲取MainActivity的Context參照，以利許多Function呼叫

    public static int POST_ITEM = 0;


    private List<SensorItem> list_SensorItem;      //List of SensorItem 用來存放從IoT平台網頁上該Device底下的所有Sensor資訊

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // 初始化 List of SensorItem
        list_SensorItem = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 獲取被使用者點擊的device相關資訊 (由DevicesFragment中透過setArguments所傳入的參數)
        device_name = getArguments().getString(DevicesFragment.DEVICE_NAME);
        device_desc = getArguments().getString(DevicesFragment.DEVICE_DESC);
        device_id = getArguments().getString(DevicesFragment.DEVICE_ID);

        Log.e("[SensorFragment]設備資訊 ", "(1)名稱:" + device_name + ", (2)描述:" + device_desc + ", (3)ID:" + device_id);

        // 將fragment_sensor的xml面版Layout，轉成View類別，且當成子物件黏至container上
        view = inflater.inflate(R.layout.fragment_sensor, container, false);

        // 獲取MainActivity的Context參照，以利許多Function呼叫
        context = view.getContext();

        // 執行GetSensorsInfoTask異步任務建立App與IoT平台之RESTful連線
        // 並獲取Device底下所有的Sensor資訊，再呈現於RecycleView上
        new SensorFragment.GetSensorsInfoTask().execute();

        // mqtt init part
        mqtt = new OpenMqttClient(IoTServer.MQTT_HOST, IoTServer.MQTT_PORT, ApiKey);
        mqtt.setListener(new OpenMqttClient.ListenerAdapter() {
            @Override
            public void onRawdata(String topic, Rawdata rawdata) {
                onRawdataChanged(rawdata);
            }
        });
        // mqtt int part

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 設置fragment_sensor.xml上的設備名稱與描述
        TextView tv_Name = (TextView) getView().findViewById(R.id.tv_Name);
        TextView tv_Desc = (TextView) getView().findViewById(R.id.tv_Desc);
        tv_Name.setText(device_name);
        tv_Desc.setText(device_desc);

        // for 環保署
        tv_UpdateTime = (TextView) getView().findViewById(R.id.updatetime);



    }

    /*
    public void testMQTT(){
        Log.i("iotapp", "testMQTT");
        String host = "iot.cht.com.tw";
        String deviceKeyA = "DKPWWEMFPGX7C2571M";
        String deviceKeyB = "DKAEZE792XM93U9HWM";
        String deviceA = "276664437";
        String deviceB = "281494258";
        String sensorA = "aaa";
        String sensorB = "light";
        OpenMqttClient mqcA = new OpenMqttClient(host, 8883, deviceKeyA, true);
        mqcA.subscribe(deviceA, sensorA);
        OpenMqttClient mqcB = new OpenMqttClient(host, 8883, deviceKeyB, true);
        mqcB.subscribe(deviceB, sensorB);

        mqcA.setListener(new OpenMqttClient.Listener() {
            @Override
            public void onRawdata(String topic, Rawdata rawdata) {
                System.out.printf("Rawdata - deviceId: %s, id: %s, time: %s, value: %s\n", rawdata.getDeviceId(), rawdata.getId(), rawdata.getTime(), rawdata.getValue()[0]);
            }

            @Override
            public void onHeartBeat(String topic, HeartBeat heartbeat) {
                System.out.printf("HeartBeat - deviceId: %s, pulse: %s, from: %s, time: %s, type: %s\n", heartbeat.getDeviceId(), heartbeat.getPulse(), heartbeat.getFrom(), heartbeat.getTime(), heartbeat.getType());
            }

            @Override
            public void onReconfigure(String topic, String apiKey) {
                System.out.printf("Reconfigure - topic: %s, apiKey: %s\n", topic, apiKey);
            }

            @Override
            public void onSetDeviceId(String topic, String apiKey, String deviceId) {
                System.out.printf("SetDeviceId - topic: %s, apiKey: %s, deviceId: %s\n", topic, apiKey, deviceId);
            }
        });

        mqcB.setListener(new OpenMqttClient.Listener() {
            @Override
            public void onRawdata(String topic, Rawdata rawdata) {
                System.out.printf("Rawdata - deviceId: %s, id: %s, time: %s, value: %s\n", rawdata.getDeviceId(), rawdata.getId(), rawdata.getTime(), rawdata.getValue()[0]);
            }

            @Override
            public void onHeartBeat(String topic, HeartBeat heartbeat) {
                System.out.printf("HeartBeat - deviceId: %s, pulse: %s, from: %s, time: %s, type: %s\n", heartbeat.getDeviceId(), heartbeat.getPulse(), heartbeat.getFrom(), heartbeat.getTime(), heartbeat.getType());
            }

            @Override
            public void onReconfigure(String topic, String apiKey) {
                System.out.printf("Reconfigure - topic: %s, apiKey: %s\n", topic, apiKey);
            }

            @Override
            public void onSetDeviceId(String topic, String apiKey, String deviceId) {
                System.out.printf("SetDeviceId - topic: %s, apiKey: %s, deviceId: %s\n", topic, apiKey, deviceId);
            }
        });

        mqcA.start(); // wait for incoming message
        mqcB.start(); // wait for incoming message
    }
    */

    // SensorAdapter中取得SensorFragment的實例，並呼叫startActivityForResult
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("SensorFragment", requestCode + ", " + resultCode);

        if(requestCode == SensorAdapter.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
        {
            Log.e("SensorFragment ", "onActivityResult");

            Bitmap mImageBitmap;

            // 方法(一) 簡單的Bitmap
            Bundle extras = data.getExtras();
            mImageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mImageBitmap.compress((Bitmap.CompressFormat.PNG), 0, bos);
            byte[] bytes = bos.toByteArray();
            SensorAdapter.IMAGE_BODY = new ByteArrayInputStream(bytes);

            adapter.new SendInfoTask(POST_ITEM).execute();
            Log.e("BITMAP", "PHOTO OK");

            /* 方法(二) 取得內建相機所拍的檔案 礙於原尺寸高解析度 使上下載速度變慢

            try {
                mImageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(SensorAdapter.PHOTO_PATH));

                // 複雜的檔案轉換 設定最重要的ImageBody
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                mImageBitmap.compress((Bitmap.CompressFormat.PNG), 0, bos);
                byte[] bytes = bos.toByteArray();
                SensorAdapter.IMAGE_BODY = new ByteArrayInputStream(bytes);

                // 進行圖片資料的上傳
                adapter.new SendInfoTask(POST_ITEM).execute();

                Log.e("BITMAP", "PHOTO OK");
            } catch (IOException e) {
                e.printStackTrace();
            }

            */
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // TODO Add your menu entries here
        inflater.inflate(R.menu.sensor_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rawdata_update:
                new SensorFragment.GetSensorsInfoTask().execute();
                break;

        }
        return true;

    }

    public void onRawdataChanged(final Rawdata rawdata) {
        Log.v("iotapp","mqtt on message:"+rawdata.getId());  //JsonUtils.toJson(

        // not yet implement refresh item in adaptor
    }

    /*  AsyncTask enables proper and easy use of the UI thread.
            This class allows you to perform background operations and publish results
            on the UI thread without having to manipulate threads and/or handlers.
        */
    private class GetSensorsInfoTask extends AsyncTask<String, Integer, String>
    {
        //ProgressDialog instance
        private ProgressDialog progressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Get the ApiKey First
            ApiKey = MainActivity.getApiKey();

            //setting RecycleView and give it an adapter
            recyclerView = (RecyclerView) view.findViewById(R.id.recycleview_Sensors);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            //initialize the ProgressDialog
            progressBar = new ProgressDialog(context);
            progressBar.setMessage("讀取中...");
            progressBar.setCancelable(false);
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.show();

        }



        @Override
        protected String doInBackground(String... params) {

            OpenRESTfulClient client = new OpenRESTfulClient(IoTServer.RESTful_HOST, IoTServer.RESTful_PORT, ApiKey);

            try {

                sensors = client.getSensors(device_id);


                // 先清空現有資料，以免於SensorFragment按下手動更新時，造成item重複新增於list之上
                list_SensorItem.clear();

                int length = sensors.length;
                float max = (float)length;
                int progress = 0;

                if(length != 0)
                {

                    for(int i = 0; i<length; i++) {

                        // mqtt subscribe
                        Log.v("iotapp","add subscribe: "+device_id+", "+sensors[i].getId());
                        mqtt.subscribe(device_id, sensors[i].getId());

                        SensorItem item = new SensorItem();

                        // Give device_id & sensor_id to get the Rawdata
                        final Rawdata rawdata = client.getRawdata(device_id, sensors[i].getId());

                        // Convert rawdata to String Array to use
                        String[] str_RawData = rawdata.getValue();

                        //for 環保署
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                            if(rawdata!=null && rawdata.getTime()!=null) {
                                tv_UpdateTime.setText(rawdata.getTime().toString());
                            }
                            }
                        });



                        // If data have not initialize, we should give it a default value.
                        if(str_RawData.length == 0)
                        {
                            str_RawData = new String[1];
                            str_RawData[0] = "NO RAWDATA IN THIS SENSOR!";
                        }

                        item.setSensorName(sensors[i].getName());

                        String type = sensors[i].getType();

                        if (type.equalsIgnoreCase("gauge"))
                        {
                            item.setSensorIcon(R.mipmap.image_gauge);
                            item.setType(SensorItem.BUTTON);
                            item.setSensorTextOrNumber(str_RawData[0]);
                        }
                        else if(type.equalsIgnoreCase("switch"))
                        {
                            item.setSensorIcon(R.mipmap.image_switch);
                            item.setType(SensorItem.TOGGLEBTN);

                            boolean bool;
                            int value = Integer.parseInt(str_RawData[0]);
                            if(value == 0)
                                bool = false;
                            else
                                bool = true;

                            item.setSensorToggleBtn(bool);
                        }
                        else if(type.equalsIgnoreCase("snapshot"))
                        {
                            item.setSensorIcon(R.mipmap.image_snapshot);
                            item.setType(SensorItem.IMAGEBTN);

                            InputStream inputStream = client.getSnapshotBody(device_id, sensors[i].getId());
                            item.setSensorImageBtn(BitmapFactory.decodeStream(inputStream));
                        }
                        else if(type.equalsIgnoreCase("counter"))
                        {
                            item.setSensorIcon(R.mipmap.image_counter);
                            item.setType(SensorItem.BUTTON);
                            item.setSensorTextOrNumber(str_RawData[0]);
                        }
                        else if(type.equalsIgnoreCase("text"))
                        {
                            item.setSensorIcon(R.mipmap.image_gateway);
                            item.setType(SensorItem.BUTTON);
                            item.setSensorTextOrNumber(str_RawData[0]);
                        }
                        else
                        {
                            item.setSensorIcon(R.mipmap.image_sensor);
                            item.setType(SensorItem.BUTTON);
                        }

                        /*
                            Log.e("SENSOR => " , "sensor " + i);
                            Log.e("SENSOR TYPE => ",sensors[i].getType());
                            Log.e("SENSOR Name => ", sensors[i].getName());
                            Log.e("SENSOR Value => ", str_RawData[0]);
                            Log.e("SENSOR", "-------------------------------");
                        */

                        list_SensorItem.add(item);

                        //Update the progress bar
                        progress = Math.round(i/max) * 100;
                        publishProgress(progress);
                    }

                    mqtt.start();
                }

            }
            catch (IOException o)
            {
                o.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //Cancel progressDailog when it finished
            progressBar.dismiss();

            //ensure the devices data capture is finished so that we can send data to adapter
            adapter = new SensorAdapter(SensorFragment.this , list_SensorItem, context, device_id, sensors);

            recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));

            recyclerView.setAdapter(adapter);
            //adapter.setItemClickCallBack(SensorFragment.this);
        }
    }
}
