package crypto.manager.bittfolio.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import crypto.manager.bittfolio.adapter.HourlyDateAxisFormatter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CoinGraphFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CoinGraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CoinGraphFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private LineChart mChart;
    private boolean mNewGraph;
    private TextView m24High, mBid, m24Volume, m24Low, mAsk, m24Change;

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
        xAxis.setValueFormatter(new HourlyDateAxisFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        m24High = view.findViewById(R.id.text_view_24_high);
        mBid = view.findViewById(R.id.text_view_current_bid_price);
        m24Volume = view.findViewById(R.id.text_view_24_hour_volume);

        m24Low = view.findViewById(R.id.text_view_24_low);
        mAsk = view.findViewById(R.id.text_view_current_ask_price);
        m24Change = view.findViewById(R.id.text_view_24_hour_change);

        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void updateGraph(String coinGraph) {
        mNewGraph = true;
        List<Entry> entriesBTC = new ArrayList<Entry>();
        List<Entry> entriesUSDT = new ArrayList<Entry>();
        try {
            JSONObject jsonData = new JSONObject(coinGraph);
            JSONArray jsonPriceArray = jsonData.getJSONArray("Data");
            for (int i = 0; i < jsonPriceArray.length(); i++) {
                JSONObject priceObj = jsonPriceArray.getJSONObject(i);
                long millis = priceObj.getLong("time");
                float close = Float.valueOf(priceObj.getString("close"));
                entriesBTC.add(new Entry(millis, close));
//                entriesUSDT.add(new Entry(millis, close*13500));
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        LineDataSet dataSetBTC = new LineDataSet(entriesBTC, "BTC"); // add entriesBTC to dataset
//        LineDataSet dataSetUSDT = new LineDataSet(entriesUSDT, "USDT"); // add entriesBTC to dataset
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
                m24High.setText(coinDataJSON.getString("High"));
                mBid.setText(coinDataJSON.getString("Bid"));
                double volume = coinDataJSON.getDouble("Volume");
                m24Volume.setText(new DecimalFormat("#.##").format(volume));
                m24Low.setText(coinDataJSON.getString("Low"));
                mAsk.setText(coinDataJSON.getString("Ask"));
                double change = (coinDataJSON.getDouble("Last") / coinDataJSON.getDouble("PrevDay")) - 1;
                change *= 100;
                m24Change.setText((new DecimalFormat("#.##").format(change)) + "%");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
