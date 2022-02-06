package bluedot.electrochemistry.commons;

/**
 * @author Senn
 * @Create 2021/12/16 18:58
 */
public interface Lifecycle {
    /**
     * 初始化
     */
    void init();

    /**
     * 销毁
     */
    void destroy();
}
