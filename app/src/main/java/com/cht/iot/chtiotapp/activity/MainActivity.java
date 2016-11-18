package com.cht.iot.chtiotapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.cht.iot.chtiotapp.other.SensorAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;

public class MainActivity extends AppCompatActivity implements SensorFragment.OnFragmentInteractionListener{

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
    private static final String urlProfileImg = "https://iot.cht.com.tw/iot/resources/iot/img/logo.png";

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

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
        txtName.setText("Cht IoT");
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
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null && !needRefresh) {
            drawer.closeDrawers();
            Log.v("IoTApp","don't do anything");
            // show or hide the fab button
            //toggleFab();
            return;
        }

        // set refresh false
        if(needRefresh) needRefresh = false;

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
                    case R.id.nav_privacy_policy:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, PrivacyPolicyActivity.class));
                        drawer.closeDrawers();
                        return true;
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

        Log.e("BACK", "BACK BACK");

        if(fm.getBackStackEntryCount() == 0)
        {
            //this.finish();
            //Log.d("STACK", "MainActivity => STACK ZERO COUNT");
        }
        else
        {
            //fm.popBackStack();
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
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadChosenFragment();
                return;
            }
        }

        super.onBackPressed();
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

        //Log.e("onActivityResult", requestCode + ", " + resultCode );

        if(requestCode == resultCode) {
            apiKey = data.getStringExtra("apikey");

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("key",apiKey);
            //editor.apply();
            editor.commit();
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
            extras.putString(DEVICE_NAME, now_Device_Name);
            extras.putString(DEVICE_DESC, now_Device_Desc);
            extras.putString(DEVICE_ID, now_Device_ID);

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


}
