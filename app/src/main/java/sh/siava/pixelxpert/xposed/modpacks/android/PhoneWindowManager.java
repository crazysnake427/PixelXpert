package sh.siava.pixelxpert.xposed.modpacks.android;

import static android.content.Context.RECEIVER_EXPORTED;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static sh.siava.pixelxpert.xposed.utils.SystemUtils.PackageManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.view.Display;
import android.view.WindowManager;

import java.util.List;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import sh.siava.pixelxpert.xposed.Constants;
import sh.siava.pixelxpert.xposed.annotations.FrameworkModPack;
import sh.siava.pixelxpert.xposed.XposedModPack;
import sh.siava.pixelxpert.xposed.utils.SystemUtils;
import sh.siava.pixelxpert.xposed.utils.toolkit.ReflectedClass;

@SuppressWarnings("RedundantThrows")
@FrameworkModPack
public class PhoneWindowManager extends XposedModPack {
	private Object windowMan = null;
	private static boolean broadcastRegistered = false;

	public PhoneWindowManager(Context context) {
		super(context);
	}

	@Override
	public void onPreferenceUpdated(String... Key) {}

	final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
				//noinspection DataFlowIssue
				switch (action) {
					case Constants.ACTION_HOME:
						callMethod(windowMan, "launchHomeFromHotKey", Display.DEFAULT_DISPLAY);
						break;
					case Constants.ACTION_BACK:
						callMethod(windowMan, "backKeyPress");
						break;
					case Constants.ACTION_SLEEP:
						SystemUtils.sleep();
						break;
				}
			} catch (Throwable ignored) {
			}
		}
	};

	@SuppressLint("WrongConstant")
	@Override
	public void onPackageLoaded(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
//		Collections.addAll(screenshotChords, KEYCODE_POWER, KEYCODE_VOLUME_DOWN);

		if (!broadcastRegistered) {
			broadcastRegistered = true;

			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.ACTION_HOME);
			intentFilter.addAction(Constants.ACTION_BACK);
			intentFilter.addAction(Constants.ACTION_SLEEP);
			mContext.registerReceiver(broadcastReceiver, intentFilter, RECEIVER_EXPORTED); //for Android 14, receiver flag is mandatory
		}

		try {
			ReflectedClass PhoneWindowManagerClass = ReflectedClass.of("com.android.server.policy.PhoneWindowManager");

			PhoneWindowManagerClass
					.after("enableScreen")
					.run(param -> windowMan = param.thisObject);
		} catch (Throwable ignored) {
		}
	}
}