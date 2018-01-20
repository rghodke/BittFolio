package crypto.manager.bittfolio.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.PortfolioFragment.OnPortfolioListFragmentInteractionListener;
import crypto.manager.bittfolio.model.CoinData;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnPortfolioListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CoinRecyclerViewAdapter extends RecyclerView.Adapter<CoinRecyclerViewAdapter.ViewHolder> {

    private final List<CoinData> mCoins;
    private final OnPortfolioListFragmentInteractionListener mListener;
    private boolean mIsDollars;
    private double mTotalBalance;
    private boolean mIsPercent;

    public CoinRecyclerViewAdapter(List<CoinData> coins, OnPortfolioListFragmentInteractionListener listener) {
        mCoins = coins;
        mListener = listener;
        mIsDollars = false;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_coin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //Append the appropriate currency symbol
        String currency = mIsDollars ? "$" : "₿";
        //Restrict number to appropriate decimal points
        String decimalFormat = mIsDollars ? "#.00" : "#.########";
        DecimalFormat df = new DecimalFormat(decimalFormat);
        //Always have btc and usdt in $
        if (mCoins.get(position).getCurrency().equals("BTC") || mCoins.get(position).getCurrency().equals("USDT")) {
            currency = "$";
        }
        holder.mItem = mCoins.get(position);
        Picasso.with(holder.mCurrencyIcon.getContext()).load(mCoins.get(position).getImageUrl()).into(holder.mCurrencyIcon);
        holder.mTickerView.setText(mCoins.get(position).getCurrency());

        holder.mHoldingView.setText(String.valueOf(mCoins.get(position).getHolding()));
        //Some coins have been delisted and have the price of 0
        if (Double.compare(mCoins.get(position).getPrice(), 0) != 0) {
            String price = currency + df.format(mCoins.get(position).getPrice());
            holder.mPriceView.setText(price);
        } else holder.mPriceView.setText("N/A");

        double balance = mCoins.get(position).getBalance();
        if (mCoins.get(position).getCurrency().equals("BTC")) {
            balance = mIsDollars ? mCoins.get(position).getBalance() : mCoins.get(position).getHolding();
        }
        String balanceStr = currency + df.format(mCoins.get(position).getBalance());
        if (mIsPercent) {
            if (!mCoins.get(position).getCurrency().equals("USDT")) {
                balance = balance / mTotalBalance;
                balance *= 100;
            }
        }
        if (!mCoins.get(position).getCurrency().equals("USDT")) {
            balanceStr = mIsPercent ? new DecimalFormat("#.##").format(balance) + "%" : currency + df.format(mCoins.get(position).getBalance());
        }
        holder.mBalanceView.setText(balanceStr);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onPortfolioListFragmentInteraction(holder.mItem);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mCoins.size();
    }

    public void setDollars(boolean dollars) {
        mIsDollars = dollars;
    }

    public void changeBalanceToPercent(boolean isPercent) {
        this.mIsPercent = isPercent;
    }

    public void setTotalBalance(double totalBalance) {
        this.mTotalBalance = totalBalance;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mCurrencyIcon;
        public final TextView mTickerView;
        public final TextView mHoldingView;
        public final TextView mPriceView;
        public final TextView mBalanceView;
        public CoinData mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCurrencyIcon = (ImageView) view.findViewById(R.id.image_view_currency_icon);
            mTickerView = (TextView) view.findViewById(R.id.text_view_ticker);
            mHoldingView = (TextView) view.findViewById(R.id.text_view_holding);
            mPriceView = (TextView) view.findViewById(R.id.text_view_price);
            mBalanceView = (TextView) view.findViewById(R.id.text_view_balance);
        }
    }
}
