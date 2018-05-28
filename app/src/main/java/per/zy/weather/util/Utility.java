package per.zy.weather.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import per.zy.weather.db.City;
import per.zy.weather.db.County;
import per.zy.weather.db.Province;
import per.zy.weather.gson.App;
import per.zy.weather.gson.Today;
import per.zy.weather.gson.Weather;

/**
 * @Author zy
 * @Date 2018\5\23 0023
 */

public class Utility {
    /**
     * 解析处理返回的省级数据
     */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvinces = new JSONArray(response);
                for (int i =0; i<allProvinces.length();i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 处理返回的市级数据
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities = new JSONArray(response);
                for (int i =0; i<allCities.length();i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 处理返回的县级数据
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounties = new JSONArray(response);
                for (int i =0; i<allCounties.length();i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的数据解析成weather实体
     */

    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将返回的数据解析成today实体
     */

    public static List<App>   handleAppResponse(String response){
       Gson gson = new Gson();
        List<App> appList = gson.fromJson(response,new TypeToken<List<App>>(){}.getType());
       for (App app:appList){
           Log.i("--------------","id is"+app.getId());
           Log.i("--------------","name is"+app.getName());
       }
        return appList;
    }

    /**
     * 将返回的数据解析成today实体
     */

    public static List<Today>   handleTodayResponse(String response){

        try {
            JSONObject jsonObject = new JSONObject(response);
            String jsonArray = jsonObject.getJSONArray("result").toString();
            Gson gson = new Gson();
            List<Today> todayList = gson.fromJson(jsonArray,new TypeToken<List<Today>>(){}.getType());

            return todayList;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }
}
