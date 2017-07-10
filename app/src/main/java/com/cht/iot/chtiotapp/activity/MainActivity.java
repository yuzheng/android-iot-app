package com.cht.iot.chtiotapp.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cht.iot.chtiotapp.R;
import com.cht.iot.chtiotapp.fragment.DevicesFragment;
import com.cht.iot.chtiotapp.fragment.HomeFragment;
import com.cht.iot.chtiotapp.fragment.RegistryFragment;
import com.cht.iot.chtiotapp.fragment.SensorFragment;
import com.cht.iot.chtiotapp.fragment.SettingsFragment;
import com.cht.iot.chtiotapp.other.CircleTransform;
import com.cht.iot.chtiotapp.other.DeviceAdapter;
import com.cht.iot.chtiotapp.other.DeviceItem;
import com.cht.iot.chtiotapp.other.DividerItemDecoration;
import com.cht.iot.chtiotapp.other.RESTful;
import com.cht.iot.chtiotapp.other.SensorAdapter;
import com.cht.iot.persistence.entity.api.IDevice;
import com.cht.iot.service.api.OpenRESTfulClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

// Android 6 need
import android.support.v4.app.ActivityCompat;
import static android.Manifest.permission.*;

//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;

public class MainActivity extends AppCompatActivity implements SensorFragment.OnFragmentInteractionListener, DevicesFragment.OnFragmentInteractionListener{

    private String APP_TAG;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgNavHeaderBg, imgProfile;
    private TextView txtName, txtWebsite;
    private Toolbar toolbar;
    //private FloatingActionButton fab;

    // urls to load navigation header background image
    // and profile image
    private static final String urlNavHeaderBg = "http://api.androidhive.info/images/nav-menu-header-bg.jpg";
    private static final String urlProfileImg = "https://iot.epa.gov.tw/iot/resources/iot/img/logo.png";

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    public static final String TAG_HOME = "home";
    public static final String TAG_DEVICES = "photos";
    public static final String TAG_REGISTRY = "movies";
    public static final String TAG_SENSOR = "notifications";
    public static final String TAG_SETTINGS = "settings";


    public static String CURRENT_TAG = TAG_HOME;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;

    private static String apiKey = "";
    private boolean needRefresh = false;

    private SharedPreferences prefs;

    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        context = this;

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        //fab = (FloatingActionButton) findViewById(R.id.fab);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        txtWebsite = (TextView) navHeader.findViewById(R.id.website);
        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        /*
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        prefs = getSharedPreferences("iot.setting", MODE_PRIVATE);
        apiKey = prefs.getString("key","");
        if(apiKey.isEmpty()) {
            Log.v(APP_TAG, "apikey is empty!");
            // load config file
            try {
                JSONObject obj = new JSONObject(loadJSONConfig());
                apiKey = obj.getString("apiKey");
                Log.v(APP_TAG, "config json : apiKey :"+apiKey);
            } catch (JSONException e) {
                Log.e(APP_TAG,"json error!");
            }
        }


        // load nav menu header data
        loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadChosenFragment();
        }



    }



    private String loadJSONConfig(){
        String json = null;

        try {
            InputStream is = getResources().openRawResource(R.raw.app);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            return null;
        }
        return json;
    }

    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, website, notifications action view (dot)
     */
    private void loadNavHeader() {
        // name, website
        txtName.setText("CHT IoT Android APP");
        txtWebsite.setText("iot.cht.com.tw");

        // loading header background image
        Glide.with(this).load(urlNavHeaderBg)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgNavHeaderBg);

        // Loading profile image
        Glide.with(this).load(urlProfileImg)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfile);

        // showing dot next to notifications label
        navigationView.getMenu().getItem(3).setActionView(R.layout.menu_dot);
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadChosenFragment() {

        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        Log.v("IoTApp","current_tag:"+CURRENT_TAG+", ("+getSupportFragmentManager().findFragmentByTag(CURRENT_TAG)+")");
        /*
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null && !needRefresh) {
            drawer.closeDrawers();
            Log.v("IoTApp","don't do anything");
            // show or hide the fab button
            //toggleFab();
            return;
        }
    */
        // set refresh false
        if(needRefresh) needRefresh = false;
        Log.v("IoTApp","ccc");
        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getChosenFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                ft.addToBackStack(null);
                ft.replace(R.id.frame, fragment, CURRENT_TAG);
                ft.commit();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        // show or hide the fab button
        //toggleFab();

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }


    // get the Fragment which is chosen by user
    // if not Fragment is chosen, then return home fragment to root container (app_bar_main.xml => R.id.frame)
    private Fragment getChosenFragment() {
        Log.v("iotapp","getChosenFragment:"+navItemIndex);
        switch (navItemIndex) {
            case 0:
                if(!apiKey.isEmpty()) {
                    // home or setting
                    HomeFragment homeFragment = new HomeFragment();
                    return homeFragment;
                }else {
                    // settings fragment
                    SettingsFragment settingsFragment = new SettingsFragment();
                    return settingsFragment;
                }
            case 1:
                // devices fragment
                DevicesFragment devicesFragment = new DevicesFragment();
                return devicesFragment;
            case 2:
                // registry fragment
                RegistryFragment registryFragment = new RegistryFragment();
                return registryFragment;
            default:
                return new HomeFragment();
        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_home:
                        Log.v("IoTApp","selected home");
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_photos:
                        Log.v("IoTApp","selected photos");
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_DEVICES;
                        break;
                    case R.id.nav_movies:
                        Log.v("IoTApp","selected movies");
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_REGISTRY;
                        break;
                    case R.id.nav_about_us:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, AboutUsActivity.class));
                        drawer.closeDrawers();
                        return true;
    /*
                    case R.id.nav_privacy_policy:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, PrivacyPolicyActivity.class));
                        drawer.closeDrawers();
                        return true;
    */
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadChosenFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {

        FragmentManager fm = this.getSupportFragmentManager();
        Fragment currentFrag =  fm.findFragmentById(R.id.frame);
        Log.v("iotapp",currentFrag.getClass().getSimpleName());

        Log.v("iotapp", "BACK :"+fm.getBackStackEntryCount());

        if(fm.getBackStackEntryCount() == 0) {
            Log.v("iotapp", "finish app");
            this.finish();
            //Log.d("STACK", "MainActivity => STACK ZERO COUNT");
        }
        else
        {
            //Log.v("iotapp", "pop back");
            //fm.popBackStack();
            if(currentFrag.getClass().equals(HomeFragment.class) || currentFrag.getClass().equals(SettingsFragment.class)){
                Log.v("iotapp", "finish app");
                this.finish();
            }else if(currentFrag.getClass().equals(DevicesFragment.class) || currentFrag.getClass().equals(RegistryFragment.class)){
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadChosenFragment();
                return;
            }

            //Log.d("STACK", "MainActivity => Fragment popBackStack");
        }

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            /*
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadChosenFragment();
                return;
            }
            */
        }

        super.onBackPressed();
    }

    public void setNavItemIndex(int index){
        navItemIndex = index;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // show menu only when home fragment is selected
        if (navItemIndex == 0 && !apiKey.isEmpty()) {
            getMenuInflater().inflate(R.menu.main, menu);
        }

        // when fragment is notifications, load the menu created for notifications
        if (navItemIndex == 3) {
            getMenuInflater().inflate(R.menu.notifications, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_remove_key) {
            apiKey = "";
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("key");
            editor.commit();

            // reload fragment
            needRefresh = true;
            loadChosenFragment();

            Toast.makeText(getApplicationContext(), "remove api key!", Toast.LENGTH_LONG).show();
            return true;
        }

        // user is in notifications fragment
        // and selected 'Mark all as Read'
        if (id == R.id.action_mark_all_read) {
            Toast.makeText(getApplicationContext(), "All notifications marked as read!", Toast.LENGTH_LONG).show();
        }

        // user is in notifications fragment
        // and selected 'Clear All'
        if (id == R.id.action_clear_notifications) {
            Toast.makeText(getApplicationContext(), "Clear all notifications!", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.e("onActivityResult", requestCode + ", " + resultCode );

        if(requestCode == resultCode) {
            if(requestCode == 1394) {
                apiKey = data.getStringExtra("apikey");

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("key", apiKey);
                //editor.apply();
                editor.commit();
            }else if(requestCode == 1943) {

                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadChosenFragment();

                Toast.makeText(getApplicationContext(), data.getStringExtra("registry"), Toast.LENGTH_LONG).show();
                String[] registryData = data.getStringExtra("registry").split(";");
                String sn = null;
                String digest = null;
                if(registryData.length == 2){
                    if(registryData[0].startsWith("sn:")){
                        sn = registryData[0].replace("sn:","");
                    }

                    if(registryData[1].startsWith("digest:")){
                        digest = registryData[1].replace("digest:","");
                    }
                }

                if(sn!=null && digest != null){
                    new RegistryDeviceTask().execute(sn, digest);
                }

            }
        }

        if(requestCode == SensorAdapter.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
        {
            Log.e("MainActivity ", "onActivityResult");

            if(data == null)
            {
                Log.e("FUK", "FUK");
            }


            /*
            Uri uri = data.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
            Log.e("Intent ", uri.toString());
            Log.e("MainActivity ", "onActivityResult");

            Bundle extras = new Bundle();
            extras.putString(DEVICE_NAME, selected_Device_Name);
            extras.putString(DEVICE_DESC, selected_Device_Desc);
            extras.putString(DEVICE_ID, selected_Device_ID);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

            SensorFragment sf = new SensorFragment();
            sf.setArguments(extras);

            ft.replace(R.id.frame,  sf, MainActivity.TAG_DEVICES);
            ft.addToBackStack(null);
            ft.commit();
            */
        }


        needRefresh = true;

        loadChosenFragment();

        super.onActivityResult(requestCode,resultCode, data);
    }

    /*
    // show or hide the fab
    private void toggleFab() {
        if (navItemIndex == 0)
            fab.show();
        else
            fab.hide();
    }
    */

    //Let other class can access apiKey to get more information
    public static String getApiKey()
    {
        return apiKey;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

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

    private class RegistryDeviceTask extends AsyncTask<String, Integer, String>
    {
        // 獲得Device資訊可能需要一些時間，故用ProgressDialog進度條來告知使用者目前載入狀況
        private ProgressDialog progressBar;

        @Override
        // Task事前準備工作
        protected void onPreExecute() {
            super.onPreExecute();

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


            String sn = params[0];
            String digest = params[1];

            Log.i("iotapp", "RegistryDeviceTask doInBackground:"+sn+","+digest);
            // OpenRESTfulClient物件
            OpenRESTfulClient client = new OpenRESTfulClient(RESTful.HOST, RESTful.PORT, getApiKey());


            try {
                client.reconfigure(sn, digest);
                /*
                {
                  "op": "Reconfigure",
                  "ck": "DKXWPBWWXBAAUE7A3Z",
                  "deviceId": "2207430052"
                }

                 */

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

            Toast.makeText(getApplicationContext(), "完成設備納管，請至我的設備查看！", Toast.LENGTH_LONG).show();

        }
    }



}
