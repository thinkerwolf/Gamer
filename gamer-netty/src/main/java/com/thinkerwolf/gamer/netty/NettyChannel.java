package com.thinkerwolf.gamer.netty;

import com.thinkerwolf.gamer.common.URL;
import com.thinkerwolf.gamer.core.remoting.Channel;
import com.thinkerwolf.gamer.core.remoting.ChannelHandler;
import com.thinkerwolf.gamer.core.remoting.RemotingException;
import io.netty.channel.ChannelFuture;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NettyChannel implements Channel {

    private static final ConcurrentMap<io.netty.channel.Channel, NettyChannel> channelMap = new ConcurrentHashMap<>();

    private io.netty.channel.Channel ch;

    private URL url;

    private ChannelHandler handler;

    public NettyChannel(io.netty.channel.Channel ch, URL url, ChannelHandler handler) {
        this.ch = ch;
        this.url = url;
    }

    static NettyChannel getOrAddChannel(io.netty.channel.Channel channel, URL url, ChannelHandler handler) {
        NettyChannel nettyChannel = channelMap.get(channel);
        if (nettyChannel == null) {
            nettyChannel = new NettyChannel(channel, url, handler);
            NettyChannel oldChannel = channelMap.putIfAbsent(channel, nettyChannel);
            if (oldChannel != null) {
                nettyChannel = oldChannel;
            }
        }
        if (!channel.isOpen()) {
            channelMap.remove(channel);
        }
        return nettyChannel;
    }

    static void removeChannelIfDisconnected(io.netty.channel.Channel ch) {
        if (ch != null && !ch.isActive()) {
            channelMap.remove(ch);
        }
    }


    @Override
    public SocketAddress getLocalAddress() {
        return ch.localAddress();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return ch.remoteAddress();
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        boolean success = true;
        try {
            ChannelFuture future = ch.writeAndFlush(message);
            if (sent) {
                future.await(3000);
                success = future.isSuccess();
            }
            Throwable thrown = future.cause();
            if (thrown != null) {
                throw thrown;
            }
        } catch (Throwable e) {
            throw new RemotingException(e);
        }
        if (!success) {
            throw new RemotingException("");
        }
    }

    @Override
    public void send(Object message) throws RemotingException {
        send(message, false);
    }

    @Override
    public void close() {
        ch.close();
        removeChannelIfDisconnected(ch);
    }

    @Override
    public boolean isClosed() {
        return false;
    }
}