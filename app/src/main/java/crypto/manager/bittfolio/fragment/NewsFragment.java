package crypto.manager.bittfolio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.adapter.NewsItemRecyclerViewAdapter;
import crypto.manager.bittfolio.model.NewsItem;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnNewsListFragmentInteractionListener}
 * interface.
 */
public class NewsFragment extends Fragment {

    List<NewsItem> mNewsItems;
    // TODO: Customize parameter argument names
    // TODO: Customize parameters
    private OnNewsListFragmentInteractionListener mListener;
    private NewsItemRecyclerViewAdapter mNewsRecyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static NewsFragment newInstance() {
        NewsFragment fragment = new NewsFragment();
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
        View view = inflater.inflate(R.layout.fragment_news_item_list, container, false);

        mNewsItems = new ArrayList<>();

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            mNewsRecyclerViewAdapter = new NewsItemRecyclerViewAdapter(mNewsItems, mListener);
            recyclerView.setAdapter(mNewsRecyclerViewAdapter);
        }

        getHeadlines();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNewsListFragmentInteractionListener) {
            mListener = (OnNewsListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNewsListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void getHeadlines() {
        mNewsItems = new ArrayList<>();
        final List<String> mTitles = new LinkedList<>();
        final List<String> mUrl = new LinkedList<>();
        final List<String> mDate = new LinkedList<>();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.reddit.com/r/CryptoCurrency.rss")
                .build();

        client.newCall(request).enqueue(new Callback() {
            Handler mainHandler = new Handler(getActivity().getMainLooper());

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            try {
                                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                                factory.setNamespaceAware(false);
                                XmlPullParser xpp = factory.newPullParser();

                                // We will get the XML from an input stream
                                xpp.setInput(response.body().byteStream(), "UTF_8");

            /* We will parse the XML content looking for the "<title>" tag which appears inside the "<item>" tag.
             * However, we should take in consideration that the rss feed name also is enclosed in a "<title>" tag.
             * As we know, every feed begins with these lines: "<channel><title>Feed_Name</title>...."
             * so we should skip the "<title>" tag which is a child of "<channel>" tag,
             * and take in consideration only "<title>" tag which is a child of "<item>"
             *
             * In order to achieve this, we will make use of a boolean variable.
             */
                                boolean insideItem = false;

                                // Returns the type of current event: START_TAG, END_TAG, etc..
                                int eventType = xpp.getEventType();
                                NewsItem newsItem = new NewsItem();
                                while (eventType != XmlPullParser.END_DOCUMENT) {
                                    if (eventType == XmlPullParser.START_TAG) {
                                        newsItem.setSource("Reddit /r/Cryptocurrency");
                                        if (xpp.getName().equalsIgnoreCase("entry")) {
                                            insideItem = true;
                                        } else if (xpp.getName().equalsIgnoreCase("title")) {
                                            if (insideItem)
                                                mTitles.add(xpp.nextText()); //extract the headline
                                        } else if (xpp.getName().equalsIgnoreCase("link")) {
                                            if (insideItem)
                                                mUrl.add(xpp.nextText()); //extract the link of article
                                        } else if (xpp.getName().equalsIgnoreCase("updated")) {
                                            if (insideItem)
                                                mDate.add(xpp.nextText()); //extract the date of article
                                        }
                                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("entry")) {
                                        insideItem = false;
                                    }
                                    eventType = xpp.next(); //move to next element
                                }

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            for (int i = 0; i < mTitles.size(); i++) {
                                NewsItem ni = new NewsItem();
                                ni.setTime(mDate.get(i));
                                ni.setSource("Reddit /r/cryptocurrency");
                                ni.setUrl(mUrl.get(i));
                                ni.setTitle(mTitles.get(i));
                                mNewsItems.add(ni);
                            }
                        }
                        mNewsRecyclerViewAdapter.setNewsItemList(mNewsItems);
                        mNewsRecyclerViewAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnNewsListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onNewsListFragmentInteraction(NewsItem item);
    }
}
