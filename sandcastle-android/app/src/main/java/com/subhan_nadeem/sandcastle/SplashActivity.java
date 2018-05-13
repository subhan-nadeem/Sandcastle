package com.subhan_nadeem.sandcastle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.subhan_nadeem.sandcastle.data.RealmHelper;
import com.subhan_nadeem.sandcastle.features.auth.AuthenticationActivity;
import com.subhan_nadeem.sandcastle.features.main.MainActivity;

/**
 * Created by Subhan Nadeem on 2017-10-10.
 * SplashActivity intended to redirect to appropriate activity
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent;

        if (new RealmHelper().isUserLoggedIn())
            intent = MainActivity.getIntent(this);
        else
            intent = AuthenticationActivity.getIntent(this);

        startActivity(intent);
        finish();
    }

}
