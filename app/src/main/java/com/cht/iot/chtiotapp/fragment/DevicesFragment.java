package com.cht.iot.chtiotapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cht.iot.chtiotapp.R;
import com.cht.iot.chtiotapp.activity.MainActivity;
import com.cht.iot.chtiotapp.other.DeviceAdapter;
import com.cht.iot.chtiotapp.other.DeviceItem;
import com.cht.iot.chtiotapp.other.DividerItemDecoration;
import com.cht.iot.chtiotapp.other.RESTful;
import com.cht.iot.persistence.entity.api.IDevice;
import com.cht.iot.service.api.OpenRESTfulClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DevicesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DevicesFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 *  DevicesFragment 是用來呈現 Device 的清單列表
 *
 */
public class DevicesFragment extends Fragment implements DeviceAdapter.ItemClickCallBack{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PhotosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DevicesFragment newInstance(String param1, String param2) {
        DevicesFragment fragment = new DevicesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private OnFragmentInteractionListener mListener;
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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

    /**
     *   以上Code都為Fragment預設或自動所產生...
     */

    public DevicesFragment() {
        // Required empty public constructor
    }

    // 利用setArgument方法可以傳遞Bundle物件(key, value)，以下靜態字串變數即Key的角色
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_DESC = "DEVICE_DESC";
    public static final String DEVICE_ID = "DEVICE_ID";

    // DeviceFragment上會列出許多Device，被使用者點擊所選中的Device資訊，使用以下變數作紀錄
    public static String selected_Device_Name;
    public static String selected_Device_Desc;
    public static String selected_Device_ID;

    // 對IoT平台進行連線請求，會需要前一個畫面(MainActivity)登入成功後的API Key
    private String ApiKey;

    private RecyclerView recyclerView;      // 官方推薦使用的高效能清單View(取代以往的ListView)
    private DeviceAdapter adapter;          // 轉接器，負責將資料配置給recycleView
    private View view;                      // 用來代表RecycleView上的單一列
    private Context context;                // 獲取MainActivity的Context參照，以利許多Function呼叫

    private List<DeviceItem> list_DeviceItem;   //List of DeviceItem用來存放從IoT平台網頁上該Project底下的所有Device資訊

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // 初始化 List of DeviceItem
        list_DeviceItem = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 將fragment_devices的xml面版Layout，轉成View類別，且當成子物件黏至container上
        view = inflater.inflate(R.layout.fragment_devices, container, false);

        // 獲取MainActivity的Context參照，以利許多Function呼叫
        context = view.getContext();

        // 執行GetDevicesInfoTask異步任務建立App與IoT平台之RESTful連線
        // 並獲取Project底下所有的Device資訊，再呈現於RecycleView上
        new GetDevicesInfoTask().execute();
        return view;
    }

    @Override
    // 從當前的DevicesFragment呼叫選中的Device，並傳相關資訊給SensorFragment
    public void onItemClick(int p) {

        // 宣告一個DeviceItem物件，對應list_DeviceItem中被點擊到的DeviceItem，並獲取設備相關資訊(名稱、描述、ID)
        DeviceItem item = list_DeviceItem.get(p);
        selected_Device_Name = item.getDeviceName();
        selected_Device_Desc = item.getDeviceDesc();
        selected_Device_ID = item.getDeviceId();

        // 使用Bundle物件來儲存設備相關資訊(KEY, VALUE)
        Bundle extras = new Bundle();
        extras.putString(DEVICE_NAME, selected_Device_Name);
        extras.putString(DEVICE_DESC, selected_Device_Desc);
        extras.putString(DEVICE_ID, selected_Device_ID);

        // FragmentManager提供Fragment的交易處理機制來進行(CRUD: Create,Remove,Update,Delete)
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        // 為Fragment加上客製化的動畫
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        // 新增並實例化待呼叫的SensorFragment
        SensorFragment sf = new SensorFragment();
        sf.setArguments(extras);

        // replace方法之參數說明
        // 1.欲置換內容的容器Container名稱
        // 2.新Fragment物件,
        // 3.指定標籤別名，方便以後使用findFragmentByTag找出該Fragment物件
        ft.replace(R.id.frame,  sf, MainActivity.TAG_DEVICES);

        // Fragment不像Activity自動會記錄狀態，需自行呼叫addToBackStack紀錄，
        // 以便日後[返回鍵]or[popBackStack()]回朔狀態
        ft.addToBackStack(null);
        ft.commit();

    }

    /**
     *  AsyncTask特色
     *       此類別可適當並簡易的操作UI thread，主要提供背景執行作業(non-UI thread)來完成某任務，
     *       且可以將結果返回至UI thread，而不用操作麻煩的thread或是handler技巧
     *
     *  PS:
     *      如果您預期要執行的工作能在幾秒內完成，就可以選擇使用 AsyncTask，
     *      若執行的時間很長，Android則強烈建議採用: Executor, ThreadPoolExecutor & FutureTask。
     *
     *      [參數說明]
     *      1. Params -- 要執行doInBackground() 時傳入的參數，數量可以不止一個
     *      2. Progress -- doInBackground() 執行過程中回傳給 onProgressUpdate()，數量可以不止一個
     *      3. Result -- 傳回執行結果 onPostExecute()，若您沒有參數要傳入，則填入 Void (注意 V 為大寫)
     *
     *      GetDevicesInfoTask之目的即為:
     *      向IoT平台發出RESTful連線，並要求取得專案下所有Device資訊，呈現於RecycleView之上(可視為新型ListView)
     */

    private class GetDevicesInfoTask extends AsyncTask<String, Integer, String>
    {
        // 獲得Device資訊可能需要一些時間，故用ProgressDialog進度條來告知使用者目前載入狀況
        private ProgressDialog progressBar;

        @Override
        // Task事前準備工作
        protected void onPreExecute() {
            super.onPreExecute();

            // IoT平台進行連線請求，會需要前一個畫面(MainActivity)登入成功後的API Key
            ApiKey = MainActivity.getApiKey();

            // 初始化RecycleView，並配置一個LayoutManager
            recyclerView = (RecyclerView) view.findViewById(R.id.recycleview);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            // 設定ProgressDialog
            progressBar = new ProgressDialog(context);
            progressBar.setMessage("讀取中...");
            progressBar.setCancelable(false);
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.show();
        }

        @Override
        // 開始背景執行某項較費時的任務(non-UI Thread)
        protected String doInBackground(String... params) {

            // OpenRESTfulClient物件
            OpenRESTfulClient client = new OpenRESTfulClient(RESTful.HOST, RESTful.PORT, ApiKey);
            // 先清空現有資料，以免於SensorFragment按下返回鍵時，造成item重複新增於list之上
            list_DeviceItem.clear();

            try {
                IDevice[] devices = client.getDevices();
                int length = devices.length;
                float max = (float)length;
                int progress = 0;

                if(length != 0)
                {
                    for(int i = 0; i<length; i++) {
                        // 產生一個新的DeviceItem物件，並根據RESTful API取得的資訊，更新至DeviceItem
                        DeviceItem item = new DeviceItem();
                        item.setDeviceName(devices[i].getName());
                        item.setDeviceDesc(devices[i].getDesc());
                        item.setDeviceId(devices[i].getId());
                        item.setImgSource(R.mipmap.image_gateway);

                        // 看IoT網頁上此Project有幾個Device，每次抓取一筆後，就新增至list of DeviceItem
                        list_DeviceItem.add(item);

                        // 更新進度條
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

        // 用來顯示進度
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        // 執行完的結果，第三個參數 [Result] 會被傳入此方法，此處為UI-Thread(代表可以操作recycleView...)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // 進度已完成，故取消ProgressBar
            progressBar.dismiss();

            // 美化每個Item，為其加上分隔線
            recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));

            // [重要]必須確保設備資訊的抓取已完成之後，才能將list_DeviceItem丟至Adapter
            adapter = new DeviceAdapter(list_DeviceItem, context);
            recyclerView.setAdapter(adapter);

            // 為每個Item加上ClickCallBack，處理按下後該執行的動作
            adapter.setItemClickCallBack(DevicesFragment.this);
        }
    }

}
