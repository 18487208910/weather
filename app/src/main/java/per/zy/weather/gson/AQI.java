package per.zy.weather.gson;

/**
 * @Author zy
 * @Date 2018/5/25
 */

public class AQI {

    public AQICity city;

    public class AQICity{

        public String aqi;
        public String pm25;
    }
}
