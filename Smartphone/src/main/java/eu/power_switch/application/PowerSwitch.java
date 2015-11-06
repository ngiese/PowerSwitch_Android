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

package eu.power_switch.application;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import eu.power_switch.log.Log;
import eu.power_switch.log.Log4JConfiguration;

/**
 * Entry Point for the Application
 * <p/>
 * Created by Markus on 11.08.2015.
 */
public class PowerSwitch extends MultiDexApplication {

    // Default System Handler for uncaught Exceptions
    private Thread.UncaughtExceptionHandler originalUncaughtExceptionHandler;

    public PowerSwitch() {
        // Configure Log4J Logger
        Log4JConfiguration.configure();

        // save original uncaught exception handler
        originalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        // Set up our own UncaughtExeptionHandler to log errors we couldn't even think of
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e("Fatal Exception", new Exception(ex));
                // pass on exception to android system
                originalUncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}