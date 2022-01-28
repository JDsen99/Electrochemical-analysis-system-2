package bluedot.electrochemistry.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 静态日志管理器，每个线程拥有自己的日志管理对象
 * @author Senn
 * @create 2022/1/22 17:03
 */
public class LogUtil {
    /**
     * 根据线程名获取 Log 对象
     * @return slf4j.Logger
     */
    public static Logger getLogger() {
        return LoggerFactory.getLogger(Thread.currentThread().getName());
    }

    /**
     * 根据 Class 获取 Log 对象
     * @return slf4j.Logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}