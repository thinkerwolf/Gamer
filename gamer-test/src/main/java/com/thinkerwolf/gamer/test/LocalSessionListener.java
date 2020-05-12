package com.thinkerwolf.gamer.test;

import com.thinkerwolf.gamer.core.servlet.Session;
import com.thinkerwolf.gamer.core.servlet.SessionEvent;
import com.thinkerwolf.gamer.core.servlet.SessionListener;
import com.thinkerwolf.gamer.core.util.ResponseUtil;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocalSessionListener implements SessionListener {

    private Set<Session> sessions = new LinkedHashSet<>();
    private ScheduledExecutorService schedule = Executors.newScheduledThreadPool(3);
    private volatile boolean started;

    private final Object lock = new Object();

    @Override
    public void sessionCreated(SessionEvent se) {
        synchronized (lock) {
            sessions.add(se.getSource());
        }
        System.out.println("session create : " + se.getSource());
        if (!started) {
            synchronized (this) {
                if (!started) {
                    started = true;
                    startPushTest();
                }
            }
        }
    }

    @Override
    public void sessionDestroyed(SessionEvent se) {
        synchronized (lock) {
            sessions.remove(se.getSource());
        }
        System.out.println("session destroy : " + se.getSource());
    }

    public void startPushTest() {
        schedule.schedule(new Runnable() {
            private int num = 1;
            @Override
            public void run() {
                System.out.println("session push = " + sessions.size());
                synchronized (lock) {
                    for (Session session : sessions) {
                        session.push(ResponseUtil.CONTENT_JSON, "push@command",("{\"num\":" + num + ",\"netty\":\"4.1.19\"}").getBytes());
                    }
                }
                num++;
                schedule.schedule(this, 2, TimeUnit.SECONDS);

            }
        }, 10, TimeUnit.SECONDS);
    }

}