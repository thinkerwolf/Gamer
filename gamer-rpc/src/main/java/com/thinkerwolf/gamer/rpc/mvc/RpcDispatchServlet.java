package com.thinkerwolf.gamer.rpc.mvc;

import com.thinkerwolf.gamer.common.DefaultObjectFactory;
import com.thinkerwolf.gamer.common.ObjectFactory;
import com.thinkerwolf.gamer.common.util.ClassUtils;
import com.thinkerwolf.gamer.core.mvc.Invocation;
import com.thinkerwolf.gamer.core.servlet.*;
import com.thinkerwolf.gamer.core.spring.SpringObjectFactory;
import com.thinkerwolf.gamer.rpc.annotation.RpcClient;
import com.thinkerwolf.gamer.rpc.exception.RpcException;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class RpcDispatchServlet implements Servlet {

    private Servlet delegate;

    private ServletConfig servletConfig;

    private ObjectFactory objectFactory;

    private Map<String, Invocation> rpcInvocationMap = new ConcurrentHashMap<>();

    public RpcDispatchServlet(Servlet delegate) {
        this.delegate = delegate;
    }

    @Override
    public void init(ServletConfig servletConfig) throws Exception {
        this.servletConfig = servletConfig;
        initObjectFactory(servletConfig);
        initRpcAction();
    }

    private void initObjectFactory(ServletConfig config) {
        objectFactory = (ObjectFactory) config.getServletContext().getAttribute(ServletContext.ROOT_OBJECT_FACTORY);
        if (objectFactory == null) {
            ApplicationContext springContext = (ApplicationContext) config.getServletContext().getAttribute(ServletContext.SPRING_APPLICATION_CONTEXT_ATTRIBUTE);
            if (springContext != null) {
                this.objectFactory = new SpringObjectFactory(springContext);
            } else {
                this.objectFactory = new DefaultObjectFactory();
            }
        }
    }

    private void initRpcAction() throws Exception {
        Set<Class> set = ClassUtils.scanClasses(servletConfig.getInitParam(ServletConfig.COMPONENT_SCAN_PACKAGE));
        for (Class clazz : set) {
            int mod = clazz.getModifiers();
            if (Modifier.isInterface(mod) || Modifier.isAbstract(mod)) {
                continue;
            }
            Object obj = null;
            Class<?>[] ifaces = clazz.getInterfaces();
            for (Class<?> iface : ifaces) {
                RpcClient rpcClient = iface.getAnnotation(RpcClient.class);
                if (rpcClient != null) {
                    for (Method method : iface.getDeclaredMethods()) {
                        if (obj == null) {
                            obj = objectFactory.buildObject(clazz);
                        }
                        RpcInvocation invocation = createInvocation(obj, iface, method, rpcClient);
                        if (rpcInvocationMap.containsKey(invocation.getCommand())) {
                            throw new RpcException("Duplicate action command :" + invocation.getCommand());
                        }
                        rpcInvocationMap.put(invocation.getCommand(), invocation);
                    }
                }
            }
        }
    }

    private RpcInvocation createInvocation(Object obj, Class interfaceClass, Method method, RpcClient rpcClient) {
        RpcInvocation invocation = new RpcInvocation(interfaceClass, method, obj, rpcClient);
        return invocation;
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        Invocation invocation = rpcInvocationMap.get(request.getCommand());
        if (invocation != null) {
            invocation.handle(request, response);
            return;
        }

        if (delegate != null) {
            delegate.service(request, response);
        }
    }

    @Override
    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    @Override
    public void destroy() throws Exception {

    }
}
