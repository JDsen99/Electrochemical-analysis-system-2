package bluedot.electrochemistry.simplemybatis.session;

import bluedot.electrochemistry.simplemybatis.executor.Executor;

import java.util.List;

/**
 * 与数据库交互的会话，完成必要数据库增删改查功能，进行数据库操作的主要接口
 *
 * @Author zero
 * @Create 2022/2/10 14:57
 */
public interface SqlSession {
    /**
     * 查询单条记录
     *
     * @param statement 封装好sql语句的唯一id
     * @param parameter 参数
     * @param <T>       实体类类型
     * @return 类对象
     */
    <T> T selectOne(String statement, Object parameter);

    /**
     * 查询多条记录
     *
     * @param statement 封装好sql语句的唯一id
     * @param parameter 参数
     * @param <E>       实体类类型
     * @return 集合
     */
    <E> List<E> selectList(String statement, Object parameter);

    /**
     * 更新
     *
     * @param statement 封装好sql语句的唯一id
     * @param parameter 参数
     * @return 受影响的行数
     */
    int update(String statement, Object parameter);

    /**
     * 传入po类对象自动生成sql语句并执行
     *
     * @param type 封装好数据的po类对象
     * @param <T>  泛型
     * @return 受影响的行数
     */
    <T> int update(T type);

    /**
     * 插入
     *
     * @param statement 封装好sql语句的唯一id
     * @param parameter 参数
     * @return 受影响的行数
     */
    int insert(String statement, Object parameter);

    /**
     * 传入po类对象自动生成sql语句并执行
     *
     * @param type 封装好数据的po类对象
     * @param <T>  泛型
     * @return 受影响的行数
     */
    <T> int insert(T type);

    /**
     * 删除
     *
     * @param statement 封装好sql语句的唯一id
     * @param parameter 参数
     * @return 受影响的行数
     */
    int delete(String statement, Object parameter);

    /**
     * 传入po类对象自动生成sql语句并执行
     *
     * @param type 封装好数据的po类对象
     * @param <T>  泛型
     * @return 受影响的行数
     */
    <T> int delete(T type);

    /**
     * 获取mapper
     *
     * @param type 实体类字节码对象
     * @param <T>  mapper的接口类
     * @return 绑定到此Session的mapper
     */
    <T> T getMapper(Class<T> type);

    /**
     * 获取配置类
     *
     * @return 配置类
     */
    Configuration getConfiguration();

    /**
     * 获取执行器
     *
     * @return 执行器
     */
    Executor getExecutor();

    /**
     * 设置自动提交，默认是false;
     * @param flag false
     */
    void setAutoCommit(boolean flag);

    /**
     * 事务回滚策略，默认数据库事务回滚
     */
    void rollback();

    /**
     * 提交事务
     */
    void commit();

    /**
     * 关闭连接
     */
    void close();

}
