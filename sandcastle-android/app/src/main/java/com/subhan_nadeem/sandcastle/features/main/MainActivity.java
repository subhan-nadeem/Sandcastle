package com.subhan_nadeem.sandcastle.features.main;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.subhan_nadeem.sandcastle.App;
import com.subhan_nadeem.sandcastle.GlideApp;
import com.subhan_nadeem.sandcastle.R;
import com.subhan_nadeem.sandcastle.data.RealmHelper;
import com.subhan_nadeem.sandcastle.databinding.ActivityMainBinding;
import com.subhan_nadeem.sandcastle.databinding.NavHeaderMainBinding;
import com.subhan_nadeem.sandcastle.features.chat_room.ChatFragment;
import com.subhan_nadeem.sandcastle.features.maps.MapsFragment;
import com.subhan_nadeem.sandcastle.features.maps.OpenChatroomListener;
import com.subhan_nadeem.sandcastle.models.ToolbarOptions;
import com.subhan_nadeem.sandcastle.utils.FragmentController;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OpenChatroomListener,
        ToolbarChangeListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private ActivityMainBinding mBinding;
    private ToolbarController mToolbarController;
    private FragmentController mFragmentController;
    private NavHeaderMainBinding mNavHeaderMainBinding;

    public static Intent getIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mNavHeaderMainBinding = NavHeaderMainBinding.bind(mBinding.navView.getHeaderView(0));
        mNavHeaderMainBinding.setUser(new RealmHelper().getUser());
        mNavHeaderMainBinding.setView(this);
        mNavHeaderMainBinding.setViewModel(ViewModelProviders.of(this)
                .get(MainActivityViewModel.class));

        mToolbarController = new ToolbarController(this,
                mBinding.contentMain.appBarLayout,
                mBinding.contentMain.toolbar,
                mBinding.contentMain.fragmentContainer);

        mFragmentController
                = new FragmentController(getSupportFragmentManager(), R.id.fragment_container);

        initDrawer();

        subscribeUI();

        if (isLocationEnabled()) mFragmentController.initFragment(MapsFragment.newInstance());
    }

    private void subscribeUI() {
        final MainActivityViewModel viewModel = ViewModelProviders.of(this)
                .get(MainActivityViewModel.class);

        viewModel.getUserHasPhoto().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean userHasAvatar) {
                if (userHasAvatar) {
                   loadProfileImage(mNavHeaderMainBinding.profileImageView, viewModel.getUserAvatarFile());
                } else {
                    loadProfileImage(mNavHeaderMainBinding.profileImageView, R.drawable.placeholder_profile);
                }
            }
        });

        viewModel.getAvatarChangeUploaded().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isUploaded) {
                if (isUploaded) {
                    showSnackbar("Profile photo changed successfully");
                } else {
                    showSnackbar("Couldn't change your profile photo, something went wrong :(");
                }
            }
        });
    }

    private void showSnackbar(String snackbarText) {
        final Snackbar snackbar = Snackbar.make(mBinding.contentMain.coordinatorMainActivity,
                snackbarText, Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("DISMISS", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });

        snackbar.show();
    }

    private void loadProfileImage(ImageView imageView, Object object) {
        GlideApp.with(MainActivity.this)
                .load(object)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }

    public boolean isLocationEnabled() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_LOCATION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mFragmentController.initFragment(MapsFragment.newInstance());
                } else {
                    // TODO Show user dialog about why location is important, request location again
                    // or close app
                }
            }
        }
    }


    private void initDrawer() {
        DrawerLayout drawer = mBinding.drawerMain;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mBinding.contentMain.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = mBinding.navView;
        navigationView.getMenu().getItem(0).setChecked(true); // Home is set to on by default
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void showProfileImageDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.profile_picture)
                .btnStackedGravity(GravityEnum.START)
                .positiveText(R.string.new_profile_picture)
                .neutralText(R.string.delete_profile_picture)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        startCropActivity();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ViewModelProviders.of(MainActivity.this)
                                .get(MainActivityViewModel.class).deleteAvatar();
                    }
                })
                .show();
    }

    private void startCropActivity() {
        CropImage.activity(null)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (result == null) return;

            ViewModelProviders.of(this).get(MainActivityViewModel.class)
                    .uploadAvatar(result.getUri().getPath());
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = mBinding.drawerMain;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        NavigationView navigationView = mBinding.navView;
        int id = item.getItemId();

        // Don't do anything if currently selected page is pressed
        if (!navigationView.getMenu().findItem(id).isChecked()) {

            if (id == R.id.nav_home)

                // Pop entire backstack
                getSupportFragmentManager().popBackStack(null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);

            if (id == R.id.nav_logout)
                App.logUserOut();

            navigationView.getMenu().findItem(id).setChecked(true);
        }

        mBinding.drawerMain.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onChatroomClicked(ChatFragment chatFragment, String roomName) {

        ToolbarOptions options = new ToolbarOptions.Builder()
                .setMargins(0)
                .setText(roomName).build();

        onToolbarChangeRequested(options);

        deselectNavHomeButton();

        mFragmentController.changeFragment(chatFragment, true, false);
    }

    private void deselectNavHomeButton() {
        mBinding.navView.getMenu().getItem(0).setChecked(false);
    }

    @Override
    public void onToolbarChangeRequested(ToolbarOptions toolbarOptions) {
        mToolbarController.changeToolbar(toolbarOptions);
    }
}
