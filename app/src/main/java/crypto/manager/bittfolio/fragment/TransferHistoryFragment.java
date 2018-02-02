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
import crypto.manager.bittfolio.adapter.TransferHistoryRecyclerViewAdapter;
import crypto.manager.bittfolio.model.TransferHistoryEntry;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TransferHistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TransferHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransferHistoryFragment extends Fragment {
    private static final String ARG_TRANSFER_HISTORY_JSON_STRING = "ARG_TRANSFER_HISTORY_JSON_STRING";
    private TransferHistoryRecyclerViewAdapter mRecyclerViewAdapter;


    // TODO: Rename and change types of parameters
    private List<TransferHistoryEntry> mDepositHistoryEntries;
    private RecyclerView mRecyclerView;
    private List<TransferHistoryEntry> mWithdrawHistoryEntries;
    private OnTransferHistoryListFragmentInteractionListener mListener;


    public TransferHistoryFragment() {
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
    public static TransferHistoryFragment newInstance() {
        TransferHistoryFragment fragment = new TransferHistoryFragment();
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
        View view = inflater.inflate(R.layout.fragment_transfer_history_list, container, false);

        mDepositHistoryEntries = new ArrayList<>();
        mWithdrawHistoryEntries = new ArrayList<>();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);

        // Set the adapter
        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerViewAdapter = new TransferHistoryRecyclerViewAdapter(mDepositHistoryEntries, mWithdrawHistoryEntries);
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
                return true;
            case R.id.context_open_order_menu_more_details:
                TransferDetailDialog orderDetailDialog = new TransferDetailDialog(getActivity(), mRecyclerViewAdapter.getTransferHistoryEntryAtPosition(position));
                orderDetailDialog.show();
                doKeepDialog(orderDetailDialog);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTransferHistoryListFragmentInteractionListener) {
            mListener = (OnTransferHistoryListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPortfolioListFragmentInteractionListener");
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


    public void updateWithdrawTransferHistory(String stringExtra) {
        mWithdrawHistoryEntries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(stringExtra);
            JSONArray withdrawTransferHistoryJSON = jsonObject.getJSONArray("result");
            for (int i = 0; i < withdrawTransferHistoryJSON.length(); i++) {
                JSONObject withdrawTransferHistoryEntry = withdrawTransferHistoryJSON.getJSONObject(i);
                mWithdrawHistoryEntries.add(new TransferHistoryEntry(withdrawTransferHistoryEntry.getString("Currency"), "WITHDRAWAL", withdrawTransferHistoryEntry.getString("Amount"), withdrawTransferHistoryEntry.getString("PaymentUuid"), withdrawTransferHistoryEntry.getString("Opened"), withdrawTransferHistoryEntry.getString("Address"), withdrawTransferHistoryEntry.getBoolean("Authorized"), withdrawTransferHistoryEntry.getBoolean("PendingPayment"), withdrawTransferHistoryEntry.getString("TxCost"), withdrawTransferHistoryEntry.getString("TxId"), withdrawTransferHistoryEntry.getBoolean("Canceled"), withdrawTransferHistoryEntry.getBoolean("InvalidAddress")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshTransferHistoryData();
    }

    public void updateDepositOrderHistory(String stringExtra) {
        mDepositHistoryEntries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(stringExtra);
            JSONArray depositTransferHistoryJSON = jsonObject.getJSONArray("result");
            for (int i = 0; i < depositTransferHistoryJSON.length(); i++) {
                JSONObject depositTransferHistoryEntry = depositTransferHistoryJSON.getJSONObject(i);
                mDepositHistoryEntries.add(new TransferHistoryEntry(depositTransferHistoryEntry.getString("Currency"), "WITHDRAWAL", depositTransferHistoryEntry.getString("Amount"), depositTransferHistoryEntry.getString("PaymentUuid"), depositTransferHistoryEntry.getString("Opened"), depositTransferHistoryEntry.getString("Address"), depositTransferHistoryEntry.getBoolean("Authorized"), depositTransferHistoryEntry.getBoolean("PendingPayment"), depositTransferHistoryEntry.getString("TxCost"), depositTransferHistoryEntry.getString("TxId"), depositTransferHistoryEntry.getBoolean("Canceled"), depositTransferHistoryEntry.getBoolean("InvalidAddress")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshTransferHistoryData();
    }


    private void refreshTransferHistoryData() {
        mRecyclerViewAdapter.updateDepositTransferHistoryData(mDepositHistoryEntries);
        mRecyclerViewAdapter.updateWithdrawTransferHistoryData(mWithdrawHistoryEntries);
        mListener.dismissProgressDialog();
    }

    public interface OnTransferHistoryListFragmentInteractionListener {
        void onTransferCancelled(TransferHistoryEntry item);

        void dismissProgressDialog();

    }
}
