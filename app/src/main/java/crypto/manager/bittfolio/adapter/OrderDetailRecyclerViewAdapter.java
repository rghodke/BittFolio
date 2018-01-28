package crypto.manager.bittfolio.adapter;

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

    public OrderDetailRecyclerViewAdapter(OrderHistoryEntry entry) {
        this.mOrderHistoryEntry = entry;
    }

    public void updateData(OrderHistoryEntry entry) {
        this.mOrderHistoryEntry = entry;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_order_detail_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mItem = mOrderHistoryEntry;
        holder.mOrderId.setText(mOrderHistoryEntry.getUuid());
        holder.mOrderType.setText(mOrderHistoryEntry.getType());
        holder.mQuantity.setText(mOrderHistoryEntry.getQuantity());
        holder.mQuantityRemaining.setText(mOrderHistoryEntry.getQuantityRemaining());
        holder.mLimit.setText(mOrderHistoryEntry.getLimit());
        holder.mCommissionPaid.setText(mOrderHistoryEntry.getCommissionPaid());
        holder.mConditionTarget.setText(mOrderHistoryEntry.getConditionTarget());
        holder.mCondition.setText(mOrderHistoryEntry.getCondition());
        holder.mIsConditional.setText(mOrderHistoryEntry.getIsConditional());
        holder.mImmediateOrCancel.setText(mOrderHistoryEntry.getImmediateOrCancel());
        holder.mDateClosed.setText(mOrderHistoryEntry.getCloseDate());
        holder.mDateOpened.setText(mOrderHistoryEntry.getOpenDate());
        holder.mPricePerUnit.setText(mOrderHistoryEntry.getPricePerUnit());
        holder.mPrice.setText(mOrderHistoryEntry.getPrice());
    }


    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        private final TextView mOrderId;
        private final TextView mOrderType;
        private final TextView mQuantity;
        private final TextView mQuantityRemaining;
        private final TextView mLimit;
        private final TextView mCommissionPaid;
        private final TextView mConditionTarget;
        private final TextView mCondition;
        private final TextView mIsConditional;
        private final TextView mImmediateOrCancel;
        private final TextView mDateClosed;
        private final TextView mDateOpened;
        private final TextView mPricePerUnit;
        private final TextView mPrice;
        public OrderHistoryEntry mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mOrderId = (TextView) view.findViewById(R.id.text_view_order_id);
            mOrderType = (TextView) view.findViewById(R.id.text_view_order_type);
            mQuantity = (TextView) view.findViewById(R.id.text_view_quantity);
            mQuantityRemaining = (TextView) view.findViewById(R.id.text_view_quantity_remaining);
            mLimit = (TextView) view.findViewById(R.id.text_view_limit);
            mCommissionPaid = (TextView) view.findViewById(R.id.text_view_commission_paid);
            mPrice = (TextView) view.findViewById(R.id.text_view_price);
            mPricePerUnit = (TextView) view.findViewById(R.id.text_view_price_per_unit);
            mDateOpened = (TextView) view.findViewById(R.id.text_view_date_opened);
            mDateClosed = (TextView) view.findViewById(R.id.text_view_date_closed);
            mImmediateOrCancel = (TextView) view.findViewById(R.id.text_view_immediate_or_cancel);
            mIsConditional = (TextView) view.findViewById(R.id.text_view_is_conditional);
            mCondition = (TextView) view.findViewById(R.id.text_view_condition);
            mConditionTarget = (TextView) view.findViewById(R.id.text_view_condition_target);
        }
    }
}
