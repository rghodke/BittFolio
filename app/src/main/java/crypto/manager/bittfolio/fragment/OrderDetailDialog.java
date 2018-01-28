package crypto.manager.bittfolio.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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


        // Set the adapter
        Context context = getContext();
        mRecyclerView = (RecyclerView) findViewById(R.id.list_order_details);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerViewAdapter = new OrderDetailRecyclerViewAdapter(mOrderHistoryEntry, context);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    public class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        public DividerItemDecoration(Context context) {
            mDivider = context.getResources().getDrawable(R.drawable.line_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }
}
