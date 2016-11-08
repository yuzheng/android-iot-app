package com.cht.iot.chtiotapp.other;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cht.iot.chtiotapp.R;

import java.util.List;

/**
 * Created by Arron on 2016/11/2.
 */

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorHolder>{

    private List<SensorItem> sensorList;
    private LayoutInflater inflater;
    private Context context;
    private String type;

    public SensorAdapter(List<SensorItem> sensorList, Context c)
    {
        this.context = c;
        this.sensorList = sensorList;
        inflater = LayoutInflater.from(c);
    }

    @Override
    public SensorHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.list_sensor, parent, false);
        return new SensorHolder(view);
    }

    @Override
    public void onBindViewHolder(SensorHolder holder, int position) {

        int dp_Scale = (int)context.getResources().getDisplayMetrics().density;

        LinearLayout.LayoutParams visible_Params = new LinearLayout.LayoutParams(55 * dp_Scale, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams un_Visible_Params = new LinearLayout.LayoutParams(0 , LinearLayout.LayoutParams.WRAP_CONTENT);
        visible_Params.setMargins(0, 0, 20 * dp_Scale, 0);
        visible_Params.setMargins(0, 0, 20 * dp_Scale, 0);

        SensorItem item = sensorList.get(position);
        holder.tv_SensorName.setText(item.getSenserName());
        holder.img_SensorIcon.setImageResource(item.getSensorIcon());

        // three types: 1.TOGGLEBTN 2.IMAGEBTN 3.BUTTON
        type = item.getType();

        //Default count is five, after removing the unnecessary child view (at first) the count will be three.
        int count = ((ViewGroup)holder.tv_SensorName.getParent()).getChildCount();
        Log.e("SLENGTH and POSITION=> ", sensorList.size() + ", " + position + ", " + type + ", " + count);

        if (type.equalsIgnoreCase(SensorItem.BUTTON)) {

            holder.btn_Iuput.setText(item.getSensorTextOrNumber());

            //Modify the width to make it looks like dynamic view showing
            holder.btn_Iuput.setLayoutParams(visible_Params);
            holder.img_SensorStore.setLayoutParams(un_Visible_Params);
            holder.tgbtn_SensorStore.setLayoutParams(un_Visible_Params);

        }
        else if (type.equalsIgnoreCase(SensorItem.IMAGEBTN))
        {
            holder.img_SensorStore.setImageBitmap(item.getSensorImageBtn());

            //Modify the width to make it looks like dynamic view showing
            holder.btn_Iuput.setLayoutParams(un_Visible_Params);
            holder.img_SensorStore.setLayoutParams(visible_Params);
            holder.tgbtn_SensorStore.setLayoutParams(un_Visible_Params);

        }
        else if (type.equalsIgnoreCase(SensorItem.TOGGLEBTN))
        {
            holder.tgbtn_SensorStore.setChecked(item.isSensorToggleBtn());

            //Modify the width to make it looks like dynamic view showing
            holder.btn_Iuput.setLayoutParams(un_Visible_Params);
            holder.img_SensorStore.setLayoutParams(un_Visible_Params);
            holder.tgbtn_SensorStore.setLayoutParams(visible_Params);
        }
        else
        {
            Log.e("SensorAdapter.java", "sensor type error!");
        }

        holder.btn_Iuput.setOnClickListener(btn_InputListener);

    }

    View.OnClickListener btn_InputListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder
                    (new ContextThemeWrapper(context, R.style.AlertDialogCustom) );
            builder.setTitle("請輸入數值");

            // Set up the input
            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);


            // Set up the buttons
            builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    input.getText().toString();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

            /*  set button background color

                AlertDialog alert = builder.create();
                alert.show();

                Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setBackgroundColor(Color.MAGENTA);
                Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setBackgroundColor(Color.YELLOW);
            */
        }
    };


    @Override
    public int getItemCount() {
        return sensorList.size();
    }

    class SensorHolder extends RecyclerView.ViewHolder {

        private TextView tv_SensorName;
        private ImageView img_SensorIcon;
        private ToggleButton tgbtn_SensorStore;
        private ImageView img_SensorStore;
        private Button btn_Iuput;
        private View container;

        public SensorHolder(View itemView) {
            super(itemView);

            tv_SensorName = (TextView) itemView.findViewById(R.id.tv_sensorName);
            img_SensorIcon = (ImageView) itemView.findViewById(R.id.iv_small_Sensor);
            tgbtn_SensorStore = (ToggleButton) itemView.findViewById(R.id.tgbtn_SensorStore);
            img_SensorStore = (ImageView) itemView.findViewById(R.id.imgbtn_SensorStore);
            btn_Iuput = (Button) itemView.findViewById(R.id.btn_InputTextOrNum);
            container = itemView.findViewById(R.id.list_Sensor);
        }
    }

    /*
    private class SendInfoTask extends AsyncTask<String, Integer, String>
    {
        //ProgressDialog instance
        private ProgressDialog progressBar;

        String ApiKey = "";

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

                ISensor[] sensors = client.getSensors(device_id);

                int length = sensors.length;
                float max = (float)length;
                int progress = 0;

                if(length != 0)
                {
                    for(int i = 0; i<length; i++) {

                        SensorItem item = new SensorItem();

                        Rawdata rawdata = client.getRawdata(device_id, sensors[i].getId());

                        String[] str_RawData = rawdata.getValue();

                        if(str_RawData.length == 0)
                        {
                            str_RawData = new String[1];
                            str_RawData[0] = "NO RAWDATA IN THIS SENSOR!";
                        }

                        item.setSenserName(sensors[i].getName());

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


                        Log.e("SENSOR => " , "sensor " + i);
                        Log.e("SENSOR TYPE => ",sensors[i].getType());
                        Log.e("SENSOR Name => ", sensors[i].getName());
                        Log.e("SENSOR Value => ", str_RawData[0]);
                        Log.e("SENSOR", "-------------------------------");


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
            adapter = new SensorAdapter(listData, context);

            recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));

            recyclerView.setAdapter(adapter);
            //adapter.setItemClickCallBack(SensorFragment.this);
        }
    }
    */



}
