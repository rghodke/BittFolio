package crypto.manager.bittfolio.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

}
