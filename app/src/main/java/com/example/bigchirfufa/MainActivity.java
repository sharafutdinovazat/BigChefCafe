package com.example.bigchirfufa;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.FontRequestEmojiCompatConfig;
import android.support.v4.provider.FontRequest;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainMenuRecyclerAdapter.ItemClickListener, RecyclerBuyAdapter.ItemClickListener, View.OnClickListener{
    
    public View active_view = null;
    public View inactive_view = null;
    
    MainMenuRecyclerAdapter main_menu_adapter;
    MenuAdapter             menu_adapter;
    NewsAdapter             news_adapter;
    RecyclerBuyAdapter      recycler_buy_adapter;
    
    private Button button66;
    private TextView textView;
    private ImageView korzina;
   

    private ArrayDeque<Integer> views;

    //https://api.vk.com/method/wall.get?owner_id=-106468312&v=5.52&access_token=a1b7b189a1b7b189a1b7b189c0a1d30d21aa1b7a1b7b189faaaf5bdd028cdee78829ab4

    private static MainActivity context;
    //current main_menu_adapter
    // 0 - main menu
    // 1 - menu dishes
    // 2 - recycler dishes
    Integer current_adapter;

    RecyclerView main_menu_recycler_view;
    RecyclerView menu_view;
    RecyclerView buy_recycler_view;
    RecyclerView news_recycler_view;

    ImageFactory factory;

    User user;

    public static MainActivity getAppContext()
    {
        return context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity.context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        views = new ArrayDeque<Integer>();

        Typeface mFont = Typeface.createFromAsset(getAssets(), "fonts/calibril.ttf");
        ViewGroup root = (ViewGroup) findViewById(R.id.drawer_layout);
        setFont(root, mFont);

        FontRequest fontRequest = new FontRequest(
                "com.example.fontprovider",
                "com.example",
                "emoji compat Font Query", R.array.com_google_android_gms_fonts_certs);
        EmojiCompat.Config config = new FontRequestEmojiCompatConfig(this, fontRequest)
                .setReplaceAll(true)
                .setEmojiSpanIndicatorEnabled(false)
                .setEmojiSpanIndicatorColor(Color.GREEN);
        EmojiCompat.init(config);


        ArrayList<com.example.bigchirfufa.MenuItem> animalNames = new ArrayList<com.example.bigchirfufa.MenuItem>();
        animalNames.add(new com.example.bigchirfufa.MenuItem("Мясо", R.drawable.meat));
        animalNames.add(new com.example.bigchirfufa.MenuItem("Супы", R.drawable.soap));
        animalNames.add(new com.example.bigchirfufa.MenuItem("Салаты", R.drawable.salts));
        animalNames.add(new com.example.bigchirfufa.MenuItem("Горячие блюда", R.drawable.seconddish));
        animalNames.add(new com.example.bigchirfufa.MenuItem("Выпечка", R.drawable.vypechka));
        animalNames.add(new com.example.bigchirfufa.MenuItem("Сладости", R.drawable.desert));
        animalNames.add(new com.example.bigchirfufa.MenuItem("Напитки", R.drawable.drinks));

        user = new User();
        user.first_name = findViewById(R.id.first_name_id);
        user.last_name = findViewById(R.id.last_name_id);
        user.phone_number = findViewById(R.id.phone_number_id);
        user.adress = findViewById(R.id.postal_address_id);
        user.dom = findViewById(R.id.postal_dom_id);
        user.kvartira = findViewById(R.id.postal_kv_id);
        user.padik = findViewById(R.id.postal_padik_id);
        user.ettage = findViewById(R.id.postal_ettage_id);


        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        user.first_name.setText(settings.getString("first_name", "").toString());
        user.last_name.setText(settings.getString("last_name", "").toString());
        user.phone_number.setText(settings.getString("phone_number", "").toString());
        user.adress.setText(settings.getString("address", "").toString());
        user.dom.setText(settings.getString("dom", "").toString());
        user.kvartira.setText(settings.getString("kvartira", "").toString());
        user.padik.setText(settings.getString("padik", "").toString());
        user.ettage.setText(settings.getString("ettage", "").toString());


        main_menu_recycler_view = findViewById(R.id.menuRcV);
        menu_view = findViewById(R.id.menu_dishesRcV);
        buy_recycler_view = findViewById(R.id.recycler_buy_id);
        news_recycler_view = findViewById(R.id.news_recycler);
        
        main_menu_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        menu_view.setLayoutManager(new LinearLayoutManager(this));
        buy_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        news_recycler_view.setLayoutManager(new LinearLayoutManager(this));

        main_menu_adapter = new MainMenuRecyclerAdapter(this, animalNames);
        recycler_buy_adapter = new RecyclerBuyAdapter(findViewById(R.id.drawer_layout));
        menu_adapter = new MenuAdapter();

        main_menu_adapter.setClickListener(this);
        recycler_buy_adapter.setClickListener(this);
        
        main_menu_recycler_view.setAdapter(main_menu_adapter);
        menu_view.setAdapter(menu_adapter);
        buy_recycler_view.setAdapter(recycler_buy_adapter);

   

        findViewById(R.id.button_buy).setOnClickListener(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button recycler_empty_btn = (Button) findViewById(R.id.recycler_start_buy);
        recycler_empty_btn.setOnClickListener(this);

        ImageView recycler_bar_image = (ImageView) findViewById(R.id.recycler_bar_image);
        recycler_bar_image.setOnClickListener(this);

        findViewById(R.id.profile).setOnClickListener(this);

        String api_url = "https://api.vk.com/method/wall.get?owner_id=-106468312&v=5.52&access_token=a1b7b189a1b7b189a1b7b189c0a1d30d21aa1b7a1b7b189faaaf5bdd028cdee78829ab4";
        DownloadNews news = new DownloadNews();
        news.execute(api_url);
        nextView(R.id.menu);
    }

    public void parse_news(String html)
    {
        ArrayList<Pair<String, String>> news = new ArrayList<Pair<String, String>>();
        try
        {
            JSONObject object = (JSONObject) new JSONParser().parse(html);
            JSONObject tmp_object = (JSONObject) object.get("response");
            JSONArray posts = (JSONArray) tmp_object.get("items");
            Iterator iter = posts.iterator();
            while(iter.hasNext())
            {
                JSONObject post = (JSONObject) iter.next();
                String text = (String) post.get("text");
                JSONObject attachments = (JSONObject)((JSONArray)post.get("attachments")).get(0);
                JSONObject photo = (JSONObject)attachments.get("photo");
                String photo_url = (String)photo.get("photo_807");
                Pair<String, String> pair = new Pair<String, String>(text, photo_url);
                news.add(pair);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        news_adapter = new NewsAdapter(this, news);
        news_recycler_view.setAdapter(news_adapter);
        //nextView(R.id.news);

    }

    public void setFont(ViewGroup group, Typeface font) {
        int count = group.getChildCount();
        View v;
        for(int i = 0; i < count; i++) {
            v = group.getChildAt(i);
            if(v instanceof TextView || v instanceof Button /*etc.*/)
                ((TextView)v).setTypeface(font);
            else if(v instanceof ViewGroup)
                setFont((ViewGroup)v, font);
        }
    }

    public void onDishClick(View view, Dish dish)
    {
        ImageView img_view = findViewById(R.id.image_dish_layout);
        TextView txt_view = findViewById(R.id.text_dish_layout);
        TextView titledish = findViewById(R.id.titledish);

        final ProgressBar progress_view = findViewById(R.id.progress_bar_dish);
        final ImageView image_view = img_view;
        Picasso.get().load(dish.image)
                .resize(500, 400)
                .into(img_view, new Callback() {
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
                    public void onError(Exception e) {

                    }
                });
        txt_view.setText(dish.text);
        titledish.setText(dish.title);
        nextView(R.id.dish_layout);
    }

    public void stopAnimation(Boolean stop)
    {
        View view = findViewById(R.id.wait_anim_layout);
        if (view == null)
        {
            throw new NullPointerException("Animation view is null!");
        }
        if (stop) {
            view.setVisibility(View.GONE);
        }
        else {
            view.setVisibility(View.VISIBLE);

        }

    }


    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        if (id == R.id.recycler_start_buy)
        {
            nextView(R.id.menu);
            return;
        }
        if (id == R.id.recycler_bar_image)
        {
            buy_recycler_view.getRecycledViewPool().clear();
            if (recycler_buy_adapter.mData == null || recycler_buy_adapter.mData.isEmpty())
            {
                nextView(R.id.recycler_is_empty);
            }
            else {
                menu_adapter.mDataBuy.clear();
                buy_recycler_view.setAdapter(recycler_buy_adapter);
                nextView(R.id.recycler_buy);
            }
            return;
        }
        if (id == R.id.button_buy)
        {
            if (user.isEmpty())
            {
                nextView(R.id.profile);
                Toast.makeText(this, "Пожалуйста, заполните личные данные", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences settings = getSharedPreferences("UserInfo", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("first_name", user.first_name.getText().toString());
            editor.putString("last_name",user.last_name.getText().toString());
            editor.putString("phone_number",user.phone_number.getText().toString());
            editor.putString("address",user.adress.getText().toString());
            editor.putString("dom",user.dom.getText().toString());
            editor.putString("kvartira",user.kvartira.getText().toString());
            editor.putString("padik",user.padik.getText().toString());
            editor.putString("ettage",user.ettage.getText().toString());

            editor.commit();

            Toast.makeText(this, "Вы сделали покупку, заказ отправляется", Toast.LENGTH_SHORT).show();
            String body = "";
            ArrayList<Pair<Dish, Integer>> data = recycler_buy_adapter.mData;
            for (Pair<Dish, Integer> dish: data) {
                body += dish.first.title + " : в количестве " + dish.second.toString() + " шт " + '\n';
            }
            body += " Имя: " + user.first_name.getText().toString() + '\n' + " Фамилия: " + user.last_name.getText().toString() + '\n';
            body += " Номер телефона: " + user.phone_number.getText().toString() + '\n' + " Улица: " + user.adress.getText().toString() + '\n';
            body += " Номер дома: " + user.dom.getText().toString() + '\n' + " Квартира: " + user.kvartira.getText().toString() + '\n';
            body += " Подъезд: " + user.padik.getText().toString() + '\n' + " Этаж: " + user.ettage.getText().toString() + '\n';
            new MailSenderAsynс().execute(body);
            recycler_buy_adapter.mData.clear();
            nextView(R.id.recycler_is_empty);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        } else {
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemRecyclerClick(View view, int pos)
    {
        //
    }
    
    public void backView()
    {
        if (views.isEmpty() || views.size() == 1) return;
        findViewById(views.pollFirst()).setVisibility(View.GONE);
        findViewById(views.peekFirst()).setVisibility(View.VISIBLE);
    }

    public View getCurrentView()
    {
        return findViewById(views.peekFirst());
    }

    public void nextView(Integer view_id)
    {
        if (views.isEmpty())
        {
            views.push(view_id);
            active_view = findViewById(view_id);
            inactive_view = active_view;
            active_view.setVisibility(View.VISIBLE);
            return;
        }
        if (view_id == R.id.menu_layout)
        {
            if (views.peekFirst() == R.id.recycler_buy)
            {
                view_id = R.id.menu;
            }
        }
        if (view_id == R.id.recycler_buy)
        {
            if (recycler_buy_adapter.mData.isEmpty())
                view_id = R.id.recycler_is_empty;
        }
        for (Integer id: views)
        {
            findViewById(id).setVisibility(View.GONE);
        }
        if (!view_id.equals(views.peekFirst()))
        {
            views.addFirst(view_id);
        }
        active_view = findViewById(view_id);
        active_view.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id)
        {
            case R.id.nav_mail:
            {
                nextView(R.id.feedback);
                break;
            }
            case R.id.nav_profile:
            {
                nextView(R.id.profile);
                break;
            }
            case R.id.nav_menufood:
            {
                nextView(R.id.menu);
                break;
            }
            case R.id.nav_app:
            {
                nextView(R.id.about_app);
                break;
            }
            case R.id.nav_car:
            {
                nextView(R.id.car);
                break;
            }
            case R.id.nav_news:
            {
                nextView(R.id.news);
                break;
            }
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            openQuitDialog();
        }
        return true;
    }

    private void openQuitDialog() {
        onBackPressed();
        backView();
    }

    @Override
    public void onItemClick(View view, int position) {
//        if (menu_adapter.mDataset != null && !menu_adapter.mDataset.isEmpty())
//        {
//
//        }
//        else
//        {
//            return;
//        }
        menu_adapter.update_menu(position);
        menu_view.setAdapter(menu_adapter);
        nextView(R.id.main_menu);

        Typeface mFont = Typeface.createFromAsset(MainActivity.getAppContext().getAssets(), "fonts/calibril.ttf");
        ViewGroup root = (ViewGroup) MainActivity.getAppContext().findViewById(R.id.menu);
        MainActivity.getAppContext().setFont(root, mFont);

    }
}

class User
{
    TextView first_name;
    TextView last_name;
    TextView phone_number;
    TextView adress;
    TextView dom;
    TextView kvartira;
    TextView padik;
    TextView ettage;

    public boolean isEmpty()
    {
        if (first_name.getText().toString().isEmpty())
        {
            return true;
        }
        if (last_name.getText().toString().isEmpty())
        {
            return true;
        }
        if (phone_number.getText().toString().isEmpty())
        {
            return true;
        }
        if (dom.getText().toString().isEmpty())
        {
            return true;
        }
        if (kvartira.getText().toString().isEmpty())
        {
            return true;
        }
        if (padik.getText().toString().isEmpty())
        {
            return true;
        }
        if (ettage.getText().toString().isEmpty())
        {
            return true;
        }
        return adress.getText().toString().isEmpty();
    }
}


class DownloadNews extends AsyncTask<String, Void, String>
{
    @Override
    protected String doInBackground(String... params) {
        String html = "";
        try {
           html = Jsoup.connect(params[0]).ignoreContentType(true).execute().body();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return html;
    }

    @Override
    protected void onPostExecute(String html)
    {
        MainActivity.getAppContext().parse_news(html);
    }

}
