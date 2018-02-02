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
import crypto.manager.bittfolio.model.OrderHistoryEntry;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnPortfolioListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class OrderHistoryRecyclerViewAdapter extends RecyclerView.Adapter<OrderHistoryRecyclerViewAdapter.ViewHolder> {

    private List<OrderHistoryEntry> mOrderHistoryEntries;
    private List<OrderHistoryEntry> mClosedOrderHistoryEntries;
    private List<OrderHistoryEntry> mOpenOrderHistoryEntries;
    private int mPosition;

    public OrderHistoryRecyclerViewAdapter(List<OrderHistoryEntry> closedOrder, List<OrderHistoryEntry> openOrder) {
        mOrderHistoryEntries = new ArrayList<>();
        mClosedOrderHistoryEntries = closedOrder;
        mOpenOrderHistoryEntries = openOrder;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_order_history_entry, parent, false);
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
        String orderStatus = mOrderHistoryEntries.get(position).getStatus();
        if (orderStatus.equals("OPEN")) {
            holder.setOpen(true);
        } else {
            holder.setOpen(false);
        }

        holder.mItem = mOrderHistoryEntries.get(position);
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(position);
                return false;
            }
        });
        holder.mView.setLongClickable(true);
        holder.mStatusView.setText(orderStatus);
        holder.mTypeView.setText(mOrderHistoryEntries.get(position).getType());
        holder.mQuantityView.setText(mOrderHistoryEntries.get(position).getQuantity());
        holder.mQuantityRemainingView.setText(mOrderHistoryEntries.get(position).getQuantityRemaining());
        holder.mPriceView.setText(mOrderHistoryEntries.get(position).getPrice());
    }

    @Override
    public int getItemCount() {
        return mOrderHistoryEntries.size();
    }

    public void updateClosedOrderHistoryData(List<OrderHistoryEntry> freshData) {
        mClosedOrderHistoryEntries = freshData;
        mOrderHistoryEntries.clear();
        mOrderHistoryEntries.addAll(mOpenOrderHistoryEntries);
        mOrderHistoryEntries.addAll(mClosedOrderHistoryEntries);
        Collections.sort(mOrderHistoryEntries, new Comparator<OrderHistoryEntry>() {
            @Override
            public int compare(OrderHistoryEntry orderHistoryEntry, OrderHistoryEntry t1) {
                return t1.getOpenDate().compareTo(orderHistoryEntry.getOpenDate());
            }
        });
        notifyDataSetChanged();
    }

    public void updateOpenOrderHistoryData(List<OrderHistoryEntry> freshData) {
        mOpenOrderHistoryEntries = freshData;
        mOrderHistoryEntries.clear();
        mOrderHistoryEntries.addAll(mOpenOrderHistoryEntries);
        mOrderHistoryEntries.addAll(mClosedOrderHistoryEntries);
        Collections.sort(mOrderHistoryEntries, new Comparator<OrderHistoryEntry>() {
            @Override
            public int compare(OrderHistoryEntry orderHistoryEntry, OrderHistoryEntry t1) {
                return t1.getOpenDate().compareTo(orderHistoryEntry.getOpenDate());
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

    public OrderHistoryEntry getOrderHistoryEntryAtPosition(int position) {
        if (position != -1) return mOrderHistoryEntries.get(position);
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public final View mView;
        public final TextView mStatusView;
        public final TextView mTypeView;
        public final TextView mQuantityView;
        public final TextView mQuantityRemainingView;
        public final TextView mPriceView;
        public OrderHistoryEntry mItem;
        public boolean isOpen;

        public ViewHolder(View view) {
            super(view);
            view.setOnCreateContextMenuListener(this);
            mView = view;
            mStatusView = (TextView) view.findViewById(R.id.text_view_status);
            mTypeView = (TextView) view.findViewById(R.id.text_view_type);
            mQuantityView = (TextView) view.findViewById(R.id.text_view_quantity);
            mQuantityRemainingView = (TextView) view.findViewById(R.id.text_view_quantity_remaining);
            mPriceView = (TextView) view.findViewById(R.id.text_view_price);
            isOpen = false;
        }

        public void setOpen(boolean isOpen) {
            this.isOpen = isOpen;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {

            menu.setHeaderTitle(R.string.menu_header_select_order_action);
            if (isOpen) {
                menu.add(Menu.NONE, R.id.context_open_order_menu_close_order,
                        Menu.NONE, R.string.menu_label_close_order);
            }
            menu.add(Menu.NONE, R.id.context_open_order_menu_more_details,
                    Menu.NONE, R.string.menu_label_more_details);

        }
    }
}
