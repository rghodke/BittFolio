package crypto.manager.bittfolio.model;

/**
 * Created by ghodk on 1/21/2018.
 */

public class PriceData {

    private double mLast, mPrevDay;

    public PriceData(double last, double prevDay) {
        this.mLast = last;
        this.mPrevDay = prevDay;
    }

    public double getPrevDay() {
        return mPrevDay;
    }

    public void setPrevDay(double mPrevDay) {
        this.mPrevDay = mPrevDay;
    }

    public double getLast() {
        return mLast;
    }

    public void setLast(double mLast) {
        this.mLast = mLast;
    }
}
