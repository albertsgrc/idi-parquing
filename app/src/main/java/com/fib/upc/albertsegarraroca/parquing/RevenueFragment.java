package com.fib.upc.albertsegarraroca.parquing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.fib.upc.albertsegarraroca.parquing.Model.Utils;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleActivity;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleActivityList;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleEntry;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class RevenueFragment extends Fragment {

    public RevenueFragment() {}

    private Long initialDate = null;
    private Long finalDate = null;

    private boolean todaySelected = false;

    private View view;

    private static boolean INITIAL = true;
    private static boolean FINAL = false;

    private int lastSize = 0;
    private long firstActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_revenue, container, false);

        setListeners();

        return view;
    }

    private void setListeners() {
        final Activity activity = getActivity();

        view.findViewById(R.id.btnInitialDate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate(INITIAL, activity);
            }
        });

        view.findViewById(R.id.btnFinalDate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate(FINAL, activity);
            }
        });

        ((CheckBox) view.findViewById(R.id.chckToday)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean wasSelected = todaySelected;
                todaySelected = isChecked;
                if (todaySelected) showToday();
                else {
                    printDates();
                    if (wasSelected && initialDate != null && finalDate != null) showDates();
                }
            }
        });

        view.findViewById(R.id.btnReload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadList();
            }
        });
    }

    private void reloadList() {
        int size = lastSize;
        long first = firstActivity;

        if (todaySelected) showToday();
        else if (initialDate != null && finalDate != null) showDates();
        if (lastSize == 0 && size == 0 || (lastSize == size &&  first == firstActivity))
            Utils.showToast(getString(R.string.nothing_to_reload), Toast.LENGTH_SHORT);
    }


    private void printDates() {
        if (initialDate != null) setInitialText(initialDate);
        else resetInitialText();
        if (finalDate != null) setFinalText(finalDate);
        else resetFinalText();
    }

    private void resetInitialText() {
        ((TextView) view.findViewById(R.id.txtInitialDate)).setText(R.string.indicate_start);
    }

    private void resetFinalText() {
        ((TextView) view.findViewById(R.id.txtFinalDate)).setText(R.string.indicate_end);
    }

    private void selectDate(final boolean initial, final Activity activity) {
        final View dialogView = activity.getLayoutInflater().inflate(R.layout.datetime_picker, null);
        final TimePicker tp = (TimePicker) dialogView.findViewById(R.id.timePicker);
        final DatePicker dp = (DatePicker) dialogView.findViewById(R.id.datePicker);

        Long date = initial ? initialDate : finalDate;

        if (date != null) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(date);
            tp.setCurrentHour(c.get(Calendar.HOUR));
            tp.setCurrentMinute(c.get(Calendar.MINUTE));
            dp.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        }

        tp.setIs24HourView(true);

        new AlertDialog.Builder(activity)
                .setTitle(initial ? R.string.select_initial_date_dialog_title : R.string.select_final_date_dialog_title)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), tp.getCurrentHour(), tp.getCurrentMinute());

                        if (initial) {
                            initialDate = cal.getTimeInMillis();
                            setInitialText(initialDate);
                            disableToday();
                        } else {
                            finalDate = cal.getTimeInMillis();
                            setFinalText(finalDate);
                            disableToday();
                        }

                        if (initialDate != null && finalDate != null) showDates();
                    }
                })
                .create().show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (initialDate != null) outState.putLong("initialDate", initialDate);
        if (finalDate != null) outState.putLong("finalDate", finalDate);
        outState.putLong("firstActivity", firstActivity);
        outState.putBoolean("todaySelected", todaySelected);
        outState.putInt("lastSize", lastSize);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            initialDate = savedInstanceState.getLong("initialDate", 0);
            finalDate = savedInstanceState.getLong("finalDate", 0);
            todaySelected = savedInstanceState.getBoolean("todaySelected", false);
            lastSize = savedInstanceState.getInt("lastSize", 0);
            firstActivity = savedInstanceState.getLong("firstActivity");
            if (initialDate == 0) initialDate = null;
            if (finalDate == 0) finalDate = null;
        }

        if (todaySelected) showToday();
        else if (initialDate != null && finalDate != null) showDates();
    }

    private void showToday() {
        VehicleActivityList val = new VehicleActivityList(((MainActivity) getActivity()).getDatabase());

        setInitialText(Utils.getTodays00time().getTime());

        setFinalText(new Date().getTime());

        setVehicleActivityList(val);
    }

    private void disableToday() {
        todaySelected = false;

        ((CheckBox) view.findViewById(R.id.chckToday)).setChecked(false);
    }

    private void showMessage(String txt) {
        Utils.showToast(txt, Toast.LENGTH_LONG);
    }

    private void showDates() {
        disableToday();
        setInitialText(initialDate);
        setFinalText(finalDate);

        if (initialDate > finalDate) {
            showMessage(getString(R.string.null_interval));
        }

        setVehicleActivityList(new VehicleActivityList(((MainActivity) getActivity()).getDatabase(), new Date(initialDate), new Date(finalDate)));
    }

    public void setInitialText(long initialDate) {
        ((TextView) view.findViewById(R.id.txtInitialDate)).setText(Utils.formatDateShort(new Date(initialDate)));
    }

    public void setFinalText(long finalDate) {
        ((TextView) view.findViewById(R.id.txtFinalDate)).setText(Utils.formatDateShort(new Date(finalDate)));
    }

    public void setVehicleActivityList(VehicleActivityList vehicleActivityList) {
        final ArrayList<VehicleActivity> list = vehicleActivityList.getList();
        this.lastSize = list.size();
        if (lastSize > 0) {
            firstActivity = list.get(0).getDate().getTime();
        }
        else if (finalDate == null || initialDate == null || finalDate >= initialDate) showMessage(getString(R.string.no_activities));

        final ListView lv = (ListView) view.findViewById(R.id.listView);

        lv.setAdapter(new AdapterActivity(getActivity(), 0, list));

        view.findViewById(R.id.txtSelectInterval).setVisibility(View.GONE);

        ((TextView) view.findViewById(R.id.txtIncomeInfo)).setText(Utils.toEurosValueString(vehicleActivityList.getTotalIncome()) + " €");
        ((TextView) view.findViewById(R.id.txtEntriesInfo)).setText(Utils.toEurosValueString(vehicleActivityList.getTotalEntries()));
        ((TextView) view.findViewById(R.id.txtExitsInfo)).setText(Utils.toEurosValueString(vehicleActivityList.getTotalExits()));

        view.findViewById(R.id.infoRow).setVisibility(View.VISIBLE);

        lv.setVisibility(View.VISIBLE);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View dv = getActivity().getLayoutInflater().inflate(R.layout.dialog_info_item, null);

                VehicleActivity ac = list.get(position);

                boolean isEntry = ac.getClass() == VehicleEntry.class;

                ((TextView) dv.findViewById(R.id.itemTypeInfo).findViewById(R.id.title)).setText(R.string.type);
                ((TextView) dv.findViewById(R.id.itemPlaceInfo).findViewById(R.id.title)).setText(R.string.parking_place_short);
                ((TextView) dv.findViewById(R.id.itemRegistrationInfo).findViewById(R.id.title)).setText(R.string.registration_long);
                ((TextView) dv.findViewById(R.id.itemDateInfo).findViewById(R.id.title)).setText(R.string.date);

                if (isEntry) dv.findViewById(R.id.itemIncomeInfo).setVisibility(View.GONE);
                else ((TextView) dv.findViewById(R.id.itemIncomeInfo).findViewById(R.id.title)).setText(R.string.price);

                ((TextView) dv.findViewById(R.id.itemTypeInfo).findViewById(R.id.info)).setText(isEntry ? R.string.entry_short : R.string.exit_short);
                ((TextView) dv.findViewById(R.id.itemPlaceInfo).findViewById(R.id.info)).setText(ac.getPlace());
                ((TextView) dv.findViewById(R.id.itemRegistrationInfo).findViewById(R.id.info)).setText(ac.getVehicle().getRegistration());
                ((TextView) dv.findViewById(R.id.itemDateInfo).findViewById(R.id.info)).setText(Utils.formatDateLong(ac.getDate()));

                if (!isEntry)
                    ((TextView) dv.findViewById(R.id.itemIncomeInfo).findViewById(R.id.info)).setText(Utils.toEurosValueString(ac.getIncome()) + " €");

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.info)
                        .setView(dv)
                        .setPositiveButton(R.string.okay, null).create().show();
            }
        });
    }

    private class AdapterActivity extends ArrayAdapter<VehicleActivity> {
        private Activity activity;
        private ArrayList<VehicleActivity> lActivity;
        private LayoutInflater inflater = null;

        public AdapterActivity(Activity activity, int textViewResource, ArrayList<VehicleActivity> list) {
            super(activity, textViewResource, list);

            this.activity = activity;
            this.lActivity = list;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return lActivity.size();
        }

        public long getItemId(int position) {
            return position;
        }

        public class ViewHolder {
            public TextView type;
            public TextView vehicle;
            public TextView date;
            public ImageView typeIcon;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_activity, parent, false);

                holder = new ViewHolder();

                holder.type = (TextView) convertView.findViewById(R.id.type);
                holder.vehicle = (TextView) convertView.findViewById(R.id.vehicle);
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.typeIcon = (ImageView) convertView.findViewById(R.id.typeIcon);

                convertView.setTag(holder);
            }
            else holder = (ViewHolder) convertView.getTag();

            VehicleActivity va = lActivity.get(position);

            boolean isEntry = va.getClass() == VehicleEntry.class;

            holder.type.setText(isEntry ? R.string.entry_short : R.string.exit_short);
            holder.vehicle.setText(Html.fromHtml(getString(R.string.vehicle)+"<b>" + "  " + va.getVehicle().getRegistration() + "</b>"));
            holder.typeIcon.setImageResource(isEntry ? R.drawable.ic_enter : R.drawable.ic_exit);
            holder.date.setText(Utils.formatDateShort(va.getDate()));

            return convertView;
        }
    }
}
