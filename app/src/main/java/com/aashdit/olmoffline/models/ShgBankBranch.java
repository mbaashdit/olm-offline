package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class ShgBankBranch {
    public Long bankBranchId;
    public String bankName;
    public String ifsc;
    public String branchCode;
    public String branchName;
    public String address;
    public boolean isActive;

    public static ShgBankBranch parseShgBankBranch(JSONObject object){
        ShgBankBranch branch = new ShgBankBranch();
        branch.bankBranchId = object.optLong("bankBranchId");
        branch.bankName = object.optString("bankName");
        branch.ifsc = object.optString("ifsc");
        branch.branchCode = object.optString("branchCode");
        branch.branchName = object.optString("branchName");
        branch.address = object.optString("address");
        branch.isActive = object.optBoolean("isActive");
        return branch;
    }
}
