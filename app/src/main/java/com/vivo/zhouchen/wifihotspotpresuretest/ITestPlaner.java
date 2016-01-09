package com.vivo.zhouchen.wifihotspotpresuretest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by vivo on 2015/10/21.
 */
public interface ITestPlaner {

    public  String testType = null;
    Queue<Runnable> actions = new LinkedList<Runnable>();
    Map<String,String> errorReasons = new HashMap<String, String>();

    Map<String,String> checkConditions();

    Queue<Runnable> getTestActions();


    boolean execute();
    boolean reportResults();
    boolean stopTestForcely();
    int getTestType();
    boolean setTestType(int testType);
    String toString();

}


