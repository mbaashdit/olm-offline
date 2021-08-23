package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class Block /*extends RealmObject*/ {
    public String blockCode;
    public Long blockId;
    public String blockName;

    public static Block parseBlock(JSONObject object){
        Block block = new Block();
        block.blockCode = object.optString("blockCode");
        block.blockId = object.optLong("blockId");
        block.blockName = object.optString("blockName");
        return block;
    }
}
