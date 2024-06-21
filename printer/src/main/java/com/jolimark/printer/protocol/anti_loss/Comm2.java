package com.jolimark.printer.protocol.anti_loss;


import static com.jolimark.printer.protocol.anti_loss.Comm2.Task.STAGE_IDLE;
import static com.jolimark.printer.protocol.anti_loss.Comm2.Task.STAGE_SEND_TASK;

import com.jolimark.printer.bean.PrinterInfo;
import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.protocol.CommBase;
import com.jolimark.printer.protocol.anti_loss.object.PrinterStatusInfo;
import com.jolimark.printer.protocol.anti_loss.object.ReceiveHolder;
import com.jolimark.printer.protocol.anti_loss.object.ReceivePackageInfo;
import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.util.ByteArrayUtil;
import com.jolimark.printer.util.LogUtil;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.CRC32;

public class Comm2 extends CommBase {
    private final String TAG = "Comm2";
    /**
     * 接收超时
     */
    private final int RECEIVE_TIMEOUT = 7000;

    private Task task;

    public Comm2(TransBase transBase) {
        super(transBase);
    }


    @Override
    public boolean connect() {
        if (!transBase.connect()) {
            return false;
        }
        if (config.enableVerification && !printerVerification()) {
            return false;
        }
        return true;
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
        LogUtil.i(TAG, "PRINT");
        task = new Task();
        task.byteArrayList = bytesList;
        task.setMode(task.MODE_PRINT);
        boolean ret = next1(task.stage);
        return ret;
    }

    public boolean resumeSend() {
        if (task == null || task.isFinish()) {
            MsgCode.setLastErrorCode(MsgCode.ER_BIDIRECTIONAL_STAGE_ERROR_RESUME_PRINT);
            return false;
        }
        LogUtil.i(TAG, "RESUME PRINT");
        task.setMode(task.MODE_RESUME_PRINT);
        boolean ret = next1(task.stage);
        return ret;
    }

    public boolean resend() {
        if (task == null) {
            MsgCode.setLastErrorCode(MsgCode.ER_BIDIRECTIONAL_STAGE_ERROR_REPRINT);
            return false;
        }
        LogUtil.i(TAG, "REPRINT");
        //打印任务重置
        task.setMode(task.MODE_REPRINT);
        boolean ret = next1(task.stage);
        return ret;
    }


    /**
     * 获取双向打印机类型，并校验打印机是否是映美打印机
     * <p>
     * 打印机返回：TAG n d1 … dn
     * <p>
     * TAG为类别，00h=24针，01h=9针，02h=热敏。
     * n为内容d的字节数（最好控制在十几字节以内）。
     * d的前4个字节为CRC32校验码，低字节在前，其后为2字节的客户编码（默认为0），接着为1字节的型号字符串长度，最后是型号字符串。
     *
     * @return
     */
    public boolean printerVerification() {
        LogUtil.i(TAG, "printer verification.");
        long recValue = 0;//与打印机连接绑定的CRC校验码，在发送包的第6~9位
        long SurValue = 0;//包完整校验的CRC校验码，对发送包的第0~9位的校验，打印机接收时用于校验包是否完整
        long DesValue = 0;//打印机返回的连接绑定的CRC检验码，在接收包的内容区域（不是接收包的包完整校验码）
        int contentSize = 0;
        byte contentData[] = null;

        // 旧的获取打印机类型数据包格式
        Random rnd = new Random();
        byte[] nums = new byte[4];
        int p;
        for (int i = 0; i <= 3; i++) {
            p = (int) (Math.abs(rnd.nextInt(255)) * System.currentTimeMillis() % 256);
            nums[i] = (byte) p;
        }

        // 封装数据包 1A 16 SEQ N1 N2  CHK1 CHK2 CHK3 CHK4
//        packageNum = rnd.nextInt(254);
        updatePackageSeq();
        byte[] cmd = new byte[13];
        cmd[0] = 0x1A; // 包头
        cmd[1] = 0x16;
        cmd[2] = (byte) task.packageSeq; // 包序号
        cmd[3] = 0x08; // 长度 包括数据D1…Dn和校验CHK1…CHK4
        cmd[4] = 0x00;
        cmd[5] = nums[0];// 4字节CRC
        cmd[6] = nums[1];
        cmd[7] = nums[2];
        cmd[8] = nums[3];

        // 计算整包校验码
        CRC32 cc = new CRC32();
        try {
            cc.update(cmd, 0, 9);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SurValue = cc.getValue();

        //计算连接绑定校验码
        CRC32 cc1 = new CRC32();
        try {
            cc1.update(cmd, 5, 4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        recValue = cc1.getValue();

        //包完整校验码，按低字节在前保存在包的最后4字节里
        cmd[9] = (byte) (SurValue & 0xFF);//取第4字节
        cmd[10] = (byte) ((SurValue >> 8) & 0xFF);//取第3字节
        cmd[11] = (byte) ((SurValue >> 16) & 0xFF);//取第2字节
        cmd[12] = (byte) ((SurValue >> 24) & 0xFF);//取第1字节

        LogUtil.i(TAG, "send : " + ByteArrayUtil.toArrayString(cmd, cmd.length));


        // 发送指令包，返回发送长度
        if (!transBase.sendData(cmd))
            return false;

        //接收数据并返回处理结果
        ReceivePackageInfo info = receivePackageInfo();
        task.packageInfo = info;
        if (!PacketEngine.checkPackageResponse(info)) {
            LogUtil.i(TAG, "printer verification fail.");
            return false;
        }

        contentData = info.contentData;
        //打印机返回信息内容为空
        if (contentData == null) {
            LogUtil.i(TAG, "printer verification fail.");
            return false;
        }

        contentSize = contentData.length;
        if (contentSize < 2) {
            LogUtil.i(TAG, "printer verification fail.");
            return false;
        }

        //返回内容长度减去前两位后，剩余长度不足长度位
        if (contentSize - 2 != contentData[1]) {
            return false;
        }

        DesValue = ((long) contentData[2] & 0xFF)
                + (((long) contentData[3] & 0xFF) << 8)
                + (((long) contentData[4] & 0xFF) << 16)
                + (((long) contentData[5] & 0xFF) << 24);

        if (DesValue != recValue) {
            LogUtil.i(TAG, "printer verification not pass.");
            return false;
        }

        //打印机类型
        if (contentData[0] < 0) {
            LogUtil.i(TAG, "get printer type fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_PRINTER_TYPE);
            return false;
        }

        int type = 0;
        String typeStr = "";
        if (contentData[0] == 0) {
            type = PrinterInfo.PRINTER_TYPE_24DOT;
            typeStr = "24 dot";
        } else if (contentData[0] == 1) {
            type = PrinterInfo.PRINTER_TYPE_9DOT;
            typeStr = "9 dot";
        } else if (contentData[0] == 2) {
            type = PrinterInfo.PRINTER_TYPE_THERMAL;
            typeStr = "terminal";
        } else if (contentData[0] >= 3) {
            typeStr = "ink";
        }

        LogUtil.i(TAG, "printer type: " + typeStr);


        int clientCode = (contentData[6] << 8) + (contentData[7]);
        LogUtil.i(TAG, "client code： " + clientCode);

        //直接取数组中除去前面固定位后剩余的数据，则是打印机型号的字符串
        int printModeStringLength = contentSize - 9;
        byte[] temp = new byte[printModeStringLength];
        for (int i = 0; i < printModeStringLength; i++) {
            temp[i] = contentData[9 + i];
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


        LogUtil.i(TAG, "printer verification finish.");
        return true;
    }

    private byte[] packData(byte[] data) {
        return PacketEngine.pack(data, task.packageSeq);
    }

    /**
     * 计算包序号
     * 包序号（从0开始，顺序增加，达到253后复位为0，重发的数据包序号不变，255预留，254校验）包序号（从0开始，顺序增加，达到253后复位为0，重发的数据包序号不变，255预留，254校验）
     */
    private void updatePackageSeq() {
        if (task.packageSeq < 253) {
            task.packageSeq++;
        } else {//超过253置0
            task.packageSeq = 0;
        }
    }

    private void resetPackageSeq() {
        task.packageSeq = 0;
    }


    @Override
    public int receiveData(byte[] buff, int timeout) {
        return 0;
    }

    public PrinterStatusInfo getPrinterStatusInfo() {
        LogUtil.i(TAG, "get printer status info.");

        byte[] data = {0x10, 0x04, 0x10};
        if (!transBase.sendData(data)) {
            LogUtil.i(TAG, "get printer status fail.");
            return null;
        }
        byte[] buffer = new byte[32];
        int len = transBase.receiveData(buffer, 5000);
        if (len <= 0) {
            return null;
        }
        if (len > 1) {
            MsgCode.setLastErrorCode(MsgCode.ER_PRINTER_RECEIVE_WRONG);
            LogUtil.i(TAG, "get printer status response error.");
            return null;
        }
        PrinterStatusInfo info = PacketEngine.AnalyseStatusData(buffer[0]);
        return info;
//        if (task == null)
//            getPrinterStatus();
//        return task.statusInfo;
    }


    class Task {
        static final int STAGE_SWITCH_PROTOCOL = 1;
        static final int STAGE_CHECK_PRINTER_STATUS = 2;
        static final int STAGE_CLEAR_CACHE = 3;
        static final int STAGE_RESET_PACKAGE_NUM = 4;
        static final int STAGE_START_TASK = 5;
        static final int STAGE_SEND_TASK = 6;
        static final int STAGE_END_TASK = 7;
        static final int STAGE_SWITCH_PROTOCOL_2 = 8;
        static final int STAGE_IDLE = 9;

        //打印数据分包队列
        List<byte[]> byteArrayList;
        //通讯流程阶段
        int stage;
        int pre_stage;
        //该次通讯中当前包的序号
        int packageSeq;
        //分包发送中当前发送包的序号
        int byteArrayIndex;
        //前一次通讯返回包信息
        ReceivePackageInfo packageInfo;
        PrinterStatusInfo statusInfo;

        int tryCount;

        public Task() {
            stage = STAGE_SWITCH_PROTOCOL;
            mode = MODE_PRINT;
            tryCount = 3;
        }

        public boolean isFinish() {
            if (stage == STAGE_IDLE) return true;
            return false;
        }


        int mode;//任务类型
        final int MODE_PRINT = 100;//正常打印
        final int MODE_RESUME_PRINT = 101;//续打
        final int MODE_REPRINT = 102;//重打

        public void setMode(int mode) {
            LogUtil.i(TAG, toString());
            if (mode == MODE_RESUME_PRINT) {
                if (pre_stage == 0) {
                    //如果上次打印在切换防丢单或者查询状态阶段就出错，那么续打的下一步直接定位到清空缓存，
                    //因为续打本来也需要切换防丢单与查询状态
                    if (stage == STAGE_SWITCH_PROTOCOL || stage == STAGE_CHECK_PRINTER_STATUS)
                        pre_stage = STAGE_CLEAR_CACHE;
                    else
                        pre_stage = stage;
                }
                stage = STAGE_SWITCH_PROTOCOL;
            } else if (mode == MODE_REPRINT) {
                byteArrayIndex = 0;
                stage = STAGE_SWITCH_PROTOCOL;
                resetTryCount();
            } else if (mode == MODE_PRINT) {
                stage = STAGE_SWITCH_PROTOCOL;
                resetTryCount();
            }
            this.mode = mode;
            LogUtil.i(TAG, toString());
        }

        @Override
        public String toString() {
            return "Task{" +
                    "stage=" + stage +
                    ", pre_stage=" + pre_stage +
                    ", packageSeq=" + packageSeq +
                    ", byteArrayIndex=" + byteArrayIndex +
                    ", mode=" + mode +
                    '}';
        }

        public void resetTryCount() {
            tryCount = 3;
        }
    }

    private boolean next1(int stage) {
        boolean ret = false;
        switch (stage) {
            case Task.STAGE_SWITCH_PROTOCOL: {
                //进入防丢单模式
                if (switchProtocol(true)) {
                    ret = true;
                    task.stage = Task.STAGE_CHECK_PRINTER_STATUS;
                }
                break;
            }
            case Task.STAGE_CHECK_PRINTER_STATUS: {
                //检查打印机状态
                if (getPrinterStatus()) {
                    ret = true;
                    LogUtil.i(TAG, "mode -> " + task.mode);
                    if (task.mode == task.MODE_RESUME_PRINT) {
                        task.stage = task.pre_stage;
                        task.pre_stage = 0;
                    } else
                        task.stage = Task.STAGE_CLEAR_CACHE;
                }

                break;
            }
            case Task.STAGE_CLEAR_CACHE: {
                //清除打印机缓存
                if (cleanPrinterCache()) {
                    ret = true;
                    task.stage = Task.STAGE_RESET_PACKAGE_NUM;
                }
                break;
            }
            case Task.STAGE_RESET_PACKAGE_NUM: {
                //重置打印机包序号
                if (resetPackageNum()) {
                    ret = true;
                    task.stage = Task.STAGE_START_TASK;
                }
                task.stage = Task.STAGE_START_TASK;
                break;
            }
            case Task.STAGE_START_TASK: {
                //开始任务
                if (startTask()) {
                    ret = true;
                    task.stage = Task.STAGE_SEND_TASK;
                }
                break;
            }
            case Task.STAGE_SEND_TASK: {
                //发送数据
                if (sendTask(task.byteArrayList)) {
                    ret = true;
                    task.stage = Task.STAGE_END_TASK;
                }
                break;
            }
            case Task.STAGE_END_TASK: {
                //结束任务
                if (endTask()) {
                    ret = true;
                    task.stage = Task.STAGE_SWITCH_PROTOCOL_2;
                }
                break;
            }
            case Task.STAGE_SWITCH_PROTOCOL_2: {
                //切换非防丢单
                if (switchProtocol(false)) {
                    task.stage = STAGE_IDLE;
                }
                return true;
            }
            default: {
                LogUtil.i(TAG, "stage -> null");
                return false;
            }
        }


        if (!ret) {
            return dealError1();
        }

        updatePackageSeq();
        //阶段成功则重置重试次数
        task.resetTryCount();
        return next1(task.stage);
    }

    private boolean dealError1() {
        if (task != null) {
            ReceivePackageInfo packageInfo = task.packageInfo;
            if (packageInfo != null) {
                //通讯异常，在重试次数范围内，自动重试
                if (packageInfo.isCommunicationAbnormal()) {
                    //包序号重复，在序号正常设置的情况下，即打印机收到重复的包，应更新包序号，并发送下一个内容
                    if (packageInfo.result == ReceivePackageInfo.STATUS_SAME_PACKET_NUM) {
                        updatePackageSeq();
                        task.resetTryCount();
                        if (task.stage == STAGE_SEND_TASK) {
                            task.byteArrayIndex++;
                        } else if (task.stage < STAGE_IDLE) task.stage++;
                    }
                    //打印机缓存满，等待若干时间再试
                    else if (packageInfo.result == ReceivePackageInfo.STATUS_RECEIVE_BUFFER_FULL) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    //重试次数内，则重试
                    if (task.tryCount > 0) {
                        task.tryCount--;
                        LogUtil.i(TAG, "retry , remain " + task.tryCount + " round");
                        dropReceiveBuff();
                        return next1(task.stage);
                    }
                }
                //打印机状态异常，通常需要手动调整打印机后再打印，直接退出流程，后续可使用续打功能
                if (packageInfo.isPrinterAbnormal()) {
                    //虽然异常，一般数据包是收到了，包序号更新
                    updatePackageSeq();
                }
            } else {//PackageInfo为空，即打印机回复包异常，不清楚打印机是否收到当前包，选择重发
                //重试次数内，则重试
                if (task.tryCount > 0) {
                    task.tryCount--;
                    LogUtil.i(TAG, "retry , remain " + task.tryCount + " round");
                    dropReceiveBuff();
                    return next1(task.stage);
                }
            }
        }
        return false;
    }


    /**
     * 重试流程时，先清空一下接收缓存的数据，避免之前的返回数据影响后续的接收判断
     */
    private void dropReceiveBuff() {
        int recLength = transBase.receiveData(new byte[1024], 1000);
        LogUtil.i(TAG, "dropReceiveBuff -> " + recLength + " bytes");
    }


    /**
     * 切换打印机通讯协议
     * 用于设置打印机本次打印是否使用《双向通讯协议》，
     * 1A 15 SEQ 05 00 D1 CK1 CH2 CH3 CH4 ，D1为1时使用，0不使用。
     *
     * @param isBidirectional
     * @return
     */
    public boolean switchProtocol(boolean isBidirectional) {
        LogUtil.i(TAG, "STAGE: PRINTER SWITCH " + (isBidirectional ? "BIDIRECTIONAL" : "UNIDIRECTIONAL") + " PROTOCOL.");
        byte[] cmd = PacketEngine.pack(new byte[]{(byte) (isBidirectional ? 0x01 : 0x00)}, task.packageSeq, 3);
        LogUtil.i(TAG, "send : " + ByteArrayUtil.toArrayString(cmd, cmd.length));

        //发送切换指令
        if (!transBase.sendData(cmd)) {
            LogUtil.i(TAG, "printer switch protocol fail.");
            return false;
        }

        //接收数据并返回处理结果
        ReceivePackageInfo info = receivePackageInfo();
        task.packageInfo = info;
        if (!PacketEngine.checkCommunication(info)) {
            LogUtil.i(TAG, "printer switch protocol fail.");
            return false;
        }

        LogUtil.i(TAG, "printer switch " + (isBidirectional ? "bidirectional" : "unidirectional") + " protocol success.");
        return true;
    }

    /**
     * 查询打印机状态
     *
     * @return
     */
    private boolean getPrinterStatus() {
        LogUtil.i(TAG, "STAGE: GET PRINTER STATUS.");
        byte[] data = {0x10, 0x04, 0x10};
        byte[] cmd = PacketEngine.pack(data, task.packageSeq, 1);
        LogUtil.i(TAG, "send cmd(get printer status)： " + ByteArrayUtil.toArrayString(cmd, cmd.length));

        if (!transBase.sendData(cmd)) {
            LogUtil.i(TAG, "get printer status fail.");
            return false;
        }

        ReceivePackageInfo info = receivePackageInfo();
        task.packageInfo = info;
        if (!PacketEngine.checkCommunication(info)) {
            LogUtil.i(TAG, "get printer status fail.");
            return false;
        }
        boolean ret = false;
        byte[] content = info.contentData;
        if (content != null) {
            PrinterStatusInfo statusInfo = PacketEngine.AnalyseStatusData(content[0]);
            task.statusInfo = statusInfo;
            LogUtil.i(TAG, statusInfo.toString());
            ret = statusInfo.isNormal();
        } else
            LogUtil.i(TAG, "printer status null");

        LogUtil.i(TAG, "printer status " + (ret ? "normal" : "abnormal"));
        return ret;
    }


    public boolean cleanPrinterCache() {
        LogUtil.i(TAG, "clean printer cache.");
        byte[] data = {0x10, 0x14, 0x08, 0x01, 0x03, 0x14, 0x01, 0x06, 0x02, 0x08};
        byte[] cmd = PacketEngine.pack(data, task.packageSeq, 2);


        LogUtil.i(TAG, "send ： " + ByteArrayUtil.toArrayString(cmd, cmd.length));

        if (!transBase.sendData(cmd)) {
            LogUtil.i(TAG, "clean printer cache fail.");
            return false;
        }

        ReceivePackageInfo info = receivePackageInfo();
        task.packageInfo = info;
        if (!PacketEngine.checkCommunication(info)) {
            LogUtil.i(TAG, "clean printer cache fail.");
            return false;
        }

        LogUtil.i(TAG, "clean printer cache success.");
        return true;
    }

    private boolean resetPackageNum() {
        LogUtil.i(TAG, "STAGE: RESET PACKAGE NUMBER.");
        task.packageSeq = 0xFF;
        byte[] cmd = PacketEngine.pack(new byte[]{}, task.packageSeq, 1);
        LogUtil.i(TAG, "send ： " + ByteArrayUtil.toArrayString(cmd, cmd.length));

        if (!transBase.sendData(cmd)) {
            LogUtil.i(TAG, "reset package number fail.");
            return false;
        }
        ReceivePackageInfo info = receivePackageInfo();
        task.packageInfo = info;
        if (!PacketEngine.checkCommunication(info)) {
            LogUtil.i(TAG, "reset package number fail.");
            return false;
        }

        LogUtil.i(TAG, "reset package number success.");
        return true;
    }


    private boolean startTask() {
        LogUtil.i(TAG, "STAGE: TASK(START).");
        byte[] cmd = new byte[]{0x1b, 0x1d, 0x1e, 0x05, 0x06, 0x1b, 0x1d, 0x1f};
        byte[] packageData = packData(cmd);
        LogUtil.i(TAG, "send : " + ByteArrayUtil.toArrayString(cmd, cmd.length));
        if (!transBase.sendData(packageData)) {
            LogUtil.i(TAG, "send cmd(start) fail.");
            return false;
        }
        ReceivePackageInfo info = receivePackageInfo();
        task.packageInfo = info;
        if (!PacketEngine.checkCommunication(info)) {
            LogUtil.i(TAG, "task(start) fail.");
            return false;
        }
        LogUtil.i(TAG, "task(start) finished.");
        return true;
    }

    private boolean sendTask(List<byte[]> arrays) {
        LogUtil.i(TAG, "STAGE: TASK(SEND DATA).");
        byte[] array;
        //从当前包开始发送，包括了从头开始与中途继续
        for (int i = task.byteArrayIndex; i < arrays.size(); i++) {
            array = packData(arrays.get(i));
            LogUtil.i(TAG, "send : [byteArrayIndex " + task.byteArrayIndex + ", packageSeq " + task.packageSeq + "]");
            if (!transBase.sendData(array)) {
                LogUtil.i(TAG, "send cmd(send data) fail.");
                return false;
            }
            ReceivePackageInfo info = receivePackageInfo();
            task.packageInfo = info;
            if (!PacketEngine.checkCommunication(info)) {
                LogUtil.i(TAG, "task(send data) fail.");
                return false;
            }
            //只要通信正常即当前包已经发送成功，下标应该指向下一包
            if (i < arrays.size() - 1) {
                //发送成功才能更新包序号
                task.byteArrayIndex++;
                updatePackageSeq();
                LogUtil.i(TAG, "byteArrayIndex " + task.byteArrayIndex + ", packageSeq " + task.packageSeq);
            }
            if (!PacketEngine.checkPrinterStatus(info)) {
                LogUtil.i(TAG, "task(send data) fail.");
                return false;
            }
        }
        LogUtil.i(TAG, "task(send data) finished.");
        return true;
    }

    private boolean endTask() {
        LogUtil.i(TAG, "STAGE: TASK(END).");
        byte[] cmd = new byte[]{0x1b, 0x1d, 0x1e, 0x05, 0x07, 0x1b, 0x1d, 0x1f};
        byte[] packageData = packData(cmd);
        LogUtil.i(TAG, "send : " + ByteArrayUtil.toArrayString(cmd, cmd.length));

        if (!transBase.sendData(packageData)) {
            LogUtil.i(TAG, "send cmd(end) fail.");
            return false;
        }

        ReceivePackageInfo info = receivePackageInfo();
        task.packageInfo = info;
        if (!PacketEngine.checkCommunication(info)) {
            LogUtil.i(TAG, "task(end) fail.");
            return false;
        }
        LogUtil.i(TAG, "task(end) finished.");
        return true;
    }


    /**
     * 通信结果应答包
     * 1A 18 SEQ N1 N2 D1 … Dn CHK1 CHK2 CHK3 CHK4
     * <p>
     * 说明：
     * 1A 18:包头
     * SEQ：包序号（与发送包一致）
     * N1，N2: 长度（包括result、CHK1…CHK4，两个字节，低字节在前）
     * D1…Dn：返回结果数据，其中D1为通信结果，00为正确，其它值为错误代码。D2为打印机状态，各位定义如下：
     * Bit0 – 1=联机，0=脱机
     * Bit1 – 1=钱箱打开，0=钱箱闭合
     * Bit2 – 1=上盖打开，0=上盖闭合
     * Bit3 – 1=纸尽，0=正常
     * Bit4 – 1=纸将尽，0=正常
     * Bit5 – 1=切刀故障，0=切刀正常
     * Bit6 – 1=其它故障，0=无其它故障
     * CHK1 CHK2 CHK3 CHK4：包头至结果内容的CRC32校验，低字节在前
     */
    private ReceivePackageInfo receivePackageInfo() {
        ReceivePackageInfo info = null;
        byte[] buffer = new byte[1024];

        boolean flag_locateHeader = false;
        boolean flag_getPackageLength = false;
        boolean flag_decodePackageContent = false;

//        byte[] dataBuffer = null;

        ReceiveHolder rHolder = new ReceiveHolder();

//        index = 0;
//        task.packageOffset = 0;

//        loop_receiveData = true;
        //初始计时，超时没有收到数据，则结束接收
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                rHolder.r_loop = false;
            }

        }, RECEIVE_TIMEOUT);

        while (rHolder.r_loop) {

            //读取通讯数据
            int recLength = transBase.receiveData(buffer, 5000);
            //读取不到数据，则继续读
            if (recLength == 0 || recLength == -1) {
                continue;
            }
            //读取失败，连接有问题
            else if (recLength == -2) {
                break;
            }
            //截取接收buffer中有效的数据部分
            byte[] receiveData = ByteArrayUtil.subArray(buffer, 0, recLength);
            LogUtil.i(TAG, "receive ： " + ByteArrayUtil.toArrayString(receiveData, receiveData.length));

            rHolder.dataBuffer = ByteArrayUtil.mergeArrays(rHolder.dataBuffer, receiveData);

            //定位包头
            if (!flag_locateHeader) {
                if (!PacketEngine.locateHeader(rHolder))
                    continue;
                flag_locateHeader = true;
            }

            //解析包长度
            if (!flag_getPackageLength) {
                int restLength = rHolder.dataBuffer.length - rHolder.index;
                if (restLength < 3) {
                    continue;
                }
                PacketEngine.getPackageLength(rHolder);
                flag_getPackageLength = true;
            }

            //获取完整数据包
            int restLength = rHolder.dataBuffer.length - rHolder.index;
            if (restLength < rHolder.packageLength) {
                continue;
            }
            info = PacketEngine.decodeContent(rHolder, task.packageSeq);
            if (info != null) {
                flag_decodePackageContent = true;
                break;
            }
        }
        timer.cancel();

        if (!rHolder.r_loop && !flag_decodePackageContent) {
            MsgCode.setLastErrorCode(MsgCode.ER_PRINTER_RECEIVE_TIMEOUT);
            LogUtil.i(TAG, "handle response packet -> receive timeout");
        }

        return info;
    }

}
