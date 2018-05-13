package com.subhan_nadeem.sandcastle.features.maps;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.skyfishjy.library.RippleBackground;
import com.subhan_nadeem.sandcastle.R;
import com.subhan_nadeem.sandcastle.databinding.FragmentMapBinding;
import com.subhan_nadeem.sandcastle.features.chat_room.ChatFragment;
import com.subhan_nadeem.sandcastle.features.new_room.NewRoomSheetHandler;
import com.subhan_nadeem.sandcastle.helpers.ToolbarCallbackFragment;
import com.subhan_nadeem.sandcastle.models.ChatRoom;
import com.subhan_nadeem.sandcastle.models.LocationLiveData;
import com.subhan_nadeem.sandcastle.models.ToolbarOptions;

import java.util.List;

import it.sephiroth.android.library.tooltip.Tooltip;

@SuppressWarnings("MissingPermission")
public class MapsFragment extends ToolbarCallbackFragment implements
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, NewRoomSheetHandler.NewRoomListener {

    private final String TAG = this.getClass().getName();
    private GoogleMap mMap;
    private FragmentMapBinding mBinding;
    private OpenChatroomListener mCallback;
    private MapMarkerController mMapMarkerController;
    private NewRoomSheetHandler mNewRoomSheetHandler;
    private MaterialDialog mCreatingChatroomDialog;

    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_maps, menu);
        MenuItem item = menu.findItem(R.id.map_people);

        initToolbarIcon(item);
    }

    private void initToolbarIcon(MenuItem item) {
        final RippleBackground ripple
                = (RippleBackground) getLayoutInflater().inflate(R.layout.menu_item_people, null);
        ripple.startRippleAnimation();

        ripple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Here");

                showToolbarTooltip(ripple);
            }
        });

        item.setActionView(ripple);
    }

    private void showToolbarTooltip(View anchor) {
        MapsViewModel viewModel = ViewModelProviders.of(MapsFragment.this)
                .get(MapsViewModel.class);

        final long showDurationMS = 2500;
        Tooltip.Builder tooltipBuilder = new Tooltip.Builder(101)
                .anchor(anchor, Tooltip.Gravity.BOTTOM)
                .closePolicy(new Tooltip.ClosePolicy().outsidePolicy(false,
                        false), showDurationMS)
                .withArrow(true)
                .fadeDuration(showDurationMS / 5)
                .withOverlay(true);

        if (viewModel.getNumUsersNearby().getValue() == null) {
            tooltipBuilder.text("Please wait...");
        } else {

            String suffix;
            if (viewModel.getNumUsersNearby().getValue() == 1)
                suffix = " person nearby";
            else
                suffix = " people nearby";

            String tooltipText = viewModel.getNumUsersNearby().getValue() + suffix;

            tooltipBuilder.text(tooltipText)
                    .floatingAnimation(Tooltip.AnimationBuilder.SLOW);
        }

        Tooltip.make(getContext(), tooltipBuilder).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        initLocationSubscription();

        mMapMarkerController = new MapMarkerController(getContext());

        subscribeUI();
    }


    /**
     * Continually receives location updates as long as activity is active
     */
    private void initLocationSubscription() {
        LiveData<Location> locationLiveData = new LocationLiveData(getContext());

        locationLiveData.observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                ViewModelProviders.of(MapsFragment.this).get(MapsViewModel.class)
                        .setLatestLocation(location);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        ViewModelProviders.of(MapsFragment.this)
                .get(MapsViewModel.class).onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewModelProviders.of(MapsFragment.this)
                .get(MapsViewModel.class).onResume();
    }

    private void subscribeUI() {
        MapsViewModel viewModel = ViewModelProviders.of(MapsFragment.this)
                .get(MapsViewModel.class);

        // Chatroom updates
        viewModel.getChatRooms().observe(this, new Observer<List<ChatRoom>>() {
            @Override
            public void onChanged(@Nullable List<ChatRoom> chatRooms) {
                mMapMarkerController.reconcileMapMarkers(chatRooms);
            }
        });

        viewModel.getNewChatroom().observe(this, new Observer<ChatRoom>() {
            @Override
            public void onChanged(@Nullable ChatRoom chatRoom) {
                mCreatingChatroomDialog.dismiss();
                mCallback.onChatroomClicked(ChatFragment.newInstance(chatRoom),
                        chatRoom.getName());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {

        initToolbar();

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, viewGroup, false);

        mBinding.setView(this);

        mNewRoomSheetHandler = new NewRoomSheetHandler(mBinding.bottomSheetLayout.newRoomSheet,
                mBinding.fabAdd, mBinding.fabLocate, this);

        mBinding.setNewRoomHandler(mNewRoomSheetHandler);

        mCreatingChatroomDialog = new MaterialDialog.Builder(getContext())
                .progress(true, 0)
                .title("Creating Chatroom")
                .content(R.string.please_wait)
                .build();

        initMap();

        return mBinding.getRoot();
    }

    private void initToolbar() {
        ToolbarOptions toolbarOptions = new ToolbarOptions.Builder()
                .setMargins(getResources()
                        .getDimensionPixelSize(R.dimen.toolbar_margins_map_fragment))
                .build();

        mToolbarChangeListener.onToolbarChangeRequested(toolbarOptions);
    }

    private void initMap() {
        FragmentManager fragmentManager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setInfoWindowAdapter(new InfoWindowAdapter(getLayoutInflater()));
        mMap.setOnInfoWindowClickListener(this);
        moveCameraToUserLocation(true);
        mMapMarkerController.setMap(mMap);
    }


    /**
     * Moves/animates the camera to the user's last known location
     *
     * @param animate Whether or not movement should be animated
     */
    public void moveCameraToUserLocation(final boolean animate) {
        LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) return;

                        LatLng coordinate = new LatLng(location.getLatitude(),
                                location.getLongitude());

                        CameraUpdate yourLocation
                                = CameraUpdateFactory.newLatLngZoom(coordinate, 15);

                        if (animate)
                            mMap.animateCamera(yourLocation);
                        else
                            mMap.moveCamera(yourLocation);
                    }
                });
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OpenChatroomListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OpenChatroomListener");
        }
    }

    /**
     * Bound to new chat button. Only enables user to create new chat if
     * there are no other accessible chats and user has location
     */
    public void onNewChatClick() {
        MapsViewModel viewModel = ViewModelProviders.of(this).get(MapsViewModel.class);

        if (!viewModel.userLocationRetrieved()) {
            Snackbar.make(mBinding.coordinatorMapFragment,
                    "We can't detect your location yet!",
                    Snackbar.LENGTH_LONG).show();
        } else if (viewModel.isInRangeOfChat()) {
            Snackbar.make(mBinding.coordinatorMapFragment,
                    "You're too close to another Sandcastle!",
                    Snackbar.LENGTH_LONG).show();
        } else
            mNewRoomSheetHandler.onOpenClicked();
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        ChatRoom chatRoom = mMapMarkerController.getMarkersMap().get(marker.getId()).getChatRoom();
        ChatFragment fragment = ChatFragment.newInstance(chatRoom);
        mCallback.onChatroomClicked(fragment, chatRoom.getName());
    }

    @Override
    public void onNewRoomRequested(String roomName) {
        mCreatingChatroomDialog.show();
        ViewModelProviders.of(this).get(MapsViewModel.class).createNewRoom(roomName);
    }
}
