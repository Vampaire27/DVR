package com.wwc2.dvr.utils;

import android.databinding.ObservableList;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.DriveVideoFont;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * description ： TODO:类的作用
 * user: wangpeng on 2019/8/30.
 * emai: wpeng@waterworld.com.cn
 */

public class ListSortUtils {


    //升序
  public static  ObservableList<DriveVideoFont> invertOrderList (ObservableList<DriveVideoFont> L){

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
      Date d1;
      Date d2;
      DriveVideoFont temp_r;
      //做一个冒泡排序，大的在数组的前列
      for(int i=0; i<L.size()-1; i++){
          for(int j=i+1; j<L.size();j++){

              ParsePosition pos1 = new ParsePosition(0);
              ParsePosition pos2 = new ParsePosition(0);
              String str=  L.get(i).getName();
              String str1=  L.get(j).getName();

             String datastr1 =  FileUtils.replaceExt(str.substring(getPosition(str,2),str.length()));
             String datastr2 =  FileUtils.replaceExt(str.substring(getPosition(str1,2),str.length()));

            LogUtils.d("WPTAG","datastr1 :" + datastr1);
            LogUtils.d("WPTAG","datastr2 :" + datastr2);

              d1 = sdf.parse(datastr1, pos1);
              d2 = sdf.parse(datastr2, pos2);

              if(d1.before(d2)){//如果队前日期靠前，调换顺序
                  temp_r = L.get(i);
                  L.set(i, L.get(j));
                  L.set(j, temp_r);
              }
          }
      }
      return L;
  }
  //  /storage/emulated/0/recordVideo/main/main_1080P_2019-07-01_00-09-58.mp4

    //获取字符"_"第一次出现的位置，第二次出现的位置，第三次出现的位置；(传参为，字符串数据，和次数)

    public static int getPosition(String str, int ciShu) {
        int number = 0;
        char arr[] = str.toCharArray();
        for (int i = 0; i < arr.length; i++) {
     if (arr[i] == '_') {
           number++;
         }
      if (number == ciShu) {
        return i+1;
    }
   }
    return 1;
  }
    // storage/emulated/0/recordPicture/2019-07-01-10-10-39

    //获取字符"_"第一次出现的位置，第二次出现的位置，第三次出现的位置；(传参为，字符串数据，和次数)

    public static int getPositionimg(String str, int ciShu) {
        int number = 0;
        char arr[] = str.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '/') {
                number++;
            }
            if (number == ciShu) {
                return i+1;
            }
        }
        return 1;
    }


    public static Date parseServerTime(String serverTime) {

        String format = "yyyy-MM-dd_HH-mm-ss";

        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINESE);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        Date date = null;
        try {
            date = sdf.parse(serverTime);
        } catch (Exception e) {

        }
        return date;
    }

}
