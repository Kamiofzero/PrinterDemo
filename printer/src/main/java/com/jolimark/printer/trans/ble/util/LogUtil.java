package com.jolimark.printer.trans.ble.util;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LogUtil {

    private static boolean isDebug;
    private static boolean isRecordLocalFile;
    private String TAG = "tag";
    private String dayStr;

    private String foldDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).toString() + "/JmPrinter";

    private LogPrinter logPrinter;

    private FileWriter fileWriter;

    private Object lock = new Object();

    private static LogUtil logUtil;

    private LogUtil() {
    }

    private static LogUtil getInstance() {
        synchronized (LogUtil.class) {
            if (logUtil == null)
                logUtil = new LogUtil();
        }
        return logUtil;
    }


    public static void setFoldDir(String foldDir) {
        getInstance().foldDir = foldDir;
    }


    public static void setDebug(boolean flag) {
        isDebug = flag;
    }

    public static void setIsRecordLocalFile(boolean flag) {
        isRecordLocalFile = flag;
        if (!isRecordLocalFile) {
            getInstance().closeFileWriter();
        }
    }

    public static void i(String tag, String msg) {
        if (!isDebug)
            return;
        getInstance().info(tag, msg);
    }


    public static void setLogPrinter(LogPrinter logPrinter) {
        getInstance().logPrinter = logPrinter;
    }


    private void openFileWriter() {
        closeFileWriter();

        File dir = new File(foldDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, dayStr + ".txt");

        try {
            fileWriter = new FileWriter(file, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeFileWriter() {
        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                fileWriter = null;
            }
        }
    }


    private void info(String tag, String msg) {
        String text = tag + " -- " + msg;
        Log.i(TAG, text);

        try {
            if (logPrinter != null) {
                logPrinter.onLog(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isRecordLocalFile) {

            Date date = new Date();
            String day = new SimpleDateFormat(" yyyy-MM-dd").format(date);

            synchronized (lock) {
                //如果fileWriter为空，创建一个新的，设置为当前日期
                if (fileWriter == null) {
                    dayStr = day;
                    openFileWriter();
                }
                //如果fileWriter存在，则检测当前日期与记录日期是否一致
                else {
                    //日期不一致，认为不是同一天了，则创建新的文档
                    if (dayStr != null && !dayStr.equals(day)) {
                        dayStr = day;
                        openFileWriter();
                    }
                }
            }

            String time = new SimpleDateFormat("HH:mm:ss:SSS ").format(new Date());
            try {
                String fMsg = tag + " -- " + msg;
                fileWriter.write(time + fMsg + "\n");
                fileWriter.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                closeFileWriter();
            } catch (Exception e) {
                e.printStackTrace();
                closeFileWriter();
            }
        }

    }


    public interface LogPrinter {
        void onLog(String log);
    }
}
