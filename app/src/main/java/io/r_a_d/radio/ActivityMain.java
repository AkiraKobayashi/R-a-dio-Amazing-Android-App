package io.r_a_d.radio;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;

public class ActivityMain extends AppCompatActivity {

    private boolean playing = false;
    private SimpleExoPlayer sep;
    private Integer api_update_delay = 10000;
    private ViewPager viewPager;
    private JSONScraperTask jsonTask = new JSONScraperTask(this);
    private String radio_url = "https://stream.r-a-d.io/main.mp3";
    private String api_url = "https://r-a-d.io/api";
    public JSONObject current_ui_json;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == 0){
                updateUI();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new CustomPagerAdapter(this));
        viewPager.setOffscreenPageLimit(3);
        setupMediaPlayer();
        handler.postDelayed(new Runnable(){
            public void run(){
                scrapeJSON(api_url);
                handler.postDelayed(this, api_update_delay);
            }
        }, api_update_delay);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sep.stop();
        sep.release();
        sep = null;
    }

    public void setupMediaPlayer() {
        TrackSelector tSelector = new DefaultTrackSelector();
        LoadControl lc = new DefaultLoadControl();
        sep = ExoPlayerFactory.newSimpleInstance(this, tSelector, lc);

        DataSource.Factory dsf = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "R/a/dio-Android-App"));
        ExtractorsFactory extractors = new DefaultExtractorsFactory();
        MediaSource audioSource = new ExtractorMediaSource(Uri.parse(radio_url), dsf, extractors, null, null);

        sep.prepare(audioSource);
    }

    public void updateUI(){
        try {
            View now_playing = viewPager.getChildAt(0);
            TextView np = (TextView)now_playing.findViewById(R.id.tags);
            np.setText(current_ui_json.getString("np"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void scrapeJSON(String urlToScrape){
        jsonTask.cancel(false);
        jsonTask = new JSONScraperTask(this);
        jsonTask.execute(urlToScrape);
    }

    public void setUIJSON(String jsonString) throws JSONException {
        current_ui_json = new JSONObject(new JSONObject(jsonString).getString("main"));
        updateUI();
    }

    public void togglePlayPause(View v) {
        ImageButton img = (ImageButton)v.findViewById(R.id.play_pause);
        if(!playing){
            img.setImageResource(R.drawable.pause_small);
            playing = true;
            sep.seekToDefaultPosition();
            sep.setPlayWhenReady(playing);
        } else {
            img.setImageResource(R.drawable.arrow_small);
            playing = false;
            sep.setPlayWhenReady(playing);
        }
    }
}
