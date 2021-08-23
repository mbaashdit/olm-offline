package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class State {
    public Long stateId;
    public String stateNameEN;
    public String stateNameHI;
    public String stateName;
    public String stateCode;
    public boolean isActive;

    public static State parseState(JSONObject object) {
        State state = new State();
        state.isActive = object.optBoolean("isActive");
        state.stateCode = object.optString("stateCode");
        state.stateName = object.optString("stateName");
        state.stateNameHI = object.optString("stateNameHi");
        state.stateNameEN = object.optString("stateNameEn");
        state.stateId = object.optLong("stateId");

        return state;
    }
}
