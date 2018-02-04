package crypto.manager.bittfolio.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.adapter.OverallOrderHistoryRecyclerViewAdapter;
import crypto.manager.bittfolio.adapter.OverallTransferHistoryRecyclerViewAdapter;
import crypto.manager.bittfolio.model.OrderHistoryEntry;
import crypto.manager.bittfolio.model.TransferHistoryEntry;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OverallOrderHistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OverallOrderHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OverallOrderHistoryFragment extends Fragment {
    private static final String ARG_OVERALL_ORDER_HISTORY_JSON_STRING = "OVERALL_ORDER_HISTORY_JSON";
    private OverallOrderHistoryRecyclerViewAdapter mOrderRecyclerViewAdapter;
    private OverallTransferHistoryRecyclerViewAdapter mTransferRecyclerViewAdapter;


    // TODO: Rename and change types of parameters
    private String mOrderHistoryJSON;
    private String mParam2;
    private List<OrderHistoryEntry> mOverallClosedOrderHistoryEntries;
    private RecyclerView mOverallOrderRecyclerView;
    private List<OrderHistoryEntry> mOverallOpenOrderHistoryEntries;
    private OnOverallOrderHistoryListFragmentInteractionListener mListener;
    private ArrayList<TransferHistoryEntry> mOverallWithdrawalHistoryEntries;
    private ArrayList<TransferHistoryEntry> mOverallDepositHistoryEntries;
    private RecyclerView mOverallTransferRecyclerView;
//    private OnOrderHistoryListFragmentInteractionListener mListener;

    public OverallOrderHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrderHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OverallOrderHistoryFragment newInstance() {
        OverallOrderHistoryFragment fragment = new OverallOrderHistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    //Workaround to persist dialog through rotation
    //https://stackoverflow.com/questions/7557265/prevent-dialog-dismissal-on-screen-rotation-in-android
    private static void doKeepDialog(Dialog dialog) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //No menu for this fragment
        setRetainInstance(true);
        if (getArguments() != null) {
        }
    }

    //No menu for this fragment
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overall_order_history_list, container, false);

        mOverallClosedOrderHistoryEntries = new ArrayList<>();
        mOverallOpenOrderHistoryEntries = new ArrayList<>();

        mOverallWithdrawalHistoryEntries = new ArrayList<>();
        mOverallDepositHistoryEntries = new ArrayList<>();

        mOverallOrderRecyclerView = (RecyclerView) view.findViewById(R.id.list_overall_order);
        mOverallTransferRecyclerView = (RecyclerView) view.findViewById(R.id.list_overall_transfer);

        Context context = view.getContext();

        // Set the order adapter
        mOverallOrderRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mOrderRecyclerViewAdapter = new OverallOrderHistoryRecyclerViewAdapter(mOverallClosedOrderHistoryEntries, mOverallOpenOrderHistoryEntries);
        mOverallOrderRecyclerView.setAdapter(mOrderRecyclerViewAdapter);
        registerForContextMenu(mOverallOrderRecyclerView);
        mListener.startOverallHistoryDataService();

        // Set the transfer adapter
        mOverallTransferRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mTransferRecyclerViewAdapter = new OverallTransferHistoryRecyclerViewAdapter(mOverallWithdrawalHistoryEntries, mOverallDepositHistoryEntries);
        mOverallTransferRecyclerView.setAdapter(mTransferRecyclerViewAdapter);


        return view;


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = -1;
        try {

        } catch (Exception e) {
            e.printStackTrace();
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.context_open_order_menu_close_order:
                position = mOrderRecyclerViewAdapter.getPosition();
                mListener.onOrderCancelled(mOrderRecyclerViewAdapter.getOrderHistoryEntryAtPosition(position));
                return true;
            case R.id.context_open_order_menu_more_details:
                position = mOrderRecyclerViewAdapter.getPosition();
                OrderDetailDialog orderDetailDialog = new OrderDetailDialog(getActivity(), mOrderRecyclerViewAdapter.getOrderHistoryEntryAtPosition(position));
                orderDetailDialog.show();
                doKeepDialog(orderDetailDialog);
                return true;
            case R.id.context_open_transfer_menu_close_order:
                position = mTransferRecyclerViewAdapter.getPosition();
                return true;
            case R.id.context_open_transfer_menu_more_details:
                position = mTransferRecyclerViewAdapter.getPosition();
                TransferDetailDialog transferDetailDialog = new TransferDetailDialog(getActivity(), mTransferRecyclerViewAdapter.getTransferHistoryEntryAtPosition(position));
                transferDetailDialog.show();
                doKeepDialog(transferDetailDialog);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOverallOrderHistoryListFragmentInteractionListener) {
            mListener = (OnOverallOrderHistoryListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOverallOrderHistoryListFragmentInteractionListener");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void updateClosedOrderHistory(String stringExtra) {
        if (stringExtra == null || stringExtra.isEmpty()) {
            return;
        }
        mOverallClosedOrderHistoryEntries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(stringExtra);
            JSONArray orderHistoryJSON = jsonObject.getJSONArray("result");
            for (int i = 0; i < orderHistoryJSON.length(); i++) {
                JSONObject orderHistoryEntry = orderHistoryJSON.getJSONObject(i);
                String orderType = (orderHistoryEntry.getString("OrderType"));
                if (orderType.equals("LIMIT_SELL")) orderType = "Sell";
                if (orderType.equals("LIMIT_BUY")) orderType = "Buy";
                String exchange = orderHistoryEntry.getString("Exchange");
                mOverallClosedOrderHistoryEntries.add(new OrderHistoryEntry(exchange, "CLOSED", orderType, orderHistoryEntry.getString("Quantity"), orderHistoryEntry.getString("QuantityRemaining"), orderHistoryEntry.getString("Price"), orderHistoryEntry.getString("OrderUuid"), orderHistoryEntry.getString("PricePerUnit"), orderHistoryEntry.getString("Limit"), orderHistoryEntry.getString("Commission"), orderHistoryEntry.getString("TimeStamp"), orderHistoryEntry.getString("Closed"), orderHistoryEntry.getString("ImmediateOrCancel"), orderHistoryEntry.getString("IsConditional"), orderHistoryEntry.getString("Condition"), orderHistoryEntry.getString("ConditionTarget")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshOrderHistoryData();
    }

    public void updateOpenOrderHistory(String stringExtra) {
        if (stringExtra == null || stringExtra.isEmpty()) {
            return;
        }
        mOverallOpenOrderHistoryEntries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(stringExtra);
            JSONArray orderHistoryJSON = jsonObject.getJSONArray("result");
            for (int i = 0; i < orderHistoryJSON.length(); i++) {
                JSONObject orderHistoryEntry = orderHistoryJSON.getJSONObject(i);
                String orderType = (orderHistoryEntry.getString("OrderType"));
                if (orderType.equals("LIMIT_SELL")) orderType = "Sell";
                if (orderType.equals("LIMIT_BUY")) orderType = "Buy";
                String exchange = orderHistoryEntry.getString("Exchange");
                mOverallOpenOrderHistoryEntries.add(new OrderHistoryEntry(exchange, "OPEN", orderType, orderHistoryEntry.getString("Quantity"), orderHistoryEntry.getString("QuantityRemaining"), orderHistoryEntry.getString("Price"), orderHistoryEntry.getString("OrderUuid"), orderHistoryEntry.getString("PricePerUnit"), orderHistoryEntry.getString("Limit"), orderHistoryEntry.getString("CommissionPaid"), orderHistoryEntry.getString("Opened"), orderHistoryEntry.getString("Closed"), orderHistoryEntry.getString("ImmediateOrCancel"), orderHistoryEntry.getString("IsConditional"), orderHistoryEntry.getString("Condition"), orderHistoryEntry.getString("ConditionTarget")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshOrderHistoryData();
    }


    public void updateWithdrawTransferHistory(String stringExtra) {
        if (stringExtra == null || stringExtra.isEmpty()) {
            return;
        }
        mOverallWithdrawalHistoryEntries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(stringExtra);
            JSONArray withdrawTransferHistoryJSON = jsonObject.getJSONArray("result");
            for (int i = 0; i < withdrawTransferHistoryJSON.length(); i++) {
                JSONObject withdrawTransferHistoryEntry = withdrawTransferHistoryJSON.getJSONObject(i);
                mOverallWithdrawalHistoryEntries.add(new TransferHistoryEntry(withdrawTransferHistoryEntry.getString("Currency"), "WITHDRAWAL", withdrawTransferHistoryEntry.getString("Amount"), withdrawTransferHistoryEntry.getString("PaymentUuid"), withdrawTransferHistoryEntry.getString("Opened"), withdrawTransferHistoryEntry.getString("Address"), withdrawTransferHistoryEntry.getBoolean("Authorized"), withdrawTransferHistoryEntry.getBoolean("PendingPayment"), withdrawTransferHistoryEntry.getString("TxCost"), withdrawTransferHistoryEntry.getString("TxId"), withdrawTransferHistoryEntry.getBoolean("Canceled"), withdrawTransferHistoryEntry.getBoolean("InvalidAddress")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshTransferHistoryData();
    }

    public void updateDepositTransferHistory(String stringExtra) {
        if (stringExtra == null || stringExtra.isEmpty()) {
            return;
        }
        mOverallDepositHistoryEntries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(stringExtra);
            JSONArray depositTransferHistoryJSON = jsonObject.getJSONArray("result");
            for (int i = 0; i < depositTransferHistoryJSON.length(); i++) {
                JSONObject depositTransferHistoryEntry = depositTransferHistoryJSON.getJSONObject(i);
                if (!depositTransferHistoryEntry.has("PaymentUuid")) continue;
                mOverallDepositHistoryEntries.add(new TransferHistoryEntry(depositTransferHistoryEntry.getString("Currency"), "WITHDRAWAL", depositTransferHistoryEntry.getString("Amount"), depositTransferHistoryEntry.getString("PaymentUuid"), depositTransferHistoryEntry.getString("Opened"), depositTransferHistoryEntry.getString("Address"), depositTransferHistoryEntry.getBoolean("Authorized"), depositTransferHistoryEntry.getBoolean("PendingPayment"), depositTransferHistoryEntry.getString("TxCost"), depositTransferHistoryEntry.getString("TxId"), depositTransferHistoryEntry.getBoolean("Canceled"), depositTransferHistoryEntry.getBoolean("InvalidAddress")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshTransferHistoryData();
    }

    private void refreshOrderHistoryData() {
        mOrderRecyclerViewAdapter.updateClosedOrderHistoryData(mOverallClosedOrderHistoryEntries);
        mOrderRecyclerViewAdapter.updateOpenOrderHistoryData(mOverallOpenOrderHistoryEntries);
    }

    private void refreshTransferHistoryData() {
        mTransferRecyclerViewAdapter.updateWithdrawTransferHistoryData(mOverallWithdrawalHistoryEntries);
        mTransferRecyclerViewAdapter.updateDepositTransferHistoryData(mOverallDepositHistoryEntries);
    }

    public interface OnOverallOrderHistoryListFragmentInteractionListener {
        void startOverallHistoryDataService();

        void onOrderCancelled(OrderHistoryEntry item);
    }
}
