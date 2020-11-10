package com.licheedev.serialportapisample.serial;

public class SerialManager {

    private static class InstanceHolder {
        private static final SerialManager instance = new SerialManager();
    }

    public static SerialManager get() {
        return InstanceHolder.instance;
    }

    public final SerialWorker mSerialWorker;

    private SerialManager() {
        mSerialWorker = new SerialWorker();
    }

    public SerialWorker getSerialWorker() {
        return mSerialWorker;
    }

    // TODO: 2020/11/10  

    public void openDevice() {
        // TODO: 2020/11/10  
    }
}
