package com.openrec.fragments.main;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.openrec.R;
import com.openrec.preferences.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import static com.openrec.activities.MainActivity.RECORD_AUDIO_REQUEST_CODE;
import static com.openrec.preferences.PreferenceManager.FORMAT_PREF;

public class RecordFragment extends Fragment {

    private Chronometer chronometer;
    private ImageButton recordButton, pauseButton, saveButton;
    private MediaRecorder recorder = null;
    private ImageView discardButton, pulseAnimImg1, pulseAnimImg2, pulseAnimImg3;
    private static long pauseOffset;
    private boolean isRecording, isPaused;
    private ObjectAnimator pulseAnimator1, pulseAnimator2, pulseAnimator3;
    private Animatable animatable;
    private PreferenceManager preferenceManager;
    private String appDir, tempFileName;

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        preferenceManager=new PreferenceManager(requireContext());
        chronometer=view.findViewById(R.id.chronometer);
        recordButton=view.findViewById(R.id.start_record);
        pauseButton=view.findViewById(R.id.pause_record);
        saveButton=view.findViewById(R.id.save_record);
        discardButton=view.findViewById(R.id.discard_record);
        pulseAnimImg1 =view.findViewById(R.id.pulse_anim_img1);
        pulseAnimImg2 =view.findViewById(R.id.pulse_anim_img2);
        pulseAnimImg3 =view.findViewById(R.id.pulse_anim_img3);
        final AudioManager audioManager=(AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);

        // CREATE records FOLDER IN /Android/data/package-name/
        appDir = Objects.requireNonNull(requireContext()
                .getExternalFilesDir("/records"))
                .getAbsolutePath()
                + "/";

    /*============================================================================================*/



        // ON CLICK RECORD BUTTON
        recordButton.setOnClickListener(view1 -> {

            // RECORD AUDIO PERMISSION
            // IF NOT GRANTED, ASK FOR PERMISSION
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[] {Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
            }

            // IF PERMISSION GRANTED, START RECORDING
            else{

                // IF ON CALL, DISABLE RECORDING FEATURE
                if (audioManager.getMode()== AudioManager.MODE_IN_CALL){
                    Toast.makeText(requireContext(), getString(R.string.mic_used_by_other_app), Toast.LENGTH_LONG).show();
                }

                else {
                    // START RECORD
                    if (!isRecording && !isPaused){
                        Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);


                        // SHOW PAUSE BUTTON IF API 24 AND HIGHER
                        // ELSE SHOW STOP BUTTON
                        if (Build.VERSION.SDK_INT>=24) {
                            recordButton.setVisibility(View.GONE);
                            pauseButton.setVisibility(View.VISIBLE);
                            pauseButton.startAnimation(fadeIn);
                        }
                        else {
                            recordButton.setImageResource(R.drawable.stop_recording);
                        }

                        // SHOW DISCARD BUTTON
                        discardButton.setVisibility(View.VISIBLE);
                        saveButton.setVisibility(View.VISIBLE);
                        discardButton.startAnimation(fadeIn);
                        saveButton.startAnimation(fadeIn);

                        StartChronometer();
                        StartRecording();
                        StartPulseAnim();
                        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        isRecording =true;

                    }

                    // STOP RECORD
                    // ONLY FOR API=23
                    else {
                        recordButton.setImageResource(R.drawable.create_record);

                        PauseChronometer();
                        StopRecording();
                        StopPulseAnim();
                        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        isRecording=false;

                        recordButton.setEnabled(false); // DISABLE RECORD BUTTON WHEN RECORDING STOPPED
                    }
                }
            }
        });

        // ON CLICK PAUSE BUTTON
        // ONLY FOR API>=24
        pauseButton.setOnClickListener(view12 -> {

            // PAUSE RECORD
            if (!isPaused){
                pauseButton.setImageResource(R.drawable.avd_pause_to_play);
                PauseRecording();
                PauseChronometer();
                StopPulseAnim();
                isPaused=true;
                isRecording=false;
            }

            // RESUME RECORD
            else {
                pauseButton.setImageResource(R.drawable.avd_play_to_pause);
                ResumeRecording();
                StartChronometer();
                StartPulseAnim();
                isPaused=false;
                isRecording=true;
            }
            animatable=(Animatable) pauseButton.getDrawable();
            animatable.start();
        });

        // ON CLICK SAVE BUTTON
        saveButton.setOnClickListener(view11 ->
                AudioTitleBottomSheet());

        // ON CLICK DISCARD BUTTON
        discardButton.setOnClickListener(view13 ->
                DiscardAudioDialog());

    }

    // START CHRONOMETER
    private void StartChronometer(){
        // START CHRONOMETER FROM 00:00 WHEN FIRST CLICKED
        // IF PAUSED, START FROM PAUSED TIME, AND NOT FROM 00:00
        chronometer.setBase(SystemClock.elapsedRealtime()-pauseOffset);
        chronometer.start();
    }

    // PAUSE CHRONOMETER
    private void PauseChronometer(){
        chronometer.stop();
        pauseOffset=SystemClock.elapsedRealtime()-chronometer.getBase();
    }

    // RESET CHRONOMETER
    private void ResetChronometer(){
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset=0;
    }

    // START RECORDING
    private void StartRecording(){
        recorder = new MediaRecorder();

        String randomNumber = String.valueOf(new Random().nextInt(10000));
        tempFileName = appDir + randomNumber;

        // CHECK WHETHER TO RECORD IN AAC OR AMR
        if (preferenceManager.getInt(FORMAT_PREF)==R.id.option_default
            || preferenceManager.getInt(FORMAT_PREF)==0){

            // SET AUDIO SOURCE, FORMAT AND ENCODER
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        }
        else {

            // SET AUDIO SOURCE, FORMAT AND ENCODER
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }

        // SAVE AUDIO NOTE TEMPORARILY IN LOCATION
        recorder.setOutputFile(tempFileName);

        try {
            recorder.prepare();
        }
        catch (IOException e) {
            Log.e("Audio Record","prepare() failed");
        }

        recorder.start(); // START RECORDER
    }

    // PAUSE RECORDING
    private void PauseRecording(){
        if (Build.VERSION.SDK_INT>=24){
            recorder.pause();
        }
    }

    // RESUME RECORDING
    private void ResumeRecording(){
        if (Build.VERSION.SDK_INT>=24){
            recorder.resume();
        }
    }

    // STOP RECORDING
    private void StopRecording(){
        try {
            recorder.stop(); // STOP RECORDING
        }
        catch (RuntimeException e){
            Log.e("Audio Record","stop() failed");
        }

        recorder.release(); // RELEASE RECORDER, CAN'T BE REUSED
        recorder=null;
    }

    // START PULSE ANIMATION
    private void StartPulseAnim(){
        pulseAnimImg1.setVisibility(View.VISIBLE);
        pulseAnimImg2.setVisibility(View.VISIBLE);
        pulseAnimImg3.setVisibility(View.VISIBLE);

        pulseAnimator1 = ObjectAnimator.ofPropertyValuesHolder(
                pulseAnimImg1,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 2.4f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 2.4f),
                PropertyValuesHolder.ofFloat(View.ALPHA,1.0f,0f));
        pulseAnimator1.setDuration(5000);
        pulseAnimator1.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimator1.setRepeatMode(ObjectAnimator.RESTART);
        pulseAnimator1.setInterpolator(new LinearOutSlowInInterpolator());
        pulseAnimator1.start();

        pulseAnimator2 = ObjectAnimator.ofPropertyValuesHolder(
                pulseAnimImg2,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 2.1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 2.1f),
                PropertyValuesHolder.ofFloat(View.ALPHA,1.0f,0f));
        pulseAnimator2.setDuration(5000);
        pulseAnimator2.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimator2.setRepeatMode(ObjectAnimator.RESTART);
        pulseAnimator2.setInterpolator(new LinearOutSlowInInterpolator());
        pulseAnimator2.setStartDelay(3000);
        pulseAnimator2.start();

        pulseAnimator3 = ObjectAnimator.ofPropertyValuesHolder(
                pulseAnimImg3,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.5f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.5f),
                PropertyValuesHolder.ofFloat(View.ALPHA,1.0f,0f));
        pulseAnimator3.setDuration(5000);
        pulseAnimator3.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimator3.setRepeatMode(ObjectAnimator.RESTART);
        pulseAnimator3.setInterpolator(new LinearOutSlowInInterpolator());
        pulseAnimator2.setStartDelay(1200);
        pulseAnimator3.start();
    }

    // STOP PULSE ANIMATION
    private void StopPulseAnim(){
        pulseAnimator1.cancel();
        pulseAnimator2.cancel();
        pulseAnimator3.cancel();
        pulseAnimImg1.setScaleX(1);
        pulseAnimImg1.setScaleY(1);
        pulseAnimImg2.setScaleX(1);
        pulseAnimImg2.setScaleY(1);
        pulseAnimImg3.setScaleX(1);
        pulseAnimImg3.setScaleY(1);
        pulseAnimImg1.setVisibility(View.GONE);
        pulseAnimImg2.setVisibility(View.GONE);
        pulseAnimImg3.setVisibility(View.GONE);
    }

    // SHOW AUDIO TITLE BOTTOM SHEET
    private void AudioTitleBottomSheet(){

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        @SuppressLint("InflateParams") View view  = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_text, null);
        bottomSheetDialog.setContentView(view);

        final TextInputEditText recordTitleInput;
        final TextView positiveButton;
        recordTitleInput=view.findViewById(R.id.bottom_sheet_edit_text_input);
        positiveButton=view.findViewById(R.id.dialog_positive_button);
        positiveButton.setEnabled(false);

        final TextWatcher recordingTitleTextWatcher= new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            // IF TEXT IS EMPTY, DISABLE POSITIVE BUTTON
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                positiveButton.setEnabled(!Objects.requireNonNull(recordTitleInput.getText()).toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };

        // TITLE
        ((TextView)view.findViewById(R.id.bottom_sheet_title)).setText(R.string.save_recording);

        // HINT
        ((TextInputLayout)view.findViewById(R.id.bottom_sheet_edit_text_layout)).setHint(getString(R.string.files_title));

        // TITLE EDIT TEXT
        recordTitleInput.addTextChangedListener(recordingTitleTextWatcher);

        // POSITIVE BUTTON
        positiveButton.setOnClickListener(view1 -> {

            if (recorder!=null){
                PauseChronometer();
                StopRecording();
                StopPulseAnim();
                isRecording=false;
                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                if (Build.VERSION.SDK_INT >= 24) {
                    pauseButton.setVisibility(View.GONE);
                    pauseButton.setImageResource(R.drawable.pause_recording);
                    recordButton.setVisibility(View.VISIBLE);
                    isPaused = false;
                }
                else if (Build.VERSION.SDK_INT == 23) {
                    recordButton.setEnabled(true);
                    recordButton.setImageResource(R.drawable.create_record);
                }
            }

            // SAVE RECORD IN EXTERNAL STORAGE
            File newFileName;

            if (preferenceManager.getInt(FORMAT_PREF)==R.id.option_default
                    || preferenceManager.getInt(FORMAT_PREF)==0){

                newFileName = new File(appDir
                        + recordTitleInput.getText()
                        + ".amr");
            }
            else {
                newFileName = new File(appDir
                        + recordTitleInput.getText()
                        + ".aac");
            }

            boolean saved=new File(tempFileName).renameTo(newFileName);

            saveButton.setVisibility(View.GONE);
            discardButton.setVisibility(View.GONE);
            ResetChronometer();
            bottomSheetDialog.dismiss();

            if (!recordButton.isEnabled()){
                recordButton.setEnabled(true); // ENABLE RECORD IF DISABLED
            }

            if (saved){
                Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }

        });

        // NEGATIVE BUTTON
        view.findViewById(R.id.dialog_negative_button)
                .setOnClickListener(view12 ->
                        bottomSheetDialog.cancel());

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

    // SHOW DISCARD AUDIO DIALOG
    private void DiscardAudioDialog(){
        final Dialog dialog = new Dialog(requireContext(), R.style.DialogTheme);
        dialog.setCancelable(true);

        @SuppressLint("InflateParams") View view  = getLayoutInflater().inflate(R.layout.dialog_alert, null);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(ContextCompat
                .getDrawable(requireContext(),R.drawable.dialog_shape));
        dialog.setContentView(view);

        // TITLE
        ((TextView)view.findViewById(R.id.dialog_title)).setText(R.string.discard_audio_title);

        // SUBTITLE
        ((TextView)view.findViewById(R.id.dialog_subtitle)).setText(R.string.discard_audio_subtitle);

        // POSITIVE BUTTON
        final TextView positiveButton=view.findViewById(R.id.dialog_positive_button);
        positiveButton.setText(R.string.discard);
        positiveButton.setTextColor(getResources().getColor(R.color.dialogDeleteButtonColor,null));
        positiveButton.findViewById(R.id.dialog_positive_button)
                .setOnClickListener(view1 -> {

                    if (recorder!=null){
                        PauseChronometer();
                        StopRecording();
                        StopPulseAnim();
                        isRecording=false;
                        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        if (Build.VERSION.SDK_INT >= 24) {
                            pauseButton.setVisibility(View.GONE);
                            pauseButton.setImageResource(R.drawable.pause_recording);
                            recordButton.setVisibility(View.VISIBLE);
                            isPaused = false;
                        }
                        else if (Build.VERSION.SDK_INT == 23) {
                            recordButton.setEnabled(true);
                            recordButton.setImageResource(R.drawable.create_record);
                        }
                    }

                    boolean discarded=new File(tempFileName).delete();

                    saveButton.setVisibility(View.GONE);
                    discardButton.setVisibility(View.GONE);
                    ResetChronometer();
                    dialog.dismiss();

                    if (!recordButton.isEnabled()){
                        recordButton.setEnabled(true); // ENABLE RECORD IF DISABLED
                    }

                    if (discarded){
                        Toast.makeText(requireContext(), getString(R.string.discarded), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
                    }
                });

        // NEGATIVE BUTTON
        view.findViewById(R.id.dialog_negative_button)
                .setOnClickListener(view12 ->
                        dialog.cancel());

        // SHOW DIALOG WITH CUSTOM ANIMATION
        Objects.requireNonNull(dialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
    }

    // IF APP IS IN BACKGROUND OR WHEN FRAGMENT DESTROYED, STOP RECORDING
    @Override
    public void onStop() {
        super.onStop();
        if (recorder!=null){
            recordButton.setImageResource(R.drawable.create_record);

            // HIDE PAUSE BUTTON IF API 24 AND HIGHER
            if (Build.VERSION.SDK_INT>=24) {
                pauseButton.setVisibility(View.GONE);
                recordButton.setVisibility(View.VISIBLE);
                recordButton.setEnabled(false); // DISABLE RECORD BUTTON WHEN RECORDING STOPPED
            }

            PauseChronometer();
            StopRecording();
            StopPulseAnim();
            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            isRecording=false;
            recordButton.setEnabled(false);
        }
    }

    // RESET CHRONOMETER WHEN FRAGMENT DESTROYED
    // OR ELSE CHRONOMETER WILL START FROM PAUSED TIME
    // WHEN FRAGMENT REOPENED WITHOUT FINISHING ACTIVITY
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ResetChronometer();
    }
}
