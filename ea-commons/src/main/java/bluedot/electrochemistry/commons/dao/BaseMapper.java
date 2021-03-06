package bluedot.electrochemistry.commons.dao;

import bluedot.electrochemistry.commons.entity.EaFile;
import bluedot.electrochemistry.commons.entity.Right;
import bluedot.electrochemistry.commons.entity.Role;
import bluedot.electrochemistry.commons.entity.User;

import java.util.List;

/**
 * @author Senn
 * @createDate 2021/12/16 19:19
 */
public interface BaseMapper {

    Integer checkEmail(String account);

    User loginByEmail(String account);

    Integer changePassword(Long id,String password);

    List<Right> getRights(Long id);

    EaFile getFileById(Long id);

    List<String> getRolesById(String account);
}
