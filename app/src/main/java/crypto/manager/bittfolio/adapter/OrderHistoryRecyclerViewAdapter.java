package crypto.manager.bittfolio.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.PortfolioFragment.OnPortfolioListFragmentInteractionListener;
import crypto.manager.bittfolio.model.OrderHistoryEntry;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnPortfolioListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class OrderHistoryRecyclerViewAdapter extends RecyclerView.Adapter<OrderHistoryRecyclerViewAdapter.ViewHolder> {

    private final List<OrderHistoryEntry> mOrderHistoryEntries;


    public OrderHistoryRecyclerViewAdapter(List<OrderHistoryEntry> coins) {
        mOrderHistoryEntries = coins;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_order_book_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mOrderHistoryEntries.get(position);
        holder.mTypeView.setText(mOrderHistoryEntries.get(position).getType());
        holder.mQuantityView.setText(mOrderHistoryEntries.get(position).getQuantity());
        holder.mQuantityRemainingView.setText(mOrderHistoryEntries.get(position).getQuantityRemaining());
        holder.mPriceView.setText(mOrderHistoryEntries.get(position).getPrice());
    }

    @Override
    public int getItemCount() {
        return mOrderHistoryEntries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTypeView;
        public final TextView mQuantityView;
        public final TextView mQuantityRemainingView;
        public final TextView mPriceView;
        public OrderHistoryEntry mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTypeView = (TextView) view.findViewById(R.id.text_view_type);
            mQuantityView = (TextView) view.findViewById(R.id.text_view_quantity);
            mQuantityRemainingView = (TextView) view.findViewById(R.id.text_view_quantity_remaining);
            mPriceView = (TextView) view.findViewById(R.id.text_view_price);
        }
    }
}
