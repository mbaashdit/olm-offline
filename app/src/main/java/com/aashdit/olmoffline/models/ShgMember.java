package com.aashdit.olmoffline.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class ShgMember implements Parcelable {
    public Long memberId;
    public String memberCode;
    public Long shgId;
    public String memberName;
    public String fatherHusbName;
    public Long socialCategory;
    public Long disabilityId;
    public Long religionId;
    public Long genderId;
    public Long leaderId;
    public Long bankBranchId;
    public Long pipCategoryId;
    public String memberDob;
    public Long disabilityType;
    public Long religion;
    public Long gender;
    public Long pipCategory;
    public Long leader;
    public String dateOfJoin;
    public String dateOfBirth;
    public String eduLevel;
    public String branchName;
    public boolean enrolledInPmjjy;
    public boolean enrolledInPmsby;
    public boolean enrolledInOther;
    public boolean enrolledInApy;
    public String aadharNo;
    public String mobileNo;
    public String accountNo;
    public String bankName;
    public boolean isAadharSeeded;

    public ShgMember() {
    }

    protected ShgMember(Parcel in) {
        if (in.readByte() == 0) {
            memberId = null;
        } else {
            memberId = in.readLong();
        } if (in.readByte() == 0) {
            memberCode = null;
        } else {
            memberCode = in.readString();
        }
        if (in.readByte() == 0) {
            shgId = null;
        } else {
            shgId = in.readLong();
        }
        memberName = in.readString();
        fatherHusbName = in.readString();
        if (in.readByte() == 0) {
            socialCategory = null;
        } else {
            socialCategory = in.readLong();
        }
        if (in.readByte() == 0) {
            disabilityId = null;
        } else {
            disabilityId = in.readLong();
        }
        if (in.readByte() == 0) {
            religionId = null;
        } else {
            religionId = in.readLong();
        }
        if (in.readByte() == 0) {
            genderId = null;
        } else {
            genderId = in.readLong();
        }
        if (in.readByte() == 0) {
            leaderId = null;
        } else {
            leaderId = in.readLong();
        }
        if (in.readByte() == 0) {
            bankBranchId = null;
        } else {
            bankBranchId = in.readLong();
        }
        if (in.readByte() == 0) {
            pipCategoryId = null;
        } else {
            pipCategoryId = in.readLong();
        }
        memberDob = in.readString();
        if (in.readByte() == 0) {
            disabilityType = null;
        } else {
            disabilityType = in.readLong();
        }
        if (in.readByte() == 0) {
            religion = null;
        } else {
            religion = in.readLong();
        }
        if (in.readByte() == 0) {
            gender = null;
        } else {
            gender = in.readLong();
        }
        if (in.readByte() == 0) {
            pipCategory = null;
        } else {
            pipCategory = in.readLong();
        }
        if (in.readByte() == 0) {
            leader = null;
        } else {
            leader = in.readLong();
        }
        dateOfJoin = in.readString();
        dateOfBirth = in.readString();
        eduLevel = in.readString();
        branchName = in.readString();
        enrolledInPmjjy = in.readByte() != 0;
        enrolledInPmsby = in.readByte() != 0;
        enrolledInOther = in.readByte() != 0;
        enrolledInApy = in.readByte() != 0;
        aadharNo = in.readString();
        mobileNo = in.readString();
        accountNo = in.readString();
        bankName = in.readString();
        isAadharSeeded = in.readByte() != 0;
    }

    public static final Creator<ShgMember> CREATOR = new Creator<ShgMember>() {
        @Override
        public ShgMember createFromParcel(Parcel in) {
            return new ShgMember(in);
        }

        @Override
        public ShgMember[] newArray(int size) {
            return new ShgMember[size];
        }
    };

    public static ShgMember parseShgMember(JSONObject object){
        ShgMember member = new ShgMember();
        member.memberName = object.optString("memberName");
        member.fatherHusbName = object.optString("fatherHusbName");
        member.memberDob = object.optString("memberDob2");
        member.dateOfJoin = object.optString("dateOfJoin2");
        member.dateOfBirth = object.optString("memberDob2");
        member.eduLevel = object.optString("eduLevel");
        member.bankName = object.optString("bankName");
        member.branchName = object.optString("branchName");
        member.enrolledInPmjjy = object.optBoolean("enrolledInPmjjy");
        member.enrolledInPmsby = object.optBoolean("enrolledInPmsby");
        member.enrolledInPmsby = object.optBoolean("enrolledInPmsby");
        member.enrolledInOther = object.optBoolean("enrolledInOther");
        member.enrolledInApy = object.optBoolean("enrolledInApy");
        member.aadharNo = object.optString("aadharNo");
        member.mobileNo = object.optString("mobileNo");
        member.accountNo = object.optString("accountNo");
        member.isAadharSeeded = object.optBoolean("isAadharSeeded");
        member.enrolledInPmjjy = object.optBoolean("enrolledInPmjjy");
        member.memberId = object.optLong("memberId");
        member.shgId = object.optLong("shgId");
        member.religionId = object.optLong("religionId");
        member.genderId = object.optLong("genderId");
        member.leaderId = object.optLong("leaderId");
        member.bankBranchId = object.optLong("bankBranchId");
        member.pipCategoryId = object.optLong("pipCategoryId");
        member.disabilityId = object.optLong("disabilityId");
        member.socialCategory = object.optLong("socialCategory");
        member.disabilityType = object.optLong("disabilityType");
        member.religion = object.optLong("religion");
        member.gender = object.optLong("gender");
        member.pipCategory = object.optLong("pipCategory");
        member.leader = object.optLong("leader");
        member.memberCode = object.optString("memberCode");

        String bankDetails = object.optString("bankBranch");
        try {
            JSONObject bankObj = new JSONObject(bankDetails);
            member.bankName = bankObj.optString("bankName");
            member.bankBranchId = bankObj.optLong("bankBranchId");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return member;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (memberId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(memberId);
        }
        if (memberCode == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(memberCode);
        }
        if (shgId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(shgId);
        }
        dest.writeString(memberName);
        dest.writeString(fatherHusbName);
        if (socialCategory == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(socialCategory);
        }
        if (disabilityId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(disabilityId);
        }
        if (religionId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(religionId);
        }
        if (genderId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(genderId);
        }
        if (leaderId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(leaderId);
        }
        if (bankBranchId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(bankBranchId);
        }
        if (pipCategoryId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(pipCategoryId);
        }
        dest.writeString(memberDob);
        if (disabilityType == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(disabilityType);
        }
        if (religion == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(religion);
        }
        if (gender == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(gender);
        }
        if (pipCategory == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(pipCategory);
        }
        if (leader == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(leader);
        }
        dest.writeString(dateOfJoin);
        dest.writeString(dateOfBirth);
        dest.writeString(eduLevel);
        dest.writeString(branchName);
        dest.writeByte((byte) (enrolledInPmjjy ? 1 : 0));
        dest.writeByte((byte) (enrolledInPmsby ? 1 : 0));
        dest.writeByte((byte) (enrolledInOther ? 1 : 0));
        dest.writeByte((byte) (enrolledInApy ? 1 : 0));
        dest.writeString(aadharNo);
        dest.writeString(mobileNo);
        dest.writeString(accountNo);
        dest.writeString(bankName);
        dest.writeByte((byte) (isAadharSeeded ? 1 : 0));
    }
}
