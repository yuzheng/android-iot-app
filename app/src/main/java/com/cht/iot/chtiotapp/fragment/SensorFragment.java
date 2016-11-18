package com.cht.iot.chtiotapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cht.iot.chtiotapp.R;
import com.cht.iot.chtiotapp.activity.MainActivity;
import com.cht.iot.chtiotapp.other.DividerItemDecoration;
import com.cht.iot.chtiotapp.other.RESTful;
import com.cht.iot.chtiotapp.other.SensorAdapter;
import com.cht.iot.chtiotapp.other.SensorItem;
import com.cht.iot.persistence.entity.api.ISensor;
import com.cht.iot.persistence.entity.data.Rawdata;
import com.cht.iot.service.api.OpenRESTfulClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.cht.iot.chtiotapp.fragment.DevicesFragment.DEVICE_DESC;
import static com.cht.iot.chtiotapp.fragment.DevicesFragment.DEVICE_ID;
import static com.cht.iot.chtiotapp.fragment.DevicesFragment.DEVICE_NAME;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SensorFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SensorFragment#newInstance} factory method to
 * create an instance of this fragment.
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

    private String device_name;
    private String device_desc;
    private String device_id;

    private String ApiKey;
    private ISensor[] sensors;

    //create a instance of RecycleView
    private RecyclerView recyclerView;
    private SensorAdapter adapter;
    private View view;
    private Context context;

    public static int POST_ITEM = 0;

    private List<SensorItem> listData;

    private OnFragmentInteractionListener mListener;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        listData = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Get the device information to set SensorFragment up
        device_name = getArguments().getString(DEVICE_NAME);
        device_desc = getArguments().getString(DEVICE_DESC);
        device_id = getArguments().getString(DEVICE_ID);

        //Log.e("DEVICES information", device_name + ",  " + device_desc + ", " + device_id);

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sensor, container, false);
        context = view.getContext();

        //start the GetSensorInfoTask to trigger RESTful connection to get the all sensors information to show on RecycleView
        new SensorFragment.GetSensorsInfoTask().execute();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView tv_Name = (TextView) getView().findViewById(R.id.tv_Name);
        TextView tv_Desc = (TextView) getView().findViewById(R.id.tv_Desc);

        tv_Name.setText(device_name);
        tv_Desc.setText(device_desc);
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
        super.onDetach();
        mListener = null;
    }


    // SensorAdapter中取得SensorFragment的實例，並呼叫startActivityForResult
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("SensorFragment", requestCode + ", " + resultCode);

        if(requestCode == SensorAdapter.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
        {
            Log.e("SensorFragment ", "onActivityResult");

            Bitmap mImageBitmap;

            try {
                //取得內建相機所拍的檔案 礙於原尺寸高解析度 使上下載速度變慢
                mImageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(SensorAdapter.PHOTO_PATH));

                // 方法(一) 簡單的Bitmap
                //Bundle extras = data.getExtras();
                //mImageBitmap = (Bitmap) extras.get("data");

                //複雜的檔案轉換
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                mImageBitmap.compress((Bitmap.CompressFormat.PNG), 0, bos);
                byte[] bytes = bos.toByteArray();
                SensorAdapter.IMAGE_BODY = new ByteArrayInputStream(bytes);

                //傳送圖片資料
                adapter.new SendInfoTask(POST_ITEM).execute();

                Log.e("BITMAP", "PHOTO OK");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

            OpenRESTfulClient client = new OpenRESTfulClient(RESTful.HOST, RESTful.PORT, ApiKey);

            try {

                sensors = client.getSensors(device_id);

                int length = sensors.length;
                float max = (float)length;
                int progress = 0;

                if(length != 0)
                {
                    for(int i = 0; i<length; i++) {

                        SensorItem item = new SensorItem();

                        // Give device_id & sensor_id to get the Rawdata
                        Rawdata rawdata = client.getRawdata(device_id, sensors[i].getId());

                        // Convert rawdata to String Array to use
                        String[] str_RawData = rawdata.getValue();

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

                        listData.add(item);

                        //Update the progress bar
                        progress = Math.round(i/max) * 100;
                        publishProgress(progress);
                    }
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
            adapter = new SensorAdapter(SensorFragment.this ,listData, context, device_id, sensors);

            recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));

            recyclerView.setAdapter(adapter);
            //adapter.setItemClickCallBack(SensorFragment.this);
        }

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


}
