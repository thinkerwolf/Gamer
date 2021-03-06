package com.thinkerwolf.gamer.common;

import org.apache.commons.collections.MapUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 连接地址
 *
 * @author wukai
 * @since 2020-05-07
 */
public class URL implements Serializable {

    private String protocol;
    private String username;
    private String password;
    private String host;
    private int port;
    private String path;
    /** 传输参数。Value值必须为String */
    private volatile Map<String, Object> parameters;
    /** 附加对象，不参与序列化 */
    private transient volatile Map<String, Object> attachments;

    public URL() {}

    public URL(
            String protocol,
            String username,
            String password,
            String host,
            int port,
            String path,
            Map<String, Object> parameters) {
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = path;
        this.parameters = parameters;
    }

    public URL(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public URL(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public static URL parse(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, Object> parameters = null;
        int i = url.indexOf("?");
        if (i >= 0) {
            String[] parts = url.substring(i + 1).split("\\&");
            parameters = new HashMap<>(16);
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            url = url.substring(0, i);
        }
        i = url.indexOf("://");
        if (i >= 0) {
            if (i == 0) {
                throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            }
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else {
            // case: file:/path/to/file.txt
            i = url.indexOf(":/");
            if (i >= 0) {
                if (i == 0) {
                    throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                }
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }

        i = url.indexOf("/");
        if (i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0, i);
        }
        i = url.indexOf("@");
        if (i >= 0) {
            username = url.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            url = url.substring(i + 1);
        }
        i = url.indexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }
        if (url.length() > 0) {
            host = url;
        }
        return new URL(protocol, username, password, host, port, path, parameters);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String toProtocolHostPort() {
        return String.format("%s://%s:%d", protocol, host, port);
    }

    public String toHostPort() {
        return String.format("%s:%d", host, port);
    }

    public String getStringParameter(String key, String defaultValue) {
        return MapUtils.getString(parameters, key, defaultValue);
    }

    public String getStringParameter(String key) {
        return getStringParameter(key, null);
    }

    public Integer getIntParameter(String key, Integer defaultValue) {
        return MapUtils.getInteger(parameters, key, defaultValue);
    }

    public Integer getIntParameter(String key) {
        return getIntParameter(key, null);
    }

    public Long getLongParameter(String key, Long defaultValue) {
        return MapUtils.getLong(parameters, key, defaultValue);
    }

    public Long getLongParameter(String key) {
        return getLongParameter(key, null);
    }

    public Boolean getBooleanParameter(String key, Boolean defaultValue) {
        return MapUtils.getBoolean(parameters, key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key) {
        return (T) MapUtils.getObject(parameters, key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, T defaultValue) {
        return (T) MapUtils.getObject(parameters, key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttach(String key) {
        synchronized (this) {
            return (T) MapUtils.getObject(attachments, key);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttach(String key, T dv) {
        synchronized (this) {
            return (T) MapUtils.getObject(attachments, key, dv);
        }
    }

    public java.net.URL toURL() {
        try {
            return new java.net.URL(protocol, host, port, Optional.ofNullable(path).orElse(""));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAttach(String key, Object value) {
        if (key == null || value == null) {
            return;
        }
        synchronized (this) {
            if (attachments == null) {
                attachments = new HashMap<>();
            }
            attachments.put(key, value);
        }
    }

    private Map<String, Object> internalAttach() {
        if (attachments == null) {
            synchronized (this) {
                if (attachments == null) {
                    attachments = new HashMap<>();
                }
            }
        }
        return attachments;
    }

    public static String encode(String s) {
        try {
            return URLEncoder.encode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String decode(String s) {
        try {
            return URLDecoder.decode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        URL url = (URL) o;
        return port == url.port
                && Objects.equals(protocol, url.protocol)
                && Objects.equals(username, url.username)
                && Objects.equals(password, url.password)
                && Objects.equals(host, url.host)
                && Objects.equals(path, url.path)
                && Objects.equals(parameters, url.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, username, password, host, port, path, parameters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://");
        if (username != null) {
            sb.append(username);
            if (password != null) {
                sb.append(":").append(password);
            }
            sb.append("@");
        }

        if (host != null) {
            sb.append(host);
            if (port > 0) {
                sb.append(":").append(port);
            }
        }

        if (path != null) {
            sb.append("/").append(path);
        }
        if (parameters != null && parameters.size() > 0) {
            final int size = parameters.size();
            int pos = 0;
            sb.append("?");
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                if (pos >= size - 1) {
                    break;
                }
                sb.append("&");
                pos++;
            }
        }
        return sb.toString();
    }
}
