package com.licheedev.serialportapisample.serial;

import android.serialport.SerialPort;
import androidx.annotation.Nullable;
import com.licheedev.hwutils.SystemClockEx;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class SerialWorker {

    private static final long TIMEOUT = 1000L;

    private final ExecutorService mSendExecutor = Executors.newSingleThreadExecutor();

    private final List<SampleWaitRoom> mWaitRooms = new CopyOnWriteArrayList<>();
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mThread;
    private SerialPort mSerialPort;

    public synchronized void open(File device, int baudrate) throws Exception {
        try {
            mSerialPort = new SerialPort(device, baudrate);
            mInputStream = mSerialPort.getInputStream();
            mOutputStream = mSerialPort.getOutputStream();
            ReadThread thread = new ReadThread(mInputStream);
            mThread = thread;
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
            close();
            throw e;
        }
    }

    public synchronized void close() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }

        if (mSerialPort != null) {
            try {
                mSerialPort.close();
            } catch (Exception e) {
                //e.printStackTrace();
            }
            mSerialPort = null;
        }

        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
            mInputStream = null;
        }

        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
            mOutputStream = null;
        }
    }

    public synchronized void release() {
        close();
        mSendExecutor.shutdown();
    }

    /**
     * send data. will block current thread
     *
     * @param bytes Send bytes
     * @return Receive bytes
     * @throws Exception
     */
    @Nullable
    public byte[] send(byte[] bytes) throws Exception {
        SampleWaitRoom waitRoom = new SampleWaitRoom(bytes);
        try {
            mWaitRooms.add(waitRoom);
            Future<byte[]> future = mSendExecutor.submit(new Callable<byte[]>() {
                @Override
                public byte[] call() throws Exception {
                    mOutputStream.write(bytes);
                    mOutputStream.flush();
                    return waitRoom.getResponse(TIMEOUT);
                }
            });
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            mWaitRooms.remove(waitRoom);
        }
    }

    /**
     * send data. will block current thread
     *
     * @param bytes Send bytes
     * @return Receive bytes,
     */
    @Nullable
    public byte[] sendNoThrow(byte[] bytes) {
        try {
            return send(bytes);
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    private void onReceiveData(byte[] bytes) {

        Iterator<SampleWaitRoom> iterator = mWaitRooms.iterator();
        while (iterator.hasNext()) {
            try {
                iterator.next().putResponse(bytes);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    private class ReadThread extends Thread {

        private final InputStream mInputStream;
        private final AtomicBoolean mRunning = new AtomicBoolean(true);

        public ReadThread(InputStream inputStream) {
            mInputStream = inputStream;
        }

        @Override
        public void run() {
            byte[] buff = new byte[2048];
            int read;
            while (mRunning.get()) {
                try {
                    int available = mInputStream.available();
                    if (available > 0) {
                        read = mInputStream.read(buff);
                        if (read > 0) {
                            onReceiveData(Arrays.copyOf(buff, read));
                        }
                    } else {
                        // if no data, sleep for a while to reduce the cpu usage
                        SystemClockEx.sleep(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void interrupt() {
            mRunning.set(false);
            super.interrupt();
        }
    }
}
