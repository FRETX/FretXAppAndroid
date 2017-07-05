package fretx.version4.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import fretx.version4.R;
import fretx.version4.onboarding.light.HelpDialog;

public class LightActivity extends BaseActivity {
    private static final int NB_STATE = 4;
    private int state = 0;
    private TextView title;
    private TextView help;
    private ImageView gif;
    private Button ok;
    private SeekBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);

        title = (TextView) findViewById(R.id.title);
        gif = (ImageView) findViewById(R.id.gif);
        help = (TextView) findViewById(R.id.need_help);
        ok = (Button) findViewById(R.id.ok);
        bar = (SeekBar) findViewById(R.id.seekbar);
        bar.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ++state;
                if (state == NB_STATE) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                } else {
                    updateState();
                }
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpDialog.newInstance().show(getSupportFragmentManager(), null);
            }
        });

        updateState();
    }

    @Override
    public void onBackPressed() {
        if (state > 0) {
            --state;
            updateState();
        }
    }

    private void updateState() {
        bar.setProgress(state);
        switch (state) {
            case 0:
                Glide.with(getActivity()).load(R.raw.light0).into(gif);
                title.setText("When your device is \"ON\"");
                break;
            case 1:
                Glide.with(getActivity()).load(R.raw.light1).into(gif);
                title.setText("When your device is \"CONNECTED\"");
                break;
            case 2:
                Glide.with(getActivity()).load(R.raw.light2).into(gif);
                title.setText("When you hit \"SUCCESS\"");
                break;
            case 3:
                Glide.with(getActivity()).load(R.raw.light3).into(gif);
                title.setText("Turning your device \"OFF\"");
                break;
            default:
                title.setText("Oups...");
                break;
        }
    }
}
