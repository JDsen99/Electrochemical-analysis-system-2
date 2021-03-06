package bluedot.electrochemistry.simplespring.inject;

import bluedot.electrochemistry.utils.ClassUtil;
import bluedot.electrochemistry.utils.LogUtil;
import bluedot.electrochemistry.utils.StringUtil;
import bluedot.electrochemistry.utils.ValidationUtil;
import bluedot.electrochemistry.simplespring.core.BeanContainer;
import bluedot.electrochemistry.simplespring.inject.annotation.Autowired;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * @author Senn
 * @create 2022/1/24 19:37
 */
public class DependencyInject {
    private BeanContainer beanContainer;
    private static final Logger LOGGER = LogUtil.getLogger("spring.ioc");

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS");

    public DependencyInject() {
        beanContainer = BeanContainer.getInstance();
    }

    /**
     * 依赖注入
     */
    public void doDependencyInject() {
        LOGGER.debug(" do dependency inject start time : {}",dateTimeFormatter.format(LocalDateTime.now()));

        //遍历Bean容器中的所有class对象
        Set<Class<?>> classSet = beanContainer.getClasses();
        if (ValidationUtil.isEmpty(classSet)) {
            LOGGER.warn("There is an empty classSet in beanContainer ");
        }
        for (Class<?> clazz : classSet) {
            //遍历class对象的所有成员变量
            Field[] fields = clazz.getDeclaredFields();
            if (ValidationUtil.isEmpty(fields)) {
                continue;
            }
            for (Field field : fields) {
                //找出被Autowired标记的成员变量
                if (field.isAnnotationPresent(Autowired.class)) {
                    String autowiredValue = field.getAnnotation(Autowired.class).value();
                    //获取成员变量类型
                    Class<?> fieldClass = field.getType();
                    //获取成员变量类型在容器中对应的实例
                    Object fieldValue = getFieldInstance(fieldClass, autowiredValue);
                    if (fieldValue == null) {
                        throw new RuntimeException("unable to inject relevant type, target fieldClass is " + fieldClass.getSimpleName());
                    }
                    //通过反射将对应的成员变量实例注入到成员变量中
                    Object targetBean = beanContainer.getBean(clazz);
                    LOGGER.debug("Dependency inject for class: " + clazz.getName() + ";injected value：" + fieldClass.getName());
                    ClassUtil.setField(targetBean, field, fieldValue, true);

                }
            }
        }
        LOGGER.debug(" end dependency inject end time : {}",dateTimeFormatter.format(LocalDateTime.now()));
    }

    /**
     * 获取属性的类型对应的实现类（如果一个接口有多个实现类如何处理？先找到实现类，在通过定义的名称找到具体类）
     *
     * @param fieldClass     属性的class类
     * @param autowiredValue 需要注入实例的类名称，约定首字母小写
     * @return 实例对象
     */
    private Object getFieldInstance(Class<?> fieldClass, String autowiredValue) {
        //传入类非接口或者父类，在容器中找得到实例
        Object fieldValue = beanContainer.getBean(fieldClass);
        if (null != fieldValue) {
            return fieldValue;
        } else {
            //获取接口的实现类
            Class<?> implementedClass = getImplementClass(fieldClass, autowiredValue);
            if (null != implementedClass) {
                return beanContainer.getBean(implementedClass);
            } else {
                return null;
            }
        }
    }

    /**
     * 获取接口的实现类class对象
     *
     * @param fieldClass     属性的class类
     * @param autowiredValue 需要注入实例的类名称，约定首字母小写
     * @return 实例对象
     */
    private Class<?> getImplementClass(Class<?> fieldClass, String autowiredValue) {
        Set<Class<?>> classSet = beanContainer.getClassesBySuper(fieldClass);
        if (!ValidationUtil.isEmpty(classSet)) {
            if (ValidationUtil.isEmpty(autowiredValue)) {
                if (classSet.size() == 1) {
                    return classSet.iterator().next();
                } else {
                    //如果该接口或父类多于一个实现类但用户未指定则抛出异常
                    throw new RuntimeException("multiple implemented classes for " + fieldClass.getName() + " please set @Autowired's value to pick one");
                }

            } else {
                //这里获取的是该接口的实现类，我们给autowired命名时需要指定实现类名（首字母小写）
                for (Class<?> clazz : classSet) {
                    String classSimpleName = StringUtil.firstCharToLowerCase(clazz.getSimpleName());
                    if (classSimpleName.equals(autowiredValue)) {
                        return clazz;
                    }
                }
            }
        }
        return null;
    }
}
