package com.example.camerademo.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
	public static final int TOTAL_SECONDS = 24 * 3600;
    /**
     * 将长时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss
     * 
     * @param dateDate
     * @return
     */
    public static String longToStrDate(long dateDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dateString = formatter.format(dateDate);
        return dateString;
    }
	
	  /**
   * 进度 -> 日历时间
   * 
   * @param progress
   * @return
   */
  public static Calendar Progress2Calendar(float progress) {
      int seconds = (int) (TOTAL_SECONDS * progress);
      int h = seconds / 3600;
      int m = (seconds / 60) % 60;
      int s = seconds % 60;
      Calendar time = Calendar.getInstance();
      time.set(0, 0, 0, h, m, s);
      return time;
  }
  
  	private static long lastClickTime;
  
	public static boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < 800) {
			return true;
		}
		lastClickTime = time;
		return false;
	}
	
	
	public static final long U_RIGHT_MONITOR				= 0x00000001;		// 实时监视
	public static final long U_RIGHT_PLAYBACK				= 0x00000002;		// 录像回放	
    public static boolean getChannelRight(long channelRight, long status) {
        long temp = channelRight & status;
        if (temp == status) {
            return true;
        }
        return false;
    }
}
