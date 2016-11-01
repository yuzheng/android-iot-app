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
import com.cht.iot.chtiotapp.other.ListItem;
import com.cht.iot.chtiotapp.other.MyAdapter;
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
 */
public class DevicesFragment extends Fragment implements MyAdapter.ItemClickCallBack{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_DESC = "DEVICE_DESC";
    public static final String IMG_SOURCE = "IMG_SOURCE";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String ApiKey;
    private String host = "iot.cht.com.tw";
    private int port_RESTful = 80;

    private OnFragmentInteractionListener mListener;

    //create a instance of RecycleView
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private View view;
    private Context context;

    private List<ListItem> listData;

    public DevicesFragment() {
        // Required empty public constructor
    }

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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_devices, container, false);
        context = view.getContext();

        //start the GetDevicesInfoTask to trigger RESTful connection to get the all device information to show on RecycleView
        new GetDevicesInfoTask().execute();

        return view;
    }

    @Override
    // call the SensorFragment from DeviceFragment
    public void onItemClick(int p) {

        ListItem item = listData.get(p);
        Bundle extras = new Bundle();
        extras.putString(DEVICE_NAME, item.getDeviceName());
        extras.putString(DEVICE_DESC, item.getDeviceDesc());
        extras.putInt(IMG_SOURCE, item.getImgSource());

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        SensorFragment sf = new SensorFragment();
        sf.setArguments(extras);

        fragmentTransaction.replace(R.id.frame,  sf, MainActivity.TAG_DEVICES);
        fragmentTransaction.commitAllowingStateLoss();

    }

    /*  AsyncTask enables proper and easy use of the UI thread.
        This class allows you to perform background operations and publish results
        on the UI thread without having to manipulate threads and/or handlers.
    */
    private class GetDevicesInfoTask extends AsyncTask<String, Integer, String>
    {
        //ProgressDialog instance
        private ProgressDialog progressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Get the ApiKey First
            ApiKey = MainActivity.getApiKey();

            //setting RecycleView and give it an adapter
            recyclerView = (RecyclerView) view.findViewById(R.id.recycleview);
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

            OpenRESTfulClient client = new OpenRESTfulClient(host, port_RESTful, ApiKey);

            try {
                IDevice[] devices = client.getDevices();
                int length = devices.length;
                float max = (float)length;
                int progress = 0;

                if(length != 0)
                {
                    for(int i = 0; i<length; i++) {
                        ListItem item = new ListItem();
                        item.setDeviceName(devices[i].getName());
                        item.setDeviceDesc(devices[i].getDesc());
                        item.setImgSource(R.drawable.image_gateway);
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
            adapter = new MyAdapter(listData, context);
            recyclerView.setAdapter(adapter);
            adapter.setItemClickCallBack(DevicesFragment.this);
        }

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
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
