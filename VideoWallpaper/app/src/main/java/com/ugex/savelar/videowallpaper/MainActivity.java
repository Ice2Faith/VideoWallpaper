package com.ugex.savelar.videowallpaper;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.VideoView;

import com.ugex.savelar.videowallpaper.Activities.FileViewerActivity;
import com.ugex.savelar.videowallpaper.SvcWallpaper.VideoWallpaperService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private List<String> vdosList=new ArrayList<>();
    private ListView lsvVideos;
    private ArrayAdapter<String> adapter;
    private CheckBox ckbOpenVoice;
    private VideoView vvPreview;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0x1001);
        }

        ListViewAdpte();
    }
    private void ListViewAdpte(){

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
            VideoWallpaperService.gotoSetWallpaper(MainActivity.this);

            MainActivity.this.finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
