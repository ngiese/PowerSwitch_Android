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

package eu.power_switch.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import eu.power_switch.application.RunConfig;
import eu.power_switch.persistence.PersistenceHandler;
import eu.power_switch.persistence.data.demo_mode.DemoModePersistenceHandler;
import eu.power_switch.persistence.data.sqlite.handler.SqlitePersistenceHandler;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class PersistenceBindingsModule {

    @Provides
    public static PersistenceHandler providePersistenceHandler(RunConfig runConfig, DemoModePersistenceHandler demoModePersistenceHandler,
                                                               SqlitePersistenceHandler sqlitePersistenceHandler) {
        switch (runConfig.getMode()) {
            case DEMO:
                return demoModePersistenceHandler;
            case NORMAL:
            default:
                return sqlitePersistenceHandler;
        }

    }

    @Provides
    @Singleton
    public static WearablePreferencesHandler provideWearablePreferencesHandler(Context context) {
        return new WearablePreferencesHandler(context);
    }

}
