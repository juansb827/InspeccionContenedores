package com.juans.inspeccion.Other;

import java.util.Observable;

/**
 * Created by Juan on 10/05/2015.
 */
public class BroadcastObserver extends Observable {
    private void triggerObservers(boolean internet) {
        setChanged();
        notifyObservers(internet);
    }

    static BroadcastObserver observer;

    public synchronized static BroadcastObserver instance(){
        if(observer==null)observer=new BroadcastObserver();
        return observer;
    }




    public void change(boolean internet) {
        triggerObservers(internet);
    }

}
