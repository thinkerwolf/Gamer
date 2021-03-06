package com.thinkerwolf.gamer.core.servlet;

import com.thinkerwolf.gamer.common.URL;
import com.thinkerwolf.gamer.remoting.Server;

import java.util.List;
import java.util.Map;

/**
 * Servlet启动器
 *
 * @author wukai
 */
public interface ServletBootstrap {
    /**
     * 启动
     *
     * @throws Exception exception
     */
    void startup() throws Exception;

    /**
     * 获取服务器启动的URL
     *
     * @return url list
     */
    List<URL> getUrls();

    /**
     * 获取ServletConfig
     *
     * @return ServletConfig
     */
    ServletConfig getServletConfig();

    void close();

    boolean isClosed();
}
