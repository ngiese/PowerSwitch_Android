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

package eu.power_switch.database.handler;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import eu.power_switch.database.table.timer.TimerTable;
import eu.power_switch.database.table.timer.TimerWeekdayTable;
import eu.power_switch.timer.IntervalTimer;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;
import eu.power_switch.timer.action.TimerAction;
import eu.power_switch.timer.alarm.AlarmHandler;

/**
 * Provides database methods for managing Timers
 */
public abstract class TimerHandler {

    /**
     * Adds Timer to Database
     *
     * @param timer Timer
     */
    protected static long add(Timer timer) {
        ContentValues values = new ContentValues();
        values.put(TimerTable.COLUMN_ACTIVE, timer.isActive());
        values.put(TimerTable.COLUMN_NAME, timer.getName());
        values.put(TimerTable.COLUMN_EXECUTION_TIME, timer.getExecutionTime().getTimeInMillis());
        values.put(TimerTable.COLUMN_EXECUTION_INTERVAL, timer.getExecutionInterval());
        values.put(TimerTable.COLUMN_EXECUTION_TYPE, timer.getExecutionType());

        long timerId = DatabaseHandler.database.insert(TimerTable.TABLE_NAME, null, values);

        if (timerId > -1) {
            if (timer.getExecutionType().equals(Timer.EXECUTION_TYPE_WEEKDAY)) {
                insertWeekdayDetails((WeekdayTimer) timer, timerId);
            }

            TimerActionHandler.add(timer.getActions(), timerId);
        } else {
            // throw exception
            throw new RuntimeException();
        }

        // activate alarm
        AlarmHandler.createAlarm(DatabaseHandler.context, get(timerId));
        return timerId;
    }

    private static void insertWeekdayDetails(WeekdayTimer weekdayTimer, long timerId) {
        for (WeekdayTimer.Day day : weekdayTimer.getExecutionDays()) {
            ContentValues values = new ContentValues();
            values.put(TimerWeekdayTable.COLUMN_EXECUTION_DAY, day.positionInWeek);
            values.put(TimerWeekdayTable.COLUMN_TIMER_ID, timerId);

            DatabaseHandler.database.insert(TimerWeekdayTable.TABLE_NAME, null, values);
        }
    }

    /**
     * Deletes Timer from Database
     *
     * @param timerId ID of Timer
     */
    protected static void delete(long timerId) {
        AlarmHandler.cancelAlarm(DatabaseHandler.context, get(timerId));

        TimerActionHandler.delete(timerId);

        deleteWeekdayDetails(timerId);

        DatabaseHandler.database.delete(TimerTable.TABLE_NAME, TimerTable.COLUMN_ID +
                "=" + timerId, null);
    }

    private static void deleteWeekdayDetails(long timerId) {
        DatabaseHandler.database.delete(TimerWeekdayTable.TABLE_NAME, TimerWeekdayTable.COLUMN_TIMER_ID +
                "=" + timerId, null);
    }

    /**
     * Updates an existing Timer
     *
     * @param timer new Timer Object with same ID as existing one
     */
    protected static void update(Timer timer) {
        AlarmHandler.cancelAlarm(DatabaseHandler.context, get(timer.getId()));

        TimerActionHandler.update(timer);

        deleteWeekdayDetails(timer.getId());

        ContentValues values = new ContentValues();
        values.put(TimerTable.COLUMN_ACTIVE, timer.isActive());
        values.put(TimerTable.COLUMN_NAME, timer.getName());
        values.put(TimerTable.COLUMN_EXECUTION_TYPE, timer.getExecutionType());
        values.put(TimerTable.COLUMN_EXECUTION_INTERVAL, timer.getExecutionInterval());
        values.put(TimerTable.COLUMN_EXECUTION_TIME, timer.getExecutionTime().getTimeInMillis());

        DatabaseHandler.database.update(TimerTable.TABLE_NAME, values,
                TimerTable.COLUMN_ID + "=" + timer.getId(), null);

        if (Timer.EXECUTION_TYPE_WEEKDAY.equals(timer.getExecutionType())) {
            insertWeekdayDetails((WeekdayTimer) timer, timer.getId());
        }

        // activate new alarm if timer is active
        if (timer.isActive()) {
            AlarmHandler.createAlarm(DatabaseHandler.context, timer);
        }
    }

    /**
     * Gets a Timer from Database
     *
     * @param timerId
     * @return Timer
     */
    protected static Timer get(long timerId) {
        String[] columns = {TimerTable.COLUMN_ID, TimerTable.COLUMN_ACTIVE, TimerTable.COLUMN_NAME,
                TimerTable.COLUMN_EXECUTION_TIME, TimerTable.COLUMN_EXECUTION_INTERVAL,
                TimerTable.COLUMN_EXECUTION_TYPE};
        Cursor cursor = DatabaseHandler.database.query(TimerTable.TABLE_NAME, columns,
                TimerTable.COLUMN_ID + "=" + timerId, null, null, null, null);
        cursor.moveToFirst();

        Timer timer = dbToTimer(cursor);
        cursor.close();
        return timer;
    }

    /**
     * Gets all Timers from Database
     *
     * @return List of Timer
     */
    protected static List<Timer> getAll() {
        List<Timer> timers = new ArrayList<>();
        String[] columns = {TimerTable.COLUMN_ID, TimerTable.COLUMN_ACTIVE, TimerTable.COLUMN_NAME,
                TimerTable.COLUMN_EXECUTION_TIME, TimerTable.COLUMN_EXECUTION_INTERVAL,
                TimerTable.COLUMN_EXECUTION_TYPE};
        Cursor cursor = DatabaseHandler.database.query(TimerTable.TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            timers.add(dbToTimer(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return timers;
    }

    /**
     * Gets all active/inactive Timers from Database
     *
     * @return List of Timer
     */
    protected static List<Timer> getAll(boolean isActive) {
        List<Timer> timers = new ArrayList<>();
        int isActiveInt = isActive ? 1 : 0;
        String[] columns = {TimerTable.COLUMN_ID, TimerTable.COLUMN_ACTIVE, TimerTable.COLUMN_NAME,
                TimerTable.COLUMN_EXECUTION_TIME, TimerTable.COLUMN_EXECUTION_INTERVAL,
                TimerTable.COLUMN_EXECUTION_TYPE};
        Cursor cursor = DatabaseHandler.database.query(TimerTable.TABLE_NAME, columns, TimerTable.COLUMN_ACTIVE +
                "=" + isActiveInt, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            timers.add(dbToTimer(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return timers;
    }

    /**
     * Enables a Timer in Database
     *
     * @param id ID of Timer
     */
    protected static void enable(long id) {
        ContentValues values = new ContentValues();
        values.put(TimerTable.COLUMN_ACTIVE, 1);
        DatabaseHandler.database.update(TimerTable.TABLE_NAME, values, TimerTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Disables a Timer in Database
     *
     * @param id ID of Timer
     */
    protected static void disable(long id) {
        ContentValues values = new ContentValues();
        values.put(TimerTable.COLUMN_ACTIVE, 0);
        DatabaseHandler.database.update(TimerTable.TABLE_NAME, values, TimerTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Creates a Timer Object out of Database information
     *
     * @param c cursor pointing to a Timer database entry
     * @return Timer
     */
    private static Timer dbToTimer(Cursor c) {
        int timerId = c.getInt(0);
        int rawActive = c.getInt(1);
        boolean active;
        if (rawActive > 0) {
            active = true;
        } else {
            active = false;
        }
        String name = c.getString(2);
        long executionTimeRAW = c.getLong(3);
        Calendar executionTime = Calendar.getInstance();
        executionTime.setTime(new Date(executionTimeRAW));

        long executionInterval = c.getLong(4);
        String executionType = c.getString(5);

        ArrayList<TimerAction> actions = TimerActionHandler.getByTimerId(timerId);

        if (executionType.equals(Timer.EXECUTION_TYPE_WEEKDAY)) {
            ArrayList<WeekdayTimer.Day> weekdays = getWeekdayDetails(timerId);

            return new WeekdayTimer(timerId, active, name, executionTime, weekdays, actions);
        } else if (executionType.equals(Timer.EXECUTION_TYPE_INTERVAL)) {
            return new IntervalTimer(timerId, active, name, executionTime, executionInterval, actions);
        }

        return null;
    }

    private static ArrayList<WeekdayTimer.Day> getWeekdayDetails(int timerId) {
        return getExecutionDays(timerId);
    }

    private static ArrayList<WeekdayTimer.Day> getExecutionDays(long timerId) {
        ArrayList<WeekdayTimer.Day> days = new ArrayList<>();

        String[] columns = {TimerWeekdayTable.COLUMN_EXECUTION_DAY};
        Cursor cursor = DatabaseHandler.database.query(TimerWeekdayTable.TABLE_NAME, columns,
                TimerWeekdayTable.COLUMN_TIMER_ID + "=" + timerId, null, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            switch (cursor.getInt(0)) {
                case Calendar.MONDAY:
                    days.add(WeekdayTimer.Day.MONDAY);
                    break;
                case Calendar.TUESDAY:
                    days.add(WeekdayTimer.Day.TUESDAY);
                    break;
                case Calendar.WEDNESDAY:
                    days.add(WeekdayTimer.Day.WEDNESDAY);
                    break;
                case Calendar.THURSDAY:
                    days.add(WeekdayTimer.Day.THURSDAY);
                    break;
                case Calendar.FRIDAY:
                    days.add(WeekdayTimer.Day.FRIDAY);
                    break;
                case Calendar.SATURDAY:
                    days.add(WeekdayTimer.Day.SATURDAY);
                    break;
                case Calendar.SUNDAY:
                    days.add(WeekdayTimer.Day.SUNDAY);
                    break;
            }
            cursor.moveToNext();
        }

        cursor.close();
        return days;
    }
}