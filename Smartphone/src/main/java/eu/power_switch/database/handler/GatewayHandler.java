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
import java.util.List;

import eu.power_switch.database.table.gateway.GatewayTable;
import eu.power_switch.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.exception.gateway.GatewayHasBeenEnabledException;
import eu.power_switch.exception.gateway.UnknownGatewayException;
import eu.power_switch.log.Log;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;

/**
 * Provides database methods for managing Gateways
 */
public abstract class GatewayHandler {

    /**
     * Adds Gateway information to Database
     *
     * @param gateway the new Gateway
     * @return ID of new Database entry
     * @throws GatewayAlreadyExistsException
     * @throws GatewayHasBeenEnabledException
     */
    protected static long add(Gateway gateway) throws GatewayAlreadyExistsException, GatewayHasBeenEnabledException {
        for (Gateway existingGateway : getAll()) {
            if (existingGateway.hasSameAddress(gateway)) {
                if (existingGateway.isActive()) {
                    throw new GatewayAlreadyExistsException();
                } else {
                    enable(existingGateway.getId());
                    throw new GatewayHasBeenEnabledException();
                }
            }
        }

        ContentValues values = new ContentValues();
        values.put(GatewayTable.COLUMN_ACTIVE, gateway.isActive());
        values.put(GatewayTable.COLUMN_NAME, gateway.getName());
        values.put(GatewayTable.COLUMN_MODEL, gateway.getModelAsString());
        values.put(GatewayTable.COLUMN_FIRMWARE, gateway.getFirmware());
        values.put(GatewayTable.COLUMN_ADDRESS, gateway.getHost());
        values.put(GatewayTable.COLUMN_PORT, gateway.getPort());

        long newId = DatabaseHandler.database.insert(GatewayTable.TABLE_NAME, null, values);
        return newId;
    }

    /**
     * Enables an existing Gateway
     *
     * @param id ID of Gateway
     */
    protected static void enable(long id) {
        ContentValues values = new ContentValues();
        values.put(GatewayTable.COLUMN_ACTIVE, 1);
        DatabaseHandler.database.update(GatewayTable.TABLE_NAME, values, GatewayTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Disables an existing Gateway
     *
     * @param id ID of Gateway
     */
    protected static void disable(long id) {
        ContentValues values = new ContentValues();
        values.put(GatewayTable.COLUMN_ACTIVE, 0);
        DatabaseHandler.database.update(GatewayTable.TABLE_NAME, values, GatewayTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Updates an existing Gateway
     *
     * @param id      ID of Gateway
     * @param name    new Name
     * @param model   new Model
     * @param address new Address (Host)
     * @param port    new Port
     */
    protected static void update(long id, String name, String model, String address, Integer port) {
        ContentValues values = new ContentValues();
        values.put(GatewayTable.COLUMN_NAME, name);
        values.put(GatewayTable.COLUMN_MODEL, model);
        values.put(GatewayTable.COLUMN_ADDRESS, address);
        values.put(GatewayTable.COLUMN_PORT, port);
        DatabaseHandler.database.update(GatewayTable.TABLE_NAME, values, GatewayTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Deletes Gateway information from Database
     *
     * @param id ID of Gateway
     */
    protected static void delete(long id) {
        DatabaseHandler.database.delete(GatewayTable.TABLE_NAME, GatewayTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Gets Gateway from Database
     *
     * @param id ID of Gateway
     * @return Gateway
     */
    protected static Gateway get(long id) {
        Cursor cursor = DatabaseHandler.database.query(GatewayTable.TABLE_NAME, null, GatewayTable.COLUMN_ID + "=" + id, null, null,
                null, null);
        cursor.moveToFirst();
        Gateway gateway = dbToGateway(cursor);
        cursor.close();
        return gateway;
    }

    /**
     * Gets all Gateways from Database
     *
     * @return List of Gateways
     */
    protected static List<Gateway> getAll() {
        List<Gateway> gateways = new ArrayList<>();
        Cursor cursor = DatabaseHandler.database.query(GatewayTable.TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            gateways.add(dbToGateway(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return gateways;
    }

    /**
     * Gets all Gateways from Database
     *
     * @param isActive true if Gateway is enabled
     * @return List of enabled/disabled Gateways
     */
    protected static List<Gateway> getAll(boolean isActive) {
        List<Gateway> gateways = new ArrayList<>();
        int isActiveInt = isActive ? 1 : 0;
        Cursor cursor = DatabaseHandler.database.query(GatewayTable.TABLE_NAME, null, GatewayTable.COLUMN_ACTIVE + "=" + isActiveInt,
                null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            gateways.add(dbToGateway(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return gateways;
    }

    /**
     * Creates a Gateway Object out of Database information
     *
     * @param c cursor pointing to a gateway database entry
     * @return Gateway, can be null
     */
    private static Gateway dbToGateway(Cursor c) {
        try {
            Gateway gateway;
            int id = c.getInt(0);
            int rawActive = c.getInt(1);
            boolean active;
            String name = c.getString(2);
            String rawModel = c.getString(3);
            String firmware = c.getString(4);
            String address = c.getString(5);
            int port = c.getInt(6);

            if (rawActive > 0) {
                active = true;
            } else {
                active = false;
            }

            if (rawModel.equals(BrematicGWY433.MODEL)) {
                gateway = new BrematicGWY433(id, active, name, firmware, address, port);
            } else if (rawModel.equals(ConnAir.MODEL)) {
                gateway = new ConnAir(id, active, name, firmware, address, port);
            } else if (rawModel.equals(ITGW433.MODEL)) {
                gateway = new ITGW433(id, active, name, firmware, address, port);
            } else {
                throw new UnknownGatewayException();
            }

            return gateway;
        } catch (Exception e) {
            Log.e(e);
            return null;
        }
    }

}