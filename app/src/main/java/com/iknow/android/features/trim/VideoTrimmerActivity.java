package com.iknow.android.features.trim;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iknow.android.R;
import com.iknow.android.databinding.ActivityVideoTrimBinding;
import com.iknow.android.features.common.ui.BaseActivity;
import com.iknow.android.features.compress.VideoCompressor;
import com.iknow.android.interfaces.VideoCompressListener;
import com.iknow.android.interfaces.VideoTrimListener;
import com.iknow.android.utils.StorageUtil;
import com.iknow.android.utils.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author：J.Chou
 * Date：  2016.08.01 2:23 PM
 * Email： who_know_me@163.com
 * Describe:
 */
public class VideoTrimmerActivity extends BaseActivity implements VideoTrimListener {

  private static final String TAG = "jason";
  private static final String VIDEO_PATH_KEY = "video-file-path";
  private static final String COMPRESSED_VIDEO_FILE_NAME = "compress.mp4";
  public static final int VIDEO_TRIM_REQUEST_CODE = 0x001;
  private ActivityVideoTrimBinding mBinding;
  private ProgressDialog mProgressDialog;

  public static void call(FragmentActivity from, String videoPath) {
    if (!TextUtils.isEmpty(videoPath)) {
      Bundle bundle = new Bundle();
      bundle.putString(VIDEO_PATH_KEY, videoPath);
      Intent intent = new Intent(from, VideoTrimmerActivity.class);
      intent.putExtras(bundle);
      from.startActivityForResult(intent, VIDEO_TRIM_REQUEST_CODE);
    }
  }
  String path = "";
  int count=0;
  @Override public void initUI() {
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_video_trim);
    Bundle bd = getIntent().getExtras();
    if (bd != null) path = bd.getString(VIDEO_PATH_KEY);
    if (mBinding.trimmerView != null) {
      mBinding.trimmerView.setOnTrimVideoListener(this);
      mBinding.trimmerView.initVideoByURI(Uri.parse(path));
    }
    Log.e(TAG, path);


    List<String> urls = new ArrayList<>();
    urls.add("/storage/emulated/0/DCIM/Pivo/20191226_15443110.mp4");
    urls.add("/storage/emulated/0/DCIM/Pivo/20191226_15443112.mp4");
    urls.add("/storage/emulated/0/20191108_160118.mp4");
    urls.add("/storage/43E2-AFDA/DCIM/Camera/20191121_091255.mp4");

    Log.e(TAG, "filePath: "+path);

    findViewById(R.id.btn_play).setOnClickListener(view -> {
      Log.e(TAG, "path: "+path);
      if (count == 0){
        count++;
        path = urls.get(0);
      }else if(count == 1){
        count++;
        path = urls.get(1);
      }else {
        count = 0;
        path = urls.get(2);
      }
      mBinding.trimmerView.initVideoByURI(Uri.parse(path));
    });

  }

  @Override public void onResume() {
    super.onResume();
  }

  @Override public void onPause() {
    super.onPause();
    mBinding.trimmerView.onVideoPause();
    mBinding.trimmerView.setRestoreState(true);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mBinding.trimmerView.onDestroy();
  }

  @Override public void onStartTrim() {
    buildDialog(getResources().getString(R.string.trimming)).show();
  }

  public String makeFolderIfNotExists(Context context, String dirName){
    File outDir = new File(context.getExternalFilesDir(null), dirName);
    if (!outDir.exists() && !outDir.mkdirs()) {
      return null;
    }
    return outDir.getAbsolutePath();
  }

  @Override public void onFinishTrim(String in) {
    if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
    ToastUtil.longShow(this, getString(R.string.trimmed_done));
    finish();
    //TODO: please handle your trimmed video url here!!!
    String fileDir = makeFolderIfNotExists(this, "Trimmer");
//    String out = StorageUtil.getCacheDir() + File.separator + COMPRESSED_VIDEO_FILE_NAME;
    String out = fileDir + File.separator + COMPRESSED_VIDEO_FILE_NAME;
    buildDialog(getResources().getString(R.string.compressing)).show();
    VideoCompressor.compress(this, in, out, new VideoCompressListener() {
      @Override public void onSuccess(String message) {
      }

      @Override public void onFailure(String message) {
      }

      @Override public void onFinish() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
        finish();
      }
    });
  }

  @Override public void onCancel() {
    mBinding.trimmerView.onDestroy();
    finish();
  }

  private ProgressDialog buildDialog(String msg) {
    if (mProgressDialog == null) {
      mProgressDialog = ProgressDialog.show(this, "", msg);
    }
    mProgressDialog.setMessage(msg);
    return mProgressDialog;
  }
}
