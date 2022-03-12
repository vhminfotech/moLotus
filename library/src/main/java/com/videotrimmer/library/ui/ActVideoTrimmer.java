package com.videotrimmer.library.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.videotrimmer.library.R;
import com.videotrimmer.library.utils.CompressOption;
import com.videotrimmer.library.utils.CustomProgressView;
import com.videotrimmer.library.utils.FileUtils;
import com.videotrimmer.library.utils.LogMessage;
import com.videotrimmer.library.utils.TrimVideo;
import com.videotrimmer.library.utils.TrimVideoOptions;
import com.videotrimmer.library.utils.TrimmerUtils;
import com.linkedin.android.litr.MediaTransformer;
import com.linkedin.android.litr.TransformationListener;
import com.linkedin.android.litr.TransformationOptions;
import com.linkedin.android.litr.analytics.TrackTransformationInfo;
import com.linkedin.android.litr.io.MediaRange;
import com.linkedin.android.litr.utils.CodecUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class ActVideoTrimmer extends AppCompatActivity {

    private PlayerView playerView;

    private static final int PER_REQ_CODE = 115;

    private SimpleExoPlayer videoPlayer;

    private ImageView imagePlayPause;

    private ImageView[] imageViews;

    private long totalDuration;

    private Dialog dialog;

    private Uri uri;

    private TextView txtStartDuration, txtEndDuration;

    private CrystalRangeSeekbar seekbar;

    private long lastMinValue = 0;

    private long lastMaxValue = 0;

    private MenuItem menuDone;

    private CrystalSeekbar seekbarController;

    private boolean isValidVideo = true, isVideoEnded;

    private android.os.Handler seekHandler;

    private long currentDuration, lastClickedTime;

    private String outputPath, lastRequestId;

    private int trimType;

    private long fixedGap, minGap, minFromGap, maxToGap;

    private boolean hidePlayerSeek;

    private CustomProgressView progressView;

    private String fileName;

    private Uri realUri;

    private CompressOption compressOption;

    private MediaTransformer mediaTransformer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_video_trimmer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpToolBar(getSupportActionBar(), getString(R.string.txt_edt_video));
        toolbar.setNavigationOnClickListener(v -> finish());
        progressView = new CustomProgressView(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        playerView = findViewById(R.id.player_view_lib);
        imagePlayPause = findViewById(R.id.image_play_pause);
        seekbar = findViewById(R.id.range_seek_bar);
        txtStartDuration = findViewById(R.id.txt_start_duration);
        txtEndDuration = findViewById(R.id.txt_end_duration);
        seekbarController = findViewById(R.id.seekbar_controller);
        ImageView imageOne = findViewById(R.id.image_one);
        ImageView imageTwo = findViewById(R.id.image_two);
        ImageView imageThree = findViewById(R.id.image_three);
        ImageView imageFour = findViewById(R.id.image_four);
        ImageView imageFive = findViewById(R.id.image_five);
        ImageView imageSix = findViewById(R.id.image_six);
        ImageView imageSeven = findViewById(R.id.image_seven);
        ImageView imageEight = findViewById(R.id.image_eight);
        imageViews = new ImageView[]{imageOne, imageTwo, imageThree,
                imageFour, imageFive, imageSix, imageSeven, imageEight};
        seekHandler = new Handler(getMainLooper());
        mediaTransformer = new MediaTransformer(this);
        initPlayer();
        if (checkStoragePermission())
            setDataInView();
    }

    private void setUpToolBar(ActionBar actionBar, String title) {
        try {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SettingUp exoplayer
     **/
    private void initPlayer() {
        try {
            videoPlayer = new SimpleExoPlayer.Builder(this).build();
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            playerView.setPlayer(videoPlayer);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.CONTENT_TYPE_MOVIE)
                        .build();
                videoPlayer.setAudioAttributes(audioAttributes, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDataInView() {
        try {
            uri = Uri.parse(getIntent().getStringExtra(TrimVideo.TRIM_VIDEO_URI));
            realUri = uri;
            uri = Uri.parse(FileUtils.getPath(this, uri));
            LogMessage.v("VideoUri:: " + uri);
            totalDuration = TrimmerUtils.getDuration(this, uri);
            imagePlayPause.setOnClickListener(v ->
                    onVideoClicked());
            Objects.requireNonNull(playerView.getVideoSurfaceView()).setOnClickListener(v ->
                    onVideoClicked());
            initTrimData();
            buildMediaSource(uri);
            loadThumbnails();
            setUpSeekBar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTrimData() {
        try {
            TrimVideoOptions trimVideoOptions = getIntent().getParcelableExtra(TrimVideo.TRIM_VIDEO_OPTION);
            assert trimVideoOptions != null;
            trimType = TrimmerUtils.getTrimType(trimVideoOptions.trimType);
            fileName = trimVideoOptions.fileName;
            hidePlayerSeek = trimVideoOptions.hideSeekBar;
            compressOption = trimVideoOptions.compressOption;
            fixedGap = trimVideoOptions.fixedDuration;
            fixedGap = fixedGap != 0 ? fixedGap : totalDuration;
            minGap = trimVideoOptions.minDuration;
            minGap = minGap != 0 ? minGap : totalDuration;
            if (trimType == 3) {
                minFromGap = trimVideoOptions.minToMax[0];
                maxToGap = trimVideoOptions.minToMax[1];
                minFromGap = minFromGap != 0 ? minFromGap : totalDuration;
                maxToGap = maxToGap != 0 ? maxToGap : totalDuration;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void onVideoClicked() {
        try {
            if (isVideoEnded) {
                seekTo(lastMinValue);
                videoPlayer.setPlayWhenReady(true);
                imagePlayPause.setVisibility(View.GONE);
                return;
            }
            if ((currentDuration - lastMaxValue) > 0)
                seekTo(lastMinValue);
            videoPlayer.setPlayWhenReady(!videoPlayer.getPlayWhenReady());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seekTo(long sec) {
        if (videoPlayer != null)
            videoPlayer.seekTo(sec * 1000);
    }

    private void buildMediaSource(Uri mUri) {
        try {
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, getString(R.string.app_name));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(mUri));
            videoPlayer.addMediaSource(mediaSource);
            videoPlayer.prepare();
            videoPlayer.setPlayWhenReady(true);
            videoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                    imagePlayPause.setVisibility(playWhenReady ? View.GONE :
                            View.VISIBLE);
                }

                @Override
                public void onPlaybackStateChanged(int state) {
                    switch (state) {
                        case Player.STATE_ENDED:
                            LogMessage.v("onPlayerStateChanged: Video ended.");
                            imagePlayPause.setVisibility(View.VISIBLE);
                            isVideoEnded = true;
                            break;
                        case Player.STATE_READY:
                            isVideoEnded = false;
                            startProgress();
                            LogMessage.v("onPlayerStateChanged: Ready to play.");
                            break;
                        default:
                            break;
                        case Player.STATE_BUFFERING:
                            LogMessage.v("onPlayerStateChanged: STATE_BUFFERING.");
                            break;
                        case Player.STATE_IDLE:
                            LogMessage.v("onPlayerStateChanged: STATE_IDLE.");
                            break;
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     *  loading thumbnails
     * */
    private void loadThumbnails() {
        try {
            long diff = totalDuration / 8;
            int sec = 1;
            for (ImageView img : imageViews) {
                long interval = (diff * sec) * 1000000;
                RequestOptions options = new RequestOptions().frame(interval);
                Glide.with(this)
                        .load(realUri)
                        .apply(options)
                        .transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(img);
                if (sec < totalDuration)
                    sec++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpSeekBar() {
        seekbar.setVisibility(View.VISIBLE);
        txtStartDuration.setVisibility(View.VISIBLE);
        txtEndDuration.setVisibility(View.VISIBLE);

        seekbarController.setMaxValue(totalDuration).apply();
        seekbar.setMaxValue(totalDuration).apply();
        seekbar.setMaxStartValue((float) totalDuration).apply();
        if (trimType == 1) {
            seekbar.setFixGap(fixedGap).apply();
            lastMaxValue = totalDuration;
        } else if (trimType == 2) {
            seekbar.setMaxStartValue((float) minGap);
            seekbar.setGap(minGap).apply();
            lastMaxValue = totalDuration;
        } else if (trimType == 3) {
            seekbar.setMaxStartValue((float) maxToGap);
            seekbar.setGap(minFromGap).apply();
            lastMaxValue = maxToGap;
        } else {
            seekbar.setGap(2).apply();
            lastMaxValue = totalDuration;
        }
        if (hidePlayerSeek)
            seekbarController.setVisibility(View.GONE);

        seekbar.setOnRangeSeekbarFinalValueListener((minValue, maxValue) -> {
            if (!hidePlayerSeek)
                seekbarController.setVisibility(View.VISIBLE);
        });

        seekbar.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
            long minVal = (long) minValue;
            long maxVal = (long) maxValue;
            if (lastMinValue != minVal) {
                seekTo((long) minValue);
                if (!hidePlayerSeek)
                    seekbarController.setVisibility(View.INVISIBLE);
            }
            lastMinValue = minVal;
            lastMaxValue = maxVal;
            txtStartDuration.setText(TrimmerUtils.formatSeconds(minVal));
            txtEndDuration.setText(TrimmerUtils.formatSeconds(maxVal));
       /*    MediaTransformer mediaTransformer= new MediaTransformer(this);
            LogMessage.v("Estimated size "+mediaTransformer.getEstimatedTargetVideoSize(
                    realUri,getVideoFormat(),null
            ));*/
            if (trimType == 3)
                setDoneColor(minVal, maxVal);
        });

        seekbarController.setOnSeekbarFinalValueListener(value -> {
            long value1 = (long) value;
            if (value1 < lastMaxValue && value1 > lastMinValue) {
                seekTo(value1);
                return;
            }
            if (value1 > lastMaxValue)
                seekbarController.setMinStartValue((int) lastMaxValue).apply();
            else if (value1 < lastMinValue) {
                seekbarController.setMinStartValue((int) lastMinValue).apply();
                if (videoPlayer.getPlayWhenReady())
                    seekTo(lastMinValue);
            }
        });
    }

    /**
     * will be called whenever seekBar range changes
     * it checks max duration is exceed or not.
     * and disabling and enabling done menuItem
     *
     * @param minVal left thumb value of seekBar
     * @param maxVal right thumb value of seekBar
     */
    private void setDoneColor(long minVal, long maxVal) {
        try {
            if (menuDone == null)
                return;
            //changed value is less than maxDuration
            if ((maxVal - minVal) <= maxToGap) {
                menuDone.getIcon().setColorFilter(
                        new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorWhite)
                                , PorterDuff.Mode.SRC_IN)
                );
                isValidVideo = true;
            } else {
                menuDone.getIcon().setColorFilter(
                        new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorWhiteLt)
                                , PorterDuff.Mode.SRC_IN)
                );
                isValidVideo = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PER_REQ_CODE) {
            if (isPermissionOk(grantResults))
                setDataInView();
            else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaTransformer.release();
        if (videoPlayer != null)
            videoPlayer.release();
        if (progressView != null && progressView.isShowing())
            progressView.dismiss();
        stopRepeatingTask();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuDone = menu.findItem(R.id.action_done);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            //prevent multiple clicks
            if (SystemClock.elapsedRealtime() - lastClickedTime < 800)
                return true;
            lastClickedTime = SystemClock.elapsedRealtime();
            trimVideo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void trimVideo() {
        //not exceed given maxDuration if has given
        if (isValidVideo) {
            File outputFile = getOutputFile();
            outputPath = String.valueOf(outputFile);
            LogMessage.v("outputPath::" + outputPath);
            LogMessage.v("sourcePath::" + uri);
            videoPlayer.setPlayWhenReady(false);
            lastRequestId = UUID.randomUUID().toString();
            MediaRange mediaRange = new MediaRange(
                    TimeUnit.MILLISECONDS.toMicros(lastMinValue * 1000),
                    TimeUnit.MILLISECONDS.toMicros(lastMaxValue * 1000));
            try {
                TransformationOptions transformationOptions = new TransformationOptions.Builder()
                        .setGranularity(MediaTransformer.GRANULARITY_DEFAULT)
                        .setSourceMediaRange(mediaRange)
                        .build();
                mediaTransformer.transform(
                        lastRequestId, realUri,
                        outputFile.getPath(),
                        getVideoFormat(),
                        null,
                        transformListener,
                        transformationOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            Toast.makeText(this, getString(R.string.txt_smaller) + " " + TrimmerUtils.getLimitedTimeFormatted(maxToGap), Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private MediaFormat getVideoFormat() {
        //No compression option
        if (compressOption == null) {
            LogMessage.v("No compression option used");
            return null;
        }
        //Default compress option
        else if (compressOption.getBitRate() <= 0 && compressOption.getFrameRate() == 30
                && compressOption.getHeight() <= 0 && compressOption.getWidth() <= 0) {
            LogMessage.v("Default compression option used");
            return getDefaultVideoFormat();
        } else {
            LogMessage.v("Custom compression option used");
            int[] widthHeight = TrimmerUtils.getVideoWidthHeight(this, uri);
            int width = compressOption.getWidth();
            int height = compressOption.getHeight();
            if (width <= 0 || height <= 0) {
                width = widthHeight[0];
                height = widthHeight[1];
                if (width > 800) {
                    width = width / 2;
                    height = height / 2;
                }
            }
            MediaFormat mediaFormat = new MediaFormat();
            String mimeType = CodecUtils.MIME_TYPE_VIDEO_AVC;
            mediaFormat.setString(MediaFormat.KEY_MIME, mimeType);
            mediaFormat.setInteger(MediaFormat.KEY_WIDTH, width);
            mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, height);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,
                    compressOption.getBitRate());
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, compressOption.getFrameRate());
            return mediaFormat;

        }
    }

    private MediaFormat getDefaultVideoFormat() {
        int[] widthHeight = TrimmerUtils.getVideoWidthHeight(this, uri);
        int width = widthHeight[0];
        int height = widthHeight[1];
        if (width > 800) {
            width = width / 2;
            height = height / 2;
        }
        MediaFormat mediaFormat = new MediaFormat();
        String mimeType = CodecUtils.MIME_TYPE_VIDEO_AVC;
        mediaFormat.setString(MediaFormat.KEY_MIME, mimeType);
        mediaFormat.setInteger(MediaFormat.KEY_WIDTH, width);
        mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 2000000);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        return mediaFormat;
    }

    private final TransformationListener transformListener = new TransformationListener() {
        @Override
        public void onStarted(@NonNull String id) {
            LogMessage.v("onStarted");
            showProcessingDialog();
        }

        @Override
        public void onProgress(@NonNull String id, float progress) {
            LogMessage.v("onProgress " + progress);
            ProgressBar progressBar = dialog.findViewById(R.id.progress_trimmer);
            progressBar.setProgress((int) (progress * 100));
        }

        @Override
        public void onCompleted(@NonNull String id, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
            LogMessage.v("onCompleted");
            dialog.dismiss();
            Intent intent = new Intent();
            intent.putExtra(TrimVideo.TRIMMED_VIDEO_PATH, outputPath);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void onCancelled(@NonNull String id, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
            LogMessage.v("onCancelled");
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
        }

        @Override
        public void onError(@NonNull String id, @Nullable Throwable cause, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
            LogMessage.v("onError " + cause);
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            runOnUiThread(() ->
                    Toast.makeText(ActVideoTrimmer.this, "Failed to trim", Toast.LENGTH_SHORT).show());
        }
    };

    private String getFileDateTime() {
        Calendar calender = Calendar.getInstance();
        return calender.get(Calendar.YEAR) + "_" +
                calender.get(Calendar.MONTH) + "_" +
                calender.get(Calendar.DAY_OF_MONTH) + "_" +
                calender.get(Calendar.HOUR_OF_DAY) + "_" +
                calender.get(Calendar.MINUTE) + "_" +
                calender.get(Calendar.SECOND);
    }

    private File getOutputFile() {
        String path = getExternalCacheDir().getAbsolutePath() +"/" /*+ "/VideoCompress"*/;
        /*if (!new File(path).exists()) {

            new File(path).mkdir();

        }*/
        String fName = "trimmed_video_";
        if (fileName != null && !fileName.isEmpty())
            fName = fileName;
        File newFile = new File(path + File.separator +
                (fName) + getFileDateTime() + "." + TrimmerUtils.getFileExtension(this, uri));
        return newFile;
    }

/*    private String getFileName() {
        String path = getExternalFilesDir("Download").getPath();
        String fName = "trimmed_video_";
        if (fileName != null && !fileName.isEmpty())
            fName = fileName;
        File newFile = new File(path + File.separator +
                (fName) +getFileDateTime()+ "." + TrimmerUtils.getFileExtension(this, uri));
        return String.valueOf(newFile);
    }*/

    private void showProcessingDialog() {
        try {
            if (dialog == null) {
                dialog = new Dialog(this);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.alert_convert);
                TextView txtCancel = dialog.findViewById(R.id.txt_cancel);
                dialog.setCancelable(false);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                txtCancel.setOnClickListener(v ->
                        mediaTransformer.cancel(lastRequestId));
            }
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return checkPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION);
        } else
            return checkPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE);

    }

    private boolean checkPermission(String... permissions) {
        boolean allPermitted = false;
        for (String permission : permissions) {
            allPermitted = (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED);
            if (!allPermitted)
                break;
        }
        if (allPermitted)
            return true;
        ActivityCompat.requestPermissions(this, permissions,
                PER_REQ_CODE);
        return false;
    }


    private boolean isPermissionOk(int... results) {
        boolean isAllGranted = true;
        for (int result : results) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                isAllGranted = false;
                break;
            }
        }
        return isAllGranted;
    }

    void startProgress() {
        updateSeekbar.run();
    }

    void stopRepeatingTask() {
        seekHandler.removeCallbacks(updateSeekbar);
    }

    Runnable updateSeekbar = new Runnable() {
        @Override
        public void run() {
            try {
                currentDuration = videoPlayer.getCurrentPosition() / 1000;
                if (!videoPlayer.getPlayWhenReady())
                    return;
                if (currentDuration <= lastMaxValue)
                    seekbarController.setMinStartValue((int) currentDuration).apply();
                else
                    videoPlayer.setPlayWhenReady(false);
            } finally {
                seekHandler.postDelayed(updateSeekbar, 1000);
            }
        }
    };

}
