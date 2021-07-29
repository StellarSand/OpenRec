package com.openrec.fragments.settings;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.openrec.R;
import com.openrec.activities.SettingsActivity;
import com.openrec.preferences.PreferenceManager;

import java.util.Objects;

import static com.openrec.preferences.PreferenceManager.FORMAT_PREF;
import static com.openrec.preferences.PreferenceManager.THEME_PREF;

public class SettingsDefaultFragment extends Fragment {

    private BottomSheetDialog bottomSheetDialog;
    private TextView chooseThemeSubtitle, formatSubtitle;
    private PreferenceManager preferenceManager;

    public SettingsDefaultFragment() {
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
        View v = inflater.inflate(R.layout.fragment_settings_default, container,  false);
        Objects.requireNonNull(((SettingsActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.settings);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        preferenceManager=new PreferenceManager(requireActivity());
        chooseThemeSubtitle=view.findViewById(R.id.settings_theme_subtitle);
        formatSubtitle=view.findViewById(R.id.format_subtitle);

    /*============================================================================================*/

        // THEME
        view.findViewById(R.id.settings_theme_holder)
                .setOnClickListener(v ->
                        ThemeBottomSheet());

        if (preferenceManager.getInt(THEME_PREF)==0){
            if (Build.VERSION.SDK_INT>=29){
                chooseThemeSubtitle.setText(R.string.system_default);
            }
            else{
                chooseThemeSubtitle.setText(R.string.light);
            }
        }
        else if (preferenceManager.getInt(THEME_PREF)==R.id.option_default){
            chooseThemeSubtitle.setText(R.string.system_default);
        }
        else if (preferenceManager.getInt(THEME_PREF)==R.id.option1){
            chooseThemeSubtitle.setText(R.string.light);
        }
        else if (preferenceManager.getInt(THEME_PREF)==R.id.option2){
            chooseThemeSubtitle.setText(R.string.dark);
        }

        // FORMAT
        view.findViewById(R.id.settings_format_holder)
                .setOnClickListener(v ->
                        FormatBottomSheet());

        if (preferenceManager.getInt(FORMAT_PREF)==R.id.option_default
                || preferenceManager.getInt(FORMAT_PREF)==0){
            formatSubtitle.setText(R.string.amr);
        }
        else if (preferenceManager.getInt(FORMAT_PREF)==R.id.option1){
            formatSubtitle.setText(R.string.aac);
        }

        // HELP
        view.findViewById(R.id.settings_help_holder)
                .setOnClickListener(v -> {
                    try
                    {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/the-weird-aquarian/OpenRec/issues")));
                    }
                    // IF BROWSERS NOT INSTALLED, SHOW TOAST
                    catch (ActivityNotFoundException e)
                    {
                        Toast.makeText(requireContext(), getString(R.string.no_browsers), Toast.LENGTH_SHORT).show();
                    }
                });


        // ABOUT
        view.findViewById(R.id.settings_about_holder)
                .setOnClickListener(view19 ->
                        getParentFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                        R.anim.slide_from_start, R.anim.slide_to_end)
                                .replace(R.id.activity_host_fragment, new AboutFragment())
                                .addToBackStack(null)
                                .commit());
    }

    // THEME BOTTOM SHEET
    @SuppressLint("NonConstantResourceId")
    private void ThemeBottomSheet(){
        bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        @SuppressLint("InflateParams") final View view  = getLayoutInflater().inflate(R.layout.bottom_sheet_options, null);
        bottomSheetDialog.setContentView(view);

        final RadioGroup themeRadioGroup = view.findViewById(R.id.options_radiogroup);
        final TextView positiveButton=view.findViewById(R.id.dialog_positive_button);

        // DEFAULT CHECKED RADIO
        if (preferenceManager.getInt(THEME_PREF)==0){
            if (Build.VERSION.SDK_INT>=29){
                preferenceManager.setInt(THEME_PREF, R.id.option_default);
            }
            else{
                preferenceManager.setInt(THEME_PREF, R.id.option1);
            }
        }
        themeRadioGroup.check(preferenceManager.getInt(THEME_PREF));

        // TITLE
        ((TextView)view.findViewById(R.id.bottom_sheet_title)).setText(R.string.choose_theme_title);

        // POSITIVE BUTTON
        positiveButton.setText(R.string.dialog_negative_button);
        positiveButton.setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        // SHOW SYSTEM DEFAULT OPTION ONLY ON SDK 29 AND ABOVE
        if (Build.VERSION.SDK_INT>=29){
            view.findViewById(R.id.option_default).setVisibility(View.VISIBLE);
        }
        else{
            view.findViewById(R.id.option_default).setVisibility(View.GONE);
        }

        // ON SELECTING OPTION
        ((RadioGroup)view.findViewById(R.id.options_radiogroup))
                .setOnCheckedChangeListener((radioGroup, checkedId) -> {
                    switch (checkedId){
                        case R.id.option_default:
                            chooseThemeSubtitle.setText(R.string.system_default);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            break;

                        case R.id.option1:
                            chooseThemeSubtitle.setText(R.string.light);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            break;

                        case R.id.option2:
                            chooseThemeSubtitle.setText(R.string.dark);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                    }
                    preferenceManager.setInt(THEME_PREF, checkedId);
                    bottomSheetDialog.dismiss();
                    requireActivity().recreate();
                });

        // NEGATIVE BUTTON
        view.findViewById(R.id.dialog_negative_button).setVisibility(View.GONE);

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

    // RECORDINGS FORMAT BOTTOM SHEET
    @SuppressLint("NonConstantResourceId")
    private void FormatBottomSheet(){
        bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        @SuppressLint("InflateParams") final View view  = getLayoutInflater().inflate(R.layout.bottom_sheet_options, null);
        bottomSheetDialog.setContentView(view);

        final RadioGroup formatRadioGroup = view.findViewById(R.id.options_radiogroup);
        final TextView positiveButton=view.findViewById(R.id.dialog_positive_button);

        view.findViewById(R.id.option2).setVisibility(View.GONE);

        // DEFAULT CHECKED RADIO
        if (preferenceManager.getInt(FORMAT_PREF)==0){
            preferenceManager.setInt(FORMAT_PREF, R.id.option_default);
        }
        formatRadioGroup.check(preferenceManager.getInt(FORMAT_PREF));

        // TITLE
        ((TextView)view.findViewById(R.id.bottom_sheet_title)).setText(R.string.format_title);

        ((TextView)view.findViewById(R.id.option_default)).setText(R.string.amr);
        ((TextView)view.findViewById(R.id.option1)).setText(R.string.aac);

        // POSITIVE BUTTON
        positiveButton.setText(R.string.dialog_negative_button);
        positiveButton.setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        // ON SELECTING OPTION
        ((RadioGroup)view.findViewById(R.id.options_radiogroup))
                .setOnCheckedChangeListener((radioGroup, checkedId) -> {
                    switch (checkedId){
                        case R.id.option_default:
                            formatSubtitle.setText(R.string.amr);
                            break;

                        case R.id.option1:
                            formatSubtitle.setText(R.string.aac);
                            break;
                    }
                    preferenceManager.setInt(FORMAT_PREF, checkedId);
                    bottomSheetDialog.dismiss();
                });

        // NEGATIVE BUTTON
        view.findViewById(R.id.dialog_negative_button).setVisibility(View.GONE);

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

}
