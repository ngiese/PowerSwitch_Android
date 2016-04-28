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

package eu.power_switch.gui.fragment.configure_room;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigurationDialogTabbedSummaryFragment;
import eu.power_switch.gui.dialog.ConfigureRoomDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.gui.listener.CheckBoxInteractionListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.RoomWidgetProvider;

/**
 * Dialog to edit a Room
 */
public class ConfigureRoomDialogPage2SummaryFragment extends ConfigurationDialogFragment implements ConfigurationDialogTabbedSummaryFragment {

    private View rootView;

    private long roomId;

    private String currentRoomName;
    private ArrayList<Receiver> currentReceivers;

    private CheckBox checkBoxUseCustomGatewaySelection;
    private LinearLayout apartmentGateways;
    private LinearLayout otherGateways;
    private LinearLayout linearLayoutOfApartmentGateways;
    private LinearLayout linearLayoutOfOtherGateways;

    private Apartment apartment;
    private List<Gateway> gateways = new ArrayList<>();
    private List<CheckBox> gatewayCheckboxList = new ArrayList<>();
    private BroadcastReceiver broadcastReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_room_page_2, container, false);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (LocalBroadcastConstants.INTENT_ROOM_NAME_CHANGED.equals(intent.getAction())) {
                    currentRoomName = intent.getStringExtra(ConfigureRoomDialogPage1Fragment.KEY_NAME);
                    currentReceivers = (ArrayList<Receiver>) intent.getSerializableExtra(ConfigureRoomDialogPage1Fragment.KEY_RECEIVERS);
                }

                notifyConfigurationChanged();
            }
        };

        checkBoxUseCustomGatewaySelection = (CheckBox) rootView.findViewById(R.id.checkbox_use_custom_gateway_selection);
        CheckBoxInteractionListener checkBoxInteractionListener = new CheckBoxInteractionListener() {
            @Override
            public void onCheckedChangedByUser(CompoundButton buttonView, boolean isChecked) {
                updateCustomGatewaySelectionVisibility();

                notifyConfigurationChanged();
            }
        };
        checkBoxUseCustomGatewaySelection.setOnCheckedChangeListener(checkBoxInteractionListener);
        checkBoxUseCustomGatewaySelection.setOnTouchListener(checkBoxInteractionListener);

        apartmentGateways = (LinearLayout) rootView.findViewById(R.id.apartmentGateways);
        otherGateways = (LinearLayout) rootView.findViewById(R.id.otherGateways);

        linearLayoutOfApartmentGateways = (LinearLayout) rootView.findViewById(R.id.linearLayoutOfApartmentGateways);
        linearLayoutOfOtherGateways = (LinearLayout) rootView.findViewById(R.id.linearLayoutOfOtherGateways);

        try {
            apartment = DatabaseHandler.getApartment(SmartphonePreferencesHandler.getCurrentApartmentId());
            gateways = DatabaseHandler.getAllGateways();
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }

        updateGatewayViews(false);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureRoomDialog.ROOM_ID_KEY)) {
            roomId = args.getLong(ConfigureRoomDialog.ROOM_ID_KEY);
            initExistingData(roomId);
        }

        updateCustomGatewaySelectionVisibility();

        return rootView;
    }

    private void initExistingData(long roomId) {
        try {
            Room room = DatabaseHandler.getRoom(roomId);

            currentRoomName = room.getName();
            currentReceivers = room.getReceivers();

            if (!room.getAssociatedGateways().isEmpty()) {
                checkBoxUseCustomGatewaySelection.setChecked(true);
            }

            for (Gateway gateway : room.getAssociatedGateways()) {
                for (CheckBox checkBox : gatewayCheckboxList) {
                    Gateway checkBoxGateway = (Gateway) checkBox.getTag(R.string.gateways);
                    if (gateway.getId().equals(checkBoxGateway.getId())) {
                        checkBox.setChecked(true);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }

    private void updateGatewayViews(boolean keepCheckedItems) {
        try {
            List<Gateway> previouslyCheckedGateways = new ArrayList<>();
            if (keepCheckedItems) {
                previouslyCheckedGateways.addAll(getCheckedGateways());
            }

            gateways = DatabaseHandler.getAllGateways();

            String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(inflaterString);

            // clear previous items
            linearLayoutOfApartmentGateways.removeAllViews();
            linearLayoutOfOtherGateways.removeAllViews();
            gatewayCheckboxList.clear();
            for (Gateway gateway : gateways) {
                LinearLayout gatewayLayout;
                if (apartment != null && apartment.isAssociatedWith(gateway)) {
                    gatewayLayout = (LinearLayout) inflater.inflate(R.layout.gateway_overview, linearLayoutOfApartmentGateways, false);
                    linearLayoutOfApartmentGateways.addView(gatewayLayout);
                } else {
                    gatewayLayout = (LinearLayout) inflater.inflate(R.layout.gateway_overview, linearLayoutOfOtherGateways, false);
                    linearLayoutOfOtherGateways.addView(gatewayLayout);
                }

                final CheckBox checkBox = (CheckBox) gatewayLayout.findViewById(R.id.checkbox_use_gateway);
                checkBox.setTag(R.string.gateways, gateway);
                CheckBoxInteractionListener checkBoxInteractionListener = new CheckBoxInteractionListener() {
                    @Override
                    public void onCheckedChangedByUser(CompoundButton buttonView, boolean isChecked) {
                        notifyConfigurationChanged();
                    }
                };
                checkBox.setOnTouchListener(checkBoxInteractionListener);
                checkBox.setOnCheckedChangeListener(checkBoxInteractionListener);
                if (keepCheckedItems && !previouslyCheckedGateways.isEmpty()) {
                    for (Gateway previousGateway : previouslyCheckedGateways) {
                        if (previousGateway.getId().equals(gateway.getId())) {
                            checkBox.setChecked(true);
                            break;
                        }
                    }
                }
                gatewayCheckboxList.add(checkBox);

                gatewayLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkBox.setChecked(!checkBox.isChecked());

                        updateCustomGatewaySelectionVisibility();

                        notifyConfigurationChanged();
                    }
                });

                TextView gatewayName = (TextView) gatewayLayout.findViewById(R.id.textView_gatewayName);
                gatewayName.setText(gateway.getName());

                TextView gatewayType = (TextView) gatewayLayout.findViewById(R.id.textView_gatewayType);
                gatewayType.setText(gateway.getModel());

                TextView gatewayHost = (TextView) gatewayLayout.findViewById(R.id.textView_gatewayHost);
                gatewayHost.setText(String.format(Locale.getDefault(), "%s:%d", gateway.getLocalHost(), gateway.getLocalPort()));

                TextView gatewayDisabled = (TextView) gatewayLayout.findViewById(R.id.textView_disabled);
                if (gateway.isActive()) {
                    gatewayDisabled.setVisibility(View.GONE);
                } else {
                    gatewayDisabled.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    private void updateCustomGatewaySelectionVisibility() {
        if (checkBoxUseCustomGatewaySelection.isChecked()) {
            // hide sections if empty
            if (linearLayoutOfApartmentGateways.getChildCount() == 0) {
                apartmentGateways.setVisibility(View.GONE);
            } else {
                apartmentGateways.setVisibility(View.VISIBLE);
            }
            if (linearLayoutOfOtherGateways.getChildCount() == 0) {
                otherGateways.setVisibility(View.GONE);
            } else {
                otherGateways.setVisibility(View.VISIBLE);
            }
        } else {
            if (linearLayoutOfApartmentGateways.getChildCount() == 0) {
                apartmentGateways.setVisibility(View.GONE);
            } else {
                apartmentGateways.setVisibility(View.INVISIBLE);
            }
            if (linearLayoutOfOtherGateways.getChildCount() == 0) {
                otherGateways.setVisibility(View.GONE);
            } else {
                otherGateways.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Get selected Gateways
     *
     * @return List of Gateways
     */
    private ArrayList<Gateway> getCheckedGateways() {
        ArrayList<Gateway> checkedGateways = new ArrayList<>();

        if (!checkBoxUseCustomGatewaySelection.isChecked()) {
            return checkedGateways;
        }

        for (CheckBox checkBox : gatewayCheckboxList) {
            if (checkBox.isChecked()) {
                Gateway gateway = (Gateway) checkBox.getTag(R.string.gateways);
                checkedGateways.add(gateway);
            }
        }

        return checkedGateways;
    }

    /**
     * Checks the Setup page for validity
     *
     * @return true if valid, false otherwise
     */
    @Override
    public boolean checkSetupValidity() {

        if (TextUtils.isEmpty(currentRoomName)) {
            return false;
        }

        if (currentReceivers == null) {
            return false;
        }

        return getCheckedGateways() != null;

    }

    @Override
    public void saveCurrentConfigurationToDatabase() throws Exception {

        DatabaseHandler.updateRoom(roomId, currentRoomName, getCheckedGateways());

        // save receiver order
        for (int position = 0; position < currentReceivers.size(); position++) {
            Receiver receiver = currentReceivers.get(position);
            DatabaseHandler.setPositionOfReceiver(receiver.getId(), (long) position);
        }

        RoomsFragment.sendRoomChangedBroadcast(getActivity());
        // scenes could change too if room was used in a scene
        ScenesFragment.sendScenesChangedBroadcast(getActivity());

        // update room widgets
        RoomWidgetProvider.forceWidgetUpdate(getActivity());

        // update wear data
        UtilityService.forceWearDataUpdate(getActivity());

        StatusMessageHandler.showInfoMessage(((RecyclerViewFragment) getTargetFragment()).getRecyclerView()
                , R.string.room_saved, Snackbar.LENGTH_LONG);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_ROOM_NAME_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}