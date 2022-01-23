package bluedot.electrochemistry.service.query.searchable;

import bluedot.electrochemistry.dao.BaseMapper;
import bluedot.electrochemistry.factory.MapperFactory;
import bluedot.electrochemistry.pojo.domain.User;
import bluedot.electrochemistry.service.query.SearchResult;
import bluedot.electrochemistry.service.query.SearchType;
import bluedot.electrochemistry.service.query.condition.Conditional;
import bluedot.electrochemistry.simplespring.inject.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sens
 * @createDate 2021/12/16 19:34
 */
abstract class AbstractSearch<T> implements Searchable<T> {

    @Autowired
    protected MapperFactory mapperFactory;

    @Override
    public SearchResult<T> search(Conditional condition) {
        String sql = condition.decodeCondition();
        BaseMapper mapper = new BaseMapper() {
            @Override
            public List<User> getAccountList(String condition) {
                ArrayList<User> list = new ArrayList<>();
                User user1 = new User();
                User user2 = new User();
                user1.setName("1");
                user2.setName("2");
                list.add(user1);
                list.add(user2);
                return list;
            }

            @Override
            public Integer getAccountCount(String condition) {
                return 100;
            }

            @Override
            public User getOneUser(String condition) {
                User user = new User();
                user.setName("张三");
                user.setAge(18);
                return user;
            }
        };

        if (condition.getType() == SearchType.LIST) {
            List<T> list = getList(mapper, sql);
            Integer count = count(mapper, condition);
            return new SearchResult<>(count,list);
        }else if (condition.getType() == SearchType.ONE){
            List<T> list = new ArrayList<>();
            T one = getOne(mapper, condition.decodeCondition());
            list.add(one);
            return new SearchResult<>(0,list);
        }else {
            return new SearchResult<>(count(mapper,condition),null);
        }
    }

    @Override
    public Integer count(BaseMapper mapper, Conditional condition) {
        return null;
    }

    List<T> getList(BaseMapper mapper, String condition) {
        return null;
    }

    T getOne(BaseMapper mapper, String condition){
        return null;
    }
}