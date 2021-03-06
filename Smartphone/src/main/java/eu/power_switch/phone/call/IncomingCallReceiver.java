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

package eu.power_switch.phone.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import eu.power_switch.shared.log.Log;

/**
 * BroadcastReceiver to get notified about incoming calls
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    private Context mContext;
    private Intent mIntent;
    private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            String callState = "UNKNOWN";
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    callState = "IDLE";
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    // -- check international call or not.
                    if (incomingNumber.startsWith("00")) {
                        Toast.makeText(mContext, "International Call- " + incomingNumber, Toast.LENGTH_LONG).show();
                        callState = "International - Ringing (" + incomingNumber + ")";
                    } else {
                        Toast.makeText(mContext, "Local Call - " + incomingNumber, Toast.LENGTH_LONG).show();
                        callState = "Local - Ringing (" + incomingNumber + ")";
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    String dialingNumber = mIntent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    if (dialingNumber.startsWith("00")) {
                        Toast.makeText(mContext, "International - " + dialingNumber, Toast.LENGTH_LONG).show();
                        callState = "International - Dialing (" + dialingNumber + ")";
                    } else {
                        Toast.makeText(mContext, "Local Call - " + dialingNumber, Toast.LENGTH_LONG).show();
                        callState = "Local - Dialing (" + dialingNumber + ")";
                    }
                    break;
            }
            Log.d(">>>Broadcast", "onCallStateChanged " + callState);
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int events = PhoneStateListener.LISTEN_CALL_STATE;
        tm.listen(phoneStateListener, events);
    }

}