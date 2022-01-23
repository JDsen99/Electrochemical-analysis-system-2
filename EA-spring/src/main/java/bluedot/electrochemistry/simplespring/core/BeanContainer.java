package bluedot.electrochemistry.simplespring.core;

import bluedot.electrochemistry.simplespring.aop.annotation.Aspect;
import bluedot.electrochemistry.simplespring.core.annotation.*;
import bluedot.electrochemistry.simplespring.util.ClassUtil;
import bluedot.electrochemistry.simplespring.util.LogUtil;
import bluedot.electrochemistry.simplespring.util.ValidationUtil;
import com.sun.deploy.net.proxy.UserDefinedProxyConfig;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Senn
 * @create 2022/1/22 17:03
 */
public class BeanContainer {
    /**
     * bean容器
     */
    private final Map<Class<?>, Object> beanMap = new ConcurrentHashMap<>();
    /**
     * 加载bean的注解列表
     */
    private static final List<Class<? extends Annotation>> BEAN_ANNOTATION = Arrays.asList(
            Component.class,
            Controller.class,
            Service.class,
            Repository.class,
            Aspect.class,
            Configuration.class,
            Filter.class,
            BeforeFilter.class,
            AfterFilter.class);
    /**
     * 判断容器是否被加载
     */
    private boolean loaded = false;
    /**
     * 获取静态日志管理器
     */
    private static final Logger LOGGER = LogUtil.getLogger();


    /**
     * 通过内部枚举类来实现bean容器的单例，线程安全，不会被反射或者序列化破坏
     */
    private enum ContainerHolder {
        /**
         * 存储bean容器的枚举对象
         */
        HOLDER;
        private BeanContainer instance;

        ContainerHolder() {
            instance = new BeanContainer();
        }
    }

    /**
     * 获取单例容器对象
     *
     * @return 容器对象
     */
    public static BeanContainer getInstance() {

        return ContainerHolder.HOLDER.instance;
    }

    public Map getBeanContainer() {
        return this.beanMap;
    }

    /**
     * 将bean对象加载进容器
     *
     * @param packageName 扫描包名
     */
    public void loadBeans(String packageName) {
        if (isLoaded()) {
            LOGGER.warn("BeanContainer has been loaded");
            return;
        }
        // 获得扫描路径下所有类的Class文件存放到HashSet中
        Set<Class<?>> classSet = ClassUtil.extractPackageClass(packageName);
        // 判断Class集合是否非空
        if (ValidationUtil.isEmpty(classSet)) {
            LOGGER.warn("Extract nothing from packageName:" + packageName);
            return;
        }
        for (Class<?> clazz : classSet) {
            for (Class<? extends Annotation> annotation : BEAN_ANNOTATION) {
                //如果类对象中存在注解则加载进bean容器中
                if (clazz.isAnnotationPresent(annotation)) {
                    LOGGER.debug("load bean: " + clazz.getName());
                    beanMap.put(clazz, ClassUtil.newInstance(clazz, true));
                    //如果注解为Configuration，则需要将该类中被@Bean标记的方法返回的对象也加载进容器中
                    if (Configuration.class == annotation) {
                        loadConfigurationBean(clazz);
                    }
                }
            }
        }
        loaded = true;
    }

    /**
     * 将bean对象加载进容器
     *
     * @param packageName 扫描包名
     */
    public void loadController(String packageName) {
        if (isLoaded()) {
            LOGGER.warn("BeanContainer has been loaded");
            return;
        }
        // 获得扫描路径下所有类的Class文件存放到HashSet中
        Set<Class<?>> classSet = ClassUtil.extractPackageClass(packageName);
        // 判断Class集合是否非空
        if (ValidationUtil.isEmpty(classSet)) {
            LOGGER.warn("Extract nothing from packageName:" + packageName);
            return;
        }
        RequestURLAdapter urlAdapter = new RequestURLAdapter();
        for (Class<?> clazz : classSet) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                LOGGER.debug("load  controller: " + clazz.getName());
                Method[] declaredMethods = clazz.getDeclaredMethods();
                String rootUrl = "";
                if (clazz.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
                    String[] value = annotation.value();
                    rootUrl = value[0];
                }

                for (Method method : declaredMethods) {
                    Annotation[] annotations = method.getDeclaredAnnotations();

                    if (annotations.length == 0) continue;
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType() == RequestMapping.class) {
                            RequestMapping annotation1 = (RequestMapping) annotation;
                            String[] value = annotation1.value();
                            String url = rootUrl + value[0];
                            urlAdapter.putUrl(url, method);
                            urlAdapter.putClass(url,clazz);
                        }
                    }
                }
                beanMap.put(clazz, ClassUtil.newInstance(clazz, true));
            }
        }
        addBean(RequestURLAdapter.class , urlAdapter);
        loaded = true;
    }

    /**
     * 添加元素
     *
     * @param clazz 类
     * @param bean  对象
     * @return bean实例，没有则返回null
     */
    public Object addBean(Class<?> clazz, Object bean) {
        return beanMap.put(clazz, bean);
    }

    /**
     * 删除元素
     *
     * @param clazz 类
     * @return bean实例，没有则返回null
     */
    public Object removeBean(Class<?> clazz) {
        return beanMap.remove(clazz);
    }

    /**
     * 判断容器是否被加载过
     *
     * @return 结果
     */
    public boolean isLoaded() {
        return loaded;
    }

    public Object getBean(Class<?> clazz) {
        return beanMap.get(clazz);
    }

    /**
     * 获取class集合，即所有的键
     *
     * @return set集合
     */
    public Set<Class<?>> getClasses() {
        return beanMap.keySet();
    }

    /**
     * 获取所有bean对象
     *
     * @return bean对象的set集合
     */
    public Set<Object> getBeans() {
        return new HashSet<>(beanMap.values());
    }

    /**
     * 根据注解获取class对象的的集合
     *
     * @param annotation 注解
     * @return class对象集合
     */
    public Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation) {
        //获取beanMap的所有class对象
        Set<Class<?>> keySet = getClasses();
        if (ValidationUtil.isEmpty(keySet)) {
            LOGGER.warn("nothing in beanMap");
            return null;
        }
        //通过注解筛选需要的class对象，并添加到classSet里
        Set<Class<?>> classSet = new HashSet<>();
        for (Class<?> clazz : keySet) {
            if (clazz.isAnnotationPresent(annotation)) {
                classSet.add(clazz);
            }
        }
        return classSet.size() > 0 ? classSet : null;
    }

    /**
     * 通过接口或者父类获取到对应的class
     * 和上面的方法大部分相同，可以使用模板方法优化，但没必要
     *
     * @param classOrInterface 接口或父类
     * @return 集合
     */
    public Set<Class<?>> getClassesBySuper(Class<?> classOrInterface) {
        //获取beanMap的所有class对象
        Set<Class<?>> keySet = getClasses();
        if (ValidationUtil.isEmpty(keySet)) {
            LOGGER.warn("nothing in beanMap");
            return null;
        }
        //通过注解筛选需要的class对象，并添加到classSet里
        Set<Class<?>> classSet = new HashSet<>();
        for (Class<?> clazz : keySet) {
            //这里只想判断该类是否是作为classOrInterface的子类，而不想获取到自己本身
            if (classOrInterface.isAssignableFrom(clazz) && !clazz.equals(classOrInterface)) {
                classSet.add(clazz);
            }
        }
        return classSet.size() > 0 ? classSet : null;
    }

    /**
     * 容器大小
     *
     * @return 大小
     */
    public int size() {
        return beanMap.size();
    }

    /**
     * 加载配置类中的bean对象
     *
     * @param clazz 配置类的class文件
     */
    private void loadConfigurationBean(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            // 判断遍历到的方法是否有@Bean注解
            if (method.isAnnotationPresent(Bean.class)) {
                Object configuration = beanMap.get(clazz);
                Object bean = null;
                try {
                    // 直接执行方法获得bean
                    bean = method.invoke(configuration);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOGGER.error("load configuration bean error: {}", e.getMessage());
                    e.printStackTrace();
                }
                // bean不为空加入IOC容器
                if (bean != null) {
                    Class<?> beanClazz = bean.getClass();
                    LOGGER.debug("load bean :{}", beanClazz.getName());
                    beanMap.put(beanClazz, bean);
                }

            }
        }
    }

}
