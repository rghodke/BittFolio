package crypto.manager.bittfolio.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.PortfolioFragment.OnPortfolioListFragmentInteractionListener;
import crypto.manager.bittfolio.model.CoinData;

import java.text.DecimalFormat;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnPortfolioListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CoinRecyclerViewAdapter extends RecyclerView.Adapter<CoinRecyclerViewAdapter.ViewHolder> {

    private final List<CoinData> mCoins;
    private final OnPortfolioListFragmentInteractionListener mListener;


    public CoinRecyclerViewAdapter(List<CoinData> coins, OnPortfolioListFragmentInteractionListener listener) {
        mCoins = coins;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_coin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mCoins.get(position);
        holder.mTicketView.setText(mCoins.get(position).getCurrency());
        holder.mHoldingView.setText(new DecimalFormat("#.####").format(mCoins.get(position).getHolding()));
        holder.mPriceView.setText(new DecimalFormat("#.####").format(mCoins.get(position).getPrice()));
        holder.mBalanceView.setText(new DecimalFormat("#.####").format(mCoins.get(position).getBalance()));

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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTicketView;
        public final TextView mHoldingView;
        public final TextView mPriceView;
        public final TextView mBalanceView;
        public CoinData mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTicketView = (TextView) view.findViewById(R.id.text_view_ticker);
            mHoldingView = (TextView) view.findViewById(R.id.text_view_holding);
            mPriceView = (TextView) view.findViewById(R.id.text_view_price);
            mBalanceView = (TextView) view.findViewById(R.id.text_view_balance);
        }
    }
}
