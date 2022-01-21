package com.ugex.savelar.videowallpaper;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.VideoView;

import com.ugex.savelar.videowallpaper.Activities.FileViewerActivity;
import com.ugex.savelar.videowallpaper.SvcWallpaper.VideoWallpaperService;
import com.ugex.savelar.videowallpaper.reveiver.BootReceiver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private List<String> vdosList=new ArrayList<>();
    private ListView lsvVideos;
    private ArrayAdapter<String> adapter;
    private CheckBox ckbOpenVoice;
    private VideoView vvPreview;
    private CheckBox ckbPlayInDirMode;
    private CheckBox ckbPlayRandomMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitActivity();

    }
    private void InitActivity() {
        lsvVideos=findViewById(R.id.listViewVdos);
        ckbOpenVoice=findViewById(R.id.checkBoxOpenAudio);
        vvPreview=findViewById(R.id.videoViewPreview);
        ckbPlayInDirMode=findViewById(R.id.checkBoxPlayInDirMode);
        ckbPlayRandomMode=findViewById(R.id.checkBoxPlayRandomMode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED},0x1001);
        }

        ListViewAdpte();
        showLastSetting();
    }

    private void showLastSetting() {
        String fileName="";
        boolean openVoice=false;
        boolean playInDir=false;
        boolean playRandom=false;
        SharedPreferences preferences=getSharedPreferences(BootReceiver.SETTING_PREFERENCE_FILE_NAME,Context.MODE_PRIVATE);
        fileName=preferences.getString(BootReceiver.SETTING_KEY_FILE_NAME,"");
        openVoice=preferences.getBoolean(BootReceiver.SETTING_KEY_OPEN_VOICE,false);
        playInDir=preferences.getBoolean(BootReceiver.SETTING_KEY_OPEN_PLAY_IN_DIR,false);
        playRandom=preferences.getBoolean(BootReceiver.SETTING_KEY_OPEN_PLAY_RANDOM,false);

        vvPreview.setVideoPath(fileName);
        vvPreview.start();

        ckbOpenVoice.setChecked(openVoice);
        ckbPlayInDirMode.setChecked(playInDir);
        ckbPlayRandomMode.setChecked(playRandom);
    }



    private void ListViewAdpte(){
        ckbPlayRandomMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoWallpaperService.setPlayRandomMode(MainActivity.this,ckbPlayRandomMode.isChecked());
            }
        });
        ckbPlayInDirMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoWallpaperService.setPlayInDirVideos(MainActivity.this,ckbPlayInDirMode.isChecked());
            }
        });
        ckbOpenVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoWallpaperService.setVideoVoiceOpenState(MainActivity.this,ckbOpenVoice.isChecked());
            }
        });

        adapter=new ArrayAdapter<String>(MainActivity.this, android.R.layout.activity_list_item, android.R.id.text1,vdosList);
        lsvVideos.setAdapter(adapter);
        lsvVideos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                vvPreview.setVideoPath(vdosList.get(position));
                vvPreview.start();
                return true;
            }
        });
        lsvVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                VideoWallpaperService.setVideoFile(MainActivity.this,vdosList.get(position));
                VideoWallpaperService.setVideoVoiceOpenState(MainActivity.this,ckbOpenVoice.isChecked());
                VideoWallpaperService.setPlayInDirVideos(MainActivity.this,ckbPlayInDirMode.isChecked());
                VideoWallpaperService.setPlayRandomMode(MainActivity.this,ckbPlayRandomMode.isChecked());
                VideoWallpaperService.gotoSetWallpaper(MainActivity.this);

                MainActivity.this.finish();
            }
        });
        vdosList=getSuffixFileList(Environment.getExternalStorageDirectory(),
                vdosList,
                new String[]{".avi",".mp4",".rmvb",".flv",".mkv"},
                true,
                3,
                true);
        adapter.notifyDataSetChanged();
    }

    public static List<String> getSuffixFileList(File file,List<String> fileList,String[] suffixes,boolean ignoreCase,int findLevel,boolean jumpHideDir){
        if(file.exists()==false){
            return fileList;
        }
        if(file.isFile()){
            for(String suffix :suffixes){
                String path=file.getAbsolutePath();
                if(ignoreCase){
                    String psuf=path.substring(path.length()-suffix.length());
                    if(suffix.equalsIgnoreCase(psuf)){
                        fileList.add(path);
                        break;
                    }
                }else if(path.endsWith(suffix)){
                    fileList.add(path);
                    break;
                }
            }
        }else if(file.isDirectory()){
            File[] files=file.listFiles();
            for(File nf :files){
                if(nf.isFile()){
                    getSuffixFileList(nf,fileList,suffixes,ignoreCase,findLevel-1,jumpHideDir);
                }else{
                    if(findLevel>0){
                        if(jumpHideDir){
                            if(nf.getName().startsWith(".")==false){
                                getSuffixFileList(nf, fileList, suffixes, ignoreCase, findLevel-1, jumpHideDir);
                            }
                        }else {
                            getSuffixFileList(nf, fileList, suffixes, ignoreCase, findLevel-1, jumpHideDir);
                        }
                    }
                }
            }
        }
        return fileList;
    }

    public static final int REQUEST_FILE_VIEWER_CODE=0x1001;
    public void OnBtnViewFileClicked(View view) {
        startActivityForResult(new Intent(this, FileViewerActivity.class),REQUEST_FILE_VIEWER_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_FILE_VIEWER_CODE && resultCode==FileViewerActivity.SELECTED_FILE_RESULT_CODE){
            String selFile=data.getStringExtra(FileViewerActivity.SELECTED_FILE_NAME_KEY);
            VideoWallpaperService.setVideoFile(MainActivity.this,selFile);
            VideoWallpaperService.setVideoVoiceOpenState(MainActivity.this,ckbOpenVoice.isChecked());
            VideoWallpaperService.setPlayInDirVideos(MainActivity.this,ckbPlayInDirMode.isChecked());
            VideoWallpaperService.setPlayRandomMode(MainActivity.this,ckbPlayRandomMode.isChecked());
            VideoWallpaperService.gotoSetWallpaper(MainActivity.this);

            MainActivity.this.finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
