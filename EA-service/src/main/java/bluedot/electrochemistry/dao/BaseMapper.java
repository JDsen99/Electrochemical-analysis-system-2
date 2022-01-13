package bluedot.electrochemistry.dao;

import bluedot.electrochemistry.pojo.domain.User;

import java.util.List;

/**
 * @author Sens
 * @createDate 2021/12/16 19:19
 */
public interface BaseMapper {

    /**
     * 获取列表
     * @param condition 条件
     * @return 结果集合
     */
    List<User> getAccountList(String condition);

    /**
     * 获取Account 总数量
     * @param condition 条件
     * @return 数量
     */
    Integer getAccountCount(String condition);

    User loginByUsername(String account, String password);

    User loginByEmail(String account, String password);

    Integer checkEmail(String account);

    Integer checkAccount(String account);
}
