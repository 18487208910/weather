package per.zy.weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @Author zy
 * @Date 2018/5/25
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }

}
