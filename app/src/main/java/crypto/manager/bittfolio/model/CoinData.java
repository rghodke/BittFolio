package crypto.manager.bittfolio.model;

/**
 * CoinData model to encapsulate all the different coin fields
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class CoinData {

    private String mCurrency;
    private double mHolding;
    private double mBalance;

    public CoinData(String currency, double holding){
        this.mCurrency = currency;
        this.mHolding = holding;
    }

    public CoinData(String currency, double holding, double balance){
        this.mCurrency = currency;
        this.mHolding = holding;
        this.mBalance = balance;
    }

    public String getCurrency(){
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
}
