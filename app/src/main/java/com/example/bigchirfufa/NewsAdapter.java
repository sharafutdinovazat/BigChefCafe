package com.example.bigchirfufa;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;



public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private ArrayList<Pair<String, String>> mData;
    private LayoutInflater mInflater;



    // data is passed into the constructor
    NewsAdapter(Context context, ArrayList<Pair<String, String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.news_rec, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pair<String, String> news = mData.get(position);
        holder.myTextView.setText(news.first);
        final ImageView img_view = holder.myImageView;
        final ProgressBar progress_view = holder.myProgressBar;
        Picasso.get().load(news.second)
                .resize(MainActivity.getAppContext().getWindow().getDecorView().getWidth(), 0)
                .into(holder.myImageView, new Callback() {
            @Override
            public void onSuccess() {

                if ((progress_view != null) && (img_view != null))
                {
                    progress_view.setVisibility(View.GONE);
                    img_view.setVisibility(View.VISIBLE);
                }
                else
                {
                    throw new IllegalStateException("162: не удается найти картинку и/или прогресс бар!");
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView myTextView;
        ImageView myImageView;
        ProgressBar myProgressBar;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.news_text);
            myImageView = itemView.findViewById(R.id.news_image);
            myProgressBar = itemView.findViewById(R.id.progress_bar_news);
        }

    }
}
