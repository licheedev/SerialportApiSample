package com.licheedev.serialportapisample.serial;

import androidx.annotation.Nullable;
import com.licheedev.hwutils.ByteUtil;

class SampleWaitRoom implements WaitRoom<byte[], byte[]> {

    private final byte[] mSend;
    private byte[] mResponse;

    public SampleWaitRoom(byte[] send) {
        mSend = send;
    }

    @Nullable
    @Override
    public synchronized byte[] getResponse(long timeout) {
        try {
            wait(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // reset isInterrupted flag
        }
        return mResponse;
    }

    @Override
    public synchronized void putResponse(byte[] recv) {
        if (isMyResponse(mSend, recv)) {
            mResponse = recv;
            notifyAll();
        }
    }

    @Override
    public boolean isMyResponse(byte[] send, byte[] recv) {

        String sendHex = ByteUtil.bytes2HexStr(send); // 40BF
        String recvHex = ByteUtil.bytes2HexStr(recv); // 40BF64, battery is 100(0x64)

        // TODO: custom this rule 
        return recvHex.startsWith(sendHex);
    }
}
