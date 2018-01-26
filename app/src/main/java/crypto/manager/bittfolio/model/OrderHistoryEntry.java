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
    private String mUuid;

    public OrderHistoryEntry(String market, String status, String type, String quantity, String quantityRemaining, String price, String uuid) {
        this.mMarket = market;
        this.mStatus = status;
        this.mType = type;
        this.mQuantity = quantity;
        this.mQuantityRemaining = quantityRemaining;
        this.mPrice = price;
        this.mUuid = uuid;
    }


    private OrderHistoryEntry(Parcel in) {
        mMarket = in.readString();
        mStatus = in.readString();
        mType = in.readString();
        mQuantity = in.readString();
        mQuantityRemaining = in.readString();
        mPrice = in.readString();
        mUuid = in.readString();
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
}
