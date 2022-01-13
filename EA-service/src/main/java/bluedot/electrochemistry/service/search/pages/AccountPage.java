package bluedot.electrochemistry.service.search.pages;

import bluedot.electrochemistry.dao.BaseMapper;
import bluedot.electrochemistry.pojo.domain.User;
import bluedot.electrochemistry.service.search.condition.Conditional;
import bluedot.electrochemistry.simplespring.core.annotation.Component;

import java.util.List;

/**
 * @author Sens
 * @Create 2021/12/16 18:58
 */
@Component
public class AccountPage extends AbstractPageSearch<User>{

    @Override
    Integer getCount(BaseMapper mapper, String condition) {
        return mapper.getAccountCount(condition);
    }

    @Override
    List<User> getList(BaseMapper mapper, String condition) {
        return mapper.getAccountList(condition);
    }
}
