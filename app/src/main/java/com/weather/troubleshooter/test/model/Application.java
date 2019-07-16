package com.weather.troubleshooter.test.model;

import interfaces.heweather.com.interfacesmodule.view.HeConfig;

public class Application  extends android.app.Application {


    @Override
    public void onCreate() {
        super.onCreate();

        HeConfig.init("HE1907161540121432", "4f2e7d5379b54566bbd362ec349d1470");
        HeConfig.switchToFreeServerNode();
    }
}
