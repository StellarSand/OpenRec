package com.openrec.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.openrec.R;
import com.openrec.fragments.settings.SettingsDefaultFragment;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    public MaterialToolbar toolbarSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbarSettings = findViewById(R.id.toolbar_settings);

        /*===========================================================================================*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(toolbarSettings);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarSettings.setNavigationOnClickListener(v -> onBackPressed());

        if (savedInstanceState==null){
            // DEFAULT FRAGMENT
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_host_fragment, new SettingsDefaultFragment())
                    .commitNow();
        }
    }

    // SET TRANSITION WHEN FINISHING ACTIVITY
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out_slide_to_end);
    }

    @Override
    public void onBackPressed() {

        // IF NOT ON DEFAULT FRAGMENT, GO TO DEFAULT FRAGMENT
        if (getSupportFragmentManager().getBackStackEntryCount()>0){
            getSupportFragmentManager().popBackStack();
        }

        // IF ON DEFAULT FRAGMENT, GO TO MAIN ACTIVITY
        else {
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            finish();
        }
    }

}
