package crypto.manager.bittfolio.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import crypto.manager.bittfolio.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link onCoinSearchFragmentInteractionListener}
 * interface.
 */
public class CoinSearchFragment extends Fragment {

    private onCoinSearchFragmentInteractionListener mListener;
    private String[] mCoins;
    private ListView mCoinListView;
    private EditText mEditTextSearch;
    private ArrayAdapter<String> mCoinListAdapter;
    private OkHttpClient mClient;

    public CoinSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CoinSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CoinSearchFragment newInstance() {
        CoinSearchFragment fragment = new CoinSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_coindata, container, false);
        mClient = new OkHttpClient();
        mCoinListView = (ListView) view.findViewById(R.id.list_view_coin_search);
        mCoinListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedCurrency = (String) adapterView.getItemAtPosition(i);
                mListener.onCoinSelected(selectedCurrency);
            }
        });
        mEditTextSearch = (EditText) view.findViewById(R.id.edit_text_quantity_coin_search);

        mEditTextSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                mCoinListAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

        new RetrieveCurrencyList().execute();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onCoinSearchFragmentInteractionListener) {
            mListener = (onCoinSearchFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onCoinSearchFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface onCoinSearchFragmentInteractionListener {
        // TODO: Update argument type and name
        void onCoinSelected(String coin);
    }

    private class RetrieveCurrencyList extends AsyncTask<Void, Void, List<String>> {

        private ProgressDialog mPDialog;

        @Override
        protected void onPreExecute() {
            mPDialog = new ProgressDialog(getContext());
            mPDialog.setTitle(getString(R.string.label_loading_currency_list));
            mPDialog.show();
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            try {
                return getCurrencyList();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

        public List<String> getCurrencyList() throws IOException, JSONException {
            final List<String> currencies = new ArrayList<>();
            Request request = new Request.Builder()
                    .url("https://bittrex.com/api/v1.1/public/getmarkets")
                    .build();

            Response response = mClient.newCall(request).execute();
            String responseStr = response.body().string();
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(responseStr);
            if (jsonObject.getString("success").equals("true")) {
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                for (int i = 0; i < jsonArray.length(); i++) {

                    String currency = jsonArray.getJSONObject(i).getString("MarketCurrency");
                    if (!currencies.contains(currency)) currencies.add(currency);
                }
                return currencies;
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<String> currencies) {
            mPDialog.dismiss();
            //Set the mCoinListAdapter
            Collections.sort(currencies);
            mCoins = currencies.toArray(new String[0]);
            mCoinListAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_singlechoice, mCoins);
            // Adding items to listview
            mCoinListView.setAdapter(mCoinListAdapter);
        }
    }
}
