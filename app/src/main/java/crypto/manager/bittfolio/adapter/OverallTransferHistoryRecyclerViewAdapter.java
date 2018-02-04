package crypto.manager.bittfolio.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.PortfolioFragment.OnPortfolioListFragmentInteractionListener;
import crypto.manager.bittfolio.model.TransferHistoryEntry;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnPortfolioListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class OverallTransferHistoryRecyclerViewAdapter extends RecyclerView.Adapter<OverallTransferHistoryRecyclerViewAdapter.ViewHolder> {

    private final List<TransferHistoryEntry> mTransferHistoryEntries;
    private List<TransferHistoryEntry> mWithdrawTransferHistoryEntries;
    private List<TransferHistoryEntry> mDepositTransferHistoryEntries;
    private int mPosition;

    public OverallTransferHistoryRecyclerViewAdapter(List<TransferHistoryEntry> withdraws, List<TransferHistoryEntry> deposits) {
        mTransferHistoryEntries = new ArrayList<>();
        mWithdrawTransferHistoryEntries = withdraws;
        mDepositTransferHistoryEntries = deposits;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_overall_transfer_history_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.mView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //Check is order is still open or closed
        boolean isAuth = mTransferHistoryEntries.get(position).isAuth();
        if (isAuth) {
            holder.setAuth(true);
        } else {
            holder.setAuth(false);
        }

        holder.mItem = mTransferHistoryEntries.get(position);
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(position);
                return false;
            }
        });
        holder.mView.setLongClickable(true);
        holder.mCurrencyView.setText(mTransferHistoryEntries.get(position).getMarket());
        String status = mTransferHistoryEntries.get(position).isAuth() ? "Authorized" : "Authorizing";
        holder.mStatusView.setText(status);
        holder.mTypeView.setText(mTransferHistoryEntries.get(position).getType());
        holder.mQuantityView.setText(mTransferHistoryEntries.get(position).getQuantity());
    }

    @Override
    public int getItemCount() {
        return mTransferHistoryEntries.size();
    }

    public void updateWithdrawTransferHistoryData(List<TransferHistoryEntry> freshData) {
        mWithdrawTransferHistoryEntries = freshData;
        mTransferHistoryEntries.clear();
        mTransferHistoryEntries.addAll(mDepositTransferHistoryEntries);
        mTransferHistoryEntries.addAll(mWithdrawTransferHistoryEntries);
        Collections.sort(mTransferHistoryEntries, new Comparator<TransferHistoryEntry>() {
            @Override
            public int compare(TransferHistoryEntry TransferHistoryEntry, TransferHistoryEntry t1) {
                return t1.getOpenDate().compareTo(TransferHistoryEntry.getOpenDate());
            }
        });
        notifyDataSetChanged();
    }

    public void updateDepositTransferHistoryData(List<TransferHistoryEntry> freshData) {
        mDepositTransferHistoryEntries = freshData;
        mTransferHistoryEntries.clear();
        mTransferHistoryEntries.addAll(mDepositTransferHistoryEntries);
        mTransferHistoryEntries.addAll(mWithdrawTransferHistoryEntries);
        Collections.sort(mTransferHistoryEntries, new Comparator<TransferHistoryEntry>() {
            @Override
            public int compare(TransferHistoryEntry TransferHistoryEntry, TransferHistoryEntry t1) {
                return t1.getOpenDate().compareTo(TransferHistoryEntry.getOpenDate());
            }
        });
        notifyDataSetChanged();
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public TransferHistoryEntry getTransferHistoryEntryAtPosition(int position) {
        if (position != -1) {
            return mTransferHistoryEntries.get(position);
        }
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public final View mView;
        public final TextView mCurrencyView;
        public final TextView mStatusView;
        public final TextView mTypeView;
        public final TextView mQuantityView;
        public TransferHistoryEntry mItem;
        public boolean isAuth;

        public ViewHolder(View view) {
            super(view);
            view.setOnCreateContextMenuListener(this);
            mView = view;
            mCurrencyView = (TextView) view.findViewById(R.id.text_view_currency);
            mStatusView = (TextView) view.findViewById(R.id.text_view_status);
            mTypeView = (TextView) view.findViewById(R.id.text_view_type);
            mQuantityView = (TextView) view.findViewById(R.id.text_view_quantity);
            isAuth = false;
        }

        public void setAuth(boolean isAuth) {
            this.isAuth = isAuth;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {

            menu.setHeaderTitle(R.string.menu_header_select_transfer_action);
            if (!isAuth) {
                menu.add(Menu.NONE, R.id.context_open_transfer_menu_close_order,
                        Menu.NONE, R.string.menu_label_cancel_transfer);
            }
            menu.add(Menu.NONE, R.id.context_open_transfer_menu_more_details,
                    Menu.NONE, R.string.menu_label_more_details);

        }
    }
}
