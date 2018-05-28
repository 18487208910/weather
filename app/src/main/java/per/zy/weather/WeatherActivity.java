package per.zy.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import per.zy.weather.gson.App;
import per.zy.weather.gson.Forecast;
import per.zy.weather.gson.Today;
import per.zy.weather.gson.Weather;
import per.zy.weather.util.HttpUtil;
import per.zy.weather.util.Utility;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private LinearLayout appLayout;

    private  LinearLayout todayLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    //Today
    private TextView today_title;


    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefresh;

    private String mWeatherId;

    public DrawerLayout drawerLayout;

    private Button navButton;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decoView = getWindow().getDecorView();
            decoView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }else{}
        setContentView(R.layout.activity_weather);

        //初始化各控件
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);

        appLayout = (LinearLayout) findViewById(R.id.app_layout);

        todayLayout = (LinearLayout) findViewById(R.id.today_layout);

        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

        //Today


        //刷新
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiper_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        //侧滑
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
//        String today = prefs.getString("todaylist", null);
//        String app = prefs.getString("applist", null);
        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);

            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);

//            showAppInfo(app);
            requestTest();
            requestToday();

        } else {
            //去服务器查询天气数据
            mWeatherId = getIntent().getStringExtra("weather_id");
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
            requestTest();
            requestToday();
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

    }

    /**
     * 根据id请求天气信息
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId +
                "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                 final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });

                loadBingPic();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });

    }

    /**
     * 处理展示weather实体中的数据
     */

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxTetx = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxTetx.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort  = "舒适度"+weather.suggestion.comfort.info;
        String carWash = "洗车指数"+weather.suggestion.carWash.info;
        String sport = "运动建议"+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 加载每日一图
     */
    public void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOKHttpRequest(requestBingPic, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            final String bingPic = response.body().string();
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
            editor.putString("bing_pic",bingPic);
            editor.apply();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                }
            });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }


        });
    }

/**
 * 测试json解析
 */
public void requestTest( ) {
    String Url = "http://aac2de1f8d056ce157ca.test.upcdn.net/apicloud/6a510a1d74326d90d8beec8f248a9421.json";
    HttpUtil.sendOKHttpRequest(Url, new Callback() {
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String responseText = response.body().string();
         final List<App> appList =  Utility.handleAppResponse(responseText);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appList != null ) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("applist", responseText);
                       editor.apply();
                        showAppInfo(appList);
                    } else {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }

        @Override
        public void onFailure(Call call, IOException e) {

        }
    });

}

    private void showAppInfo(List<App> appList) {
        appLayout.removeAllViews();
        for(App app : appList){
            View view = LayoutInflater.from(this).inflate(R.layout.app_item,appLayout,false);
            TextView id = (TextView) view.findViewById(R.id.app_id);
            TextView name = (TextView) view.findViewById(R.id.app_name);
            id.setText(app.getId());
            name.setText(app.getName());
            appLayout.addView(view);
        }
            weatherLayout.setVisibility(View.VISIBLE);
    }


    /**
     * 历史今天
     */
    public void requestToday( ) {
        Calendar calendar=Calendar.getInstance();
        String month = Integer.toString(calendar.get(Calendar.MONTH)+1);
        String day = Integer.toString(calendar.get(Calendar.DATE));
        String Url = "http://api.juheapi.com/japi/toh?v=1.0&month="+month+"&day="+day+"&key=6c3130b3c8a85d066fb18012adc1fd35";
        HttpUtil.sendOKHttpRequest(Url, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final List<Today> todayList =  Utility.handleTodayResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (todayList != null ) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("todaylist", responseText);
                        editor.apply();
                            showTodayInfo(todayList);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

    }

    private void showTodayInfo(List<Today> todayList) {
        todayLayout.removeAllViews();
        for(Today today : todayList){
            View view = LayoutInflater.from(this).inflate(R.layout.today_item,todayLayout,false);
            TextView title = (TextView) view.findViewById(R.id.today_title);

            TextView year = (TextView) view.findViewById(R.id.today_year);

            TextView des = (TextView) view.findViewById(R.id.today_des);

            ImageView img = (ImageView) view.findViewById(R.id.today_pic_img);

            Glide.with(WeatherActivity.this).load(today.getPic()).into(img);
            title.setText(today.getTitle());
            year.setText(today.getYear()+"年　　　"+today.getLunar());
            des.setText(today.getDes());

            todayLayout.addView(view);
        }
        weatherLayout.setVisibility(View.VISIBLE);
    }

}

