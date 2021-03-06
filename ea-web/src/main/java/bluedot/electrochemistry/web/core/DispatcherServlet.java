package bluedot.electrochemistry.web.core;

import bluedot.electrochemistry.commons.factory.CacheExecutorFactory;
import bluedot.electrochemistry.commons.sqlfactorybuilder.SqlSessionFactoryBuilder;
import bluedot.electrochemistry.simplemybatis.pool.MyDataSourceImpl;
import bluedot.electrochemistry.simplespring.aop.AspectWeaver;
import bluedot.electrochemistry.utils.ClassUtil;
import bluedot.electrochemistry.utils.ConfigUtil;
import bluedot.electrochemistry.utils.LogUtil;
import bluedot.electrochemistry.simplespring.core.BeanContainer;
import bluedot.electrochemistry.simplespring.core.RequestURLAdapter;
import bluedot.electrochemistry.simplespring.core.SpringConstant;
import bluedot.electrochemistry.simplespring.core.annotation.*;
import bluedot.electrochemistry.simplespring.filter.FilterAdapter;
import bluedot.electrochemistry.simplespring.inject.DependencyInject;
import bluedot.electrochemistry.simplespring.mvc.RequestProcessorChain;
import bluedot.electrochemistry.simplespring.mvc.RequestProcessor;
import bluedot.electrochemistry.simplespring.mvc.processor.impl.DoRequestProcessor;
import bluedot.electrochemistry.simplespring.mvc.processor.impl.DoFileProcessor;
import bluedot.electrochemistry.simplespring.mvc.processor.impl.PreRequestProcessor;
import bluedot.electrochemistry.simplespring.mvc.processor.impl.StaticResourceRequestProcessor;
import bluedot.electrochemistry.utils.ValidationUtil;
import org.slf4j.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Senn
 */
@WebServlet(name = "DispatcherServlet", urlPatterns = "/*",
        initParams = {@WebInitParam(name = "contextConfigLocation", value = "application.properties")},
        loadOnStartup = 1)
public class DispatcherServlet extends HttpServlet {

    private static final Logger LOGGER = LogUtil.getLogger(DispatcherServlet.class);

    /**
     * ???????????????
     */
    List<RequestProcessor> PROCESSORS = new ArrayList<>();

    private BeanContainer beanContainer;

    /**
     * ?????????
     */
    private FilterAdapter filterAdapter;

    @Override
    public void init(ServletConfig servletConfig) {
        LOGGER.info("ready init in dispatcherServlet");

        Properties contextConfig = ConfigUtil.doLoadConfig(servletConfig.getInitParameter("contextConfigLocation"));

        //???????????????
        beanContainer = BeanContainer.getInstance();

        //???????????? ?????????
        new CacheExecutorFactory().init();

        loadBeans(contextConfig.getProperty("spring.controllerPackage"));

        loadBeans(contextConfig.getProperty("spring.scanPackage"));

        filterAdapter = new FilterAdapter();
        loadBeans(contextConfig.getProperty("spring.filterPackage"));
        //AOP??????
        new AspectWeaver().doAspectOrientedProgramming();

        //???????????????mybatis????????????IoC???????????????SqlSessionFactory??????
        new SqlSessionFactoryBuilder().build(servletConfig.getInitParameter("contextConfigLocation"));

        //????????? ??????????????? TODO open
//        new SenderHandler().init();

        //????????????
        new DependencyInject().doDependencyInject();

        //?????????????????????????????????
        // ???????????????????????????
        PreRequestProcessor preRequestProcessor = new PreRequestProcessor();
        preRequestProcessor.setFilterAdapter(filterAdapter);
        PROCESSORS.add(preRequestProcessor);

        // ?????????????????????????????????????????????????????????RequestDispatcher???????????????
        PROCESSORS.add(new StaticResourceRequestProcessor(servletConfig.getServletContext()));

        PROCESSORS.add(new DoFileProcessor());

        DoRequestProcessor doRequestProcessor = new DoRequestProcessor();
        doRequestProcessor.setFilterAdapter(filterAdapter);
        PROCESSORS.add(doRequestProcessor);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        //1.???????????????????????????
        RequestProcessorChain requestProcessorChain = new RequestProcessorChain(PROCESSORS.iterator(), request, response);

        //2.????????????????????????????????????????????????????????????????????????
        requestProcessorChain.doRequestProcessorChain();
        //3.???????????????????????????
        requestProcessorChain.doRender();

    }



    @Override
    public void destroy() {

        LOGGER.info("close all resources...");
        //???????????????
        MyDataSourceImpl.getInstance().close();
        //????????????
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        Driver driver = null;
        while (drivers.hasMoreElements()) {
            try {
                driver = drivers.nextElement();
                DriverManager.deregisterDriver(driver);
                LogUtil.getLogger(DispatcherServlet.class).debug("deregister success : driver {}", driver);
            } catch (SQLException e) {
                LogUtil.getLogger(DispatcherServlet.class).error("deregister failed : driver {}", driver);
            }
        }

    }

    /**
     * ???bean?????????????????????
     * @param packageName ????????????
     */
    public void loadBeans(String packageName) {
        // ?????????????????????????????????Class???????????????HashSet???
        Set<Class<?>> classSet = ClassUtil.extractPackageClass(packageName);
        // ??????Class??????????????????
        if (ValidationUtil.isEmpty(classSet)) {
            LOGGER.warn("Extract nothing from packageName:" + packageName);
            return;
        }
        for (Class<?> clazz : classSet) {
            for (Class<? extends Annotation> annotation : SpringConstant.BEAN_ANNOTATION) {
                //??????????????????????????????????????????bean?????????
                if (clazz.isAnnotationPresent(annotation)) {
                    LOGGER.debug("load bean: " + clazz.getName());
                    //???????????????Configuration???????????????????????????@Bean???????????????????????????????????????????????????
                    if (Configuration.class == annotation) {
                        loadConfigurationBean(clazz);
                    }else if (Controller.class == annotation) {
                        loadControllerBean(clazz);
                    }else if (Filter.class == annotation || BeforeFilter.class == annotation || AfterFilter.class == annotation) {
                        loadFilterBean(clazz);
                    }else {
                        BeanContainer.getInstance().addBean(clazz, ClassUtil.newInstance(clazz, true));
                    }
                }
            }
        }
    }


    /**
     * ????????????????????? Configuration bean??????
     * @param clazz ????????????class??????
     */
    private void loadConfigurationBean(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            // ?????????????????????????????????@Bean??????
            if (method.isAnnotationPresent(Bean.class)) {
                Object configuration = BeanContainer.getInstance().getBean(clazz);
                Object bean = null;
                try {
                    // ????????????????????????bean
                    bean = method.invoke(configuration);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOGGER.error("load configuration bean error: {}", e.getMessage());
                    e.printStackTrace();
                }
                // bean???????????????IOC??????
                if (bean != null) {
                    Class<?> beanClazz = bean.getClass();
                    LOGGER.debug("load bean :{}", beanClazz.getName());
                    BeanContainer.getInstance().addBean(beanClazz, bean);
                }

            }
        }
    }
    /**
     * ??? Controller bean?????????????????????
     * ?????? Controller ????????????????????????
     * @param clazz clazz
     */
    public void loadControllerBean(Class<?> clazz) {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        String rootUrl = "";
        if (clazz.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
            String[] value = annotation.value();
            rootUrl = value[0];
        }else {
            return;
        }
        RequestURLAdapter urlAdapter = (RequestURLAdapter) beanContainer.getBeanOrNewInstance(RequestURLAdapter.class);
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                doLoadUrl(urlAdapter, clazz, method, rootUrl);
            } else if (method.isAnnotationPresent(WhiteMapping.class)) {
                doLoadWhiteUrl(urlAdapter, clazz, method, rootUrl);
            }
        }
        beanContainer.addBean(clazz, ClassUtil.newInstance(clazz, true));
        beanContainer.addBean(RequestURLAdapter.class,urlAdapter);
    }

    private void doLoadWhiteUrl(RequestURLAdapter urlAdapter, Class<?> clazz, Method method, String rootUrl) {
        String[] value = method.getAnnotation(WhiteMapping.class).value();
        String url = rootUrl + value[0];
        urlAdapter.putWhiteUrl(url, method);
        urlAdapter.putClass(url,clazz);
    }

    private void doLoadUrl(RequestURLAdapter urlAdapter, Class<?> clazz, Method method, String rootUrl) {
        String[] value = method.getAnnotation(RequestMapping.class).value();
        String url = rootUrl + value[0];
        urlAdapter.putUrl(url, method);
        urlAdapter.putClass(url,clazz);
    }

    /**
     * ??? Filter bean?????????????????????
     * ?????? Filter ????????????????????????
     * @param clazz clazz
     */
    private void loadFilterBean(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Filter.class)) {
            int level = clazz.getAnnotation(Filter.class).value();
            filterAdapter.addBeforeFilter(clazz, level);
            filterAdapter.addAfterFilter(clazz, level);
        }
        if (clazz.isAnnotationPresent(BeforeFilter.class)) {
            int level = clazz.getAnnotation(BeforeFilter.class).value();
            filterAdapter.addBeforeFilter(clazz, level);
        }
        if (clazz.isAnnotationPresent(AfterFilter.class)) {
            int level = clazz.getAnnotation(AfterFilter.class).value();
            filterAdapter.addAfterFilter(clazz, level);
        }
    }
}
