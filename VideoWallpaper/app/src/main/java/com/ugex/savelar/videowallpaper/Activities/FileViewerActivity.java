package com.ugex.savelar.videowallpaper.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ugex.savelar.videowallpaper.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class FileViewerActivity extends AppCompatActivity {
    private EditText edtShowDir;
    private ListView lsvFiles;
    private CheckBox ckbShowHide;
    private File mFlExternalStorageRootDir;
    private File mFlPresentShowDir;
    private List<File> mlstPresentDirFile =new ArrayList<>();
    private FileViewerListAdapter adapter;
    private Stack<String> historyShowDir=new Stack<>();
    public static final int SELECTED_FILE_RESULT_CODE=0x1002;
    public static final String SELECTED_FILE_NAME_KEY="selectFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);

        InitActivity();
    }

    private void InitActivity() {
        edtShowDir =findViewById(R.id.editTextTargetDir);
        lsvFiles=findViewById(R.id.listViewFiles);
        ckbShowHide=findViewById(R.id.checkBoxShowHide);
        mFlExternalStorageRootDir= Environment.getExternalStorageDirectory();
        mFlPresentShowDir=Environment.getExternalStorageDirectory();

        adapter=new FileViewerListAdapter();

        updateShowFileList(mFlPresentShowDir);

        ckbShowHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateShowFileList(mFlPresentShowDir);
            }
        });

        lsvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File sf= mlstPresentDirFile.get(position);
                if(sf.isFile()){
                    Intent intent=new Intent();
                    intent.putExtra(SELECTED_FILE_NAME_KEY,sf.getAbsolutePath());
                    FileViewerActivity.this.setResult(SELECTED_FILE_RESULT_CODE,intent);
                    FileViewerActivity.this.finish();
                }else{
                    updateShowFileList(sf);
                }
            }
        });

        lsvFiles.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                return true;
            }
        });
        lsvFiles.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    private void updateShowFileList(File fl){
        if(fl==null || fl.exists()==false){
            return;
        }
        mFlPresentShowDir=fl;
        edtShowDir.setText(mFlPresentShowDir.getAbsolutePath());
        if(historyShowDir.isEmpty()==false){
            if(mFlPresentShowDir.getAbsolutePath().equals(historyShowDir.lastElement())==false){
                historyShowDir.push(mFlPresentShowDir.getAbsolutePath());
            }
        }else{
            historyShowDir.push(mFlPresentShowDir.getAbsolutePath());
        }
        if(ckbShowHide.isChecked())
            getFileListFromDir(mFlPresentShowDir, mlstPresentDirFile,true,true);
        else
            getFileListFromDir(mFlPresentShowDir, mlstPresentDirFile,true,false);
        adapter.notifyDataSetChanged();
    }

    private List<File> getFileListFromDir(File dir,List<File> lst,boolean sorted,boolean includeHide){
        if(dir.exists()==false)
            return lst;
        if(dir.isFile()){
            dir=dir.getParentFile();
        }
        if(dir==null || dir.listFiles()==null || dir.listFiles().length==0){
            return lst;
        }
        ArrayList<File> hideFile=new ArrayList<>();
        ArrayList<File> normalFile=new ArrayList<>();
        ArrayList<File> hideDir=new ArrayList<>();
        ArrayList<File> normalDir=new ArrayList<>();
        lst.clear();
        for(File fl : dir.listFiles()){
            boolean isHide=fl.getName().startsWith(".");
            if(fl.isFile()){
                if(isHide){
                    hideFile.add(fl);
                }else{
                    normalFile.add(fl);
                }
            }else if(fl.isDirectory()){
                if(isHide){
                    hideDir.add(fl);
                }else{
                    normalDir.add(fl);
                }
            }
        }

        if(sorted){
            Comparator<File> comparator=new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            };

            sortFileList(hideDir,comparator);
            sortFileList(normalDir,comparator);
            sortFileList(hideFile,comparator);
            sortFileList(normalFile,comparator);

        }

        if(includeHide){
            lst.addAll(hideDir);
        }
        lst.addAll(normalDir);
        if(includeHide){
            lst.addAll(hideFile);
        }
        lst.addAll(normalFile);
        return lst;
    }

    public void OnBtnBackClicked(View view){
        if(historyShowDir.isEmpty()==false)
            historyShowDir.pop();
        if(historyShowDir.isEmpty()==false)
            updateShowFileList(new File(historyShowDir.pop()));
    }
    public void OnBtnParentClicked(View view){
        updateShowFileList(mFlPresentShowDir.getParentFile());
    }
    public void OnBtnEnterClicked(View view){
        updateShowFileList(new File(edtShowDir.getText().toString().trim()));
    }
    public void OnBtnExternelRootClicked(View view){
        updateShowFileList(mFlExternalStorageRootDir);
    }

    private void sortFileList(ArrayList<File> lsv, Comparator<File> comparator){
        for(int i=0;i<lsv.size();i++){
            boolean swap=false;
            for(int j=0;j<lsv.size()-1;j++){
                if(comparator.compare(lsv.get(j),lsv.get(j+1))>0){
                    File tp=lsv.get(j);
                    lsv.set(j,lsv.get(j+1));
                    lsv.set(j+1,tp);
                    swap=true;
                }
            }
            if(swap==false){
                break;
            }
        }
    }

    class FileViewerListAdapter extends BaseAdapter{
        private SimpleDateFormat dateFormater=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        private String FileSizeToShowString(long fileSize){
            String str="";
            if(fileSize<1024){
                str=fileSize+"byte";
            }else if(fileSize<1024*1024){
                str=(fileSize/1024)+"Kb";
            }else if(fileSize<1024*1024*1024){
                str=(fileSize/1024/1024)+"Mb";
            }else{
                str=(fileSize/1024/1024/1024)+"Gb";
            }
            return str;
        }
        @Override
        public int getCount() {
            return mlstPresentDirFile.size();
        }

        @Override
        public Object getItem(int position) {
            return mlstPresentDirFile.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view=null;
            if(convertView!=null){
                view=convertView;
            }else{
                view= LayoutInflater.from(FileViewerActivity.this).inflate(R.layout.item_file_list,null);
            }
            ImageView ivPreview=view.findViewById(R.id.imageViewFilePreview);
            TextView tvName=view.findViewById(R.id.textViewFileName);
            TextView tvDescript=view.findViewById(R.id.textViewFileDescript);
            TextView tvDate=view.findViewById(R.id.textViewFileDate);
            File pf= mlstPresentDirFile.get(position);
            tvName.setText(pf.getName());
            tvDate.setText(dateFormater.format(pf.lastModified()));
            if(pf.isFile()){
                tvDescript.setText(FileSizeToShowString(pf.length()));
                setImageViewIconAccorFileName(ivPreview,pf.getName());
            }else{
                tvDescript.setText("文件夹");
                ivPreview.setImageResource(R.drawable.folder);
            }
            return view;
        }

        private void setImageViewIconAccorFileName(ImageView iv,String name){
            int tailIndex=name.lastIndexOf(".");
            if(tailIndex>0) {
                String tial = name.substring(tailIndex);
                if(".txt".equalsIgnoreCase(tial)){
                    iv.setImageResource(R.drawable.txt);
                }else if(".apk".equalsIgnoreCase(tial)){
                    iv.setImageResource(R.drawable.apk);
                }else if(".pdf".equalsIgnoreCase(tial)){
                    iv.setImageResource(R.drawable.pdf);
                }else if(".html".equalsIgnoreCase(tial)
                        ||".java".equalsIgnoreCase(tial)
                        ||".c".equalsIgnoreCase(tial)
                        ||".cpp".equalsIgnoreCase(tial)
                        ||".h".equalsIgnoreCase(tial)
                        ||".py".equalsIgnoreCase(tial)
                        ||".js".equalsIgnoreCase(tial)
                        ||".bat".equalsIgnoreCase(tial)
                        ||".sh".equalsIgnoreCase(tial)
                        ||".cs".equalsIgnoreCase(tial)
                        ||".hpp".equalsIgnoreCase(tial)){
                    iv.setImageResource(R.drawable.textfile);
                }else if(".mp3".equalsIgnoreCase(tial)
                ||".ogg".equalsIgnoreCase(tial)
                ||".wav".equalsIgnoreCase(tial)){
                    iv.setImageResource(R.drawable.music);
                }else if(".jpg".equalsIgnoreCase(tial)
                        ||".png".equalsIgnoreCase(tial)
                        ||".jpeg".equalsIgnoreCase(tial)
                        ||".gif".equalsIgnoreCase(tial)){
                    iv.setImageResource(R.drawable.picture);
                }else if(".mp4".equalsIgnoreCase(tial)
                        ||".flv".equalsIgnoreCase(tial)
                        ||".mkv".equalsIgnoreCase(tial)
                        ||".rmvb".equalsIgnoreCase(tial)
                        ||".avi".equalsIgnoreCase(tial)){
                    iv.setImageResource(R.drawable.video);
                }else if(".zip".equalsIgnoreCase(tial)
                        ||".rar".equalsIgnoreCase(tial)
                        ||".7z".equalsIgnoreCase(tial)
                        ||".gz".equalsIgnoreCase(tial)
                        ||".tar".equalsIgnoreCase(tial)
                        ||".iso".equalsIgnoreCase(tial)
                        ||".img".equalsIgnoreCase(tial)){
                    iv.setImageResource(R.drawable.compress);
                }else if(".doc".equalsIgnoreCase(tial)
                        ||".docx".equalsIgnoreCase(tial)){
                    iv.setImageResource(R.drawable.doc);
                }else if(".xls".equalsIgnoreCase(tial)
                        ||".xlsx".equalsIgnoreCase(tial)){
                    iv.setImageResource(R.drawable.xls);
                }else if(".ppt".equalsIgnoreCase(tial)
                        ||".pptx".equalsIgnoreCase(tial)){
                    iv.setImageResource(R.drawable.ppt);
                }else if(".so".equalsIgnoreCase(tial)
                        ||".a".equalsIgnoreCase(tial)
                        ||".lib".equalsIgnoreCase(tial)
                        ||".class".equalsIgnoreCase(tial)
                        ||".exe".equalsIgnoreCase(tial)
                        ||".dll".equalsIgnoreCase(tial)
                        ||".sys".equalsIgnoreCase(tial)){
                    iv.setImageResource(R.drawable.binfile);
                }else{
                    iv.setImageResource(R.drawable.unknownfile);
                }
            }else{
                iv.setImageResource(R.drawable.unknownfile);
            }
        }
    }
}
