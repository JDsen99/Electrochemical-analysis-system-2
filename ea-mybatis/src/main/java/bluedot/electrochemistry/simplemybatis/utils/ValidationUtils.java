package bluedot.electrochemistry.simplemybatis.utils;

import java.util.Collection;
import java.util.Map;

/**
 * 公共工具类
 *
 * @Author zero
 * @Create 2022/2/11 8:57
 */
public class ValidationUtils {
    /**
     * list/set is not empty
     *
     * @param collection 集合
     * @return 结果
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * map is not empty
     *
     * @param map map
     * @return 结果
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    /**
     * 数组不为空
     *
     * @param objects 数组
     * @return 结果
     */
    public static boolean isNotEmpty(Object[] objects) {
        return objects != null && objects.length > 0;
    }
}

