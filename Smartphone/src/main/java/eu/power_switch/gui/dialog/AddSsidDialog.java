/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.dialog;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.animation.AnimationHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * Dialog to create a new Room
 */
public class AddSsidDialog extends DialogFragment {

    public static final String KEY_SSID = "SSID";

    private Dialog dialog;
    private int defaultTextColor;
    private View contentView;

    private ArrayList<String> ssids = new ArrayList<>();
    private ListView listView;

    private BroadcastReceiver broadcastReceiver;
    private WifiManager mainWifi;
    private ArrayAdapter<String> ssidAdapter;
    private LinearLayout layoutLoading;
    private TextInputEditText editText_ssid;

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context any suitable context
     */
    public static void sendSsidAddedBroadcast(Context context, ArrayList<String> ssid) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_GATEWAY_SSID_ADDED);
        intent.putStringArrayListExtra(KEY_SSID, ssid);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ArrayList<String> connections = new ArrayList<>();

                for (ScanResult scanResult : mainWifi.getScanResults()) {
                    if (scanResult.SSID != null && !scanResult.SSID.isEmpty()) {
                        connections.add(scanResult.SSID);
                    }
                }

                updateSSIDs(connections);

                layoutLoading.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }
        };

        mainWifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        contentView = inflater.inflate(R.layout.dialog_add_ssid, null);
        builder.setView(contentView);

        editText_ssid = (TextInputEditText) contentView.findViewById(R.id.editText_ssid);
        editText_ssid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidity();
            }
        });

        final IconicsImageView refresh = (IconicsImageView) contentView.findViewById(R.id.button_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainWifi.isWifiEnabled()) {
                    refresh.startAnimation(AnimationHandler.getRotationClockwiseAnimation(getContext()));
                    refreshSsids();
                } else {
                    Toast.makeText(getActivity(), "WiFi disabled!", Toast.LENGTH_LONG).show();
                }
            }
        });

        layoutLoading = (LinearLayout) contentView.findViewById(R.id.layoutLoading);

        listView = (ListView) contentView.findViewById(R.id.listView);
        ssidAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, ssids);
        listView.setAdapter(ssidAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkValidity();
            }
        });

        builder.setTitle(R.string.add_ssids);
        builder.setPositiveButton(R.string.add, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    sendSsidAddedBroadcast(getContext(), getSelectedSSIDs());
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getTargetFragment().getView().findViewById(R.id.listView), e);
                }
            }
        });

        builder.setNeutralButton(android.R.string.cancel, null);

        setPositiveButtonVisibility(false);

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.show();

        defaultTextColor = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).getTextColors()
                .getDefaultColor();

        checkValidity();

        refreshSsids();

        return dialog;
    }

    private ArrayList<String> getSelectedSSIDs() {
        ArrayList<String> selectedSSIDs = new ArrayList<>();

        // manual
        String manualSsid = editText_ssid.getText().toString().trim();
        if (!TextUtils.isEmpty(manualSsid)) {
            selectedSSIDs.add(manualSsid);
        }

        // available networks
        int len = listView.getCount();
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        for (int i = 0; i < len; i++) {
            if (checked.get(i)) {
                String item = ssids.get(i);
                /* do whatever you want with the checked item */
                selectedSSIDs.add(item);
            }
        }

        return selectedSSIDs;
    }

    private void refreshSsids() {
        layoutLoading.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        mainWifi.startScan();
    }

    private void updateSSIDs(List<String> ssids) {
        this.ssids.clear();
        this.ssids.addAll(ssids);
        ssidAdapter.notifyDataSetChanged();

        checkValidity();
    }

    private void checkValidity() {
        if (getSelectedSSIDs().isEmpty()) {
            setPositiveButtonVisibility(false);
        } else {
            setPositiveButtonVisibility(true);
        }
    }

    private void setPositiveButtonVisibility(boolean visibility) {
        if (dialog != null) {
            if (visibility) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(defaultTextColor);
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
            } else {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        // use activity directly, otherwise the intent wont be catched (alternative way below)
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
//        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}