package crypto.manager.bittfolio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.adapter.DateAxisFormatter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCoinGraphFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CoinGraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CoinGraphFragment extends Fragment {

    double mBtcUSDTValue = 1.0;
    private LineChart mChart;
    private boolean mNewGraph;
    private TextView m24High, mBid, m24Volume, m24Low, mAsk, m24Change;
    private OnCoinGraphFragmentInteractionListener mListener;
    private boolean mIsDollars;

    public CoinGraphFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CoinGraphFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CoinGraphFragment newInstance() {
        CoinGraphFragment fragment = new CoinGraphFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCoinGraphFragmentInteractionListener) {
            mListener = (OnCoinGraphFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCoinGraphFragmentInteractionListener");
        }
        mListener.startCoinGraphDataService();
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
        View view = inflater.inflate(R.layout.fragment_coin_graph, container, false);

        //Set-up graph
        mChart = (LineChart) view.findViewById(R.id.chart);
        mChart.setDescription(null);
        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new DateAxisFormatter(2));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        m24High = view.findViewById(R.id.text_view_24_high);
        mBid = view.findViewById(R.id.text_view_current_bid_price);
        m24Volume = view.findViewById(R.id.text_view_24_hour_volume);

        m24Low = view.findViewById(R.id.text_view_24_low);
        mAsk = view.findViewById(R.id.text_view_current_ask_price);
        m24Change = view.findViewById(R.id.text_view_24_hour_change);

        Spinner spinner = view.findViewById(R.id.spinner_timeframe);
        spinner.setSelection(2, true);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mListener != null) {
                    updateGraphInterval(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    private void updateGraphInterval(int i) {
        XAxis xAxis = mChart.getXAxis();
        DateAxisFormatter formatter = (DateAxisFormatter) xAxis.getValueFormatter();
        formatter.setInterval(i);
        mListener.updateGraphAtInterval(i);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateGraph(String coinGraph) {
        mNewGraph = true;
        List<Entry> entriesUnits = new ArrayList<Entry>();
        try {
            JSONObject jsonData = new JSONObject(coinGraph);
            JSONArray jsonPriceArray = jsonData.getJSONArray("Data");
            for (int i = 0; i < jsonPriceArray.length(); i++) {
                JSONObject priceObj = jsonPriceArray.getJSONObject(i);
                long millis = priceObj.getLong("time");
                float close = Float.valueOf(priceObj.getString("close"));
                //Convert to appropriate units
                if (mIsDollars) close *= mBtcUSDTValue;
                entriesUnits.add(new Entry(millis, close));
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        String units = mIsDollars ? "USDT" : "BTC";
        LineDataSet dataSetBTC = new LineDataSet(entriesUnits, units); // add entriesUnits to dataset
//        LineDataSet dataSetUSDT = new LineDataSet(entriesUSDT, "USDT"); // add entriesUnits to dataset
//        dataSetBTC.setColor();
//        dataSetBTC.setValueTextColor(...); // styling, ...
        LineData lineData = new LineData(dataSetBTC);
        mChart.setData(lineData);
        if (mNewGraph) {
            mChart.invalidate(); // redraw
            mNewGraph = false;
        }
        if (!mNewGraph) mChart.notifyDataSetChanged();
    }

    public void updateStats(String coinData) {
        try {
            JSONObject coinDataJSON = new JSONObject(coinData);
            JSONArray coinDataArray = coinDataJSON.getJSONArray("result");
            if (coinDataArray.length() > 0) {
                coinDataJSON = coinDataArray.getJSONObject(0);
                double high = coinDataJSON.getDouble("High");
                double bid = coinDataJSON.getDouble("Bid");
                double volume = coinDataJSON.getDouble("Volume");
                double low = coinDataJSON.getDouble("Low");
                double ask = coinDataJSON.getDouble("Ask");
                String currency = mIsDollars ? "$" : "₿";
                if (mIsDollars) {
                    high *= mBtcUSDTValue;
                    bid *= mBtcUSDTValue;
                    volume *= mBtcUSDTValue;
                    low *= mBtcUSDTValue;
                    ask *= mBtcUSDTValue;
                }
                String decimalFormat = mIsDollars ? "#.00" : "#.########";
                DecimalFormat df = new DecimalFormat(decimalFormat);
                String _24HourHighStr = currency + df.format(high);
                m24High.setText(_24HourHighStr);
                String bidStr = currency + df.format(bid);
                mBid.setText(bidStr);
                String _24HourVolumeStr = currency + new DecimalFormat("#.##").format(volume);
                m24Volume.setText(_24HourVolumeStr);
                String _24HourLowStr = currency + df.format(low);
                m24Low.setText(_24HourLowStr);
                String askStr = currency + df.format(ask);
                mAsk.setText(askStr);
                double change = (coinDataJSON.getDouble("Last") / coinDataJSON.getDouble("PrevDay")) - 1;
                change *= 100;
                String _24HourChange = (new DecimalFormat("#.##").format(change)) + "%";
                m24Change.setText(_24HourChange);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void changeUnits() {
        mIsDollars = !mIsDollars;
        mNewGraph = true;
    }

    public void updateBTCUSDTPrice(String btcUSDT) {
        try {
            JSONObject btcJson = new JSONObject(btcUSDT);
            JSONObject innerObj = btcJson.getJSONObject("result");
            mBtcUSDTValue = innerObj.getDouble("Last");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public interface OnCoinGraphFragmentInteractionListener {
        void updateGraphAtInterval(int i);

        void startCoinGraphDataService();
    }


}
