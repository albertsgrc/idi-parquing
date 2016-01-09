package com.fib.upc.albertsegarraroca.parquing;

import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fib.upc.albertsegarraroca.parquing.Model.Parking;
import com.fib.upc.albertsegarraroca.parquing.Model.ParkingPlace;
import com.fib.upc.albertsegarraroca.parquing.Model.Utils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlacesFragment extends Fragment {
    private View view;
    private String selectedPlace;

    public PlacesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) selectedPlace = savedInstanceState.getString("selectedPlace", "");

        showPlaceInfo(selectedPlace);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("selectedPlace", selectedPlace);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_places, container, false);

        putPlaces();
        setInfoPanelTitles();
        setSelectMessageVisible();
        putInfoBtnListener();

        return view;
    }

    private void putInfoBtnListener() {
        Button btn = (Button) view.findViewById(R.id.btnInfoAction);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.vibrateClick();
                String placeId = selectedPlace;
                MainActivity activity = (MainActivity) getActivity();
                ParkingPlace p = Parking.getInstance().getPlace(placeId);
                if (!p.isActive()) Parking.getInstance().activatePlace(placeId);
                else if (p.isOccupied()) activity.exitVehicle(p.getOcuppyingVehicle().getRegistration());
                else Parking.getInstance().deactivatePlace(placeId);
                activity.updateOccupation();
                updatePlace(p);
                showPlaceInfo(placeId);
            }
        });
    }

    private void setSelectMessageVisible() {
        view.findViewById(R.id.txtSelectPlaces).setVisibility(View.VISIBLE);
        view.findViewById(R.id.layoutInfo).setVisibility(View.GONE);
    }

    private void setInfoPanelTitles() {
        View layoutInfo = view.findViewById(R.id.layoutInfo);

        ((TextView) layoutInfo.findViewById(R.id.frameInfoId).findViewById(R.id.title)).setText(R.string.parking_place_short);
        ((TextView) layoutInfo.findViewById(R.id.frameInfoRegistration).findViewById(R.id.title)).setText(R.string.registration);
        ((TextView) layoutInfo.findViewById(R.id.frameInfoState).findViewById(R.id.title)).setText(R.string.state);
        ((TextView) layoutInfo.findViewById(R.id.frameInfoIncome).findViewById(R.id.title)).setText(R.string.income);
    }

    public void updatePlace(ParkingPlace place) {
        int id = Utils.idstoi(place.getId());
        View placeView = this.view.findViewById(id);
        ImageView icon = (ImageView) placeView.findViewById(R.id.iconState);

        if (!place.isActive()) {
            placeView.setBackgroundResource(R.drawable.place_inactive);
            placeView.findViewById(R.id.txtRegistration).setVisibility(View.GONE);
            placeView.findViewById(R.id.iconState).setBackgroundResource(R.drawable.ic_inactive);
            icon.getLayoutParams().width = icon.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());
        }
        else if (place.isOccupied()) {
            placeView.setBackgroundResource(R.drawable.place_occupied);
            TextView tvRegistration = (TextView) placeView.findViewById(R.id.txtRegistration);
            tvRegistration.setVisibility(View.VISIBLE);
            tvRegistration.setText(place.getOcuppyingVehicle().getRegistration());
            icon.setBackgroundResource(R.drawable.ic_car);
            icon.getLayoutParams().width = icon.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, getResources().getDisplayMetrics());
        }
        else {
            placeView.setBackgroundResource(R.drawable.place_free);
            placeView.findViewById(R.id.txtRegistration).setVisibility(View.GONE);
            placeView.findViewById(R.id.iconState).setBackgroundResource(R.drawable.ic_okay);
            icon.getLayoutParams().width = icon.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());
        }

        if (place.getId().equals(this.selectedPlace)) showPlaceInfo(place.getId());

        ((TextView) placeView.findViewById(R.id.txtPlaceId)).setText(place.getId());
    }

    public void updatePlace(String placeId) {
        updatePlace(Parking.getInstance().getPlace(placeId));
    }

    private void putPlace(ParkingPlace place) {
        int id = Utils.idstoi(place.getId());
        View placeView = this.view.findViewById(id);
        placeView.setTag(place.getId());

        placeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.vibrateClick();
                showPlaceInfo(v, v.getTag().toString());
            }
        });

        updatePlace(place);
    }


    private int drawableFromPlace(ParkingPlace place) {
        if (!place.isActive()) return R.drawable.place_inactive;
        else if (place.isOccupied()) return R.drawable.place_occupied;
        else return R.drawable.place_free;
    }
    private void showPlaceInfo(View v, String placeId) {
        if (selectedPlace != null) view.findViewById(Utils.idstoi(selectedPlace)).setBackgroundResource(drawableFromPlace(Parking.getInstance().getPlace(selectedPlace)));

        selectedPlace = placeId;

        View layoutInfo = view.findViewById(R.id.layoutInfo);
        TextView tvId = (TextView) layoutInfo.findViewById(R.id.frameInfoId).findViewById(R.id.info);

        view.findViewById(R.id.txtSelectPlaces).setVisibility(View.GONE);
        layoutInfo.setVisibility(View.VISIBLE);
        tvId.setText(placeId);

        ParkingPlace parkingPlace = Parking.getInstance().getPlace(placeId);
        View registrationView = layoutInfo.findViewById(R.id.frameInfoRegistration);
        View incomeView = layoutInfo.findViewById(R.id.frameInfoIncome);
        Button btnInfoAction = (Button) view.findViewById(R.id.btnInfoAction);

        String state;

        if (parkingPlace.isOccupied()) {
            btnInfoAction.setText(R.string.disoccupy);
            btnInfoAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exit, 0, 0, 0);

            v.setBackgroundResource(R.color.bad_focused);
            registrationView.setVisibility(View.VISIBLE);
            incomeView.setVisibility(View.VISIBLE);
            ((TextView) incomeView.findViewById(R.id.info)).setText(Utils.toEurosValueString(parkingPlace.getCurrentIncome()) + " €");
            ((TextView) registrationView.findViewById(R.id.info)).setText(parkingPlace.getOcuppyingVehicle().getRegistration());
            String languagePattern = getString(R.string.occupied_long);
            state = languagePattern.replace("_X_", DateUtils.getRelativeTimeSpanString(parkingPlace.getLastEntranceDate().getTime(), (new Date()).getTime(), 0L).toString().toLowerCase());
        }
        else {
            registrationView.setVisibility(View.GONE);
            incomeView.setVisibility(View.GONE);
            if (!parkingPlace.isActive()) {
                btnInfoAction.setText(R.string.activate);
                btnInfoAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_okay, 0, 0, 0);
                v.setBackgroundResource(R.color.inactive_focused);
                state = getString(R.string.inactive);
            }
            else {
                btnInfoAction.setText(R.string.deactivate);
                btnInfoAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_inactive, 0, 0, 0);
                v.setBackgroundResource(R.color.okay_focused);
                state = getString(R.string.free);
            }
        }

        ((TextView) layoutInfo.findViewById(R.id.frameInfoState).findViewById(R.id.info)).setText(state);
    }

    private void showPlaceInfo(String placeId) {
        if (placeId == null || placeId.isEmpty()) return;

        showPlaceInfo(view.findViewById(Utils.idstoi(placeId)), placeId);
    }

    private void putPlaces() {
        List<ParkingPlace> places = Parking.getInstance().getPlaces();

        for (ParkingPlace p : places) putPlace(p);
    }
}
