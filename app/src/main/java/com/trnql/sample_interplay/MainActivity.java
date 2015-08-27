package com.trnql.sample_interplay;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.trnql.smart.activity.ActivityEntry;
import com.trnql.smart.base.SmartCompatActivity;
import com.trnql.smart.location.AddressEntry;
import com.trnql.smart.location.LocationEntry;
import com.trnql.smart.weather.WeatherEntry;
import com.trnql.zen.core.AppData;
import com.trnql.zen.core.db.DbKVP;
import com.trnql.zen.core.localevent.LocalEventsListener;
import com.trnql.zen.core.localevent.LocalEventsManager;
import com.trnql.zen.core.observableprops.ObservablePropertyListener;
import com.trnql.zen.utlis.AndroidUtils;
import com.trnql.zen.utlis.IconPaths;

import java.util.Calendar;
import java.util.Date;


public class MainActivity extends SmartCompatActivity {

    static ActivityEntry lastActivity;
    static WeatherEntry lastWeather;
    static AddressEntry lastAddress;
    static LocationEntry lastLocation;

    private static final int SPEECH_REQUEST_CODE = 1234;

    private final double INVALID_LATLONG = -9999;

    private EditText et_inputText;
    private TextView tv_weatherRec;
    private TextView tv_sun;
    private TextView tv_locationCardText;
    private TextView tv_locationCardQuestionText;
    private Button btn_locationCardHome;
    private Button btn_locationCardWork;
    private Button btn_locationClear;
    private Toolbar toolbar;
    private ImageView img_weather;
    private ImageView img_input;
    private ImageView img_locationCardIcon;
    private ImageView backdrop;
    private ImageView img_sun;
    private FloatingActionButton fab;
    private CollapsingToolbarLayout collapsingToolbar;
    private Button btn_activityCardData;
    private Button btn_weatherCardData1;
    private Button btn_weatherCardData2;
    private Button btn_locationCardData;

    private String currentLocality;
    private double lat, lng;
    private int backdropWidth, backdropHeight;
    private String homeString;
    private String workString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppData.startAllServices(this);

        _initViews();

        _initFromDb();

        _initBackdropAsync();

        _addViewlisteners();

        _addResourceListeners();

    }

    private void _initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        et_inputText = (EditText) findViewById(R.id.et_inputText);
        tv_weatherRec = (TextView) findViewById(R.id.tv_weatherRec);
        tv_sun = (TextView) findViewById(R.id.tv_sun);
        img_weather = (ImageView) findViewById(R.id.img_weather);
        img_input = (ImageView) findViewById(R.id.img_input);
        img_sun = (ImageView) findViewById(R.id.img_sunrise);
        backdrop = (ImageView) findViewById(R.id.backdrop);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        btn_activityCardData = (Button) findViewById(R.id.btn_activity_dataset);
        btn_weatherCardData1 = (Button) findViewById(R.id.btn_weather_dataset);
        btn_weatherCardData2 = (Button) findViewById(R.id.btn_weather_dataset2);
        img_locationCardIcon = (ImageView) findViewById(R.id.img_location_card_icon);
        tv_locationCardText = (TextView) findViewById(R.id.tv_location_card_text);
        tv_locationCardQuestionText = (TextView) findViewById(R.id.location_card_question_text);
        btn_locationCardHome = (Button) findViewById(R.id.btn_home_selection);
        btn_locationCardWork = (Button) findViewById(R.id.btn_work_selection);
        btn_locationClear = (Button) findViewById(R.id.btn_clear_home_work_locations);
        btn_locationCardData = (Button) findViewById(R.id.btn_location_dataset);

        backdrop.setScaleType(ImageView.ScaleType.CENTER_CROP);
        setSupportActionBar(toolbar);
    }

    private void _initFromDb() {
        DbKVP db = getDBManager().getDB_KVP(R.id.db_kvp_locality);
        currentLocality = db.get("locality");
        String latString = db.get("lat");
        String lngString = db.get("lng");
        homeString = db.get("home");
        workString = db.get("work");
        String lastAddressString = db.get("last_address");

        if (latString == null) {
            lat = INVALID_LATLONG;
        } else {
            lat = Double.parseDouble(latString);
        }
        if (lngString == null) {
            lng = INVALID_LATLONG;
        } else {
            lng = Double.parseDouble(lngString);
        }

        _updateLocationCardUI(lastAddressString);

        if (currentLocality != null) {
            collapsingToolbar.setTitle(getString(R.string.title_default) + currentLocality);
        }
    }

    private void _initBackdropAsync() {
        ViewTreeObserver vto = backdrop.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                backdrop.getViewTreeObserver().removeOnPreDrawListener(this);
                backdropWidth = backdrop.getMeasuredWidth();
                backdropHeight = backdrop.getMeasuredHeight();
                AndroidUtils.log(IconPaths.TRNQL_Check, "*backdrop.width:" + backdropWidth, "backdrop.height:" + backdropHeight);
                //updateBackdrop();
                return true;
            }
        });
    }

    private void _addViewlisteners() {
        btn_activityCardData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent toActivityDataset = new Intent(v.getContext(), ActivityData.class);
                    startActivity(toActivityDataset);
            }
        });

        btn_weatherCardData1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent toWeatherDataset = new Intent(v.getContext(), WeatherData.class);
                    startActivity(toWeatherDataset);
            }
        });

        btn_weatherCardData2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent toWeatherDataset = new Intent(v.getContext(), WeatherData.class);
                    startActivity(toWeatherDataset);
            }
        });

        btn_locationCardData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent toLocationDataset = new Intent(v.getContext(), LocationData.class);
                    startActivity(toLocationDataset);
            }
        });

        btn_locationCardHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastAddress != null) {
                    String addressString = lastAddress.toString();
                    homeString = addressString;
                    getDBManager().getDB_KVP(R.id.db_kvp_locality).add("home", addressString);
                    _updateLocationCardUI(addressString);
                }
            }
        });

        btn_locationCardWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastAddress != null) {
                    String addressString = lastAddress.toString();
                    workString = addressString;
                    getDBManager().getDB_KVP(R.id.db_kvp_locality).add("work", addressString);
                    _updateLocationCardUI(addressString);
                }
            }
        });

        btn_locationClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbKVP db = getDBManager().getDB_KVP(R.id.db_kvp_locality);
                homeString = null;
                workString = null;
                db.remove("home");
                db.remove("work");
                if (lastAddress != null) {
                    _updateLocationCardUI(lastAddress.toString());
                } else {
                    _updateLocationCardUI(null);
                }
            }
        });
    }

    private void _addResourceListeners() {
        AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: Adding OP Listener (will call onChange() if not null)");
        addResource(new ObservablePropertyListener(R.id.op_location_bitmap, "update backdrop image") {
            @Override
            public void onChange(int i, Object o) {
                AndroidUtils.log(IconPaths.TRNQL_MyApp, "ISSUE13: Setting backdrop from OP onChange()");
                if (o != null) {
                    BitmapDrawable drawable = (BitmapDrawable) o;
                    _setBackdrop(drawable);
                } else {
                    _setBackdrop(null);
                }
            }
        });

        addResource(new LocalEventsListener(R.id.evt_user_at_home, "User is at home") {
            @Override
            public void onReceive(String s, Object o) {
                tv_locationCardText.setText("Home Sweet Home.\n" + s);
                btn_locationCardHome.setVisibility(View.GONE);
                btn_locationCardWork.setVisibility(View.GONE);
                tv_locationCardQuestionText.setVisibility(View.GONE);
                btn_locationClear.setVisibility(View.VISIBLE);
                img_locationCardIcon.setImageResource(R.drawable.home);
            }
        });

        addResource(new LocalEventsListener(R.id.evt_user_at_work, "User is at work") {
            @Override
            public void onReceive(String s, Object o) {
                tv_locationCardText.setText("You are at work. Stop slacking off!\n" + s);
                btn_locationCardHome.setVisibility(View.GONE);
                btn_locationCardWork.setVisibility(View.GONE);
                tv_locationCardQuestionText.setVisibility(View.GONE);
                btn_locationClear.setVisibility(View.VISIBLE);
                img_locationCardIcon.setImageResource(R.drawable.work);
            }
        });
    }

    private void updateBackdrop() {

        if (backdropWidth == 0 || backdropHeight == 0) return;

        AndroidUtils.log(IconPaths.TRNQL_MyApp,
                "updateBackdrop() called\n-backdrop.width: " + backdropWidth,
                "backdrop.height: " + backdropHeight,
                "localeCache: " + currentLocality + "\nlat: " + String.valueOf(lat) + "\nlng: " + String.valueOf(lng));

        if (ImageDownloadIntentService.rateLimitNotExceeded()) {
            AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: About to load new image from updateBackdrop(), ", "" + lat, ", " + lng);
            ImageDownloadIntentService.startActionDownloadImage(this, String.valueOf(lat), String.valueOf(lng));
        } else {
            AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: Backdrop refresh requested but denied due to refresh rate limit.");
            //BitmapDrawable bmd = (BitmapDrawable) getObservablePropertyManager().getValue(R.id.op_location_bitmap, null);
            //_setBackdrop(bmd);
        }
    }

    private void _setBackdrop(BitmapDrawable bitMapDrawable) {
        if (bitMapDrawable == null) {
            AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: Setting backdrop to default image");
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.earth);
            backdrop.setImageResource(R.drawable.earth);
            updateColors(bm);
        } else {
            AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: Setting backdrop to (non-null) bitmap");
            Bitmap bm = bitMapDrawable.getBitmap();
            backdrop.setImageDrawable(bitMapDrawable);
            updateColors(bm);
        }
    }

    @Override
    protected void smartAddressChange(AddressEntry address) {
        DbKVP db = getDBManager().getDB_KVP(R.id.db_kvp_locality);
        db.add("last_address", address.toString());
        lastAddress = address;

        _updateLocationCardUI(address.toString());

        String locality = address.getLocality();

        if (currentLocality == null || !currentLocality.equals(locality)) {
            currentLocality = locality;
            db.add("locality", locality);
        }
        AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: Current locality on address change is: " + currentLocality);

        collapsingToolbar.setTitle(getString(R.string.title_default) + currentLocality);
        // Due to a bug in CollapsingToolbarLayout, the title text does not change unless it was null, or the
        // size of the text is changed (due to collapse or otherwise). This was fixed in a recent release in AOSP
        // and should be rendered unnecessary eventually
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBarPlus1);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBarPlus1);
    }

    private void _updateLocationCardUI(String currentAddressString) {
        if (currentAddressString != null) {
            if (currentAddressString.equalsIgnoreCase(homeString)) {
                // User is at home - Fire a Local Event!
                LocalEventsManager.fireEvent(this, R.id.evt_user_at_home, currentAddressString, null);
            } else if (currentAddressString.equalsIgnoreCase(workString)) {
                // User is at work - Fire a local Event!
                LocalEventsManager.fireEvent(this, R.id.evt_user_at_work, currentAddressString, null);
            } else {
                // User is not at home or at work
                tv_locationCardText.setText("You are currently at: " + currentAddressString);
                btn_locationCardHome.setVisibility(View.VISIBLE);
                btn_locationCardWork.setVisibility(View.VISIBLE);
                tv_locationCardQuestionText.setVisibility(View.VISIBLE);
                btn_locationClear.setVisibility(View.GONE);
                img_locationCardIcon.setImageResource(R.drawable.map3);
            }
        } else {
            // location of user is unknown
            tv_locationCardText.setText("Finding your location...");
            btn_locationCardHome.setVisibility(View.GONE);
            btn_locationCardWork.setVisibility(View.GONE);
            tv_locationCardQuestionText.setVisibility(View.GONE);
            btn_locationClear.setVisibility(View.GONE);
            img_locationCardIcon.setImageResource(R.drawable.map3);
        }
    }

    @Override
    protected void smartLatLngChange(LocationEntry location) {
        double newLat = location.getLatitude();
        double newLng = location.getLongitude();

        if (lat != INVALID_LATLONG && lng != INVALID_LATLONG) {
            DbKVP db = getDBManager().getDB_KVP(R.id.db_kvp_locality);
            db.add("lat", String.valueOf(lat));
            db.add("lng", String.valueOf(lng));
            lastLocation = location;
        }

        if (Double.compare(lat, newLat) != 0 || Double.compare(lng, newLng) != 0) {
            lat = newLat;
            lng = newLng;
            AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: Calling updateBackdrop() from smartLatLngChange()");
            updateBackdrop();
        }
    }

    public void fabClicked(View view) {
        AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: Calling updateBackdrop() from fabClicked()");
        updateBackdrop();
    }

    @Override
    protected void smartActivityChange(ActivityEntry userActivity) {
        if (lastActivity != null && !lastActivity.getActivityString().equals(userActivity.getActivityString())) {
            Toast.makeText(getApplicationContext(), "User is now " + userActivity.getActivityString(), Toast.LENGTH_LONG).show();
        }
        lastActivity = userActivity;
        if (!userActivity.isStill()) {
            et_inputText.setFocusableInTouchMode(false);
            et_inputText.setHint("Speech " + getString(R.string.input_text_default) + " (user is on the move)");
            img_input.setImageResource(R.drawable.voice);
        } else {
            et_inputText.setFocusableInTouchMode(true);
            et_inputText.setHint("Keyboard " + getString(R.string.input_text_default) + " (user is still)");
            img_input.setImageResource(R.drawable.keyboard);
        }
        et_inputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (v.isFocusableInTouchMode()) {
                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    startVoiceRecognition();
                }
            }
        });
    }

    @Override
    protected void smartWeatherChange(WeatherEntry weather) {
        lastWeather = weather;
        Log.i("WEATHER", weather.getFeelsLikeAsString());
        float temp = weather.getFeelsLikeTemp();
        Date sunriseDate = weather.getSunriseTime();
        Date sunsetDate = weather.getSunsetTime();
        String timeUntilSunEvent = _getTimeUntilNextSunEvent(sunriseDate, sunsetDate);

        tv_sun.setText(timeUntilSunEvent);
        if(timeUntilSunEvent.contains("rise")){
            img_sun.setImageResource(R.drawable.sunrise);
        }else {
            img_sun.setImageResource(R.drawable.sunset);
        }

        if (temp >= 85f) {
            tv_weatherRec.setText(getString(R.string.hot));
            img_weather.setImageResource(R.drawable.hot);
        } else if (temp >= 75f) {
            tv_weatherRec.setText(getString(R.string.warm));
            img_weather.setImageResource(R.drawable.warm);
        } else if (temp >= 70f) {
            tv_weatherRec.setText(getString(R.string.room));
            img_weather.setImageResource(R.drawable.room);
        } else if (temp >= 58f) {
            tv_weatherRec.setText(getString(R.string.sweater));
            img_weather.setImageResource(R.drawable.sweater);
        } else if (temp >= 40f) {
            tv_weatherRec.setText(getString(R.string.jacket));
            img_weather.setImageResource(R.drawable.jacket);
        } else {
            tv_weatherRec.setText(getString(R.string.cold));
            img_weather.setImageResource(R.drawable.cold);
        }
    }

    private String _getTimeUntilNextSunEvent(Date sunriseDate, Date sunsetDate) {
        Calendar calendar = Calendar.getInstance();
        String result;

        calendar.setTime(sunriseDate);
        int sunriseHours = calendar.get(Calendar.HOUR_OF_DAY);
        int sunriseMinutes = calendar.get(Calendar.MINUTE);
        int sunriseSeconds = calendar.get(Calendar.SECOND);
        int sunriseSecondsAfterMidnight = sunriseHours * 60 * 60 + sunriseMinutes * 60 + sunriseSeconds;

        calendar.setTime(sunsetDate);
        int sunsetHours = calendar.get(Calendar.HOUR_OF_DAY);
        int sunsetMinutes = calendar.get(Calendar.MINUTE);
        int sunsetSeconds = calendar.get(Calendar.SECOND);
        int sunsetSecondsAfterMidnight = sunsetHours * 60 * 60 + sunsetMinutes * 60 + sunsetSeconds;

        calendar.setTime(new Date());
        int nowHours = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMinutes = calendar.get(Calendar.MINUTE);
        int nowSeconds = calendar.get(Calendar.SECOND);
        int nowSecondsAfterMidnight = nowHours * 60 * 60 + nowMinutes * 60 + nowSeconds;

        if(nowSecondsAfterMidnight < sunriseSecondsAfterMidnight){
            int secondsBeforeSunrise = sunriseSecondsAfterMidnight - nowSecondsAfterMidnight;
            result = "The sun will rise in " + _getTimeString(secondsBeforeSunrise*1000);
        }else if(nowSecondsAfterMidnight > sunriseSecondsAfterMidnight && nowSecondsAfterMidnight < sunsetSecondsAfterMidnight){
            int secondsBeforeSunset = sunsetSecondsAfterMidnight - nowSecondsAfterMidnight;
            result = "The sun will set in " + _getTimeString(secondsBeforeSunset*1000);
        }else{
            int secondsBeforeSunrise = (24 * 60 * 60 - nowSecondsAfterMidnight) + sunriseSecondsAfterMidnight;
            result = "The sun will rise in " + _getTimeString(secondsBeforeSunrise*1000);
        }
        return result;
    }

    private String _getTimeString(long millis) {

        long timeInMinutes = millis / (1000*60);
        if(timeInMinutes <= 0){
            return "0 minutes";
        } else if(timeInMinutes < 60){
            return String.valueOf(timeInMinutes) + " minutes";
        }else{
            long hours = timeInMinutes / 60;
            long remainingMins = timeInMinutes % 60;
            return String.valueOf(hours) + " hours and " + String.valueOf(remainingMins) + " minutes";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SPEECH_REQUEST_CODE) {
                et_inputText.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
            }
        }
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech Input");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, SPEECH_REQUEST_CODE);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    private void updateColors(Bitmap inputBitmap) {
        Palette.from(inputBitmap).maximumColorCount(24).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Do something for froyo and above versions
                    _actuallyUpdateColors(palette);
                } else {
                    // do something for phones running an SDK before froyo
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void _actuallyUpdateColors(Palette palette) {
        AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: About to update the colors from this palette.");
        Palette.Swatch vibrant = palette.getVibrantSwatch();
        Palette.Swatch vibrantDark = palette.getDarkVibrantSwatch();
        Palette.Swatch vibrantLight = palette.getLightVibrantSwatch();
        Palette.Swatch muted = palette.getMutedSwatch();
        Palette.Swatch mutedDark = palette.getDarkMutedSwatch();
        Palette.Swatch mutedLight = palette.getLightMutedSwatch();
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        if (vibrant != null && vibrantDark != null) {
            AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: Setting vibrant color scheme.");
            _updateColorScheme(vibrant, vibrantDark);
        } else if (muted != null && mutedDark != null) {
            AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: Setting muted color scheme.");
            _updateColorScheme(muted, mutedDark);
        }else if(vibrant != null && vibrantLight != null){
            _updateColorScheme(vibrantLight, vibrant);
        }else if(muted != null && mutedLight != null){
            _updateColorScheme(mutedLight, muted);
        }else if(vibrant != null){
            _updateColorScheme(vibrant);
        }else if (vibrantDark != null){
            _updateColorScheme(vibrantDark);
        }else if (muted != null){
            _updateColorScheme(muted);
        }else if (mutedDark != null){
            _updateColorScheme(mutedDark);
        }else if (mutedLight != null){
            _updateColorScheme(mutedLight);
        }else if (vibrantLight != null){
            _updateColorScheme(vibrantLight);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void _updateColorScheme(Palette.Swatch lightSwatch, Palette.Swatch darkSwatch){
        if(lightSwatch == null || darkSwatch == null){ return; }
        AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: updating color scheme");
        Window window = getWindow();
        collapsingToolbar.setContentScrimColor(lightSwatch.getRgb());
        collapsingToolbar.setCollapsedTitleTextColor(lightSwatch.getBodyTextColor());
        collapsingToolbar.setExpandedTitleColor(Color.WHITE);
        window.setStatusBarColor(darkSwatch.getRgb());
        window.setNavigationBarColor(darkSwatch.getRgb());
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{lightSwatch.getRgb(), lightSwatch.getRgb()});
        gd.setCornerRadius(25f);
        gd.setSize(gd.getIntrinsicWidth(), 15);
        gd.setStroke(2, lightSwatch.getBodyTextColor());
        btn_locationCardHome.setBackgroundDrawable(gd);
        btn_locationCardWork.setBackgroundDrawable(gd);
        btn_locationCardHome.setTextColor(lightSwatch.getBodyTextColor());
        btn_locationCardWork.setTextColor(lightSwatch.getBodyTextColor());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void _updateColorScheme(Palette.Swatch swatch){
        if(swatch == null){ return; }
        AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: updating color scheme");
        Window window = getWindow();
        collapsingToolbar.setContentScrimColor(swatch.getRgb());
        collapsingToolbar.setCollapsedTitleTextColor(swatch.getBodyTextColor());
        collapsingToolbar.setExpandedTitleColor(Color.WHITE);
        window.setStatusBarColor(swatch.getBodyTextColor());
        window.setNavigationBarColor(swatch.getBodyTextColor());
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{swatch.getRgb(), swatch.getRgb()});
        gd.setCornerRadius(25f);
        gd.setStroke(2, swatch.getBodyTextColor());
        gd.setSize(gd.getIntrinsicWidth(), 15);
        btn_locationCardHome.setBackgroundDrawable(gd);
        btn_locationCardWork.setBackgroundDrawable(gd);
        btn_locationCardHome.setTextColor(swatch.getBodyTextColor());
        btn_locationCardWork.setTextColor(swatch.getBodyTextColor());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE14: ON DESTROY CALLLLLEEEDDDDDDDD\n\n");
        //AppData.stopAllServices(getApplication());
        super.onDestroy();
    }

}
