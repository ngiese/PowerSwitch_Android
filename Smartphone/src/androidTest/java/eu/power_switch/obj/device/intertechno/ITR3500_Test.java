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

package eu.power_switch.obj.device.intertechno;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.obj.ReceiverTest;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.device.intertechno.ITR3500;

/**
 * Created by Markus on 08.08.2015.
 */
public class ITR3500_Test extends ReceiverTest {

    @Test
    public void testCodeGenerationA1() throws Exception {
        receiver = new ITR3500(getContext(), (long) 0, "Name", 'A', 1, (long) 0, new ArrayList<Gateway>());

        String generatedMessage = invokeGetSignal(connAir, getContext().getString(R.string.on));

        // ON
        String expectedMessage = "TXP:0,0,6,11125,89,25,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,12,4,4,12,12,4,4,12,12,4,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);

        generatedMessage = invokeGetSignal(connAir, getContext().getString(R.string.off));

        // OFF
        expectedMessage = "TXP:0,0,6,11125,89,25,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,12,4,4,12,12,4,4,12,4,12,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

    @Test
    public void testCodeGenerationA5() throws Exception {
        receiver = new ITR3500(getContext(), (long) 0, "Name", 'A', 5, (long) 0, new ArrayList<Gateway>());

        String generatedMessage = invokeGetSignal(connAir, getContext().getString(R.string.on));

        // ON
        String expectedMessage = "TXP:0,0,6,11125,89,25,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,12,4,4,12,4,12,4,12,4,12,4,12,12,4,4,12,12,4,4,12,12,4,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);

        generatedMessage = invokeGetSignal(connAir, getContext().getString(R.string.off));

        // OFF
        expectedMessage = "TXP:0,0,6,11125,89,25,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,12,4,4,12,4,12,4,12,4,12,4,12,12,4,4,12,12,4,4,12,4,12,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

    @Test
    public void testCodeGenerationE1() throws Exception {
        receiver = new ITR3500(getContext(), (long) 0, "Name", 'E', 1, (long) 0, new ArrayList<Gateway>());

        String generatedMessage = invokeGetSignal(connAir, getContext().getString(R.string.on));

        // ON
        String expectedMessage = "TXP:0,0,6,11125,89,25,4,12,4,12,4,12,4,12,4,12,12,4,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,12,4,4,12,12,4,4,12,12,4,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);

        generatedMessage = invokeGetSignal(connAir, getContext().getString(R.string.off));

        // OFF
        expectedMessage = "TXP:0,0,6,11125,89,25,4,12,4,12,4,12,4,12,4,12,12,4,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,12,4,4,12,12,4,4,12,4,12,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

    @Test
    public void testCodeGenerationP16() throws Exception {
        receiver = new ITR3500(getContext(), (long) 0, "Name", 'P', 16, (long) 0, new ArrayList<Gateway>());

        String generatedMessage = invokeGetSignal(connAir, getContext().getString(R.string.on));

        // ON
        String expectedMessage = "TXP:0,0,6,11125,89,25,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,4,12,4,12,12,4,4,12,12,4,4,12,12,4,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);

        generatedMessage = invokeGetSignal(connAir, getContext().getString(R.string.off));

        // OFF
        expectedMessage = "TXP:0,0,6,11125,89,25,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,4,12,4,12,12,4,4,12,12,4,4,12,4,12,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

}
