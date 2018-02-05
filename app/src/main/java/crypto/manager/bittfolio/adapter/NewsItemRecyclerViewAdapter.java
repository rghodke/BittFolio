package crypto.manager.bittfolio.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.NewsFragment.OnNewsListFragmentInteractionListener;
import crypto.manager.bittfolio.model.NewsItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link NewsItem} and makes a call to the
 * specified {@link OnNewsListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class NewsItemRecyclerViewAdapter extends RecyclerView.Adapter<NewsItemRecyclerViewAdapter.ViewHolder> {

    private final OnNewsListFragmentInteractionListener mListener;
    private List<NewsItem> mValues;

    public NewsItemRecyclerViewAdapter(List<NewsItem> items, OnNewsListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_news_item, parent, false);
        return new ViewHolder(view);
    }

    public void setNewsItemList(List<NewsItem> items) {
        this.mValues = items;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(mValues.get(position).getTitle());
        holder.mTimeView.setText(mValues.get(position).getTime());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onNewsListFragmentInteraction((holder.mItem));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mTimeView;
        public NewsItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.text_view_title);
            mTimeView = (TextView) view.findViewById(R.id.text_view_time);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTimeView.getText() + "'";
        }
    }
}
