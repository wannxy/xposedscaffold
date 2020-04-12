package org.n.xposedscaffold;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class Bus implements IXposedHookLoadPackage {

    private List<ClassLoader> loaders = new ArrayList<>();
    private String packageName = "";

    public Bus(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.log("find " + packageName);
        if (!loadPackageParam.packageName.equals(packageName)) {
            return;
        }
        XposedHelpers.findAndHookMethod("android.app.Application",
                loadPackageParam.classLoader,
                "attach", Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Context context = (Context) param.args[0];
                        loaders.add(context.getClassLoader());
                        Hook.setLoaders(loaders);
                        hooks();
                    }
                });
    }

    public abstract void hooks();
}
