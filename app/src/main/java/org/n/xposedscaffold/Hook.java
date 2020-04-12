package org.n.xposedscaffold;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public abstract class Hook {

    private static List<ClassLoader> loaders;
    private String className = "";
    private Map<String,List<Method>> methods = new HashMap<>();
    private Map<String,Field> fields = new HashMap<>();
    private List<Constructor> constructors = new ArrayList<>();
    private Class selfContext;

    public Hook(String className) {
        this.className = className;
        _init_();
    }

    public static void setLoaders(List<ClassLoader> loaders) {
        Hook.loaders = loaders;
    }

    public void hookMethod(String methodName,Object... parameterAndCallBack){
        if (methods.containsKey(methodName)){
            for (ClassLoader loader : loaders){
                XposedHelpers.findAndHookMethod(className,loader,methodName,parameterAndCallBack);
            }
        }
    }

    public static Class findClass(String className){
        for (ClassLoader loader:loaders) {
            Class c = XposedHelpers.findClass(className,loader);
            if (c != null){
                return c;
            }
        }
        return null;
    }

    public Method getMethod(String methodName,Class... parameter) throws NoSuchMethodException {
        if (methods.containsKey(methodName)){
            return selfContext.getDeclaredMethod(methodName,parameter);
        }
        return null;
    }

    public Object invoke(String methodName,Object thz,Object... args) throws Exception{
        Object ret = null;

        if (methods.containsKey(methodName)){
            Util.log(Util.DBUEG,"clz size()"+methods.get(methodName));
            for (Method m : methods.get(methodName)){
               Class[] clz = m.getParameterTypes();
               if (clz.length != args.length){continue;}
               int flag = 0;
                for (int i = 0; i < clz.length; i++) {
                    if (!_checkType_(clz[i],args[i].getClass())){
                        break;
                    }
                    flag++;
                }
                if (flag == clz.length){
                    ret = m.invoke(thz,args);
                    if (thz != null){
                        Util.log(Util.METHOD,"INVOKED:["+thz.getClass().getName()+"@"+String.format("%x",thz.hashCode())+"]"+" [" +methodName+ "()]" + "-> " + ret);
                    }

                    return ret;
                }
            }
        }
        if (thz != null){
            Util.log(Util.METHOD,"FAIL:["+thz.getClass().getName()+"@"+thz.hashCode()+"]"+" [" +methodName+ "()]" + "-> NOT FOUND");
        }
        return ret;
    }

    public void printFiledsAndValue(Object thz){
        Set<String> keys = fields.keySet();
        for (String k : keys){
            Field m = fields.get(k);
            try {
                Util.print(Util.FIELD,m.getName(),m.get(thz), thz.getClass().getName()+"@"+String.format("%x",thz.hashCode()),0);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public Object getFieldValue(String fieldName,Object thz) throws IllegalAccessException {
        Object value = null;
        if (fields.containsKey(fieldName)){
            value = fields.get(fieldName).get(thz);
            Util.log(Util.GET_FIELD,"SUCC:["+thz.getClass().getName()+"@"+String.format("%x",thz.hashCode())+"]"+" [" +fieldName+ " ]" + "-> " + value);
            return value;
        }else{
            Util.log(Util.GET_FIELD,"FAIL:["+thz.getClass().getName()+"@"+thz.hashCode()+"]"+" [" +fieldName+ " ]" + "-> NOT FOUND");
        }
        return null;
    }

    public void setFiledValue(String fieldName,Object value,Object thz) throws IllegalAccessException {
        if (fields.containsKey(fieldName)){
            fields.get(fieldName).set(thz,value);
            Util.log(Util.SET_FIELD,"SUCC:["+thz.getClass().getName()+"@"+String.format("%x",thz.hashCode())+"]"+" [" +fieldName+ " ]" + "-> " + value);
        }else{
            Util.log(Util.SET_FIELD,"FAIL:["+thz.getClass().getName()+"@"+String.format("%x",thz.hashCode())+"]"+" [" +fieldName+ " ]" + "-> NOT FOUND");
        }
    }

    public Class getSelfContext(){
        return selfContext;
    }

    public void printAllParameter(String methodName,Object... args){
        for (int i = 0; i < args.length; i++) {
            Util.print("P"+(i+1),args[i],selfContext.getName()+"."+methodName,i+1);
        }
    }

    public abstract void doit();

    private boolean _checkType_(Class L, Class R){
        String Lname = L.getName();
        String Rname = R.getName();
        Util.log(Util.DBUEG,"Lname: "+ Lname + ", Rname: "+ Rname);
        if (Lname.equals(Rname)){return true;}
        if (Lname.equals("long") && Rname.equals("java.lang.Long")){return true;}
        return false;
    }

    private void _init_(){
        for (ClassLoader loader : loaders){
            selfContext = XposedHelpers.findClass(className,loader);
            if (selfContext == null){
                Util.log("FIND"," selfContext not found");
                continue;
            }
            for (Method m : selfContext.getDeclaredMethods()){
                m.setAccessible(true);
                if (methods.containsKey(m.getName())){
                    methods.get(m.getName()).add(m);
                }else{
                    List<Method> l = new ArrayList<>();
                    l.add(m);
                    methods.put(m.getName(),l);
                }
            }
            for (Field f : selfContext.getDeclaredFields()){
                f.setAccessible(true);
                fields.put(f.getName(),f);
            }
            for (Constructor c : selfContext.getDeclaredConstructors()){
                c.setAccessible(true);
                constructors.add(c);
            }
            Util.log("FIND", selfContext.getName()+" selfContext found ");
        }
        _watcher_();
    }

    private void _watcher_(){
        for (Constructor c:constructors) {
            Object[] args = new Object[c.getParameterTypes().length + 1];
            System.arraycopy(c.getParameterTypes(),0,args,0,c.getParameterTypes().length);
            args[args.length - 1] = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Util.log("OBJECT CREATE",selfContext.getName()+"@"+param.thisObject.hashCode());
                }
            };
            XposedHelpers.findAndHookConstructor(selfContext,args);
        }
    }

}
