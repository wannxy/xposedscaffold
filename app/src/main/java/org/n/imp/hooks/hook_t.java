package org.n.imp.hooks;

import org.n.xposedscaffold.Hook;

public class hook_t extends Hook {
    private static String className = "";
    private static Hook _t_ = null;

    private hook_t(String className) {
        super(className);
    }

    @Override
    public void doit() {

    }

    public static Hook getInstance(){
        if (_t_ == null){
            _t_ = new hook_t(className);
        }
        return _t_;
    }
}
