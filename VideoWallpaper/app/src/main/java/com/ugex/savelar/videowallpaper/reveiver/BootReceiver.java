package com.ugex.savelar.videowallpaper.reveiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.ugex.savelar.videowallpaper.SvcWallpaper.VideoWallpaperService;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    public static final String SETTING_PREFERENCE_FILE_NAME= "last_vdo_setting";
    public static final String SETTING_KEY_FILE_NAME="file_name";
    public static final String SETTING_KEY_OPEN_VOICE="open_voice";
    public static final String SETTING_KEY_OPEN_PLAY_IN_DIR="open_play_in_dir";
    public static final String SETTING_KEY_OPEN_PLAY_RANDOM="open_play_random";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action=intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int count=0;
                    do{
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                        }
                        count++;
                    }while (!isServiceRunning(context,VideoWallpaperService.class) && count<(20*1000/500)); //最多延迟20秒
                    String fileName="";
                    boolean openVoice=false;
                    boolean playInDir=false;
                    boolean playRandom=false;
                    SharedPreferences preferences=context.getSharedPreferences(SETTING_PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
                    fileName=preferences.getString(SETTING_KEY_FILE_NAME,"");
                    openVoice=preferences.getBoolean(SETTING_KEY_OPEN_VOICE,false);
                    playInDir=preferences.getBoolean(SETTING_KEY_OPEN_PLAY_IN_DIR,false);
                    playRandom=preferences.getBoolean(SETTING_KEY_OPEN_PLAY_RANDOM,false);

                    VideoWallpaperService.setVideoFile(context,fileName);
                    VideoWallpaperService.setVideoVoiceOpenState(context,openVoice);
                    VideoWallpaperService.setPlayInDirVideos(context,playInDir);
                    VideoWallpaperService.setPlayRandomMode(context,playRandom);
                    VideoWallpaperService.gotoSetWallpaper(context);
                }
            }).start();

        }
    }

    public static boolean isServiceRunning(Context context,Class clazz){
        return isServiceRunning(context,clazz.getName());
    }

    public static boolean isServiceRunning(Context context,String fullClassName){
        ActivityManager activityManager=(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> list=activityManager.getRunningServices(256);
        for (ActivityManager.RunningServiceInfo item : list){
            if(item.service.getClassName().equals(fullClassName)){
                return true;
            }
        }
        return false;
    }

}
