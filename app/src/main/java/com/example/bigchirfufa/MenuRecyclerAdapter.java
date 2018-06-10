package com.example.bigchirfufa;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MenuRecyclerAdapter extends RecyclerView.Adapter<MenuRecyclerAdapter.ViewHolder> {

    public ArrayList<ArrayList<Dish>> mDataset;

    public ArrayList<Dish> mDataBuy;

    ArrayList<String> parse_urls;

    RecyclerBuyAdapter recycler_adapter;

    DownloadTask task;
    ImageFactory factory;

    Integer current_url;
    private Timer timer;
    private DownloadTimer timer_task;

    private MainActivity context;

    public MenuRecyclerAdapter this_context;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView mTextViewTitle;
        public TextView mTextViewPrice;
        public TextView mTextViewWeight;
        public TextView mTextViewTime;
        public ImageView mImageView;
        public Button   mButtonAdd;
        public String img_url;
        public ProgressBar progres_bar;


        public ViewHolder(View view) {
            super(view);
            mTextViewTitle = view.findViewById(R.id.titleRC);
            mTextViewPrice = view.findViewById(R.id.priceRC);
            mTextViewWeight = view.findViewById(R.id.weightRC);
            mTextViewTime = view.findViewById(R.id.timeRC);
            mImageView = view.findViewById(R.id.imageRc);
            mButtonAdd = view.findViewById(R.id.button_add_recycler);
            progres_bar = view.findViewById(R.id.progress_bar_dish);
            //view.setOnClickListener(this);
            mImageView.setOnClickListener(this);
            mButtonAdd.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Dish dish = new Dish(mTextViewTitle.getText().toString(), mTextViewTime.getText().toString(), mTextViewPrice.getText().toString(), mTextViewWeight.getText().toString(), img_url, "");
            for (Dish tmp_dish: mDataset.get(current_url)) {
                if (tmp_dish.title.equals(mTextViewTitle.getText().toString()))
                    dish = tmp_dish;
            }
            //Dish dish = new Dish(mTextViewTitle.getText().toString(), mTextViewTime.getText().toString(), mTextViewPrice.getText().toString(), mTextViewWeight.getText().toString(), img_url, "");
            if (view.getId() == R.id.imageRc)
            {
                context.onDishClick(view, dish);
                return;
            }
            if (view.getId() == R.id.button_add_recycler)
            {
                mDataBuy.add(dish);
                recycler_adapter.update_dataset(mDataBuy);
            }


        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MenuRecyclerAdapter(RecyclerBuyAdapter adapter, ImageFactory factory, MainActivity context) {
        this.context = context;
        this.factory = factory;
        timer = new Timer();
        timer_task = new DownloadTimer();
        recycler_adapter = adapter;
        this_context = this;
        task = new DownloadTask();
        parse_urls = new ArrayList<String>();

        mDataBuy = new ArrayList<Dish>();

        current_url = 0;

        parse_urls.add("menyu-myaso");
        parse_urls.add("supy");
        parse_urls.add("salaty");
        parse_urls.add("goryachie-blyuda");
        parse_urls.add("vypechka");
        parse_urls.add("sladosti");
        parse_urls.add("napitki");
        task.execute(parse_urls);
    }

    public void update_menu(Integer id) {
        current_url = id;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public MenuRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // create a new view
        View lay = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dish_rec, parent, false);
              return new ViewHolder(lay);

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mTextViewTitle.setText(mDataset.get(current_url).get(position).title);
        //holder.mTextViewText.setText(mDataset.get(current_url).get(position).text);
        holder.mTextViewPrice.setText(mDataset.get(current_url).get(position).price);
        holder.mTextViewWeight.setText(mDataset.get(current_url).get(position).weight);
        holder.mTextViewTime.setText(mDataset.get(current_url).get(position).time);
        holder.img_url = mDataset.get(current_url).get(position).image;
        final ProgressBar progress_view = holder.progres_bar;
        final ImageView image_view = holder.mImageView;
        Picasso.with(context).load(mDataset.get(current_url).get(position).image)
                .resize(250, 150)
                .into(holder.mImageView, new Callback() {
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

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDataset == null) return 0;
        if (mDataset.size() > current_url) {
            context.stopAnimation(true);
            return mDataset.get(current_url).size();
        } else {
            context.stopAnimation(false);
            return 0;
        }
    }

    class DownloadTask extends AsyncTask<ArrayList<String>, Void, ArrayList<ArrayList<Dish>>> {

        @Override
        protected ArrayList<ArrayList<Dish>> doInBackground(ArrayList<String>... urls) {

            if (mDataset == null) {
                mDataset = new ArrayList<ArrayList<Dish>>();
            }
            ArrayList<ArrayList<Dish>> dishArrayList = new ArrayList<ArrayList<Dish>>();
            while (true) {
                try {
                    for (int i = 0; i < urls[0].size(); i++) {
                        dishArrayList.add(new ArrayList<Dish>());
                        String url = urls[0].get(i);
                        Document doc = null;
                        url = "http://bigchefufa.ru/menyu-dostavki/" + url + "/";
                        doc = Jsoup.connect(url).timeout(5000).get();
                        Elements metaElement = doc.select("html");
                        Elements foods = doc.getElementsByClass("menusection-item");

                        for (Element food : foods)

                        {
                            String title = Jsoup.parse(food.getElementsByClass("menusection-title").text()).text();
                            String time = food.getElementsByClass("menusection-time").text();
                            String price = food.getElementsByClass("menusection-price").text();
                            String weight = food.getElementsByClass("menusection-weight").text();
                            String image = "http://bigchefufa.ru" + food.getElementsByClass("menusection-link").attr("href");
                            String text = Jsoup.parse(food.childNode(3).attr("data-title").toString()).text();
                            factory.load_image(image);
                            dishArrayList.get(i).add(new Dish(title, time, price, weight, image, text));
                        }
                        String name = metaElement.attr("name");
                        Elements mainHeaderElements = doc.select("h2.main");
                    }

                    return dishArrayList;
                } catch (Exception e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {}

                }

            }
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<Dish>> result) {
            super.onPostExecute(result);
            if (result == null)
            {
                return;
            }
            mDataset = result;
            this_context.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
    }

    class DownloadTimer extends TimerTask {

        @Override
        public void run() {
            task.execute(parse_urls);
        }
    }
}