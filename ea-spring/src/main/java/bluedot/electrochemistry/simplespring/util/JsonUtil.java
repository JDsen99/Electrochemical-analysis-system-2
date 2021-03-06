package bluedot.electrochemistry.simplespring.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Senn
 * @create 2022/1/22 17:03
 * @description 解析成json对象
 */
public class JsonUtil {
    private static volatile Gson gson = null;

    private JsonUtil() {
    }

    /**
     * 准确的多线程 的 单例模式
     *
     * @param ojb 数据
     * @return String json
     */
    public static String toJson(Object ojb) {
        if (null == gson) { //为了避免开销 先检查是否为空
            synchronized (JsonUtil.class) {
                if (null == gson) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gson = gsonBuilder.create();
                }
            }
        }
        return gson.toJson(ojb);
    }
}
