package io.kuz.screentracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenStateReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
			DatabaseWriterService.screenOn(context);
		} else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
			DatabaseWriterService.screenOff(context);
		}
	}
}
