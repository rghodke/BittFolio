package crypto.manager.bittfolio.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * CoinData model to encapsulate all the different coin fields
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class CoinData implements Parcelable {

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public CoinData createFromParcel(Parcel in) {
                    return new CoinData(in);
                }

                public CoinData[] newArray(int size) {
                    return new CoinData[size];
                }
            };
    private String mCurrency;
    private double mHolding;
    private double mPrice;
    private double mBalance;

    public CoinData(String currency, double holding) {
        this.mCurrency = currency;
        this.mHolding = holding;
    }

    public CoinData(String currency, double holding, double balance) {
        this.mCurrency = currency;
        this.mHolding = holding;
        this.mBalance = balance;
    }

    public CoinData(String currency, double holding, double balance, double price) {
        this.mCurrency = currency;
        this.mHolding = holding;
        this.mBalance = balance;
        this.mPrice = price;
    }

    private CoinData(Parcel in) {
        mCurrency = in.readString();
        mHolding = in.readDouble();
        mPrice = in.readDouble();
        mBalance = in.readDouble();
    }

    public String getCurrency() {
        return mCurrency;
    }

    public double getHolding() {
        return mHolding;
    }

    public double getBalance() {
        return mBalance;
    }

    public void setBalance(double balance) {
        this.mBalance = balance;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double mPrice) {
        this.mPrice = mPrice;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mCurrency);
        parcel.writeDouble(mHolding);
        parcel.writeDouble(mPrice);
        parcel.writeDouble(mBalance);
    }
}
