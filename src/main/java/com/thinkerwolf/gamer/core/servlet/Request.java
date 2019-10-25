package com.thinkerwolf.gamer.core.servlet;

import java.util.Map;

/**
 * request
 *
 * @author wukai
 */
public interface Request {

    String COMMAND_ATTRIBUTE = "command";

    String DECORATOR_ATTRIBUTE = "decorator";

    long getRequestId();

    Object getAttribute(String key);

    Map<String, Object> getAttributes();

    Object removeAttribute(String key);

    void setAttribute(String key, Object value);

    /**
     * 获取服务端口
     *
     * @return
     */
    int getServerPort();

    Session getSession();

    Session getSession(boolean create);

    Protocol getProtocol();


}
