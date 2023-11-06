package com.jolimark.printer.direction.anti_loss.object;

public class ReceiveHolder {
    //接收缓存的定位坐标
    public int index;
    //定位接收缓存中包头位置
    public int packageOffset;
    //接收包的长度
    public int packageLength;
    //接收循环标识
    public boolean r_loop = true;
    public byte[] dataBuffer;
}
