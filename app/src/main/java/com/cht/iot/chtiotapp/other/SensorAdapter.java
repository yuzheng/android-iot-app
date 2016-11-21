package com.cht.iot.chtiotapp.other;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cht.iot.chtiotapp.R;
import com.cht.iot.chtiotapp.activity.MainActivity;
import com.cht.iot.chtiotapp.fragment.SensorFragment;
import com.cht.iot.persistence.entity.api.ISensor;
import com.cht.iot.persistence.entity.data.Rawdata;
import com.cht.iot.service.api.OpenRESTfulClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.cht.iot.service.api.OpenRESTfulClient.now;

/**
 * Created by Arron on 2016/11/2.
 */

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorHolder> {

    private List<SensorItem> sensorList;
    private LayoutInflater inflater;
    private Context context;

    private String device_id = "";
    private ISensor[] sensors;

    Fragment fragment;

    public static String PHOTO_PATH;
    public static final int REQUEST_TAKE_PHOTO = 1;

    // 照片的檔案名稱
    private static String IMAGE_NAME = "my_Picture";
    private static final String IMAGE_TYPE = "image/jpg";
    public static InputStream IMAGE_BODY = null;

    public SensorAdapter(Fragment fragment,List<SensorItem> sensorList, Context c, String device_id, ISensor[] sensors) {
        this.fragment = fragment;
        this.context = c;
        this.sensorList = sensorList;
        inflater = LayoutInflater.from(c);
        this.device_id = device_id;
        this.sensors = sensors;
    }

    @Override
    public SensorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_sensor, parent, false);
        return new SensorHolder(view);
    }

    @Override
    public void onBindViewHolder(SensorHolder holder, int position) {

        final int now_Position = holder.getAdapterPosition();

        int dp_Scale = (int) context.getResources().getDisplayMetrics().density;

        LinearLayout.LayoutParams visible_Params = new LinearLayout.LayoutParams(55 * dp_Scale, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams un_Visible_Params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        visible_Params.setMargins(0, 0, 20 * dp_Scale, 0);
        visible_Params.setMargins(0, 0, 20 * dp_Scale, 0);

        SensorItem item = sensorList.get(position);
        holder.tv_SensorName.setText(item.getSensorName());
        holder.img_SensorIcon.setImageResource(item.getSensorIcon());

        // three types: 1.TOGGLEBTN 2.IMAGEBTN 3.BUTTON
        String my_Type = item.getType();

        if (my_Type.equalsIgnoreCase(SensorItem.BUTTON)) {

            holder.btn_Iuput.setText(item.getSensorTextOrNumber());
            holder.btn_Iuput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder
                            (new ContextThemeWrapper(context, R.style.AlertDialogCustom));
                    builder.setTitle("請輸入數值");

                    // Set up the input
                    final EditText input = new EditText(context);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String new_Data = input.getText().toString();

                            //Using notifyDataSetChanged() method to update dataset
                            sensorList.get(now_Position).setSensorTextOrNumber(new_Data);

                            handler.post(r);

                            //notifyDataSetChanged();

                            new SendInfoTask(now_Position, new_Data).execute();

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
            });

            //Modify the width to make it looks like dynamic view showing
            holder.btn_Iuput.setLayoutParams(visible_Params);
            holder.img_SensorStore.setLayoutParams(un_Visible_Params);
            holder.tgbtn_SensorStore.setLayoutParams(un_Visible_Params);

        } else if (my_Type.equalsIgnoreCase(SensorItem.IMAGEBTN)) {

            holder.img_SensorStore.setImageBitmap(item.getSensorImageBtn());
            holder.img_SensorStore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 方法(一) 複雜拍照 呼叫startActivityForResult尋找可處理拍照意圖的Activity
                    // SensorFragment.POST_ITEM = now_Position;
                    // dispatchTakePictureIntent();

                    // 方法(二) 簡易拍照 並回傳Bitmap於onActivityResult的extra預設key=data
                     SensorFragment.POST_ITEM = now_Position;
                     Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                     fragment.startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                }
            });

            //Modify the width to make it looks like dynamic view showing
            holder.btn_Iuput.setLayoutParams(un_Visible_Params);
            holder.img_SensorStore.setLayoutParams(visible_Params);
            holder.tgbtn_SensorStore.setLayoutParams(un_Visible_Params);

        } else if (my_Type.equalsIgnoreCase(SensorItem.TOGGLEBTN)) {

            holder.tgbtn_SensorStore.setChecked(item.isSensorToggleBtn());
            holder.tgbtn_SensorStore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Log.e("onChanged", now_Position + ", " + isChecked);

                    String toggle_Value = "";
                    boolean bool_Value = false;

                    if (isChecked) {
                        toggle_Value = "1";
                        bool_Value = true;
                    } else {
                        toggle_Value = "0";
                        bool_Value = false;
                    }

                    //Using notifyDataSetChanged() method to update dataset
                    sensorList.get(now_Position).setSensorToggleBtn(bool_Value);
                    //notifyDataSetChanged();

                    handler.post(r);

                    new SendInfoTask(now_Position, toggle_Value).execute();
                }
            });

            //Modify the width to make it looks like dynamic view showing
            holder.btn_Iuput.setLayoutParams(un_Visible_Params);
            holder.img_SensorStore.setLayoutParams(un_Visible_Params);
            holder.tgbtn_SensorStore.setLayoutParams(visible_Params);

        } else {
            Log.e("SensorAdapter.java", "sensor my_Type error!");
        }

    }

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
            img_SensorStore = (ImageView) itemView.findViewById(R.id.imgbtn_SensorStore);
            tgbtn_SensorStore = (ToggleButton) itemView.findViewById(R.id.tgbtn_SensorStore);
            btn_Iuput = (Button) itemView.findViewById(R.id.btn_InputTextOrNum);
            container = itemView.findViewById(R.id.list_Sensor);
        }
    }

    Handler handler = new Handler();
    Runnable r = new Runnable() {
        public void run() {
            notifyDataSetChanged();
        }
    };

    // 此任務之目的:更新IoT平台上的資料
    public class SendInfoTask extends AsyncTask<Integer, Integer, String> {

        String ApiKey = "";

        // 在Sensor列表中，被使用者選到的那一個Sensor
        int device_Pos;

        // 此為欲更新至IoT平台的資料，配合RawData定義好的Value為String Array
        String[] update_Data = new String[1];

        public SendInfoTask(int position, String data) {
            device_Pos = position;
            update_Data[0] = data;
        }

        public SendInfoTask(int position)
        {
            device_Pos = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Get the ApiKey First
            ApiKey = MainActivity.getApiKey();
        }

        @Override
        protected String doInBackground(Integer... params) {

            OpenRESTfulClient client = new OpenRESTfulClient(RESTful.HOST, RESTful.PORT, ApiKey);

            ISensor sensor = sensors[device_Pos];
            String sensor_ID = sensors[device_Pos].getId();

            String type = sensor.getType();

            try {

                /*
                    IoT平台設計上所有值都存在Rawdata這個資料結構之中
                    (1). switch => 0 or 1
                    (2). gauge/counter/text => 數值
                    (3). snapshot =>
                */

                Rawdata rawdata = client.getRawdata(device_id, sensor_ID);

                String time = now();
                Float lat = rawdata.getLat();
                Float lon = rawdata.getLon();

                /*  秀出設備的相關資訊
                    Log.e("SENSOR => ", "sensor " + device_Pos);
                    Log.e("SENSOR TYPE => ", type);
                    Log.e("SENSOR Name => ", sensors[device_Pos].getName());
                    Log.e("SENSOR Value => ", update_Data[0]);
                    Log.e("SENSOR", "-------------------------------");
                */

                if (type.equalsIgnoreCase("snapshot")) {
                    //上傳照片至IoT平台
                    client.saveSnapshot(device_id, sensor_ID, time, lat, lon, update_Data, IMAGE_NAME, IMAGE_TYPE, IMAGE_BODY);
                } else if (type.equalsIgnoreCase("gauge") ||
                        type.equalsIgnoreCase("counter") ||
                        type.equalsIgnoreCase("text") ||
                        type.equalsIgnoreCase("switch")) {
                    client.saveRawdata(device_id, sensor_ID, time, lat, lon, update_Data);
                } else {
                    // It can't be happened
                    Log.e("Sensor Adapter DoInBg", "No type Matches!!!!");
                }
            } catch (IOException o) {
                o.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }

    //方法(一) 複雜拍照(高解析度) 定義照片存放路徑
    private File createImageFile() throws IOException {

        // 照片 欲存放之目錄路徑
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // 秀出路徑
        // 以Note3來說 /storage/emulated/0/Android/data/com.cht.iot.chtiotapp/files/Pictures
        // Log.e("DIREC", storageDir.getAbsolutePath());


        File image = File.createTempFile(
                IMAGE_NAME,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        PHOTO_PATH = "file:" + image.getAbsolutePath();
        return image;
    }

    //方法(一) 複雜拍照(高解析度) 開啟Activity來處理拍照意圖
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 確保目前有可以處理拍照意圖的Activity
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {

            // 建立存放照片的 [目錄位置]
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("File Create Error", " createImageFile() Fail , 照片產生失敗");
            }

            if (photoFile != null) {

                Uri photoURI = FileProvider.getUriForFile(context,
                        "com.example.android.fileprovider",
                        photoFile);

                // 拍照並儲存至指定的路徑
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // 提高到Activity層級去執行startActivityForResult
                // ((Activity)context).startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

                // 用當前的Fragment層級去執行startActivityForResult
                fragment.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
}
