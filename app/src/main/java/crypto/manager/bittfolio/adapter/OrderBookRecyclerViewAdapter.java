package crypto.manager.bittfolio.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.PortfolioFragment.OnPortfolioListFragmentInteractionListener;
import crypto.manager.bittfolio.model.OrderBookEntry;
import crypto.manager.bittfolio.model.OrderHistoryEntry;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnPortfolioListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class OrderBookRecyclerViewAdapter extends RecyclerView.Adapter<OrderBookRecyclerViewAdapter.ViewHolder> {

    private final List<OrderBookEntry> mOrderBookEntries;


    public OrderBookRecyclerViewAdapter(List<OrderBookEntry> coins) {
        mOrderBookEntries = coins;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_order_book_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mOrderBookEntries.get(position);
        holder.mQuantityView.setText(mOrderBookEntries.get(position).getQuantity());
        holder.mPriceView.setText(mOrderBookEntries.get(position).getPrice());
    }

    @Override
    public int getItemCount() {
        return mOrderBookEntries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mQuantityView;
        public final TextView mPriceView;
        public OrderBookEntry mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mQuantityView = (TextView) view.findViewById(R.id.text_view_quantity);
            mPriceView = (TextView) view.findViewById(R.id.text_view_price);
        }
    }
}
