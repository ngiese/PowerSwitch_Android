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

package eu.power_switch.obj.device;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.gateway.Gateway;

public class UniversalReceiver extends Receiver {

    private List<UniversalButton> universalButtons = new ArrayList<>();

    public UniversalReceiver(Context context, long id, String name, List<UniversalButton> buttons, long
            roomId) {
        super(context, id, name, BRAND_UNIVERSAL, BRAND_UNIVERSAL, Receiver.TYPE_UNIVERSAL, roomId);
        universalButtons.addAll(buttons);
        for (UniversalButton universalButton : universalButtons) {
            this.buttons.add(new Button(universalButton.getId(), universalButton.getName(), universalButton
                    .getReceiverId()));
        }
    }

    @Override
    protected String getSignal(Gateway gateway, String action) {
        for (UniversalButton button : DatabaseHandler.getButtons(id)) {
            if (button.getName().equals(action)) {
                return button.getSignal();
            }
        }
        return null;
    }

    public List<UniversalButton> getUniversalButtons() {
        return universalButtons;
    }
}