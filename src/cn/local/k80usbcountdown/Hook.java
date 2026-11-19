package cn.local.k80usbcountdown;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class Hook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loaded) {
        if (!"com.miui.securitycenter".equals(loaded.packageName)
                || !"zorn".equals(Build.DEVICE)
                || !"OS4.0.3.0.XOKCNXM".equals(Build.VERSION.INCREMENTAL)) {
            return;
        }
        XposedHelpers.findAndHookMethod(
                "com.miui.permcenter.install.AdbInputApplyActivity",
                loaded.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Exception {
                        Activity activity = (Activity) param.thisObject;
                        Intent intent = activity.getIntent();
                        if (param.hasThrowable() || param.args[0] != null || intent == null
                                || !"com.miui.securitycenter.action.ADB_INPUT_APPLY".equals(intent.getAction())
                                || !isSupportedSettingsRequest(activity)) {
                            return;
                        }
                        int acceptId = activity.getResources().getIdentifier(
                                "accept", "id", activity.getPackageName());
                        View accept = activity.findViewById(acceptId);
                        if (accept == null) {
                            return;
                        }
                        View.OnClickListener flow = (View.OnClickListener) activity;
                        // Advance the three warning pages through Xiaomi's own handler.
                        // The final click launches the original verification activity.
                        for (int page = 0; page < 3; page++) {
                            flow.onClick(accept);
                        }
                        XposedBridge.log("K80UsbCountdown: advanced USB security warning pages");
                    }
                });
        XposedHelpers.findAndHookMethod(
                "com.miui.permcenter.privacymanager.SpecialPermissionInterceptActivity",
                loaded.classLoader, "B0", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Exception {
                        Activity activity = (Activity) param.thisObject;
                        Intent intent = activity.getIntent();
                        if (intent == null) {
                            return;
                        }
                        String permission = intent.getStringExtra("permName");
                        if (!"miui_open_debug".equals(permission) && !"miui_barrier_free".equals(permission)) {
                            return;
                        }
                        if (!isSupportedSettingsRequest(activity)) {
                            XposedBridge.log("K80UsbCountdown: unsupported caller or app version");
                            return;
                        }
                        // Xiaomi's USB confirmation writes this setting before returning.
                        if ("miui_open_debug".equals(permission)
                                && !Settings.Global.putInt(activity.getContentResolver(),
                                        Settings.Global.ADB_ENABLED, 1)) {
                            return;
                        }
                        activity.setResult(Activity.RESULT_OK);
                        activity.finish();
                        param.setResult(null);
                        XposedBridge.log("K80UsbCountdown: accepted Settings request: " + permission);
                    }
                });
    }

    private static boolean isSupportedSettingsRequest(Activity activity) throws Exception {
        return "com.android.settings".equals(activity.getCallingPackage())
                && activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0)
                        .getLongVersionCode() == 40001381L;
    }
}
