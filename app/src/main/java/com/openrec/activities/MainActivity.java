package com.openrec.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.openrec.R;
import com.openrec.fragments.main.FilesFragment;
import com.openrec.fragments.main.RecordFragment;
import com.openrec.preferences.PreferenceManager;

import java.util.Objects;

import static com.openrec.preferences.PreferenceManager.ASC_DSC_PREF;
import static com.openrec.preferences.PreferenceManager.SORT_PREF;
import static com.openrec.preferences.PreferenceManager.VIEW_GRID_PREF;

public class MainActivity extends AppCompatActivity {

    public static final int RECORD_AUDIO_REQUEST_CODE=1;
    private RadioGroup radioGroup;
    private PreferenceManager preferenceManager;
    private Fragment fragment;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(this);
        final MaterialToolbar toolbar = findViewById(R.id.toolbar_main);
        radioGroup=findViewById(R.id.fragments_radiogroup);

        /*===========================================================================================*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(toolbar);

        // HIDE TITLE FROM TOOLBAR
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // DEFAULT FRAGMENT
        DisplayFragment(R.id.radio_record_fragment);

        //RADIO BUTTONS
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){
                case R.id.radio_record_fragment:
                    DisplayFragment(R.id.radio_record_fragment);
                    break;

                case R.id.radio_files_fragment:
                    DisplayFragment(R.id.radio_files_fragment);
                    break;
            }
        });

    }

    // SETUP FRAGMENTS
    @SuppressLint("NonConstantResourceId")
    private void DisplayFragment(int radioButtonId) {
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();

        switch (radioButtonId) {

            case R.id.radio_record_fragment:
                fragment= new RecordFragment();
                transaction.setCustomAnimations(R.anim.slide_from_start, R.anim.slide_to_end);

                break;

            case R.id.radio_files_fragment:
                fragment= new FilesFragment();
                transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start);
                break;
        }
        transaction
                .replace(R.id.activity_host_fragment, fragment)
                .commitNow();
        radioGroup.check(radioButtonId);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        // SHOW SORT AND VIEW OPTIONS ONLY IF ON FILES FRAGMENT
        if (radioGroup.getCheckedRadioButtonId()==R.id.radio_files_fragment) {
            menu.findItem(R.id.sort).setVisible(true);
            menu.findItem(R.id.view).setVisible(true);
        }
        else {
            menu.findItem(R.id.sort).setVisible(false);
            menu.findItem(R.id.view).setVisible(false);
        }

        // SORT
        menu.findItem(R.id.sort).setOnMenuItemClickListener(menuItem -> {
            SortBottomSheet();
            return true;
        });

        // VIEW
        final MenuItem viewList=menu.findItem(R.id.view_list);
        final MenuItem viewGrid=menu.findItem(R.id.view_grid);

        if (preferenceManager.getBoolean(VIEW_GRID_PREF)){
            viewGrid.setChecked(true);
        }
        else {
            viewList.setChecked(true);
        }

        viewList.setOnMenuItemClickListener(menuItem -> {
            viewList.setChecked(true);
            preferenceManager.setBoolean(VIEW_GRID_PREF, false);
            getSupportFragmentManager().beginTransaction().detach(fragment).commit();
            getSupportFragmentManager().beginTransaction().attach(fragment).commit();
            return true;
        });

        viewGrid.setOnMenuItemClickListener(menuItem -> {
            viewGrid.setChecked(true);
            preferenceManager.setBoolean(VIEW_GRID_PREF, true);
            getSupportFragmentManager().beginTransaction().detach(fragment).commit();
            getSupportFragmentManager().beginTransaction().attach(fragment).commit();
            return true;
        });

        // SETTINGS
        menu.findItem(R.id.settings).setOnMenuItemClickListener(menuItem -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            finish();
            overridePendingTransition(R.anim.fade_in_slide_from_end, R.anim.no_movement);
            return true;
        });

        return true;
    }

    // SORT BOTTOM SHEET
    private void SortBottomSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        @SuppressLint("InflateParams") final View view  = getLayoutInflater().inflate(R.layout.bottom_sheet_options, null);
        bottomSheetDialog.setContentView(view);

        final RadioGroup sortRadioGroup = view.findViewById(R.id.options_radiogroup);
        final TextView positiveButton=view.findViewById(R.id.dialog_positive_button);
        final TextView negativeButton=view.findViewById(R.id.dialog_negative_button);

        view.findViewById(R.id.option2).setVisibility(View.GONE);

        ((TextView)view.findViewById(R.id.option_default)).setText(R.string.recently_updated);
        ((TextView)view.findViewById(R.id.option1)).setText(R.string.alphabetically);

        // DEFAULT CHECKED RADIO
        if (preferenceManager.getInt(SORT_PREF)==0){
            preferenceManager.setInt(SORT_PREF, R.id.option_default);
        }
        sortRadioGroup.check(preferenceManager.getInt(SORT_PREF));

        // TITLE
        ((TextView)view.findViewById(R.id.bottom_sheet_title)).setText(R.string.menu_sort);

        // POSITIVE BUTTON
        positiveButton.setText(R.string.descending);
        positiveButton.setOnClickListener(view12 -> {
            preferenceManager.setString(ASC_DSC_PREF, "descending");
            bottomSheetDialog.dismiss();
            getSupportFragmentManager().beginTransaction().detach(fragment).commit();
            getSupportFragmentManager().beginTransaction().attach(fragment).commit();
        });

        // ON SELECTING OPTION
        sortRadioGroup.setOnCheckedChangeListener((radioGroup, checkedId) ->
                preferenceManager.setInt(SORT_PREF, checkedId));

        // NEGATIVE BUTTON
        negativeButton.setText(R.string.ascending);
        negativeButton.setOnClickListener(view1 -> {
                preferenceManager.setString(ASC_DSC_PREF, "ascending");
                bottomSheetDialog.dismiss();
            getSupportFragmentManager().beginTransaction().detach(fragment).commit();
            getSupportFragmentManager().beginTransaction().attach(fragment).commit();
        });

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

    // MANAGE DENY AND DON'T ASK AGAIN IN RECORD AUDIO PERMISSION
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {

            // ON CLICK DENY
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                // ONLY CLICKING DENY
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this, getString(R.string.record_permission_denied), Toast.LENGTH_SHORT).show();
                }

                // CLICKING DENY WITH DON'T ASK AGAIN
                else {
                    // SHOW SNACKBAR WITH ENABLE BUTTON
                    Snackbar.make(findViewById(R.id.record_layout), getString(R.string.mic_permission_blocked), 8000)
                            .setActionTextColor(getResources().getColor(R.color.snackbarActionTextColor, null))
                            .setTextColor(getResources().getColor(R.color.snackbarTextColor, null))
                            // ON CLICK ENABLE BUTTON, SHOW APP INFO IN DEVICE SETTINGS
                            .setAction(getString(R.string.enable), view ->
                                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            .setData(Uri.parse("package:" + getPackageName()))))
                            .show();
                }
            }
        }
    }

}
