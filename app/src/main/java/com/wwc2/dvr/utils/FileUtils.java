package com.wwc2.dvr.utils;

import android.app.Activity;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.shell.ShellUtils;
import com.wwc2.dvr.DvrApplication;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.ConstantsData;
import com.wwc2.dvr.data.RecordData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * user: wangpeng on 2019/7/26.
 * emai: wpeng@waterworld.com.cn
 */

public class FileUtils {

    private static final int TYPE_AHD25fps     = 1;
    private static final int TYPE_AHD30fps     = 2;
    private static final int TYPE_CVBS_NTSC    = 3;
    private static final int TYPE_CVBS_PAL     = 4;
    private static final int TYPE_1080P25fps   = 5;
    private static final int TYPE_1080P30fps   = 6;
    private static final int TYPE_TVI          = 7;
    private static  int TYPE_CVI          = 8;
    private static final int TYPE_PVI          = 9;


    /**
     * the nand flash storage device.
     */
    public static final int NAND_FLASH = 1;

    /**
     * the nand flash storage device.
     */
    public static final int MEDIA_CARD = 2;

    public static final int MEDIA_USB = 3;
    public static final int MEDIA_USB1 = 4;

    private static final Map<Integer, String> mStorageDevices = new ConcurrentHashMap<>();

    static {
        mStorageDevices.put(NAND_FLASH, "/storage/emulated/0/");//与Main中的路径同步，避免从DVR列表进视频播放不正常的问题。2019-10-31
        mStorageDevices.put(MEDIA_CARD, "/storage/sdcard1/");
        mStorageDevices.put(MEDIA_USB, "/storage/usbotg/");
        mStorageDevices.put(MEDIA_USB1, "/storage/usbotg1/");
    }


    /**
     * 获取设备路径
     */
    public static String getPath(int storage) {
        String env = "";
        String ret = "";

        for (Map.Entry<Integer, String> entry : mStorageDevices.entrySet()) {
            if (storage == entry.getKey()) {
                env = entry.getValue();
                break;
            }
        }
        ret = env;
        return ret;
    }

    /**
     * 获取设备ID
     */
    public static int getDeviceId(String name) {
        int storage = -1;
        if (null != name) {
            for (Map.Entry<Integer, String> entry : mStorageDevices.entrySet()) {
                final int _storage = entry.getKey();
                final String path = getPath(_storage);
                if (null != path) {
                    if (name.startsWith(path)) {
                        storage = _storage;
                        break;
                    }
                }
            }
        }
        return storage;
    }

    public static int readNodeFile(String node) {
        int ret = -1;
        try {
            String prop = "";// 默认值
            BufferedReader reader = new BufferedReader(new FileReader(node));
            prop = reader.readLine();
            if (prop != null) {
                ret = Integer.parseInt(prop);
            }
        } catch (Exception e) {
            LogUtils.e("WPTAG", e.getMessage());
        }

        return ret;
    }

    public static String readTextFile(String realPath) {
        String txt = "";
        File file = new File(realPath);
        if (!file.exists()) {
            System.out.println("File not exist!");
            return null;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(realPath), "UTF-8"));
            String temp;

            while ((temp = br.readLine()) != null) {
                txt += temp;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return txt;
    }

    public static void writeNodeFile(Character n, String node) {
        try {
            FileOutputStream fbp = new FileOutputStream(node);
            fbp.write(n);
            fbp.flush();
            fbp.close();
        } catch (Exception e) {
            LogUtils.e("WPTAG", e.getMessage());
        }
    }

    public static void writeTextFile(String tivoliMsg, String fileName) {
        try {
            byte[] bMsg = tivoliMsg.getBytes();
            FileOutputStream fOut = new FileOutputStream(fileName);
            fOut.write(bMsg);
            fOut.getFD().sync();
            fOut.close();
        } catch (IOException e) {
            //throw the exception
        }

    }
    /**
     * 获取指定文件大小 　　
     */
    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {

            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }

    public static  int changeVideoType(int camera) {
        int ret = 1;
        if (camera == 0) {
            ret = TYPE_CVBS_NTSC;
        } else if (camera == 1) {
            ret = TYPE_CVBS_PAL;
        } else if (camera == 10) {
            ret = TYPE_AHD25fps;
        } else if (camera >= 11 && camera < 18) {
            ret = TYPE_AHD30fps;
        } else if (camera == 18) {
            ret = TYPE_1080P25fps;
        } else if (camera == 19) {
            ret = TYPE_1080P30fps;
        } else if (camera >= 20 && camera < 30) {
            ret = TYPE_TVI;
        } else if (camera >= 30 && camera < 40) {
            ret = TYPE_CVI;
        } else if (camera >= 40 && camera < 50) {
            ret = TYPE_PVI;
        }
        /**
         * 驱动对应的值
         enum CAM_STATUS_TYPE{
         CVBS_NTSC_60HZ = 0,
         CVBS_PAL_50HZ,

         AHD_720P_25HZ = 10,
         AHD_720P_30HZ,
         AHD_720P_50HZ,
         AHD_720P_60HZ,
         AHD_960P_25HZ,
         AHD_960P_30HZ,
         AHD_960P_50HZ,
         AHD_960P_60HZ,
         AHD_1080P_25HZ,
         AHD_1080P_30HZ,

         TVI_720P_25HZ = 20,
         TVI_720P_30HZ,
         TVI_720P_50HZ,
         TVI_720P_60HZ,
         TVI_960P_25HZ,
         TVI_960P_30HZ,
         TVI_960P_50HZ,
         TVI_960P_60HZ,
         TVI_1080P_25HZ,
         TVI_1080P_30HZ,

         CVI_720P_25HZ = 30,
         CVI_720P_30HZ,
         CVI_720P_50HZ,
         CVI_720P_60HZ,
         CVI_960P_25HZ,
         CVI_960P_30HZ,
         CVI_960P_50HZ,
         CVI_960P_60HZ,
         CVI_1080P_25HZ,
         CVI_1080P_30HZ,

         PVI_720P_25HZ =40,
         PVI_720P_30HZ,
         PVI_720P_50HZ,
         PVI_720P_60HZ,
         PVI_960P_25HZ,
         PVI_960P_30HZ,
         PVI_960P_50HZ,
         PVI_960P_60HZ,
         PVI_1080P_25HZ,
         PVI_1080P_30HZ,

         CAM_TYPE_UNKNOW = 50,
         CAM_NO_SIGNAL,
         CAM_HARDWARE_ERROR
         };*/
        return ret;
    }


    public static void write(String content,String filename) {
        try {
            //判断实际是否有SD卡，且应用程序是否有读写SD卡的能力，有则返回true
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                // 获取SD卡的目录
                File sdCardDir = Environment.getExternalStorageDirectory();
                String path = Config.LOG_PATH;
               int location =   RecordData.getInstance().mutableLocation;
                LogUtils.d("WPTAG"," write  ----location ------------>  " + location);
                LogUtils.d("WPTAG"," write ------path----------------> " + getPath(location) + path);
                File dir = new File(getPath(0) + path); //
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File targetFile = new File(sdCardDir.getCanonicalPath() + path + filename);
                //使用RandomAccessFile是在原有的文件基础之上追加内容，
                //而使用outputstream则是要先清空内容再写入
                RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
                //光标移到原始文件最后，再执行写入
                raf.seek(targetFile.length());
                raf.write(content.getBytes());
                raf.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * generate output file
     *
     * @param type Environment.DIRECTORY_MOVIES / Environment.DIRECTORY_DCIM etc.
     * @return return null when this app has no writing permission to external storage.
     */
    public static final File getCaptureFile(final String type, int location) {
        final File dir = new File(getPath(location) + File.separator + type);
        dir.mkdirs();
        if (dir.canWrite()) {
            return new File(dir, getDateTimeString() + PICTURE_EXT);
        }
        return null;
    }

    private static final String getDateTimeString() {
        final GregorianCalendar now = new GregorianCalendar();
        return mDateTimeFormat.format(now.getTime());
    }

    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static String PICTURE_EXT = ".jpg";

    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);

    public static boolean deleteFile(String filePath) {
        //test.
//        ShellUtils.CommandResult cr = ShellUtils.execCommand("rm "+ filePath, false);
//        LogUtils.d("...deleteFile...errorMsg=" + cr.errorMsg + ",,successMsg=" + cr.successMsg + ",,result=" + cr.result);

        boolean ret = false;
        File file = new File(filePath);
        if (null != file && file.exists()) {
            ret = file.delete();
            LogUtils.d("...deleteFile...ret=" + ret);
            if(ret){
                LogUtils.d("同步...-filePath=" + filePath);

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
                        updateMedia(filePath);
//                    }
//                }).start();
            }
        }
        return ret;
    }

    private static void updateMedia(String filename) {
        MediaScannerConnection.scanFile(DvrApplication.getContext(), new String[]{filename}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                LogUtils.d("updateMedia...-Scanned " + path);
            }
        });
    }

    /**
     * copyFile
     *
     * @param srcFile    Source File
     * @param targetFile Target file
     */
    @SuppressWarnings("resource")
    static public boolean copyFile(String srcFile, String targetFile) throws IOException {
        boolean ret = false;

        File file = new File(targetFile);
        if (!file.exists()) {
//            LogUtils.e("copyFile----targetFile=" + targetFile + ", parentFile=" + file.getParentFile());
            //先得到文件的上级目录，并创建上级目录，在创建文件
            file.getParentFile().mkdir();
            try {
                //创建文件
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtils.e("copyFile----targetFile exist!");
        }

        FileInputStream reader = new FileInputStream(srcFile);
        FileOutputStream writer = new FileOutputStream(targetFile);

        byte[] buffer = new byte[1024];
        int len;

        try {
            reader = new FileInputStream(srcFile);
            writer = new FileOutputStream(targetFile);

            while ((len = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, len);
            }
            ret = true;
        } catch (IOException e) {
            throw e;
        } finally {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
        }

        return ret;
    }

    //设备检测
    /**
     * Check whether the letter has been successfully mount
     */
    public static boolean isDiskMounted(Context context, String path) {
        boolean ret = false;
        if (null != path) {
            if (null != context) {
                StorageManager mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
                if (null != mStorageManager) {
                    final int size = path.length();
                    if (size > 1) {
                        final String _path = path.substring(0, size - 1);
                        Class classMethod;
                        Method method;
                        Object object;
                        try {
                            classMethod = Class.forName(StorageManager.class.getName());
                            if (null != classMethod) {
                                method = classMethod.getMethod("getVolumeState", String.class);
                                if (null != method) {
                                    object = method.invoke(mStorageManager, _path);
                                    if (object instanceof String) {
                                        String state = (String) object;
                                        ret = Environment.MEDIA_MOUNTED.equals(state);
                                        if (ret) {
                                            File file = new File(path);
                                            ret = file.exists();
                                        }
                                    }
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * 删除文件或文件夹 * @param directory
     */
    public static void delAllFile(File directory) {
        if (!directory.isDirectory()) {
            directory.delete();
        } else {
            File[] files = directory.listFiles(); // 空文件夹
            if (files.length == 0){
                directory.delete();
                LogUtils.d("删除" + directory.getAbsolutePath());
                return;
            }
            // 删除子文件夹和子文件
            for (File file : files){
                if (file.isDirectory()){
                    delAllFile(file);
                } else {
                    file.delete();
                    LogUtils.d("删除" + file.getAbsolutePath());
                }
            }
            // 删除文件夹本身
            directory.delete();
            LogUtils.d("删除" + directory.getAbsolutePath());
        }
    }

    public static String replaceExt(String file) {
        String ret = file;
        if (file.contains(Config.VIDEO_FLAG_TS)) {
            ret = file.replaceAll(Config.VIDEO_FLAG_TS, "");
        } else if (file.contains(Config.VIDEO_FLAG)) {
            ret = file.replaceAll(Config.VIDEO_FLAG, "");
        }
        return ret;
    }

    public static void formatDevice(Context context, String path) {
        Method mMethodGetPaths =null;
        StorageManager mStorageManager = context.getSystemService(StorageManager.class);
        try {
            mMethodGetPaths = mStorageManager.getClass()
                    .getMethod("formatVoldId",String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if(mMethodGetPaths !=null) {
            try {
                mMethodGetPaths.invoke(mStorageManager,path);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static void mountDevice(Context context, String volId) {
        Method mMethodGetPaths =null;
        StorageManager mStorageManager = context.getSystemService(StorageManager.class);
        try {
            mMethodGetPaths = mStorageManager.getClass()
                    .getMethod("mount",String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if(mMethodGetPaths !=null) {
            try {
                mMethodGetPaths.invoke(mStorageManager,volId);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
