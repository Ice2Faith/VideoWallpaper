package com.ugex.savelar.videowallpaper.SvcWallpaper;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.io.IOException;

public class VideoWallpaperServiceBak extends WallpaperService {
    public  static final String ACTION_REVC_VDO_ENGINE_CTRL="com.ugex.savelar.vdoengctrl";

    public static boolean mIsPlayDir=false;

    public static void gotoSetWallpaper(Context context){
        Intent intent=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, VideoWallpaperServiceBak.class));
        context.startActivity(intent);
    }
    public static void setVideoVoiceOpenState(Context context,boolean needOpen){
        Intent it=new Intent(ACTION_REVC_VDO_ENGINE_CTRL);
        it.putExtra("cmd","setVol");
        if(needOpen)
            it.putExtra("param","open");
        else
            it.putExtra("param","close");
        context.sendBroadcast(it);
    }
    public static void setVideoFile(Context context,String fileName){
        Intent it=new Intent(ACTION_REVC_VDO_ENGINE_CTRL);
        it.putExtra("cmd","setVdo");
        it.putExtra("param",fileName);
        context.sendBroadcast(it);
    }
    @Override
    public Engine onCreateEngine() {
        return new VideoWallpaperEngine();
    }

    private static String mDisplayFileName="";
    class VideoWallpaperEngine extends Engine {
        private MediaPlayer mMediaPlayer;
        private BroadcastReceiver mReceiver;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            mReceiver=new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String cmd=intent.getStringExtra("cmd");
                    String param=intent.getStringExtra("param");
                    if("setVol".equals(cmd)){
                        if("open".equals(param)){
                            mOpenVoice=true;
                            adjustVoiceState();
                        }else if("close".equals(param)){
                            mOpenVoice=false;
                            adjustVoiceState();
                        }
                    }else if("setVdo".equals(cmd)){
                        prepareToStart(param);
                    }
                }
            };
            IntentFilter filter=new IntentFilter(ACTION_REVC_VDO_ENGINE_CTRL);
            registerReceiver(mReceiver,filter);
        }

        private boolean mOpenVoice=false;
        private  void adjustVoiceState(){
            if(mOpenVoice){
                mMediaPlayer.setVolume(1.0f,1.0f);
            }else{
                mMediaPlayer.setVolume(0,0);
            }
        }
        private void prepareToStart(String displayFileName){
            mDisplayFileName=displayFileName;
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(mDisplayFileName);
                mMediaPlayer.setLooping(true);
                adjustVoiceState();
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onDestroy() {
            unregisterReceiver(mReceiver);
            super.onDestroy();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);

            mMediaPlayer=new MediaPlayer();
            mMediaPlayer.setSurface(holder.getSurface());

            prepareToStart(mDisplayFileName);

        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if(visible){
                mMediaPlayer.start();
            }else{
                mMediaPlayer.pause();
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);

            mMediaPlayer.release();
            mMediaPlayer=null;
        }
    }
}


