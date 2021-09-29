package com.metime.camera2.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLException;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.keri.CameraRecordListener;
import com.keri.CameraRecorder;
import com.keri.CameraRecorderBuilder;
import com.keri.LensFacing;
import com.metime.activity.AfterVideoCaptureActivity;
import com.metime.camera2.util.CameraUtil;
import com.metime.camera2.widget.Filters;
import com.metime.camera2.widget.SampleGLView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

public abstract class BaseCameraFragment extends Fragment {

    private SampleGLView sampleGLView;
    protected CameraRecorder cameraRecorder;
    private String filepath;
    private TextView recordBtn;
    protected LensFacing lensFacing = LensFacing.BACK;
    protected int cameraWidth = 1280;
    protected int cameraHeight = 720;
    protected int videoWidth = 720;
    protected int videoHeight = 720;
    private AlertDialog filterDialog;
    private boolean toggleClick = false;
    public FrameLayout frameLayout;

    private boolean isRecording = false;

    //method
    public abstract void isRecording(boolean isRecording);

    public abstract void isFlashAvailable(boolean flashSupport);

    public void startRecording() {

        if(cameraRecorder == null)
            return;

        if (isRecording) {
            isRecording = false;
            cameraRecorder.stop();
        } else {
            isRecording = true;
            filepath = getVideoFilePath();
            cameraRecorder.start(filepath);
        }
        isRecording(isRecording);
    }

    public void setFlash() {
        if (cameraRecorder != null && cameraRecorder.isFlashSupport()) {
            cameraRecorder.switchFlashMode();
            cameraRecorder.changeAutoFocus();
        }
    }

    public void switchCamera() {
        if (isRecording)
            startRecording();
        releaseCamera();
        if (lensFacing == LensFacing.BACK) {
            lensFacing = LensFacing.FRONT;
        } else {
            lensFacing = LensFacing.BACK;
        }

        toggleClick = true;
    }

    public void onFilter() {
        if (filterDialog == null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Choose a filter");
            builder.setOnDismissListener(dialog -> {
                filterDialog = null;
            });

            final Filters[] filters = Filters.values();
            CharSequence[] charList = new CharSequence[filters.length];
            for (int i = 0, n = filters.length; i < n; i++) {
                charList[i] = filters[i].name();
            }
            builder.setItems(charList, (dialog, item) -> {
                changeFilter(filters[item]);
            });
            filterDialog = builder.show();
        } else {
            filterDialog.dismiss();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!CameraUtil.hasPermissionsGranted(getActivity(), Camera2PermissionDialog.VIDEO_PERMISSIONS)) {
            requestVideoPermissions();
            return;
        }
        setUpCamera();
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseCamera();
    }

    public void releaseCamera() {
        if (sampleGLView != null) {
            sampleGLView.onPause();
        }

        if (cameraRecorder != null) {
            cameraRecorder.stop();
            cameraRecorder.release();
            cameraRecorder = null;
        }

        if (sampleGLView != null) {
            frameLayout.removeView(sampleGLView);
            sampleGLView = null;
        }
    }


    private void setUpCameraView() {
        getActivity().runOnUiThread(() -> {
            frameLayout.removeAllViews();
            sampleGLView = null;
            sampleGLView = new SampleGLView(getActivity().getApplicationContext());
            sampleGLView.setTouchListener((event, width, height) -> {
                if (cameraRecorder == null) return;
                cameraRecorder.changeManualFocusPoint(event.getX(), event.getY(), width, height);
            });
            frameLayout.addView(sampleGLView);
        });
    }


    private void setUpCamera() {
        setUpCameraView();

        cameraRecorder = new CameraRecorderBuilder(getActivity(), sampleGLView)
                //.recordNoFilter(true)
                .cameraRecordListener(new CameraRecordListener() {
                    @Override
                    public void onGetFlashSupport(boolean flashSupport) {
                        getActivity().runOnUiThread(() -> {
                            isFlashAvailable(flashSupport);
                        });
                    }

                    @Override
                    public void onRecordComplete() {
                        if (isVisible() || isAdded()) {
                            Log.e("CameraRecorder", "onRecordComplete");
                            releaseCamera();
                            //exportMp4ToGallery(getActivity().getApplicationContext(), filepath);
                            AfterVideoCaptureActivity.openAfterVideoCapture(requireContext(), filepath, "1");
                        }
                    }

                    @Override
                    public void onRecordStart() {

                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e("CameraRecorder", exception.toString());
                    }

                    @Override
                    public void onCameraThreadFinish() {
                        Log.e("CameraRecorder", "onCameraThreadFinish");
                        if (toggleClick) {
                            getActivity().runOnUiThread(() -> {
                                setUpCamera();
                            });
                        }
                        toggleClick = false;
                    }
                })
                .videoSize(videoWidth, videoHeight)
                .cameraSize(cameraWidth, cameraHeight)
                .lensFacing(lensFacing)
                .mute(false)
                .build();


    }

    private void changeFilter(Filters filters) {
        cameraRecorder.setFilter(Filters.getFilterInstance(filters, getActivity().getApplicationContext()));
    }


    private interface BitmapReadyCallbacks {
        void onBitmapReady(Bitmap bitmap);
    }

    private void captureBitmap(final BitmapReadyCallbacks bitmapReadyCallbacks) {
        sampleGLView.queueEvent(() -> {
            EGL10 egl = (EGL10) EGLContext.getEGL();
            GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
            Bitmap snapshotBitmap = createBitmapFromGLSurface(sampleGLView.getMeasuredWidth(), sampleGLView.getMeasuredHeight(), gl);


            getActivity().runOnUiThread(() -> {
                bitmapReadyCallbacks.onBitmapReady(snapshotBitmap);
            });
        });
    }

    private Bitmap createBitmapFromGLSurface(int w, int h, GL10 gl) {

        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(0, 0, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2, texturePixel, blue, red, pixel;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    texturePixel = bitmapBuffer[offset1 + j];
                    blue = (texturePixel >> 16) & 0xff;
                    red = (texturePixel << 16) & 0x00ff0000;
                    pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            Log.e("CreateBitmap", "createBitmapFromGLSurface: " + e.getMessage(), e);
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

    public void saveAsPngImage(Bitmap bitmap, String filePath) {
        try {
            File file = new File(filePath);
            FileOutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void exportMp4ToGallery(Context context, String filePath) {
        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, filePath);
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + filePath)));
    }

    public String getVideoFilePath() {
        return getAndroidMoviesFolder().getAbsolutePath() + "/" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + "cameraRecorder.mp4";
    }

    public File getAndroidMoviesFolder() {
        return getContext().getExternalFilesDir("video");
    }

    private static void exportPngToGallery(Context context, String filePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public String getImageFilePath() {
        return getAndroidImageFolder().getAbsolutePath() + "/" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + "cameraRecorder.png";
    }

    public File getAndroidImageFolder() {
        return getContext().getExternalFilesDir("images");
    }

    public void requestVideoPermissions() {
        if (CameraUtil.shouldShowRequestPermissionRationale(this, Camera2PermissionDialog.VIDEO_PERMISSIONS)) {
            Camera2PermissionDialog.newInstance(this, "Hey man, we need to use your camera please!").show(getChildFragmentManager(), Camera2PermissionDialog.FRAGMENT_DIALOG);
        } else {
            requestPermissions(Camera2PermissionDialog.VIDEO_PERMISSIONS, Camera2PermissionDialog.REQUEST_VIDEO_PERMISSIONS);
        }
    }
}
