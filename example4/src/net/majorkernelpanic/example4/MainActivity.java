package net.majorkernelpanic.example4;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.majorkernelpanic.streaming.MediaStream;
import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.video.VideoQuality;
import net.majorkernelpanic.streaming.video.VideoStream;

public class MainActivity extends Activity implements SurfaceHolder.Callback, Session.Callback {

    private static final String TAG = "example4";

    // TODO: change this IP with the destination IP of the video streaming
    private static final String DESTINATION_IP = "10.34.0.162";

    private byte mediaCodec;

    private SurfaceView surfaceView;
    private Session session;
    private final int BITRATE = 900000; // 1500000
    private final int FRAMERATE = 15; // 30

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaCodec = MediaStream.MODE_MEDIACODEC_API;

        ViewGroup root = (ViewGroup) getLayoutInflater().inflate(R.layout.main_activity_layout, null);

        // create dummy surface to avoid having a preview in the server side
        surfaceView = new SurfaceView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                VideoStream.VIDEO_WIDTH_INPUT, VideoStream.VIDEO_HEIGHT_INPUT);

        root.addView(surfaceView, layoutParams);
        surfaceView.getHolder().addCallback(this);
        surfaceView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (session.isStreaming()) {
                    session.stop();
                    Toast.makeText(MainActivity.this, "Stopping publisher", Toast.LENGTH_SHORT).show();
                } else {
                    session.configure();
                }
                return false;
            }
        });

        setContentView(root);

        initializeSession();
    }

    private void initializeSession() {
        VideoQuality quality = new VideoQuality(
                VideoStream.VIDEO_WIDTH_INPUT, VideoStream.VIDEO_HEIGHT_INPUT,
                FRAMERATE, BITRATE);

        session = SessionBuilder.getInstance()
                .setCallback(this)
                .setSurfaceView(surfaceView)
                .setPreviewOrientation(0)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setAudioQuality(new AudioQuality(16000, 32000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(quality)
                .build();
    }

    private void startStreaming() {
        session.setDestination(DESTINATION_IP);
        session.getVideoTrack().setStreamingMethod(mediaCodec);
        Log.i(TAG, "Streaming to IP: " + DESTINATION_IP);
        if (!session.isStreaming()) {
            Log.i(TAG, "Configure session!");
            session.configure();
        } else {
            session.stop();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startStreaming();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        session.stop();
    }

    @Override
    public void onBitrateUpdate(long bitrate) {

    }

    @Override
    public void onSessionError(int reason, int streamType, Exception e) {

    }

    @Override
    public void onPreviewStarted() {

    }

    @Override
    public void onSessionConfigured() {
        Log.i(TAG, "Session started");
        Toast.makeText(this, "Starting publisher", Toast.LENGTH_SHORT).show();
        session.start();
    }

    @Override
    public void onSessionStarted() {
    }

    @Override
    public void onSessionStopped() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        session.release();
    }
}
