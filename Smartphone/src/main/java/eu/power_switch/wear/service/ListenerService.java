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

package eu.power_switch.wear.service;

import android.support.design.widget.Snackbar;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.obj.receiver.Button;
import eu.power_switch.obj.receiver.Room;
import eu.power_switch.obj.receiver.Scene;
import eu.power_switch.obj.receiver.device.Receiver;
import eu.power_switch.shared.constants.WearableConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;

/**
 * A Wear listener service, used to receive inbound messages from
 * the Wear device.
 * <p/>
 * Created by Markus on 04.06.2015.
 */
public class ListenerService extends WearableListenerService {

    /**
     * This method is called when a message from a wearable device is received
     *
     * @param messageEvent
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LogHandler.configureLogger();

        if (messageEvent.getPath().equals(WearableConstants.RECEIVER_ACTION_TRIGGER_PATH)) {

            String messageData = new String(messageEvent.getData());
            Log.d("Wear_ListenerService", "Message received: " + messageData);

            // trigger api intent
            parseMessage(messageData);
        } else if (messageEvent.getPath().equals(WearableConstants.REQUEST_DATA_UPDATE_PATH)) {
            UtilityService.forceWearDataUpdate(this);
        } else if (messageEvent.getPath().equals(WearableConstants.REQUEST_SETTINGS_UPDATE_PATH)) {
            UtilityService.forceWearSettingsUpdate(this);
        }
    }

    /**
     * Parse message string
     *
     * @param messageData
     * @return
     */
    private void parseMessage(String messageData) {
        try {
            String roomName;
            String receiverName;
            String buttonName;

            if (messageData.contains("RoomName") && messageData.contains("ReceiverName") && messageData.contains("ButtonName")) {
                int start = messageData.indexOf("RoomName:") + 9;
                int stop = messageData.indexOf("ReceiverName:");
                roomName = messageData.substring(start, stop);
                start = stop + 13;
                stop = messageData.indexOf("ButtonName:");
                receiverName = messageData.substring(start, stop);
                start = stop + 11;
                stop = messageData.indexOf(";;");
                buttonName = messageData.substring(start, stop);

                Room room = DatabaseHandler.getRoom(roomName);
                Receiver receiver = room.getReceiver(receiverName);
                Button button = receiver.getButton(buttonName);

                ActionHandler.executeAction(getApplicationContext(), receiver, button);
            } else if (messageData.contains("RoomName") && messageData.contains("ButtonName")) {
                int start = messageData.indexOf("RoomName:") + 9;
                int stop = messageData.indexOf("ButtonName:");
                roomName = messageData.substring(start, stop);
                start = stop + 11;
                stop = messageData.indexOf(";;");
                buttonName = messageData.substring(start, stop);

                Room room = DatabaseHandler.getRoom(roomName);

                ActionHandler.executeAction(getApplicationContext(), room, buttonName);
            } else if (messageData.contains("SceneName")) {
                int start = messageData.indexOf("SceneName:") + 10;
                int stop = messageData.indexOf(";;");
                String sceneName = messageData.substring(start, stop);

                Scene scene = DatabaseHandler.getScene(sceneName);

                ActionHandler.executeAction(getApplicationContext(), scene);
            }
        } catch (Exception e) {
            Log.e("parseMessage", e);
            StatusMessageHandler.showStatusMessage(getApplicationContext(), R.string.error_executing_wear_action,
                    Snackbar.LENGTH_LONG);
        }
    }
}
