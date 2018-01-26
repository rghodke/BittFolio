package crypto.manager.bittfolio.fragment;

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
import android.widget.AdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.adapter.OrderHistoryRecyclerViewAdapter;
import crypto.manager.bittfolio.model.OrderHistoryEntry;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OrderHistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OrderHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderHistoryFragment extends Fragment {
    private static final String ARG_ORDER_HISTORY_JSON_STRING = "ORDER_HISTORY_JSON";
    private OrderHistoryRecyclerViewAdapter mRecyclerViewAdapter;


    // TODO: Rename and change types of parameters
    private String mOrderHistoryJSON;
    private String mParam2;
    private List<OrderHistoryEntry> mClosedOrderHistoryEntries;
    private RecyclerView mRecyclerView;
    private List<OrderHistoryEntry> mOpenOrderHistoryEntries;
    private OnOrderHistoryListFragmentInteractionListener mListener;
//    private OnOrderHistoryListFragmentInteractionListener mListener;

    public OrderHistoryFragment() {
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
    public static OrderHistoryFragment newInstance() {
        OrderHistoryFragment fragment = new OrderHistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
        View view = inflater.inflate(R.layout.fragment_order_history_list, container, false);

        mClosedOrderHistoryEntries = new ArrayList<>();
        mOpenOrderHistoryEntries = new ArrayList<>();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);

        // Set the adapter
        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerViewAdapter = new OrderHistoryRecyclerViewAdapter(mClosedOrderHistoryEntries, mOpenOrderHistoryEntries);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        registerForContextMenu(mRecyclerView);
        return view;

    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = -1;
        try {
            position = mRecyclerViewAdapter.getPosition();
        } catch (Exception e) {
            e.printStackTrace();
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.context_open_order_menu_close_order:
                mListener.onOrderCancelled(mRecyclerViewAdapter.getOrderHistoryEntryAtPosition(position));
                return true;
            case R.id.context_open_order_menu_more_details:
                System.out.println("MORE DETAILS @ " + position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOrderHistoryListFragmentInteractionListener) {
            mListener = (OnOrderHistoryListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPortfolioListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void updateClosedOrderHistory(String stringExtra) {
        mClosedOrderHistoryEntries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(stringExtra);
            JSONArray orderHistoryJSON = jsonObject.getJSONArray("result");
            for (int i = 0; i < orderHistoryJSON.length(); i++) {
                JSONObject orderHistoryEntry = orderHistoryJSON.getJSONObject(i);
                String orderType = (orderHistoryEntry.getString("OrderType"));
                if (orderType.equals("LIMIT_SELL")) orderType = "Sell";
                if (orderType.equals("LIMIT_BUY")) orderType = "Buy";
                mClosedOrderHistoryEntries.add(new OrderHistoryEntry("CLOSED", orderType, orderHistoryEntry.getString("Quantity"), orderHistoryEntry.getString("QuantityRemaining"), orderHistoryEntry.getString("Price"), orderHistoryEntry.getString("OrderUuid")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshOrderHistoryData();
    }

    public void updateOpenOrderHistory(String stringExtra) {
        mOpenOrderHistoryEntries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(stringExtra);
            JSONArray orderHistoryJSON = jsonObject.getJSONArray("result");
            for (int i = 0; i < orderHistoryJSON.length(); i++) {
                JSONObject orderHistoryEntry = orderHistoryJSON.getJSONObject(i);
                String orderType = (orderHistoryEntry.getString("OrderType"));
                if (orderType.equals("LIMIT_SELL")) orderType = "Sell";
                if (orderType.equals("LIMIT_BUY")) orderType = "Buy";
                mOpenOrderHistoryEntries.add(new OrderHistoryEntry("OPEN", orderType, orderHistoryEntry.getString("Quantity"), orderHistoryEntry.getString("QuantityRemaining"), orderHistoryEntry.getString("Price"), orderHistoryEntry.getString("OrderUuid")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshOrderHistoryData();
    }


    private void refreshOrderHistoryData() {
        mRecyclerViewAdapter.updateClosedOrderHistoryData(mClosedOrderHistoryEntries);
        mRecyclerViewAdapter.updateOpenOrderHistoryData(mOpenOrderHistoryEntries);
    }

    public interface OnOrderHistoryListFragmentInteractionListener {
        void onOrderCancelled(OrderHistoryEntry item);
    }
}
