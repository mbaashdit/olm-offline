package com.aashdit.olmoffline.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class SHG implements Parcelable {
    public static final Creator<SHG> CREATOR = new Creator<SHG>() {
        @Override
        public SHG createFromParcel(Parcel in) {
            return new SHG(in);
        }

        @Override
        public SHG[] newArray(int size) {
            return new SHG[size];
        }
    };
    private Long shgDetailsId;
    private Long shgId;
    private Long villageId;
    private Long shgTypeId;
    private Long gpId;
    private Long blockId;
    private Long districtId;
    private Long promotedById;
    private Long bankBranchId;
    private String shgName;
    private String shgTypeName;
    private String villageName;
    private String gpName;
    private String blockName;
    private String districtName;
    private String shgRegNumber;
    private String promotedByName;
    private String bankBranchName;
    private String bankAccountNumber;
    private String dateOfAccountOpening;
    private Long shgType;
    private String dateOfFormation;
    private String shgCode;
    private String dateOfRevival;
    private String meetingFrequencyName;
    private Long promotedBy;
    private Long meetingFrequency;
    private Long meetingFrequencyId;
    private Long trainedBookKeeper;
    private Long trainedBookKeeperId;
    private Double monthlySavingAmt;
    private boolean isBasicTrainingRecv;
    private boolean basicTrainingReceived;
    private boolean isActive;
    private String loanAccountNo;
    private String activeBankLoanAccountNumber;
    private String trainedBookKeeperName;
    private String nameOfBookKeeper;
    private Double capitalSubsidyAmt;
    private Double monthlySavingAmount;
    private Double amountOfCapitalSubsidy;
    private String bookKeeeperName;
    private String bankName;
    private String accountNo;
//    private ShgBankBranch shgBankBranch;
    private String accountOpeningDate;


    public SHG() {
    }

    protected SHG(Parcel in) {
        if (in.readByte() == 0) {
            shgDetailsId = null;
        } else {
            shgDetailsId = in.readLong();
        }
        if (in.readByte() == 0) {
            shgId = null;
        } else {
            shgId = in.readLong();
        }
        if (in.readByte() == 0) {
            villageId = null;
        } else {
            villageId = in.readLong();
        }
        if (in.readByte() == 0) {
            shgTypeId = null;
        } else {
            shgTypeId = in.readLong();
        }
        if (in.readByte() == 0) {
            gpId = null;
        } else {
            gpId = in.readLong();
        }
        if (in.readByte() == 0) {
            blockId = null;
        } else {
            blockId = in.readLong();
        }
        if (in.readByte() == 0) {
            districtId = null;
        } else {
            districtId = in.readLong();
        }
        if (in.readByte() == 0) {
            promotedById = null;
        } else {
            promotedById = in.readLong();
        }
        if (in.readByte() == 0) {
            bankBranchId = null;
        } else {
            bankBranchId = in.readLong();
        }
        shgName = in.readString();
        shgTypeName = in.readString();
        villageName = in.readString();
        gpName = in.readString();
        blockName = in.readString();
        districtName = in.readString();
        shgRegNumber = in.readString();
        promotedByName = in.readString();
        bankBranchName = in.readString();
        bankAccountNumber = in.readString();
        dateOfAccountOpening = in.readString();
        if (in.readByte() == 0) {
            shgType = null;
        } else {
            shgType = in.readLong();
        }
        dateOfFormation = in.readString();
        shgCode = in.readString();
        dateOfRevival = in.readString();
        meetingFrequencyName = in.readString();
        if (in.readByte() == 0) {
            promotedBy = null;
        } else {
            promotedBy = in.readLong();
        }
        if (in.readByte() == 0) {
            meetingFrequency = null;
        } else {
            meetingFrequency = in.readLong();
        }
        if (in.readByte() == 0) {
            meetingFrequencyId = null;
        } else {
            meetingFrequencyId = in.readLong();
        }
        if (in.readByte() == 0) {
            trainedBookKeeper = null;
        } else {
            trainedBookKeeper = in.readLong();
        }
        if (in.readByte() == 0) {
            trainedBookKeeperId = null;
        } else {
            trainedBookKeeperId = in.readLong();
        }
        if (in.readByte() == 0) {
            monthlySavingAmt = null;
        } else {
            monthlySavingAmt = in.readDouble();
        }
        isBasicTrainingRecv = in.readByte() != 0;
        basicTrainingReceived = in.readByte() != 0;
        isActive = in.readByte() != 0;
        loanAccountNo = in.readString();
        activeBankLoanAccountNumber = in.readString();
        trainedBookKeeperName = in.readString();
        nameOfBookKeeper = in.readString();
        if (in.readByte() == 0) {
            capitalSubsidyAmt = null;
        } else {
            capitalSubsidyAmt = in.readDouble();
        }
        if (in.readByte() == 0) {
            monthlySavingAmount = null;
        } else {
            monthlySavingAmount = in.readDouble();
        }
        if (in.readByte() == 0) {
            amountOfCapitalSubsidy = null;
        } else {
            amountOfCapitalSubsidy = in.readDouble();
        }
        bookKeeeperName = in.readString();
        bankName = in.readString();
        accountNo = in.readString();
        accountOpeningDate = in.readString();
    }

    public static SHG parseShg(JSONObject object) {


        SHG shg = new SHG();
        shg.shgDetailsId = object.optLong("shgDetailsId");
        shg.shgId = object.optLong("shgDetailsId");
        shg.villageId = object.optLong("villageId");
        String vilRes = object.optString("village");
        try {
            JSONObject vilObj = new JSONObject(vilRes);
            shg.villageId = vilObj.optLong("villageId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        shg.shgTypeId = object.optLong("shgTypeId");
        shg.gpId = object.optLong("gpId");
        shg.blockId = object.optLong("blockId");
        shg.districtId = object.optLong("districtId");
        shg.promotedById = object.optLong("promotedById");
        shg.bankBranchId = object.optLong("bankBranchId");
        shg.shgName = object.optString("shgName");
        shg.villageName = object.optString("villageName");
        shg.gpName = object.optString("gpName");
        shg.blockName = object.optString("blockName");
        shg.districtName = object.optString("districtName");
        shg.shgTypeName = object.optString("shgTypeName");
        shg.shgRegNumber = object.optString("shgRegNumber");
        shg.promotedByName = object.optString("promotedByName");
        shg.bankBranchName = object.optString("bankBranchName");
        shg.bankAccountNumber = object.optString("bankAccountNumber");
        shg.dateOfAccountOpening = object.optString("dateOfAccountOpening");
        shg.meetingFrequencyName = object.optString("meetingFrequencyName");
        shg.trainedBookKeeperName = object.optString("trainedBookKeeperName");
        shg.nameOfBookKeeper = object.optString("bookKeeeperName");
        shg.shgType = object.optLong("shgType");
        shg.dateOfFormation = object.optString("dateOfFormation2");
        shg.shgCode = object.optString("shgCode");
        shg.dateOfRevival = object.optString("dateOfRevival2");
        shg.promotedBy = object.optLong("promotedBy");
        shg.meetingFrequency = object.optLong("meetingFrequency");
        shg.meetingFrequencyId = object.optLong("meetingFrequencyId");
        shg.trainedBookKeeperId = object.optLong("trainedBookKeeper");
        shg.trainedBookKeeper = object.optLong("trainedBookKeeper");
        shg.monthlySavingAmt = object.optDouble("monthlySavingAmt");
        shg.monthlySavingAmount = object.optDouble("monthlySavingAmount");
        shg.amountOfCapitalSubsidy = object.optDouble("capitalSubsidyAmt");
        shg.basicTrainingReceived = object.optBoolean("isBasicTrainingRecv");
        shg.isActive = object.optBoolean("isActive");
        shg.loanAccountNo = object.optString("loanAccountNo");
        shg.activeBankLoanAccountNumber = object.optString("loanAccountNo");
        shg.bankName = object.optString("bankName");
        shg.accountNo = object.optString("accountNo");
        shg.accountOpeningDate = object.optString("accountOpeningDate2");

        String bankStr = object.optString("shgBankBranch");
        try {
            JSONObject bankObj = new JSONObject(bankStr);
            shg.bankBranchId = bankObj.optLong("bankBranchId");

        } catch (JSONException e) {
            e.printStackTrace();
        }
//        shg.shgBankBranch = new ShgBankBranch();
//        shg.shgBankBranch = ShgBankBranch.parseShgBankBranch(Objects.requireNonNull(object.optJSONObject("shgBankBranch")));
        return shg;
    }

    public Long getGpId() {
        return gpId;
    }

    public void setGpId(Long gpId) {
        this.gpId = gpId;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public String getGpName() {
        return gpName;
    }

    public void setGpName(String gpName) {
        this.gpName = gpName;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public Long getShgId() {
        return shgId;
    }

    public void setShgId(Long shgId) {
        this.shgId = shgId;
    }

    public Long getVillageId() {
        return villageId;
    }

    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    public Long getShgTypeId() {
        return shgTypeId;
    }

    public void setShgTypeId(Long shgTypeId) {
        this.shgTypeId = shgTypeId;
    }

    public Long getPromotedById() {
        return promotedById;
    }

    public void setPromotedById(Long promotedById) {
        this.promotedById = promotedById;
    }

    public Long getBankBranchId() {
        return bankBranchId;
    }

    public void setBankBranchId(Long bankBranchId) {
        this.bankBranchId = bankBranchId;
    }

    public String getShgTypeName() {
        return shgTypeName;
    }

    public void setShgTypeName(String shgTypeName) {
        this.shgTypeName = shgTypeName;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public String getPromotedByName() {
        return promotedByName;
    }

    public void setPromotedByName(String promotedByName) {
        this.promotedByName = promotedByName;
    }

    public String getBankBranchName() {
        return bankBranchName;
    }

    public void setBankBranchName(String bankBranchName) {
        this.bankBranchName = bankBranchName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getDateOfAccountOpening() {
        return dateOfAccountOpening;
    }

    public void setDateOfAccountOpening(String dateOfAccountOpening) {
        this.dateOfAccountOpening = dateOfAccountOpening;
    }

    public String getDateOfFormation() {
        return dateOfFormation;
    }

    public void setDateOfFormation(String dateOfFormation) {
        this.dateOfFormation = dateOfFormation;
    }

    public String getShgCode() {
        return shgCode;
    }

    public void setShgCode(String shgCode) {
        this.shgCode = shgCode;
    }

    public String getDateOfRevival() {
        return dateOfRevival;
    }

    public void setDateOfRevival(String dateOfRevival) {
        this.dateOfRevival = dateOfRevival;
    }

    public String getMeetingFrequencyName() {
        return meetingFrequencyName;
    }

    public void setMeetingFrequencyName(String meetingFrequencyName) {
        this.meetingFrequencyName = meetingFrequencyName;
    }

    public Long getPromotedBy() {
        return promotedBy;
    }

    public void setPromotedBy(Long promotedBy) {
        this.promotedBy = promotedBy;
    }

    public Long getMeetingFrequency() {
        return meetingFrequency;
    }

    public void setMeetingFrequency(Long meetingFrequency) {
        this.meetingFrequency = meetingFrequency;
    }

    public Long getMeetingFrequencyId() {
        return meetingFrequencyId;
    }

    public void setMeetingFrequencyId(Long meetingFrequencyId) {
        this.meetingFrequencyId = meetingFrequencyId;
    }

    public Long getTrainedBookKeeper() {
        return trainedBookKeeper;
    }

    public void setTrainedBookKeeper(Long trainedBookKeeper) {
        this.trainedBookKeeper = trainedBookKeeper;
    }

    public Long getTrainedBookKeeperId() {
        return trainedBookKeeperId;
    }

    public void setTrainedBookKeeperId(Long trainedBookKeeperId) {
        this.trainedBookKeeperId = trainedBookKeeperId;
    }

    public Double getMonthlySavingAmt() {
        return monthlySavingAmt;
    }

    public void setMonthlySavingAmt(Double monthlySavingAmt) {
        this.monthlySavingAmt = monthlySavingAmt;
    }

    public boolean isBasicTrainingRecv() {
        return isBasicTrainingRecv;
    }

    public void setBasicTrainingRecv(boolean basicTrainingRecv) {
        isBasicTrainingRecv = basicTrainingRecv;
    }

    public boolean isBasicTrainingReceived() {
        return basicTrainingReceived;
    }

    public void setBasicTrainingReceived(boolean basicTrainingReceived) {
        this.basicTrainingReceived = basicTrainingReceived;
    }

    public String getLoanAccountNo() {
        return loanAccountNo;
    }

    public void setLoanAccountNo(String loanAccountNo) {
        this.loanAccountNo = loanAccountNo;
    }

    public String getActiveBankLoanAccountNumber() {
        return activeBankLoanAccountNumber;
    }

    public void setActiveBankLoanAccountNumber(String activeBankLoanAccountNumber) {
        this.activeBankLoanAccountNumber = activeBankLoanAccountNumber;
    }

    public String getTrainedBookKeeperName() {
        return trainedBookKeeperName;
    }

    public void setTrainedBookKeeperName(String trainedBookKeeperName) {
        this.trainedBookKeeperName = trainedBookKeeperName;
    }

    public String getNameOfBookKeeper() {
        return nameOfBookKeeper;
    }

    public void setNameOfBookKeeper(String nameOfBookKeeper) {
        this.nameOfBookKeeper = nameOfBookKeeper;
    }

    public Double getCapitalSubsidyAmt() {
        return capitalSubsidyAmt;
    }

    public void setCapitalSubsidyAmt(Double capitalSubsidyAmt) {
        this.capitalSubsidyAmt = capitalSubsidyAmt;
    }

    public Double getMonthlySavingAmount() {
        return monthlySavingAmount;
    }

    public void setMonthlySavingAmount(Double monthlySavingAmount) {
        this.monthlySavingAmount = monthlySavingAmount;
    }

    public Double getAmountOfCapitalSubsidy() {
        return amountOfCapitalSubsidy;
    }

    public void setAmountOfCapitalSubsidy(Double amountOfCapitalSubsidy) {
        this.amountOfCapitalSubsidy = amountOfCapitalSubsidy;
    }

    public String getBookKeeeperName() {
        return bookKeeeperName;
    }

    public void setBookKeeeperName(String bookKeeeperName) {
        this.bookKeeeperName = bookKeeeperName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getAccountOpeningDate() {
        return accountOpeningDate;
    }

    public void setAccountOpeningDate(String accountOpeningDate) {
        this.accountOpeningDate = accountOpeningDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Long getShgDetailsId() {
        return shgDetailsId;
    }

    public void setShgDetailsId(Long shgDetailsId) {
        this.shgDetailsId = shgDetailsId;
    }

    public String getShgName() {
        return shgName;
    }

    public void setShgName(String shgName) {
        this.shgName = shgName;
    }

    public String getShgRegNumber() {
        return shgRegNumber;
    }

    public void setShgRegNumber(String shgRegNumber) {
        this.shgRegNumber = shgRegNumber;
    }

    public Long getShgType() {
        return shgType;
    }

    public void setShgType(Long shgType) {
        this.shgType = shgType;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (shgDetailsId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(shgDetailsId);
        }
        if (shgId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(shgId);
        }
        if (villageId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(villageId);
        }
        if (shgTypeId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(shgTypeId);
        }
        if (gpId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(gpId);
        }
        if (blockId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(blockId);
        }
        if (districtId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(districtId);
        }
        if (promotedById == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(promotedById);
        }
        if (bankBranchId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(bankBranchId);
        }
        dest.writeString(shgName);
        dest.writeString(shgTypeName);
        dest.writeString(villageName);
        dest.writeString(gpName);
        dest.writeString(blockName);
        dest.writeString(districtName);
        dest.writeString(shgRegNumber);
        dest.writeString(promotedByName);
        dest.writeString(bankBranchName);
        dest.writeString(bankAccountNumber);
        dest.writeString(dateOfAccountOpening);
        if (shgType == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(shgType);
        }
        dest.writeString(dateOfFormation);
        dest.writeString(shgCode);
        dest.writeString(dateOfRevival);
        dest.writeString(meetingFrequencyName);
        if (promotedBy == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(promotedBy);
        }
        if (meetingFrequency == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(meetingFrequency);
        }
        if (meetingFrequencyId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(meetingFrequencyId);
        }
        if (trainedBookKeeper == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(trainedBookKeeper);
        }
        if (trainedBookKeeperId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(trainedBookKeeperId);
        }
        if (monthlySavingAmt == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(monthlySavingAmt);
        }
        dest.writeByte((byte) (isBasicTrainingRecv ? 1 : 0));
        dest.writeByte((byte) (basicTrainingReceived ? 1 : 0));
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeString(loanAccountNo);
        dest.writeString(activeBankLoanAccountNumber);
        dest.writeString(trainedBookKeeperName);
        dest.writeString(nameOfBookKeeper);
        if (capitalSubsidyAmt == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(capitalSubsidyAmt);
        }
        if (monthlySavingAmount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(monthlySavingAmount);
        }
        if (amountOfCapitalSubsidy == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(amountOfCapitalSubsidy);
        }
        dest.writeString(bookKeeeperName);
        dest.writeString(bankName);
        dest.writeString(accountNo);
        dest.writeString(accountOpeningDate);
    }

    @Override
    public String toString() {
        return shgName;
    }
}
