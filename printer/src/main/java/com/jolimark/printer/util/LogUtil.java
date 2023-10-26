package com.jolimark.printer.util;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
    private static boolean isDebug = false;
    private static boolean isSaveLocalLog = false;

    private static final String TAG = "tag";

    public static void enable_debug_log(boolean b) {
        isDebug = b;
    }

    public static boolean enable_local_log(Context context, boolean b) {
        if (!b) {
            isSaveLocalLog = false;
            return true;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            i(TAG, "no permission for save local log");
            return false;
        }
        isSaveLocalLog = true;
        return true;
    }


    public static void i(String title, String msg) {
        if (isDebug) {
            android.util.Log.i(TAG, title + " -- " + msg);
        }
        if (isSaveLocalLog) {
            FileWriter fileWriter = null;
            try {
                String time = new SimpleDateFormat("MM-dd HH:mm:ss:SSS ").format(new Date());
                File file = new File("/sdcard/printer_log.txt");
                //日志文件超过1MB后，删除重建
                if (file.exists() && file.length() > 1000000) {
                    file.delete();
                }
                fileWriter = new FileWriter(file, true);
                fileWriter.write(time + msg + "\n");
                fileWriter.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileWriter != null)
                        fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
