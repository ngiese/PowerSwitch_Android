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

package eu.power_switch.gui.fragment.settings;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.dialog.DeveloperOptionsDialog;
import eu.power_switch.gui.dialog.PathChooserDialog;
import eu.power_switch.gui.fragment.AsyncTaskResult;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.exception.permission.MissingPermissionException;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;
import eu.power_switch.shared.permission.PermissionHelper;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Fragment containing all settings related to Smartphone app
 * <p/>
 * Created by Markus on 30.08.2015.
 */
public class GeneralSettingsFragment extends Fragment {

    private View rootView;

    private CheckBox autoDiscover;
    private CheckBox autoCollapseRooms;
    private CheckBox autoCollapseTimers;
    private CheckBox showRoomAllOnOffButtons;
    private CheckBox hideAddFAB;
    private CheckBox highlightLastActivatedButton;
    private CheckBox showToastInBackground;
    private CheckBox sendAnonymousCrashData;

    private LinearLayout vibrationDurationLayout;
    private CheckBox vibrateOnButtonPress;
    private EditText vibrationDuration;

    private RadioButton radioButtonDarkBlue;
    private RadioButton radioButtonLightBlue;
    private RadioButton radioButtonDayNightBlue;

    private int devMenuClickCounter = 0;
    private Calendar devMenuFirstClickTime;
    private Spinner startupDefaultTab;
    private BroadcastReceiver broadcastReceiver;
    private TextView textView_backupPath;
    private Spinner keepHistoryDuration;
    private ProgressBar sendLogsProgress;
    private Button resetTutorial;
    private Button sendLogs;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_general_settings, container, false);

        final Fragment fragment = this;

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                switch (buttonView.getId()) {
                    case R.id.checkBox_autoDiscover:
                        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_AUTO_DISCOVER, isChecked);
                        break;
                    case R.id.checkBox_autoCollapseRooms:
                        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS, isChecked);
                        break;
                    case R.id.checkBox_autoCollapseTimers:
                        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_AUTO_COLLAPSE_TIMERS, isChecked);
                        break;
                    case R.id.checkBox_showRoomAllOnOffButtons:
                        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_SHOW_ROOM_ALL_ON_OFF, isChecked);
                        break;
                    case R.id.checkBox_hideAddFAB:
                        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB, isChecked);
                        break;
                    case R.id.checkBox_showToast:
                        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_SHOW_TOAST_IN_BACKGROUND, isChecked);
                        break;
                    case R.id.checkBox_vibrateOnButtonPress:
                        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS, isChecked);
                        if (isChecked) {
                            vibrationDurationLayout.setVisibility(View.VISIBLE);
                        } else {
                            vibrationDurationLayout.setVisibility(View.GONE);
                        }
                        break;
                    case R.id.checkBox_highlightLastActivatedButton:
                        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON, isChecked);
                        // force receiver widget update
                        ReceiverWidgetProvider.forceWidgetUpdate(getActivity());
                        break;
                    case R.id.checkBox_sendAnonymousCrashData:
                        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_SEND_ANONYMOUS_CRASH_DATA, isChecked);

                        // TODO: Disable Fabric at RUNTIME, currently not possible :(
                        break;
                    default:
                        break;
                }
            }
        };

        // setup hidden developer menu
        TextView generalSettingsTextView = (TextView) rootView.findViewById(R.id.TextView_generalSettings);
        generalSettingsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                if (devMenuFirstClickTime != null) {
                    Calendar latestTime = Calendar.getInstance();
                    latestTime.setTime(devMenuFirstClickTime.getTime());
                    latestTime.add(Calendar.SECOND, 5);
                    if (currentTime.after(latestTime)) {
                        devMenuClickCounter = 0;
                    }
                }

                devMenuClickCounter++;
                if (devMenuClickCounter == 1) {
                    devMenuFirstClickTime = currentTime;
                }
                if (devMenuClickCounter >= 5) {
                    devMenuClickCounter = 0;

                    DeveloperOptionsDialog developerOptionsDialog = new DeveloperOptionsDialog();
                    developerOptionsDialog.show(getActivity().getSupportFragmentManager(), null);
                }
            }
        });


        startupDefaultTab = (Spinner) rootView.findViewById(R.id.spinner_startupDefaultTab);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.main_tab_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startupDefaultTab.setAdapter(adapter);
        startupDefaultTab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_STARTUP_DEFAULT_TAB, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        autoDiscover = (CheckBox) rootView.findViewById(R.id.checkBox_autoDiscover);
        autoDiscover.setOnCheckedChangeListener(onCheckedChangeListener);

        autoCollapseRooms = (CheckBox) rootView.findViewById(R.id.checkBox_autoCollapseRooms);
        autoCollapseRooms.setOnCheckedChangeListener(onCheckedChangeListener);

        autoCollapseTimers = (CheckBox) rootView.findViewById(R.id.checkBox_autoCollapseTimers);
        autoCollapseTimers.setOnCheckedChangeListener(onCheckedChangeListener);

        showRoomAllOnOffButtons = (CheckBox) rootView.findViewById(R.id.checkBox_showRoomAllOnOffButtons);
        showRoomAllOnOffButtons.setOnCheckedChangeListener(onCheckedChangeListener);

        hideAddFAB = (CheckBox) rootView.findViewById(R.id.checkBox_hideAddFAB);
        hideAddFAB.setOnCheckedChangeListener(onCheckedChangeListener);

        highlightLastActivatedButton = (CheckBox) rootView.findViewById(R.id.checkBox_highlightLastActivatedButton);
        highlightLastActivatedButton.setOnCheckedChangeListener(onCheckedChangeListener);

        showToastInBackground = (CheckBox) rootView.findViewById(R.id.checkBox_showToast);
        showToastInBackground.setOnCheckedChangeListener(onCheckedChangeListener);

        vibrateOnButtonPress = (CheckBox) rootView.findViewById(R.id.checkBox_vibrateOnButtonPress);
        vibrateOnButtonPress.setOnCheckedChangeListener(onCheckedChangeListener);

        vibrationDurationLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout_vibrationDuration);
        vibrationDuration = (EditText) rootView.findViewById(R.id.editText_vibrationDuration);
        vibrationDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_VIBRATION_DURATION, Integer.valueOf(s.toString()));
                }
            }
        });

        keepHistoryDuration = (Spinner) rootView.findViewById(R.id.spinner_keep_history);
        ArrayAdapter<CharSequence> adapterHistory = ArrayAdapter.createFromResource(getActivity(),
                R.array.entryValues_history, android.R.layout.simple_spinner_item);
        adapterHistory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        keepHistoryDuration.setAdapter(adapterHistory);
        keepHistoryDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_KEEP_HISTORY_DURATION, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        textView_backupPath = (TextView) rootView.findViewById(R.id.textView_backupPath);

        Button button_changeBackupPath = (Button) rootView.findViewById(R.id.button_changeBackupPath);
        button_changeBackupPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionHelper.isWriteExternalStoragePermissionAvailable(getActivity())) {
                    Snackbar snackbar = Snackbar.make(rootView, R.string.missing_external_storage_permission, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.grant, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.getActivity(), new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION);
                        }
                    });
                    snackbar.show();
                }

                PathChooserDialog pathChooserDialog = PathChooserDialog.newInstance();
                pathChooserDialog.setTargetFragment(fragment, 0);
                pathChooserDialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.radioButton_darkBlue:
                        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_THEME, SettingsConstants.THEME_DARK_BLUE);
                        break;
                    case R.id.radioButton_lightBlue:
                        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_THEME, SettingsConstants.THEME_LIGHT_BLUE);
                        break;
                    case R.id.radioButton_dayNight_blue:
                        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_THEME, SettingsConstants.THEME_DAY_NIGHT_BLUE);
                        break;
                    default:
                        break;
                }

                getActivity().finish();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };

        radioButtonDarkBlue = (RadioButton) rootView.findViewById(R.id.radioButton_darkBlue);
        radioButtonDarkBlue.setOnClickListener(onClickListener);

        radioButtonLightBlue = (RadioButton) rootView.findViewById(R.id.radioButton_lightBlue);
        radioButtonLightBlue.setOnClickListener(onClickListener);

        radioButtonDayNightBlue = (RadioButton) rootView.findViewById(R.id.radioButton_dayNight_blue);
        radioButtonDayNightBlue.setOnClickListener(onClickListener);


        sendAnonymousCrashData = (CheckBox) rootView.findViewById(R.id.checkBox_sendAnonymousCrashData);
        sendAnonymousCrashData.setOnCheckedChangeListener(onCheckedChangeListener);

        sendLogsProgress = (ProgressBar) rootView.findViewById(R.id.sendLogsProgress);
        sendLogs = (Button) rootView.findViewById(R.id.button_sendLogs);
        sendLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLogs.setEnabled(false);
                sendLogsProgress.setVisibility(View.VISIBLE);

                new AsyncTask<Void, Void, AsyncTaskResult<Boolean>>() {
                    @Override
                    protected AsyncTaskResult<Boolean> doInBackground(Void... params) {
                        try {
                            LogHandler.sendLogsAsMail(getActivity());
                            return new AsyncTaskResult<>(true);
                        } catch (Exception e) {
                            return new AsyncTaskResult<>(e);
                        }
                    }

                    @Override
                    protected void onPostExecute(AsyncTaskResult<Boolean> booleanAsyncTaskResult) {

                        if (booleanAsyncTaskResult.isSuccess()) {
                            // all is good
                        } else {
                            if (booleanAsyncTaskResult.getException() instanceof MissingPermissionException) {
                                Snackbar snackbar = Snackbar.make(rootView, R.string.missing_external_storage_permission, Snackbar.LENGTH_LONG);
                                snackbar.setAction(R.string.grant, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(MainActivity.getActivity(), new String[]{
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION);
                                    }
                                });
                                snackbar.show();
                            } else {
                                StatusMessageHandler.showErrorMessage(getActivity(), booleanAsyncTaskResult.getException());
                            }
                        }

                        sendLogs.setEnabled(true);
                        sendLogsProgress.setVisibility(View.GONE);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        resetTutorial = (Button) rootView.findViewById(R.id.button_resetTutorial);
        resetTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialShowcaseView.resetAll(getActivity());
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());
                updateUI();
            }
        };

        return rootView;
    }

    private void updateUI() {
        startupDefaultTab.setSelection(SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_STARTUP_DEFAULT_TAB));
        autoDiscover.setChecked(SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_AUTO_DISCOVER));
        autoCollapseRooms.setChecked(SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS));
        autoCollapseTimers.setChecked(SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_AUTO_COLLAPSE_TIMERS));
        showRoomAllOnOffButtons.setChecked(SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_SHOW_ROOM_ALL_ON_OFF));
        hideAddFAB.setChecked(SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB));
        highlightLastActivatedButton.setChecked(SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON));
        showToastInBackground.setChecked(SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_SHOW_TOAST_IN_BACKGROUND));
        vibrateOnButtonPress.setChecked(SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS));
        vibrationDuration.setText(String.valueOf(SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_VIBRATION_DURATION)));
        if (!SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS)) {
            vibrationDurationLayout.setVisibility(View.GONE);
        } else {
            vibrationDurationLayout.setVisibility(View.VISIBLE);
        }
        keepHistoryDuration.setSelection(SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_KEEP_HISTORY_DURATION));

        textView_backupPath.setText(SmartphonePreferencesHandler.<String>get(SmartphonePreferencesHandler.KEY_BACKUP_PATH));

        switch (SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_THEME)) {
            case SettingsConstants.THEME_DARK_BLUE:
                radioButtonDarkBlue.setChecked(true);
                break;
            case SettingsConstants.THEME_DARK_RED:
                break;
            case SettingsConstants.THEME_LIGHT_BLUE:
                radioButtonLightBlue.setChecked(true);
                break;
            case SettingsConstants.THEME_LIGHT_RED:
                break;
            case SettingsConstants.THEME_DAY_NIGHT_BLUE:
                radioButtonDayNightBlue.setChecked(true);
                break;
            default:
                break;
        }

        sendAnonymousCrashData.setChecked(SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_SEND_ANONYMOUS_CRASH_DATA));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_BACKUP_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}
