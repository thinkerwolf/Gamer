package com.thinkerwolf.gamer;

import java.util.ArrayList;
import java.util.List;

public class RpcTestAction implements IRpcAction {

    @Override
    public String sayHello(String hello) {
        return "Hello " + hello;
    }

    @Override
    public List<Integer> getList() {
        List<Integer> list = new ArrayList<>();
        list.add(100);
        return list;
    }
}
