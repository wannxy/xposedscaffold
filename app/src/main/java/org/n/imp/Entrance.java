package org.n.imp;

import org.n.xposedscaffold.Bus;

public class Entrance extends Bus {

    public Entrance(){
        this("com.tencent.gamehelper.smoba");
    }

    public Entrance(String packageName) {
        super(packageName);
    }

    @Override
    public void hooks() {

    }
}
