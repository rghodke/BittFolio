package crypto.manager.bittfolio.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * CoinData model to encapsulate all the different coin fields
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class OrderBookEntry implements Parcelable {

    public static final Creator CREATOR =
            new Creator() {
                public OrderBookEntry createFromParcel(Parcel in) {
                    return new OrderBookEntry(in);
                }

                public OrderBookEntry[] newArray(int size) {
                    return new OrderBookEntry[size];
                }
            };
    private String mQuantity;
    private String mPrice;

    public OrderBookEntry(String quantity, String price) {
        this.mQuantity = quantity;
        this.mPrice = price;
    }


    private OrderBookEntry(Parcel in) {
        mQuantity = in.readString();
        mPrice = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mQuantity);
        parcel.writeString(mPrice);
    }

    public String getQuantity() {
        return mQuantity;
    }

    public void setQuantity(String mQuantity) {
        this.mQuantity = mQuantity;
    }

    public String getPrice() {
        return mPrice;
    }
}
