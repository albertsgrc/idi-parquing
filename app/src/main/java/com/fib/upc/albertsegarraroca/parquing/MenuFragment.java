package com.fib.upc.albertsegarraroca.parquing;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Random;

import com.fib.upc.albertsegarraroca.parquing.Data.Database;
import com.fib.upc.albertsegarraroca.parquing.Model.Parking;
import com.fib.upc.albertsegarraroca.parquing.Model.Utils;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleActivity;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleActivityList;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleEntry;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleExit;

public class MenuFragment extends Fragment {
    private static final int SAVE_STATS_RESULT_CODE = 0;

    public MenuFragment() {
    }

    private View view;
    private Database db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = ((MainActivity) getActivity()).getDatabase();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_menu, container, false);

        setListeners();

        return view;
    }

    private void setListeners() {
        view.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleParkingReset();
            }
        });

        view.findViewById(R.id.btnUndo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleUndo();
            }
        });

        view.findViewById(R.id.btnStats).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStats();
            }
        });

        view.findViewById(R.id.btnSummary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSummary();
            }
        });

        view.findViewById(R.id.btnHelp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleHelp();
            }
        });

        view.findViewById(R.id.btnAbout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAbout();
            }
        });
    }

    private void handleParkingReset() {
        // TODO: Set dialog title to red

        Random r = new Random();
        final int v = r.nextInt(2327328 - 1000000) + 1000000;

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_reset_confirmation, null);

        Log.d("Message", getString(R.string.warning_reset));

        ((TextView) dialogView.findViewById(R.id.dialogText)).setText(Html.fromHtml(getString(R.string.warning_reset).replace("_X_", "<b>").replace("_Y_", "</b>")));
        ((TextView) dialogView.findViewById(R.id.confirmationCode)).setText("" + v);

        final EditText edit = (EditText) dialogView.findViewById(R.id.securityCode);

        final AlertDialog d = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.warning)
                .setPositiveButton(R.string.okay, null)
                .setNegativeButton(R.string.cancel, null)
                .setView(dialogView).create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String text = edit.getText().toString();
                        if (text.length() > 0) {
                            int code = Integer.valueOf(edit.getText().toString());
                            if (code == v) {
                                ((MainActivity) getActivity()).resetParking();
                                d.dismiss();
                                Utils.showToast(getString(R.string.parking_reseted), Toast.LENGTH_LONG);
                            } else edit.setError(getString(R.string.incorrect_code));
                        }
                        else edit.setError(getString(R.string.incorrect_code));
                    }
                });
            }
        });

        d.show();
    }

    private void handleUndo() {
        View dv = getActivity().getLayoutInflater().inflate(R.layout.dialog_info_item, null);

        VehicleActivity ac = Parking.getInstance().getLastActivity();

        if (ac == null) {
            Utils.showToast(getString(R.string.error_no_last_activity), Toast.LENGTH_LONG);
            return;
        }

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
                .setTitle(R.string.warning_undo)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) getActivity()).undoLastActivity(null);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setView(dv)
                .create().show();
    }

    private void handleStats() {
        final String content = new VehicleActivityList(db, true).toCsv(getString(R.string.header_csv));
        final String filename = getString(R.string.stats) + "-" + Utils.fileTimeStamp() + ".csv";

        if (saveDownloads(filename, content, "csv"))
            Utils.showToast(getString(R.string.stats_created), Toast.LENGTH_LONG);
    }

    private void handleSummary() {
        final String content = new VehicleActivityList(db, true).toHtml(
                getString(R.string.html_header),
                getString(R.string.html_title),
                getString(R.string.css),
                getString(R.string.entries),
                getString(R.string.exits),
                getString(R.string.minutes));

        final String filename = getString(R.string.summary ) + "-" + Utils.fileTimeStamp() + ".html";

        if (saveDownloads(filename, content, "html"))
            Utils.showToast(getString(R.string.html_created), Toast.LENGTH_LONG);
    }

    private boolean saveDownloads(String filename, String content, String type) {
        File dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File file = new File(dir, filename);
        try {
            dir.mkdirs();

            if (!dir.exists()) throw new IOException();

            String extension = "";

            int i = filename.lastIndexOf('.');
            if (i > 0) {
                extension = filename.substring(i);
            }

            int n = 1;
            while (file.exists()) file = new File(dir, filename.substring(0, filename.length() - extension.length()) + "("+ n++ +")" + extension);

            filename = file.getName();

            boolean created2 = file.createNewFile();

            if (!created2) throw new IOException();

            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
            writer.write(content);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            Utils.showToast(getString(R.string.error_create_file), Toast.LENGTH_LONG);
            return false;
        }

        DownloadManager dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        dm.addCompletedDownload(filename, filename, true, "text/" + type, file.getAbsolutePath(), file.length(), true);
        return true;
    }

    private void handleHelp() {
        Intent intent = new Intent(getActivity(), HelpActivity.class);
        startActivity(intent);
    }

    private void handleAbout() {
        Intent intent = new Intent(getActivity(), AboutActivity.class);
        startActivity(intent);
    }
}
