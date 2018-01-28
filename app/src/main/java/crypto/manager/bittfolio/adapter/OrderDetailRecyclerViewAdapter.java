package crypto.manager.bittfolio.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.model.OrderHistoryEntry;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnPortfolioListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class OrderDetailRecyclerViewAdapter extends RecyclerView.Adapter<OrderDetailRecyclerViewAdapter.ViewHolder> {

    private OrderHistoryEntry mOrderHistoryEntry;
    private Context mContext;
    private String[] mCategories;

    public OrderDetailRecyclerViewAdapter(OrderHistoryEntry entry, Context context) {
        this.mOrderHistoryEntry = entry;
        this.mContext = context;
        mCategories = mContext.getResources().getStringArray(R.array.order_detail_array);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_order_detail_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mCategory.setText(mCategories[position]);
        if (position == 0) holder.mCategoryData.setText(mOrderHistoryEntry.getUuid());
        if (position == 1) holder.mCategoryData.setText(mOrderHistoryEntry.getType());
        if (position == 2) holder.mCategoryData.setText(mOrderHistoryEntry.getQuantity());
        if (position == 3) holder.mCategoryData.setText(mOrderHistoryEntry.getQuantityRemaining());
        if (position == 4) holder.mCategoryData.setText(mOrderHistoryEntry.getLimit());
        if (position == 5) holder.mCategoryData.setText(mOrderHistoryEntry.getCommissionPaid());
        if (position == 6) holder.mCategoryData.setText(mOrderHistoryEntry.getPrice());
        if (position == 7) holder.mCategoryData.setText(mOrderHistoryEntry.getPricePerUnit());
        if (position == 8) holder.mCategoryData.setText(mOrderHistoryEntry.getOpenDate());
        if (position == 9) holder.mCategoryData.setText(mOrderHistoryEntry.getCloseDate());
        if (position == 10) holder.mCategoryData.setText(mOrderHistoryEntry.getImmediateOrCancel());
        if (position == 11) holder.mCategoryData.setText(mOrderHistoryEntry.getIsConditional());
        if (position == 12) holder.mCategoryData.setText(mOrderHistoryEntry.getCondition());
        if (position == 13) holder.mCategoryData.setText(mOrderHistoryEntry.getConditionTarget());
    }


    @Override
    public int getItemCount() {
        return 14;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        private final TextView mCategoryData;
        private final TextView mCategory;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCategory = (TextView) view.findViewById(R.id.label_category);
            mCategoryData = (TextView) view.findViewById(R.id.text_view_category);
        }
    }
}
