package com.example.bigchirfufa;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainMenuRecyclerAdapter.ItemClickListener, RecyclerBuyAdapter.ItemClickListener, View.OnClickListener{
    public View active_view = null;
    public View incative_view = null;
    MainMenuRecyclerAdapter adapter;
    MenuRecyclerAdapter menu_recycler_adapter;
    RecyclerBuyAdapter  recycler_buy_adapter;


    private static MainActivity context;
    //current adapter
    // 0 - main menu
    // 1 - menu dishes
    // 2 - recycler dishes
    Integer current_adapter;

    RecyclerView menu_recycler_view;
    RecyclerView buy_recycler_view;

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

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        user.first_name .setText(settings.getString("first_name", "").toString());
        user.last_name.setText(settings.getString("last_name", "").toString());
        user.phone_number.setText(settings.getString("phone_number", "").toString());
        user.adress.setText(settings.getString("address", "").toString());



        menu_recycler_view = findViewById(R.id.menuRcV);
        menu_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainMenuRecyclerAdapter(this, animalNames);
        adapter.setClickListener(this);
        menu_recycler_view.setAdapter(adapter);
        current_adapter = 0;

        factory =  new ImageFactory(findViewById(R.id.drawer_layout), this);


        buy_recycler_view = findViewById(R.id.recycler_buy_id);
        buy_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        recycler_buy_adapter = new RecyclerBuyAdapter(findViewById(R.id.drawer_layout), factory);
        recycler_buy_adapter.setClickListener(this);

        menu_recycler_adapter = new MenuRecyclerAdapter(recycler_buy_adapter, factory, this);

        findViewById(R.id.button_buy).setOnClickListener(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        findViewById(R.id.profile).setOnClickListener(this);
        changeView(R.id.menu);
    }

    public void onDishClick(View view, Dish dish)
    {
        View btn_view = findViewById(R.id.button_close_dish_layout);
        ImageView img_view = findViewById(R.id.image_dish_layout);
        TextView txt_view = findViewById(R.id.text_dish_layout);

        factory.set_image(img_view, dish.image + "big");
        txt_view.setText(dish.text);
        btn_view.setOnClickListener(this);
        changeView(R.id.dish_layout);
    }

    public void stopAnimation(Boolean stop)
    {
        View view = findViewById(R.id.wait_anim_layout);
        if (view == null)
        {
            throw new NullPointerException("Animation view is null!");
        }
        if (stop)
            view.setVisibility(View.GONE);
        else
            view.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        if (id == R.id.button_buy)
        {
            if (user.isEmpty())
            {
                changeView(R.id.profile);
                Toast.makeText(this, "Пожалуйста заполните профиль", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences settings = getSharedPreferences("UserInfo", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("first_name", user.first_name.getText().toString());
            editor.putString("last_name",user.last_name.getText().toString());
            editor.putString("phone_number",user.phone_number.getText().toString());
            editor.putString("address",user.adress.getText().toString());
            editor.commit();

            Toast.makeText(this, "Вы сделали покупку, заказ отправляется", Toast.LENGTH_SHORT).show();
            String body = "";
            ArrayList<Pair<Dish, Integer>> data = recycler_buy_adapter.mData;
            for (Pair<Dish, Integer> dish: data) {
                body += dish.first.title + " : в количестве " + dish.second.toString() + '\n';
            }
            body += user.first_name.getText().toString() + " " + user.last_name.getText().toString() + '\n';
            body += user.phone_number.getText().toString() + '\n' + user.adress.getText().toString();
            new MailSenderAsynс().execute(body);
            recycler_buy_adapter.update_dataset(new ArrayList<Dish>());
            menu_recycler_adapter.mDataBuy.clear();
            changeView(R.id.recycler_is_empty);
        }
        if (id == R.id.button_close_dish_layout)
        {
            openQuitDialog();
            //onBackPressed();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

    public void changeView(Integer view_id)
    {
        if (active_view == null)
        {
            active_view = findViewById(R.id.menu);
        }

        active_view.setVisibility(View.GONE);
        incative_view = active_view;
        active_view = findViewById(view_id);
        active_view.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        stopAnimation(true);
        if (id == R.id.nav_mail) {
/////////////////////////////////////////////////////////////////////
        } else if (id == R.id.nav_profile)
        {
            changeView(R.id.profile);

        } else if (id == R.id.nav_menufood)
        {
            menu_recycler_view.setAdapter(adapter);
            changeView(R.id.menu);

        } else if (id == R.id.nav_geolocation)
        {
            //о ресторане;
        } else if (id == R.id.nav_car)
        {
            changeView(R.id.car);

        } else if (id == R.id.nav_app)
        {
/////////////////////////////////////////////////////////////////////
        } else if (id == R.id.nav_recycler_buy)
        {
            buy_recycler_view.getRecycledViewPool().clear();
            if ((menu_recycler_adapter.mDataBuy == null) || (menu_recycler_adapter.mDataBuy.size() == 0))
            {
                changeView(R.id.recycler_is_empty);
            }
            else {
                recycler_buy_adapter.update_dataset(menu_recycler_adapter.mDataBuy);
                buy_recycler_view.setAdapter(recycler_buy_adapter);
                current_adapter = 2;
                changeView(R.id.recycler_buy);
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
        if (active_view.getId() == R.id.recycler_is_empty)
        {
            changeView(R.id.menu);
            return;
        }
        if (active_view.getId() == R.id.menu)
        {
            if (current_adapter == 1)
            {
                menu_recycler_view.setAdapter(adapter);
                current_adapter = 0;
                changeView(R.id.menu);
                stopAnimation(true);
                return;
            }
            if (current_adapter == 0)
                finish();
            stopAnimation(true);
        }

        changeView(incative_view.getId());

    }

    @Override
    public void onItemClick(View view, int position) {
        if (menu_recycler_adapter.mDataset != null && !menu_recycler_adapter.mDataset.isEmpty())
        {
            stopAnimation(true);
        }
        else
        {
            stopAnimation(false);
        }
        menu_recycler_view.getRecycledViewPool().clear();
        menu_recycler_adapter.update_menu(position);
        menu_recycler_view.setAdapter(menu_recycler_adapter);
        current_adapter = 1;
        changeView(R.id.menu);
    }
}

class User
{
    TextView first_name;
    TextView last_name;
    TextView phone_number;
    TextView adress;

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
        return adress.getText().toString().isEmpty();
    }
}

