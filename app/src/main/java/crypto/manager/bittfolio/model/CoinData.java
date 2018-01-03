package crypto.manager.bittfolio.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CoinData model to encapsulate all the different coin fields
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class CoinData {

    private String mCurrency;
    private String mBalance;

    public CoinData(String currency, String balance){
        this.mCurrency = currency;
        this.mBalance = balance;
    }

    public String getCurrency(){
        return mCurrency;
    }

    public String getBalance() {
        return mBalance;
    }
}
