package com.example.bigchirfufa;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerBuyAdapter extends RecyclerView.Adapter<RecyclerBuyAdapter.ViewHolder> {

    public ArrayList<Pair<Dish, Integer>> mData;
    private ItemClickListener mClickListener;
    private RecyclerBuyAdapter this_context;
    int summa;
    View view_root;

    private ImageFactory factory;


    // data is passed into the constructor
    RecyclerBuyAdapter(View root, ImageFactory factory) {
        this_context = this;
        view_root = root;

        this.factory = factory;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View lay = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_buy_item, parent, false);

        return new RecyclerBuyAdapter.ViewHolder(lay);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pair<Dish, Integer> dish = mData.get(position);

        holder.myTextViewTitle.setText(dish.first.title);
        holder.myTextViewCount.setText(dish.second.toString());

        holder.dish = dish.first;
        holder.dish.count = dish.second;
        holder.myTextViewPrice.setText(dish.first.price + " x " + holder.dish.count);
        final ProgressBar progress_view = holder.progres_bar;
        final ImageView image_view = holder.myImageView;
        Picasso.with(MainActivity.getAppContext()).load(dish.first.image)
                .resize(250, 150)
                .into(holder.myImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                        if ((progress_view != null) && (image_view != null))
                        {
                            progress_view.setVisibility(View.GONE);
                            image_view.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            throw new IllegalStateException("162: не удается найти картинку и прогресс бар!");
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    public void update_dataset(Dish dish)
    {
        summa = 0;
        if (dish == null)
        {
            return;
        }
        if (mData == null) mData = new ArrayList<Pair<Dish, Integer>>();
        boolean is_set = false;

        for (int j = 0; j < mData.size(); j++)
        {
            if (mData.get(j).first.title.toString().equals(dish.title.toString()))
            {
                mData.set(j, new Pair<Dish, Integer>(dish, mData.get(j).second + 1));
                is_set = true;
            }
        }
        if (!is_set)
            mData.add(new Pair<Dish, Integer>(dish, 1));
        summ_update();
        this.notifyDataSetChanged();
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
            return 0;
    }

    public void summ_update()
    {
        summa = 0;
        if ((mData != null) && (mData.size() != 0))
        {
            for (Pair<Dish, Integer> dish: mData)
            {
                String str = dish.first.price;
                str = str.replaceAll("\\D+","");
                if (str.isEmpty()) str = "0";
                summa = summa + Integer.valueOf(str) * dish.second;
            }
        }
        TextView txt  = view_root.findViewById(R.id.summa_price);
        txt.setText("Итого: " + String.valueOf(summa) + " руб.");
        TextView summa_total = view_root.getRootView().findViewById(R.id.summaKorzina);
        summa_total.setText(String.valueOf(summa) + '\u20BD');
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView myTextViewTitle;
        TextView myTextViewPrice;
        TextView myTextViewCount;
        ImageView myImageView;
        ProgressBar progres_bar;
        Button btn_inc;
        Button btn_dec;
        Dish dish;

        ViewHolder(View itemView) {
            super(itemView);
            btn_inc = itemView.findViewById(R.id.btn_inc_item_count);
            btn_dec = itemView.findViewById(R.id.btn_dec_item_count);
            btn_inc.setOnClickListener(this);
            btn_dec.setOnClickListener(this);
            myTextViewTitle = itemView.findViewById(R.id.title_recycler_buy_item);
            myTextViewPrice = itemView.findViewById(R.id.price_recycler_buy_item);
            myTextViewCount = itemView.findViewById(R.id.count_recycler_buy_item);
            myImageView = itemView.findViewById(R.id.image_recycler_buy_item);
            progres_bar = itemView.findViewById(R.id.progress_bar_dish);
        }

        @Override
        public void onClick(View view) {
            int item_to_delete = -1;
            String title = myTextViewTitle.getText().toString();
            for (int j = 0; j < mData.size(); j++)
            {
                if (mData.get(j).first.title.toString().equals(title))
                {
                    if (view.getId() == R.id.btn_inc_item_count)
                    {
                        dish.count++;
                    }
                    else
                    {
                        if (dish.count == 1) {
                            item_to_delete = j;
                            continue;
                        }
                        dish.count--;

                    }
                    mData.set(j, new Pair<Dish, Integer>(dish, dish.count));
                }
            }
            if (item_to_delete != -1)
            {
                mData.remove(item_to_delete);
            }
            if (mData.isEmpty())
            {
                MainActivity.getAppContext().menu_recycler_adapter.mDataBuy.clear();
                MainActivity.getAppContext().changeView(R.id.recycler_is_empty);
            }

            this_context.notifyDataSetChanged();
            summ_update();

        }
    }

    // convenience method for getting data at click position
//    MenuItem getItem(int id) {
//        return mData.get(id);
//    }

    // allows clicks events to be caught
    void setClickListener(MainActivity itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemRecyclerClick(View view, int position);
    }


}
