package crypto.manager.bittfolio.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * CoinData model to encapsulate all the different coin fields
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class OrderHistoryEntry implements Parcelable {

    public static final Creator CREATOR =
            new Creator() {
                public OrderHistoryEntry createFromParcel(Parcel in) {
                    return new OrderHistoryEntry(in);
                }

                public OrderHistoryEntry[] newArray(int size) {
                    return new OrderHistoryEntry[size];
                }
            };
    private String mMarket;
    private String mStatus;
    private String mType;
    private String mQuantity;
    private String mQuantityRemaining;
    private String mPrice;
    private String mPricePerUnit;
    private String mUuid;
    private String mLimit;
    private String mCommissionPaid;
    private String mOpenDate;
    private String mCloseDate;
    private String mImmediateOrCancel;
    private String mIsConditional;
    private String mCondition;
    private String mConditionTarget;

    public OrderHistoryEntry(String market, String status, String type, String quantity, String quantityRemaining, String price, String uuid, String pricePerUnit, String limit, String commissionPaid, String openDate, String closeDate, String immediateOrCancel, String isConditional, String condition, String conditionTarget) {
        this.mMarket = market;
        this.mStatus = status;
        this.mType = type;
        this.mQuantity = quantity;
        this.mQuantityRemaining = quantityRemaining;
        this.mPrice = price;
        this.mUuid = uuid;
        this.mPricePerUnit = pricePerUnit;
        this.mLimit = limit;
        this.mCommissionPaid = commissionPaid;
        this.mOpenDate = openDate;
        this.mCloseDate = closeDate;
        this.mImmediateOrCancel = immediateOrCancel;
        this.mIsConditional = isConditional;
        this.mCondition = condition;
        this.mConditionTarget = conditionTarget;
    }


    private OrderHistoryEntry(Parcel in) {
        mMarket = in.readString();
        mStatus = in.readString();
        mType = in.readString();
        mQuantity = in.readString();
        mQuantityRemaining = in.readString();
        mPrice = in.readString();
        mUuid = in.readString();
        mPricePerUnit = in.readString();
        mLimit = in.readString();
        mCommissionPaid = in.readString();
        mOpenDate = in.readString();
        mCloseDate = in.readString();
        mImmediateOrCancel = in.readString();
        mIsConditional = in.readString();
        mCondition = in.readString();
        mConditionTarget = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mMarket);
        parcel.writeString(mStatus);
        parcel.writeString(mType);
        parcel.writeString(mQuantity);
        parcel.writeString(mQuantityRemaining);
        parcel.writeString(mPrice);
        parcel.writeString(mUuid);
        parcel.writeString(mPricePerUnit);
        parcel.writeString(mLimit);
        parcel.writeString(mCommissionPaid);
        parcel.writeString(mOpenDate);
        parcel.writeString(mCloseDate);
        parcel.writeString(mImmediateOrCancel);
        parcel.writeString(mIsConditional);
        parcel.writeString(mCondition);
        parcel.writeString(mConditionTarget);
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String Uuid) {
        this.mUuid = Uuid;
    }


    public String getQuantity() {
        return mQuantity;
    }

    public void setQuantity(String mQuantity) {
        this.mQuantity = mQuantity;
    }

    public String getQuantityRemaining() {
        return mQuantityRemaining;
    }

    public void setQuantityRemaining(String mQuantityRemaining) {
        this.mQuantityRemaining = mQuantityRemaining;
    }

    public String getPrice() {
        return mPrice;
    }

    public String getMarket() {
        return mMarket;
    }

    public void setMarket(String market) {
        this.mMarket = market;
    }

    public String getLimit() {
        return mLimit;
    }

    public void setLimit(String limit) {
        this.mLimit = limit;
    }

    public String getCommissionPaid() {
        return mCommissionPaid;
    }

    public void setCommissionPaid(String commisionPaid) {
        this.mCommissionPaid = commisionPaid;
    }

    public String getPricePerUnit() {
        return mPricePerUnit;
    }

    public void setPricePerUnit(String mPricePerUnit) {
        this.mPricePerUnit = mPricePerUnit;
    }

    public String getOpenDate() {
        return mOpenDate;
    }

    public void setOpenDate(String openDate) {
        this.mOpenDate = openDate;
    }

    public String getCloseDate() {
        return mCloseDate;
    }

    public void setCloseDate(String closeDate) {
        this.mCloseDate = closeDate;
    }


    public String getImmediateOrCancel() {
        return mImmediateOrCancel;
    }

    public void setImmediateOrCancel(String immediateOrCancel) {
        mImmediateOrCancel = immediateOrCancel;
    }

    public String getIsConditional() {
        return mIsConditional;
    }

    public void setIsConditional(String isConditional) {
        mIsConditional = isConditional;
    }

    public String getCondition() {
        return mCondition;
    }

    public void setCondition(String condition) {
        mCondition = condition;
    }

    public String getConditionTarget() {
        return mConditionTarget;
    }

    public void setConditionTarget(String conditionTarget) {
        mConditionTarget = conditionTarget;
    }
}
