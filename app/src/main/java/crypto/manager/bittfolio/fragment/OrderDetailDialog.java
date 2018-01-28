package crypto.manager.bittfolio.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.adapter.OrderDetailRecyclerViewAdapter;
import crypto.manager.bittfolio.model.OrderHistoryEntry;

/**
 * Created by ghodk on 1/27/2018.
 */

public class OrderDetailDialog extends Dialog implements View.OnClickListener {

    public Activity mActivityContext;
    public Dialog mDialog;
    private OrderHistoryEntry mOrderHistoryEntry;
    private RecyclerView mRecyclerView;
    private OrderDetailRecyclerViewAdapter mRecyclerViewAdapter;
    public OrderDetailDialog(Activity a, OrderHistoryEntry orderHistoryEntryAtPosition) {
        super(a);
        // TODO Auto-generated constructor stub
        this.mActivityContext = a;
        this.mOrderHistoryEntry = orderHistoryEntryAtPosition;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_order_detail_popup);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_order_details);
        // Set the adapter
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerViewAdapter = new OrderDetailRecyclerViewAdapter(mOrderHistoryEntry);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
