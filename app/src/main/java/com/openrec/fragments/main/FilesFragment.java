package com.openrec.fragments.main;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.openrec.R;
import com.openrec.adapters.FilesItemAdapter;
import com.openrec.models.VoiceFile;
import com.openrec.preferences.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.openrec.utility.Utility.DetailsFormat12;
import static com.openrec.utility.Utility.DetailsFormat24;
import static com.openrec.utility.Utility.InputFullDateFormat;
import static com.openrec.preferences.PreferenceManager.ASC_DSC_PREF;
import static com.openrec.preferences.PreferenceManager.SORT_PREF;
import static com.openrec.preferences.PreferenceManager.VIEW_GRID_PREF;

public class FilesFragment extends Fragment {

    private FilesItemAdapter rAdapter;
    private List<VoiceFile> voiceFileList;
    private String appDir;
    private MediaPlayer mediaPlayer=null;
    private Animatable animatable;
    private ViewStub viewStub;

    public FilesFragment() {
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
        return inflater.inflate(R.layout.fragment_files,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        final RecyclerView recyclerView = view.findViewById(R.id.files_recycler_view);
        final AudioManager audioManager=(AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        voiceFileList = new ArrayList<>();
        final PreferenceManager preferenceManager=new PreferenceManager(requireContext());
        viewStub =view.findViewById(R.id.empty_db_view_stub);
        appDir=Objects.requireNonNull(requireContext()
                .getExternalFilesDir("/records"))
                .getAbsolutePath()
                + "/";

        int id = 1;
        File[] files = new File(appDir).listFiles();
        assert files != null;
            for (File file : files) {
                VoiceFile vf = new VoiceFile();
                vf.setId(id);
                vf.setTitle(file.getName());
                vf.setLastUpdated(InputFullDateFormat().format(new Date(file.lastModified())));
                id++;
                voiceFileList.add(vf);
            }

        rAdapter = new FilesItemAdapter(voiceFileList);

    /*===========================================================================================*/


        // IF DB IS EMPTY SHOW IMAGE AND TEXT,
        // ELSE SHOW RECYCLER VIEW
        if (voiceFileList.size()==0){
            EmptyDB();
        }
        else {

            // LIST OR GRID LAYOUT
            if (!preferenceManager.getBoolean(VIEW_GRID_PREF)) {
                recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
            }
            else {
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            }

            // SORT
            // RECENTLY UPDATED
            if (preferenceManager.getInt(SORT_PREF) == 0
                    || preferenceManager.getInt(SORT_PREF) == R.id.option_default) {

                // ASCENDING/DESCENDING
                if (preferenceManager.getString(ASC_DSC_PREF).equals("descending")) {

                    Collections.sort(voiceFileList, (vf1, vf2) ->
                            vf2.getLastUpdated().compareTo(vf1.getLastUpdated())); // DESCENDING (DEFAULT)
                }
                else {
                    Collections.sort(voiceFileList, (vf1, vf2) ->
                            vf1.getLastUpdated().compareTo(vf2.getLastUpdated())); // ASCENDING
                }
            }

            // ALPHABETICALLY
            else {

                // ASCENDING/DESCENDING
                if (preferenceManager.getString(ASC_DSC_PREF).equals("descending")) {

                    Collections.sort(voiceFileList, (vf1, vf2) ->
                            vf2.getTitle().compareTo(vf1.getTitle())); // DESCENDING (DEFAULT)
                }
                else {
                    Collections.sort(voiceFileList, (vf1, vf2) ->
                            vf1.getTitle().compareTo(vf2.getTitle())); // ASCENDING
                }
            }

            recyclerView.setAdapter(rAdapter);
        }

        // HANDLE CLICK EVENTS OF ITEMS
        rAdapter.setOnItemClickListener(position -> {
            // IF ON CALL, DISABLE PLAY RECORD
            if (audioManager.getMode()== AudioManager.MODE_IN_CALL){
                Toast.makeText(requireContext(), getString(R.string.speaker_used_by_other_app), Toast.LENGTH_LONG).show();
            }

            else {
                MediaPlayerBottomSheet(voiceFileList.get(position).getTitle());
            }
        });

        // HANDLE LONG CLICK EVENTS OF ITEMS
        rAdapter.setOnItemLongClickListener(position ->
                LongClickBottomSheet(voiceFileList.get(position).getTitle(), position));

    }

    // EMPTY DB VIEW STUB
    private void EmptyDB(){
        viewStub.inflate();
        viewStub.setVisibility(View.VISIBLE);
    }

    // MEDIA PLAYER BOTTOM SHEET
    private void MediaPlayerBottomSheet(String title){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        @SuppressLint("InflateParams") View view  = getLayoutInflater().inflate(R.layout.bottom_sheet_media_player, null);
        bottomSheetDialog.setContentView(view);

        final SeekBar seekbar=view.findViewById(R.id.play_recording_seekbar);
        final ImageButton playButton=view.findViewById(R.id.play_button);
        final TextView displayFormat=view.findViewById(R.id.display_format);
        mediaPlayer=new MediaPlayer();

        // TITLE
        ((TextView)view.findViewById(R.id.bottom_sheet_title)).setText(title.substring(0, title.length() - 4));

        // DISPLAY FORMAT
        if (title.endsWith("aac")) {
            displayFormat.setText(getString(R.string.aac));
        }
        else {
            displayFormat.setText(getString(R.string.amr));
        }

        // PLAY RECORD FILE
        try {
            mediaPlayer.setDataSource(new File(appDir+title).getAbsolutePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("Media Player","prepare() failed");
        }

        mediaPlayer.start();

        // END TIME
        ((Chronometer)view.findViewById(R.id.end_chronometer)).setBase(SystemClock.elapsedRealtime()
                - mediaPlayer.getDuration());

        // SEEKBAR
        final Handler seekbarHandler=new Handler(Looper.getMainLooper());
        final Runnable updateSeekbarRunnable;
        seekbar.setMax(mediaPlayer.getDuration());// MAX DURATION FOR SEEKBAR
        updateSeekbarRunnable=new Runnable() {
            @Override
            public void run() {
                seekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this, 5); // SEEKBAR POSITION WILL UPDATE EVERY 5 MS
            }
        };
        seekbarHandler.post(updateSeekbarRunnable);

        // ON SEEKBAR CHANGED BY USER
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress == mediaPlayer.getDuration()){
                        if (mediaPlayer.isPlaying()) {
                            playButton.setImageResource(R.drawable.avd_pause_to_play);
                            animatable = (Animatable) playButton.getDrawable();
                            animatable.start();
                            mediaPlayer.pause();
                        }
                    }
                    else {
                        mediaPlayer.seekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // ON MEDIA PLAYER FINISHED PLAYING
        mediaPlayer.setOnCompletionListener(mp -> {
                playButton.setImageResource(R.drawable.avd_pause_to_play);
                animatable = (Animatable) playButton.getDrawable();
                animatable.start();
                mediaPlayer.pause();
        });

        // PLAY BUTTON
        playButton.setOnClickListener(view1 -> {
            if (mediaPlayer.isPlaying()){
                playButton.setImageResource(R.drawable.avd_pause_to_play);
                mediaPlayer.pause();
            }
            else{
                playButton.setImageResource(R.drawable.avd_play_to_pause);
                mediaPlayer.start();
            }
            animatable=(Animatable) playButton.getDrawable();
            animatable.start();
        });

        bottomSheetDialog.setOnDismissListener(dialogInterface ->{
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            seekbarHandler.removeCallbacks(updateSeekbarRunnable);// STOP HANDLER

        });

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

    // LONG CLICK BOTTOM SHEET
    private void LongClickBottomSheet(String title, int position){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        @SuppressLint("InflateParams") View view  = getLayoutInflater().inflate(R.layout.bottom_sheet_long_click, null);
        bottomSheetDialog.setContentView(view);

        final RelativeLayout rename, delete, details;
        rename=view.findViewById(R.id.rename_holder);
        delete=view.findViewById(R.id.delete_holder);
        details=view.findViewById(R.id.details_holder);

        // RENAME
        rename.setOnClickListener(v -> {
            RenameBottomSheet(title, position);
            bottomSheetDialog.dismiss();
        });

        // DELETE
        delete.setOnClickListener(v -> {
            DeleteDialog(title, position);
            bottomSheetDialog.dismiss();
        });

        // DETAILS
        details.setOnClickListener(v -> {
            DetailsBottomSheet(title, position);
            bottomSheetDialog.dismiss();
        });

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

    // RENAME BOTTOM SHEET
    private void RenameBottomSheet(String title, int position){

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        @SuppressLint("InflateParams") View view  = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_text, null);
        bottomSheetDialog.setContentView(view);

        final TextInputEditText renameInput;
        final TextView positiveButton;
        renameInput=view.findViewById(R.id.bottom_sheet_edit_text_input);
        positiveButton=view.findViewById(R.id.dialog_positive_button);

        final TextWatcher recordingTitleTextWatcher= new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            // IF TEXT IS EMPTY, DISABLE POSITIVE BUTTON
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                positiveButton.setEnabled(!Objects.requireNonNull(renameInput.getText()).toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };

        // TITLE
        ((TextView)view.findViewById(R.id.bottom_sheet_title)).setText(R.string.rename);

        // HINT
        ((TextInputLayout)view.findViewById(R.id.bottom_sheet_edit_text_layout)).setHint(getString(R.string.files_title));

        // TITLE EDIT TEXT
        renameInput.addTextChangedListener(recordingTitleTextWatcher);
        renameInput.setText(title.substring(0, title.length() - 4));

        // POSITIVE BUTTON
        positiveButton.setOnClickListener(view1 -> {

            // RENAME RECORD IN EXTERNAL STORAGE
            File newFileName;

            if (title.endsWith("aac")) {
                newFileName = new File(appDir
                        + renameInput.getText()
                        + ".aac");
            }
            else {
                newFileName = new File(appDir
                        + renameInput.getText()
                        + ".amr");
            }

            boolean renamed=new File(appDir + title).renameTo(newFileName);
            bottomSheetDialog.dismiss();

            VoiceFile vf = voiceFileList.get(position);
            vf.setTitle(newFileName.getName());
            vf.setLastUpdated(InputFullDateFormat().format(new Date(newFileName.lastModified())));
            rAdapter.notifyDataSetChanged();

            if (renamed){
                Toast.makeText(requireContext(), getString(R.string.renamed), Toast.LENGTH_SHORT).show();
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

    // DELETE DIALOG
    private void DeleteDialog(String title, int position){
        final Dialog dialog = new Dialog(requireContext(), R.style.DialogTheme);
        dialog.setCancelable(true);

        @SuppressLint("InflateParams") View view  = getLayoutInflater().inflate(R.layout.dialog_alert, null);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(ContextCompat
                .getDrawable(requireContext(),R.drawable.dialog_shape));
        dialog.setContentView(view);

        // TITLE
        ((TextView)view.findViewById(R.id.dialog_title)).setText(R.string.delete_permanently_title);

        // SUBTITLE
        ((TextView)view.findViewById(R.id.dialog_subtitle)).setText(getString(R.string.delete_permanently_subtitle,
                                                            title.substring(0, title.length() - 4)));

        // POSITIVE BUTTON
        final TextView positiveButton=view.findViewById(R.id.dialog_positive_button);
        positiveButton.setText(R.string.delete);
        positiveButton.setTextColor(getResources().getColor(R.color.dialogDeleteButtonColor,null));
        positiveButton.findViewById(R.id.dialog_positive_button)
                .setOnClickListener(view1 -> {

                    boolean deleted=new File(appDir + title).delete();
                    dialog.dismiss();

                    voiceFileList.remove(position);
                    rAdapter.notifyDataSetChanged();

                    if (deleted){
                        Toast.makeText(requireContext(), getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
                    }

                    if (voiceFileList.size()==0){
                        EmptyDB();
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

    // DETAILS BOTTOM SHEET
    private void DetailsBottomSheet(String title, int position) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        @SuppressLint("InflateParams") View view  = getLayoutInflater().inflate(R.layout.bottom_sheet_details, null);
        bottomSheetDialog.setContentView(view);

        final TextView format, size, lastModified;
        final TextView okButton=view.findViewById(R.id.dialog_positive_button);
        format=view.findViewById(R.id.details_format_subtitle);
        size=view.findViewById(R.id.size_subtitle);
        lastModified=view.findViewById(R.id.last_modified_subtitle);

        File currentFile= new File(appDir +
                                    voiceFileList.get(position).getTitle());

        // TITLE
        ((TextView)view.findViewById(R.id.bottom_sheet_title)).setText(title.substring(0, title.length() - 4));

        // FORMAT
        if (title.endsWith("aac")) {
            format.setText(getString(R.string.aac));
        }
        else {
            format.setText(getString(R.string.amr));
        }

        // DURATION
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(String.valueOf(currentFile));
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("Details","mediaPLayer prepare() failed");
        }
        ((Chronometer)view.findViewById(R.id.duration_subtitle)).setBase(SystemClock.elapsedRealtime()
                - mediaPlayer.getDuration());

        // SIZE
        size.setText(Formatter.formatShortFileSize(requireContext(),currentFile.length()));

        // LAST MODIFIED
        if (DateFormat.is24HourFormat(view.getContext())) {
            lastModified.setText(DetailsFormat24().format(new Date(currentFile.lastModified()))); // 24 HRS
        }
        else {
            lastModified.setText(DetailsFormat12().format(new Date(currentFile.lastModified()))); // 12 HRS
        }

        // POSITIVE BUTTON
        okButton.setText(getString(R.string.ok));
        okButton.setOnClickListener(v ->
                bottomSheetDialog.dismiss());

        // NEGATIVE BUTTON
        view.findViewById(R.id.dialog_negative_button).setVisibility(View.GONE);

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }
}
