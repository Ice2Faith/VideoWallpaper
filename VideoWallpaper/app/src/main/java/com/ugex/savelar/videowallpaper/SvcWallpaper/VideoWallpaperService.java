package com.ugex.savelar.videowallpaper.SvcWallpaper;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import com.ugex.savelar.videowallpaper.MainActivity;
import com.ugex.savelar.videowallpaper.reveiver.BootReceiver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class VideoWallpaperService extends WallpaperService {
    public  static final String ACTION_REVC_VDO_ENGINE_CTRL="com.ugex.savelar.vdoengctrl";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    public static void gotoSetWallpaper(Context context){
        Intent intent=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, VideoWallpaperService.class));
        context.startActivity(intent);
    }
    public static void setVideoVoiceOpenState(Context context,boolean needOpen){
        Intent it=new Intent(ACTION_REVC_VDO_ENGINE_CTRL);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        it.putExtra("cmd","setVol");
        if(needOpen)
            it.putExtra("param","open");
        else
            it.putExtra("param","close");
        context.sendBroadcast(it);
    }
    public static void setPlayInDirVideos(Context context,boolean playInDir){
        Intent it=new Intent(ACTION_REVC_VDO_ENGINE_CTRL);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        it.putExtra("cmd","setPlayDir");
        if(playInDir)
            it.putExtra("param","open");
        else
            it.putExtra("param","close");
        context.sendBroadcast(it);
    }
    public static void setPlayRandomMode(Context context,boolean playInDir){
        Intent it=new Intent(ACTION_REVC_VDO_ENGINE_CTRL);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        it.putExtra("cmd","setPlayRandom");
        if(playInDir)
            it.putExtra("param","open");
        else
            it.putExtra("param","close");
        context.sendBroadcast(it);
    }
    public static void setVideoFile(Context context,String fileName){
        Intent it=new Intent(ACTION_REVC_VDO_ENGINE_CTRL);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        it.putExtra("cmd","setVdo");
        it.putExtra("param",fileName);
        context.sendBroadcast(it);
    }

    @Override
    public Engine onCreateEngine() {
        return new VideoWallpaperEngine();
    }

    private static String mDisplayFileName="";
    class VideoWallpaperEngine extends WallpaperService.Engine {
        private MediaPlayer mMediaPlayer;
        private BroadcastReceiver mReceiver;

        private Random random=new Random();
        private boolean mIsPlayRandom=false;
        private boolean mIsPlayInDir=false;
        private List<String> mPLayList=new ArrayList<>();
        private int mPlayIndex=0;
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            mMediaPlayer=new MediaPlayer();

            mReceiver=new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String cmd=intent.getStringExtra("cmd");
                    String param=intent.getStringExtra("param");
                    SharedPreferences preferences=context.getSharedPreferences(BootReceiver.SETTING_PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor=preferences.edit();
                    if("setVol".equals(cmd)){
                        if("open".equals(param)){
                            mOpenVoice=true;
                            adjustVoiceState();
                        }else if("close".equals(param)){
                            mOpenVoice=false;
                            adjustVoiceState();
                        }
                        editor.putBoolean(BootReceiver.SETTING_KEY_OPEN_VOICE,mOpenVoice);
                    }else if("setVdo".equals(cmd)){
                        mDisplayFileName=param;
                        editor.putString(BootReceiver.SETTING_KEY_FILE_NAME,mDisplayFileName);
                        prepareToStart(param);
                    }else if("setPlayDir".equals(cmd)){
                        if("open".equals(param)){
                            mIsPlayInDir=true;
                            adjustPlayListMode();
                        }else if("close".equals(param)){
                            mIsPlayInDir=false;
                            adjustPlayListMode();
                        }
                        editor.putBoolean(BootReceiver.SETTING_KEY_OPEN_PLAY_IN_DIR,mIsPlayInDir);
                    }else if("setPlayRandom".equals(cmd)){
                        if("open".equals(param)){
                            mIsPlayRandom=true;
                            adjustPlayListMode();
                        }else if("close".equals(param)){
                            mIsPlayRandom=false;
                            adjustPlayListMode();
                        }
                        editor.putBoolean(BootReceiver.SETTING_KEY_OPEN_PLAY_RANDOM,mIsPlayRandom);
                    }
                    editor.commit();
                }
            };
            IntentFilter filter=new IntentFilter(ACTION_REVC_VDO_ENGINE_CTRL);
            registerReceiver(mReceiver,filter);
        }

        private void adjustPlayListMode(){
            if(mIsPlayInDir){
                mPLayList.clear();
                mPlayIndex=0;
                File file=new File(mDisplayFileName);
                File dir=file.getParentFile();
                List<String> vdos=new ArrayList<>();
                vdos= MainActivity.getSuffixFileList(dir,
                        vdos,
                        new String[]{".avi",".mp4",".rmvb",".flv",".mkv"},
                        true,
                        1,
                        true);
                if(mIsPlayRandom){
                    while(vdos.size()>0){
                        int idx=random.nextInt(vdos.size());
                        mPLayList.add(vdos.get(idx));
                        vdos.remove(idx);
                    }
                }else{
                    TreeSet<String> set=new TreeSet<>();
                    set.addAll(vdos);
                    Iterator<String> it=set.iterator();
                    while (it.hasNext()){
                        mPLayList.add(it.next());
                    }
                }
                playNextListVideo();
            }else{
                mPLayList.clear();
                mPlayIndex=0;
            }
        }

        private void playNextListVideo(){
            if(mPLayList.size()>0){
                mPlayIndex=(mPlayIndex+1)%mPLayList.size();
                String fileName=mPLayList.get(mPlayIndex);
                mDisplayFileName=fileName;
                prepareToStart(fileName);
            }
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
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(displayFileName);
//                mMediaPlayer.setLooping(true);
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


            mMediaPlayer.setSurface(holder.getSurface());
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(mIsPlayInDir){
                        playNextListVideo();
                    }else{
                        prepareToStart(mDisplayFileName);
                    }
                }
            });

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


