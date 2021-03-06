package com.thinkerwolf.gamer.core.listener;

import com.thinkerwolf.gamer.core.servlet.ServletConfig;
import com.thinkerwolf.gamer.core.servlet.ServletContext;
import com.thinkerwolf.gamer.core.servlet.ServletContextEvent;
import com.thinkerwolf.gamer.core.servlet.ServletContextListener;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringContextLoadListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletConfig config = (ServletConfig) sce.getSource();
        ServletContext context = config.getServletContext();
        ApplicationContext applicationContext = (ApplicationContext) context.getAttribute(ServletContext.SPRING_APPLICATION_CONTEXT_ATTRIBUTE);
        if (applicationContext == null) {
            applicationContext = createApplicationContext(config);
            context.setAttribute(ServletContext.SPRING_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
        }
    }

    private ApplicationContext createApplicationContext(ServletConfig config) {
        String scanPackages = config.getInitParam(ServletConfig.COMPONENT_SCAN_PACKAGE);
        String classpathLocation = config.getInitParam(ServletConfig.CLASSPATH_LOCATION);
        ApplicationContext context = null;
        String[] scans = StringUtils.split(scanPackages, ';');
        if (ArrayUtils.isNotEmpty(scans)) {
            context = new AnnotationConfigApplicationContext(scanPackages);
        }

        String[] classpath = StringUtils.split(classpathLocation, ';');
        if (ArrayUtils.isNotEmpty(classpath)) {
            context = new ClassPathXmlApplicationContext(classpath, context);
        }
        if (context == null) {
            throw new RuntimeException();
        }
        return context;
    }

    @Override
    public void contextDestroy(ServletContextEvent sce) {

    }
}
