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

package eu.power_switch.obj.receiver.device.intertechno;

import android.content.Context;

import java.util.List;

import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.MasterSlaveReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.shared.exception.receiver.ActionNotSupportedException;

public class GRR300 extends CMR1000 implements MasterSlaveReceiver {

    private static final String MODEL = Receiver.getModelName(GRR300.class.getCanonicalName());

    public GRR300(Context context, Long id, String name, char channelMaster, int channelSlave, Long roomId, List<Gateway> associatedGateways) {
        super(context, id, name, channelMaster, channelSlave, roomId, associatedGateways);
        model = MODEL;
    }

    @Override
    public String getSignal(Gateway gateway, String action) throws GatewayNotSupportedException, ActionNotSupportedException {
        return super.getSignal(gateway, action);
    }
}