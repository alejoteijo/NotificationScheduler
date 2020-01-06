package com.example.notificationscheduler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private JobScheduler mScheduler;
    private Switch mDeviceIdleSwitch;
    private Switch mDeviceChargingSwitch;
    private SeekBar mSeekBar;
    public static final int JOB_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        mDeviceIdleSwitch = findViewById(R.id.sw_idle);
        mDeviceChargingSwitch = findViewById(R.id.sw_charging);
        mSeekBar = findViewById(R.id.skb_deadline);

        final TextView seekBarProgress = findViewById(R.id.seekBarProgress);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i>0)
                    seekBarProgress.setText(i+ " s");
                else
                    seekBarProgress.setText(getText(R.string.not_set));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void scheduleJob(View v){
        int seekBarInteger = mSeekBar.getProgress();
        boolean seekBarSet = seekBarInteger > 0;

        RadioGroup networkOptions = findViewById(R.id.network_type);
        int selectedNetWorkID = networkOptions.getCheckedRadioButtonId();

        int selectedNetWorkOption = JobInfo.NETWORK_TYPE_NONE;

        switch (selectedNetWorkID){
            case R.id.rbtn_none:
                selectedNetWorkOption = JobInfo.NETWORK_TYPE_NONE;
                break;
            case R.id.rbtn_any:
                selectedNetWorkOption = JobInfo.NETWORK_TYPE_ANY;
                break;
            case R.id.rbtn_wifi:
                selectedNetWorkOption = JobInfo.NETWORK_TYPE_UNMETERED;
                break;
        }

        ComponentName serviceName = new ComponentName(getPackageName()
                ,NotificationJobService.class.getName() );
        JobInfo.Builder builder = new JobInfo.Builder(
                JOB_ID, serviceName)
                .setRequiredNetworkType(selectedNetWorkOption)
                .setRequiresDeviceIdle(mDeviceIdleSwitch.isChecked())
                .setRequiresCharging(mDeviceChargingSwitch.isChecked());

        if (seekBarSet){
            builder.setOverrideDeadline(seekBarInteger * 1000);
        }

        boolean constraintSet = selectedNetWorkOption != JobInfo.NETWORK_TYPE_NONE
                || mDeviceChargingSwitch.isChecked() || mDeviceIdleSwitch.isChecked() || seekBarSet;

        if(constraintSet){
        JobInfo myJobInfo = builder.build();
        mScheduler.schedule(myJobInfo);
        Toast.makeText(this, "Job Scheduled, job will run when "
                +"the constraints are met.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Please set al least one contraint"
            ,Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelJobs(View v){
        if (mScheduler != null){
            mScheduler.cancelAll();
            mScheduler = null;
            Toast.makeText(this, "Jobs cancelled", Toast.LENGTH_SHORT).show();
        }
    }


}
