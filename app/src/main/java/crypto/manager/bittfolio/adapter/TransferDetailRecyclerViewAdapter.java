package crypto.manager.bittfolio.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.model.TransferHistoryEntry;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnPortfolioListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class TransferDetailRecyclerViewAdapter extends RecyclerView.Adapter<TransferDetailRecyclerViewAdapter.ViewHolder> {

    private TransferHistoryEntry mTransferHistoryEntry;
    private Context mContext;
    private String[] mCategories;

    public TransferDetailRecyclerViewAdapter(TransferHistoryEntry entry, Context context) {
        this.mTransferHistoryEntry = entry;
        this.mContext = context;
        mCategories = mContext.getResources().getStringArray(R.array.transfer_detail_array);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_transfer_detail_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mCategory.setText(mCategories[position]);
        if (position == 0) holder.mCategoryData.setText(mTransferHistoryEntry.getType());
        if (position == 1) holder.mCategoryData.setText(mTransferHistoryEntry.getUuid());
        if (position == 2) holder.mCategoryData.setText(mTransferHistoryEntry.getMarket());
        if (position == 3) holder.mCategoryData.setText(mTransferHistoryEntry.getQuantity());
        if (position == 4) holder.mCategoryData.setText(mTransferHistoryEntry.getAddress());
        if (position == 5) holder.mCategoryData.setText(mTransferHistoryEntry.getOpenDate());
        if (position == 6) {
            String auth = mTransferHistoryEntry.isAuth() ? "Authorized" : "Authorizing";
            holder.mCategoryData.setText(auth);
        }
        if (position == 7) {
            String pending = mTransferHistoryEntry.isPending() ? "Is Pending" : "Not Pending";
            holder.mCategoryData.setText(pending);
        }
        if (position == 8) holder.mCategoryData.setText(mTransferHistoryEntry.getTxCost());
        if (position == 9) holder.mCategoryData.setText(mTransferHistoryEntry.getTxId());
        if (position == 10) {
            String canceled = mTransferHistoryEntry.isCanceled() ? "Is Cancelled" : "Not cancelled";
            holder.mCategoryData.setText(canceled);
        }
        if (position == 11) {
            String isInvalidAddress = mTransferHistoryEntry.isInvalidAddress() ? "Address is Invalid" : "Not Invalid address";
            holder.mCategoryData.setText(isInvalidAddress);
        }
    }


    @Override
    public int getItemCount() {
        return 12;
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
