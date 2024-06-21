package com.jolimark.printer.protocol;

import com.jolimark.printer.bean.PrinterInfo;
import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.util.ByteArrayUtil;
import com.jolimark.printer.util.LogUtil;

import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;

public class Comm extends CommBase {
    private final String TAG = getClass().getSimpleName();

    public Comm(TransBase transBase) {
        super(transBase);
    }

    public boolean connect() {
        if (!transBase.connect()) {
            return false;
        }
        if (config.enableVerification && !printerVerification()) {
            return false;
        }
        return true;
    }


    private boolean verify() {
        LogUtil.i(TAG, "printer verification.");
        long SurValue = 0L;
        long DesValue = 0L;
        Random rnd = new Random();
        byte[] nums = new byte[4];

        for (int i = 0; i <= 3; ++i) {
            int p = (int) ((long) Math.abs(rnd.nextInt(255)) * System.currentTimeMillis() % 256L);
            nums[i] = (byte) p;
        }

        byte[] cmd = new byte[]{27, 29, 30, 4, 15, 4, nums[0], nums[1], nums[2], nums[3], 27, 29, 31};
        CRC32 cc = new CRC32();

        try {
            cc.update(nums);
        } catch (Exception var19) {
            var19.printStackTrace();
        }

        SurValue = cc.getValue();
        if (!this.transBase.sendData(cmd)) {
            return false;
        } else {
            LogUtil.i(TAG, "send : " + ByteArrayUtil.toArrayString(cmd, cmd.length));

            try {
                Thread.sleep(800L);
            } catch (InterruptedException var18) {
                var18.printStackTrace();
            }

            byte[] receiveData = new byte[128];
            int receiveSize = this.receiveDataPrivate(receiveData);
            LogUtil.i(TAG, "receive : " + ByteArrayUtil.toArrayString(receiveData, receiveSize));
            if (receiveSize < 2) {
                LogUtil.i(TAG, "receive timeout.");
                MsgCode.setLastErrorCode(3);
                return false;
            } else if (receiveSize - 2 != receiveData[1]) {
                LogUtil.i(TAG, "receive len not right.");
                MsgCode.setLastErrorCode(1);
                return false;
            } else {
                DesValue = ((long) receiveData[2] & 255L) + (((long) receiveData[3] & 255L) << 8) + (((long) receiveData[4] & 255L) << 16) + (((long) receiveData[5] & 255L) << 24);
                if (DesValue != SurValue) {
                    LogUtil.i(TAG, "crc verify fail.");
                    MsgCode.setLastErrorCode(2);
                    return false;
                } else if (receiveData[0] < 0) {
                    LogUtil.i(TAG, "get printer type fail.");
                    MsgCode.setLastErrorCode(4);
                    return false;
                } else {
                    int type = 0;
                    String typeStr = "";
                    if (receiveData[0] == 0) {
                        type = 11;
                        typeStr = "24 dot";
                    } else if (receiveData[0] == 1) {
                        type = 10;
                        typeStr = "9 dot";
                    } else if (receiveData[0] == 2) {
                        type = 12;
                        typeStr = "terminal";
                    } else if (receiveData[0] >= 3) {
                        typeStr = "ink";
                    }

                    LogUtil.i(TAG, "printer type: " + typeStr);
                    int clientCode = ((receiveData[6] & 255) << 8) + (receiveData[7] & 255);
                    LogUtil.i(TAG, "client code： " + clientCode);
                    int printModeStringLength = receiveSize - 9;
                    byte[] temp = new byte[printModeStringLength];

                    for (int i = 0; i < printModeStringLength; ++i) {
                        temp[i] = receiveData[9 + i];
                    }

                    String printerModel = new String(temp);
                    LogUtil.i(TAG, "printer model： " + printerModel);
                    printerInfo = new PrinterInfo();
                    printerInfo.printerType = type;
                    printerInfo.printerModel = printerModel;
                    printerInfo.clientCode = clientCode;

                    if (config.clientCode != 0 && config.clientCode != clientCode) {
                        LogUtil.i(TAG, "client code not match.");
                        MsgCode.setLastErrorCode(MsgCode.ER_PRINTER_VERIFY);
                        return false;
                    }

                    LogUtil.i(TAG, "printer verify success.");
                    return true;
                }
            }
        }
    }

    private boolean printerVerification() {
        return this.verify() || this.verify();
    }


    private int receiveDataPrivate(byte[] SData) {
        int rtnValue = 0;

        for (int counter = 1; counter <= 5; ++counter) {
            rtnValue = this.transBase.receiveData(SData, 1000);
            if (rtnValue > 0) {
                break;
            }
        }

        return rtnValue;
    }

    @Override
    public void disconnect() {
        transBase.disconnect();
    }

    @Override
    public void release() {
        transBase.release();
    }

    @Override
    public boolean sendData_(List<byte[]> bytesList) {
        for (int i = 0; i < bytesList.size(); ++i) {
            LogUtil.i(TAG, "send package " + i + ".");
            byte[] array = (byte[]) bytesList.get(i);
            if (!this.transBase.sendData(array)) {
                return false;
            }
            try {
                Thread.sleep(config.sendDelay);
            } catch (InterruptedException var5) {
                var5.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public int receiveData(byte[] buff, int timeout) {
        return transBase.receiveData(buff, 100);
    }

}
