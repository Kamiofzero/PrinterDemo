package com.jolimark.printer.util;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageTransformer {

    /**
     * 9针机打印头点很疏，所以打印图形时，分图形的每大行分为奇偶两分行，即偶数点与奇数点
     * 先打偶数行，然后下移一单位，打奇数行
     * 这样就可以使打印的图形密度增大，精度更高
     * <p>
     * 把Bitmap转换成9针打印数据
     */
    public static byte[] imageToData_9dot(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int x;
        int y;
        int h1;
        int h2;
        int ColorInt;
        int ColorValue;
        int PosBuf;
        byte[] data = new byte[0];

        byte[] buffer = new byte[width + 1024];
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();

            int row_1 = height / 16;
            //保证最后不足一大行的部分也被处理
            if (height % 16 > 0) {
                row_1++;
            }

            //每大行
            for (h1 = 0; h1 < row_1; h1++) {

                //分行开始
                int i;
                for (i = 0; i < 2; i++) {

                    PosBuf = 0;
                    //图形指令头
                    buffer[PosBuf++] = 0x1B;
                    buffer[PosBuf++] = 0x2A;
                    buffer[PosBuf++] = 1;
                    //行宽度
                    buffer[PosBuf++] = (byte) (width & 0xFF);
                    buffer[PosBuf++] = (byte) (width >> 8);

                    for (x = 0; x < width; x++) {
                        ColorValue = 0;
                        //i为0时是偶数行，i为1时是奇数行
                        for (h2 = i; h2 < 16; h2 += 2) {
                            y = h1 * 16 + h2;
                            if (y >= height) {
                                ColorInt = 0x00FFFFFF;
                            } else {
                                ColorInt = bitmap.getPixel(x, y) & 0x00FFFFFF;
                            }
                            ColorValue = ColorValue << 1;
                            if (ColorInt <= 0xE0E0E0) {
                                ColorValue = ColorValue + 1;
                            }
                        }
                        buffer[PosBuf++] = (byte) ColorValue;
                    }

                    buffer[PosBuf++] = 0x0D;
                    //打印头走纸一个单位
                    buffer[PosBuf++] = 0x1B;
                    buffer[PosBuf++] = 0x4A;
                    buffer[PosBuf++] = (byte) (i == 0 ? 0x01 : 0x0f);

                    baos.write(buffer, 0, PosBuf);

                }//分行结束

            } // 每大行

            baos.flush();
            data = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bitmap.recycle();
        return data;
    }

    /**
     * 热敏、24针图形转换
     *
     * @param bitmap
     * @return
     */
    public static byte[] imageToData(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int x = 0;
        int y = 0;
        int h1 = 0;
        int h2 = 0;
        int h3 = 0;
        int ColorInt = 0;
        int ColorValue = 0;

        int PosBuf = 0;
        int tmpPos = 0;


        int LineTailBlankNum;
        boolean LineBlankFlag = true;
        boolean DotBlankFlag = true;

        //打印机逐行打印，每行打印高度为24像素点，此行称为打印行

        //buffer用于存储打印行数据，即24像素 * 点阵宽
        byte[] buffer = new byte[(width + 23) * 24 / 8 + 1024];

        ByteArrayOutputStream baos = new ByteArrayOutputStream();


        //纵向按24像素划分打印行，打印行数量为（点阵高/24）
        for (h1 = 0; h1 <= (height + 23) / 24 - 1; h1++) {
            //标记打印行是否为空白
            LineBlankFlag = true;
            DotBlankFlag = true;
            //记录打印行末尾连续空白列数，用于打印行宽度的确定
            LineTailBlankNum = 0;

            tmpPos = 0;
            PosBuf = 0;

            //打印行数据初始字节为图片打印格式头部
            buffer[PosBuf++] = 0x1B;
            buffer[PosBuf++] = 0x2A;
            buffer[PosBuf++] = 39;

            //记录打印行宽度字节位置，方便后续更改
            tmpPos = PosBuf;

            //存储打印行宽度字节（两字节）
            buffer[PosBuf++] = (byte) (width & 0xFF);
            buffer[PosBuf++] = (byte) (width >> 8);

            //对打印行按列横向扫描，列数量为点阵宽
            for (x = 0; x <= width - 1; x++) {
                DotBlankFlag = true;
                //打印行每列24个像素点，每8个点的数据存储为一个字节，以此划分3份
                for (h2 = 0; h2 <= 2; h2++) {
                    ColorValue = 0;
                    //扫描每份的8个点
                    for (h3 = 0; h3 <= 7; h3++) {
                        //根据当前纵向划分，计算点的y坐标
                        y = h1 * 24 + h2 * 8 + h3;
                        if (y >= height) {
                            ColorInt = 0x00FFFFFF;
                        } else {
                            //根据当前点的x，y坐标，获取该点图像信息
                            ColorInt = bitmap.getPixel(x, y) & 0x00FFFFFF;
                        }

                        //移位，使最低位空出，存储当前点的数据
                        ColorValue = ColorValue << 1;

					/*	if (ColorInt <= 0xE0E0E0) {
							ColorValue = ColorValue + 1;
							LineBlankFlag = false;
							LineTailBlankNum = 0;
						}
                    */

                        //根据当前点的图像信息，计算打印机是否打印该点
                        int red = ((ColorInt & 0x00FF0000) >> 16);
                        int green = ((ColorInt & 0x0000FF00) >> 8);
                        int blue = (ColorInt & 0x000000FF);
                        int grey = (red * 19595 + green * 38469 + blue * 7472) >> 16;
//                        if (grey <= 230) {
                        if (grey <= 190) {
                            ColorValue = ColorValue + 1;
                            //出现一个点不为空白，则该打印行不为空白
                            LineBlankFlag = false;
                            LineTailBlankNum = 0;
                        }


                    } // end h3

                    // 每8个点为一字节
                    buffer[PosBuf++] = (byte) ColorValue;
                    if (ColorValue != 0) {
                        DotBlankFlag = false;
                    }

                } // end h2

                //如果该列都是空白点，则记录入末尾空白列数量
                if (DotBlankFlag) {
                    LineTailBlankNum++;
                }
                //如果遇到一列不为空白，则之前记录清空
                else {
                    LineTailBlankNum = 0;
                }

            } // end x

            //如果打印行不为空白，且末尾有连续空白列，则修改打印行实际打印宽度
            if ((!LineBlankFlag) && (LineTailBlankNum > 0)) {
                PosBuf = PosBuf - LineTailBlankNum * 3;
                buffer[tmpPos++] = (byte) ((width - LineTailBlankNum) & 0xFF);
                buffer[tmpPos++] = (byte) ((width - LineTailBlankNum) >> 8);
            }

            //最后加入换行走纸指令
            buffer[PosBuf++] = 0x0D;
            buffer[PosBuf++] = 0x1B;
            buffer[PosBuf++] = 0x4A;
            buffer[PosBuf++] = 24;

            try {
                //如果打印行为空白，则直接走纸
                if (LineBlankFlag) {
                    baos.write(0x1B);
                    baos.write(0x4A);
                    baos.write(24);
                }
                //如果打印行不空白，则把存储的打印数据写入总数据流
                else {
                    baos.write(buffer, 0, PosBuf);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } // end h1
        byte[] data = null;
        try {
            baos.flush();
            data = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bitmap.recycle();
        return data;
    }


}
