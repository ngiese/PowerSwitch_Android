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

package eu.power_switch.widget.provider;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import eu.power_switch.R;
import eu.power_switch.api.IntentReceiver;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.log.Log;
import eu.power_switch.obj.Room;
import eu.power_switch.widget.RoomWidget;
import eu.power_switch.widget.activity.ConfigureRoomWidgetActivity;

/**
 * This class is responsible for updating existing Room widgets
 */
public class RoomWidgetProvider extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("Updating Room Widgets...");
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews remoteViews = new RemoteViews(context.getResources()
                    .getString(eu.power_switch.shared.R.string.PACKAGE_NAME), R.layout.widget_room);

            DatabaseHandler.init(context);

            RoomWidget roomWidget = DatabaseHandler.getRoomWidget(appWidgetId);
            if (roomWidget != null) {
                Room room = DatabaseHandler.getRoom(roomWidget.getRoomId());

                if (room != null) {
                    // update UI
                    remoteViews.setTextViewText(R.id.textView_room_widget_name, room.getName());

                    // set button action
                    remoteViews.setOnClickPendingIntent(R.id.button_on,
                            IntentReceiver.buildRoomButtonPendingIntent(context, room.getName(), context.getString(R
                                    .string.on), ConfigureRoomWidgetActivity.ROOM_INTENT_ID_OFFSET + appWidgetId));
                    remoteViews.setOnClickPendingIntent(R.id.button_off,
                            IntentReceiver.buildRoomButtonPendingIntent(context, room.getName(), context.getString(R
                                    .string.off), ConfigureRoomWidgetActivity.ROOM_INTENT_ID_OFFSET + appWidgetId + 1));
                    remoteViews.setViewVisibility(R.id.linearlayout_room_widget, View.VISIBLE);
                } else {
                    remoteViews.setTextViewText(R.id.textView_room_widget_name, context.getString(R.string.room_deleted));
                    remoteViews.setViewVisibility(R.id.linearlayout_room_widget, View.GONE);
                }
            } else {
                remoteViews.setTextViewText(R.id.textView_room_widget_name, context.getString(R.string.unknown_error));
                remoteViews.setViewVisibility(R.id.linearlayout_room_widget, View.GONE);
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d("Deleting Room Widgets: " + appWidgetIds.toString());
        for (int appWidgetId : appWidgetIds) {
            DatabaseHandler.deleteRoomWidget(appWidgetId);
        }
        super.onDeleted(context, appWidgetIds);
    }
}