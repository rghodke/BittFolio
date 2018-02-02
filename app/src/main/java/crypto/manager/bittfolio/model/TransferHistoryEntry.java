package crypto.manager.bittfolio.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * CoinData model to encapsulate all the different coin fields
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class TransferHistoryEntry implements Parcelable {

    public static final Creator CREATOR =
            new Creator() {
                public TransferHistoryEntry createFromParcel(Parcel in) {
                    return new TransferHistoryEntry(in);
                }

                public TransferHistoryEntry[] newArray(int size) {
                    return new TransferHistoryEntry[size];
                }
            };
    private String mType;
    private String mUuid;
    private String mMarket;
    private String mQuantity;
    private String mAddress;
    private String mOpenDate;
    private boolean mIsAuth;
    private boolean mIsPending;
    private String mTxCost;
    private String mTxId;
    private boolean mCanceled;
    private boolean mInvalidAddress;


    public TransferHistoryEntry(String market, String type, String quantity, String uuid, String openDate, String address, boolean isAuth, boolean isPending, String txCost, String txId, boolean canceled, boolean invalidAddress) {
        this.mMarket = market;
        this.mType = type;
        this.mQuantity = quantity;
        this.mUuid = uuid;
        this.mOpenDate = openDate;
        this.mAddress = address;
        this.mIsAuth = isAuth;
        this.mIsPending = isPending;
        this.mTxCost = txCost;
        this.mTxId = txId;
        this.mCanceled = canceled;
        this.mInvalidAddress = invalidAddress;
    }


    private TransferHistoryEntry(Parcel in) {
        mMarket = in.readString();
        mType = in.readString();
        mQuantity = in.readString();
        mUuid = in.readString();
        mOpenDate = in.readString();
        mAddress = in.readString();
        mIsAuth = in.readByte() != 0;
        mIsPending = in.readByte() != 0;
        mTxCost = in.readString();
        mTxId = in.readString();
        mCanceled = in.readByte() != 0;
        mInvalidAddress = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mMarket);
        parcel.writeString(mType);
        parcel.writeString(mQuantity);
        parcel.writeString(mUuid);
        parcel.writeString(mOpenDate);
        parcel.writeString(mAddress);
        parcel.writeByte((byte) (mIsAuth ? 1 : 0));
        parcel.writeByte((byte) (mIsPending ? 1 : 0));
        parcel.writeString(mTxCost);
        parcel.writeString(mTxId);
        parcel.writeByte((byte) (mCanceled ? 1 : 0));
        parcel.writeByte((byte) (mInvalidAddress ? 1 : 0));
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


    public String getMarket() {
        return mMarket;
    }

    public void setMarket(String market) {
        this.mMarket = market;
    }

    public String getOpenDate() {
        return mOpenDate;
    }

    public void setOpenDate(String openDate) {
        this.mOpenDate = openDate;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public boolean isAuth() {
        return mIsAuth;
    }

    public void setAuth(boolean auth) {
        mIsAuth = auth;
    }

    public boolean isPending() {
        return mIsPending;
    }

    public void setPending(boolean pending) {
        mIsPending = pending;
    }

    public String getTxCost() {
        return mTxCost;
    }

    public void setTxCost(String txCost) {
        this.mTxCost = txCost;
    }

    public String getTxId() {
        return mTxId;
    }

    public void setTxId(String txId) {
        this.mTxId = txId;
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public void setCanceled(boolean canceled) {
        this.mCanceled = canceled;
    }

    public boolean isInvalidAddress() {
        return mInvalidAddress;
    }

    public void setInvalidAddress(boolean invalidAddress) {
        this.mInvalidAddress = invalidAddress;
    }
}
