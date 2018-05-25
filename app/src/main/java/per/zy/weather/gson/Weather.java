package per.zy.weather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @Author zy
 * @Date 2018/5/25
 */

public class Weather {
    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}