package it.unibo.wenv;

import it.unibo.interaction.IssObserver;
import org.json.JSONObject;

public class AnotherObserver implements IssObserver {
    @Override
    public void handleInfo(String info) {
        System.out.println("AnotherObserver | "+ info);
    }

    @Override
    public void handleInfo(JSONObject info) {
        handleInfo(info.toString());
    }
}
