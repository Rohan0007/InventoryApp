package com.example.android.inventoryapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.android.inventoryapp.data.InventoryDbHelper;
import com.example.android.inventoryapp.data.InventoryItem;

public class MainActivity extends AppCompatActivity {

    private final static String LOG_TAG = MainActivity.class.getCanonicalName();
    InventoryDbHelper dbHelper;
    InventoryCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new InventoryDbHelper(this);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                startActivity(intent);
            }
        });

        final ListView listView = (ListView) findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        Cursor cursor = dbHelper.readStock();

        adapter = new InventoryCursorAdapter(this, cursor);
        listView.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.swapCursor(dbHelper.readStock());
    }

    public void clickOnViewItem(long id) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("itemId", id);
        startActivity(intent);
    }

    public void clickOnSale(long id, int quantity) {
        dbHelper.sellOneItem(id, quantity);
        adapter.swapCursor(dbHelper.readStock());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_dummy_data:
                addDummyData();
                adapter.swapCursor(dbHelper.readStock());
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Add dummy data
     */
    private void addDummyData() {
        InventoryItem tennisRacket = new InventoryItem(
                "Tennis Racket",
                "70",
                90,
                "Babolat",
                "546 772 8983",
                "service@babolat.net");
        dbHelper.insertItem(tennisRacket);

        InventoryItem badmintonRacket = new InventoryItem(
                "Badminton Racket",
                "50",
                110,
                "Yonex",
                "463 777 2320",
                "servicerequest@yonex.com");
        dbHelper.insertItem(badmintonRacket);

        InventoryItem tennisBall = new InventoryItem(
                "Tennis Ball",
                "7",
                200,
                "Wilson",
                "929 575 4560",
                "servicemail@wilson.com");
        dbHelper.insertItem(tennisBall);

        InventoryItem shuttleCock = new InventoryItem(
                "Shuttle Cock ",
                "3",
                250,
                "Yonex Aerosensa",
                "334 456 7658",
                "servicerequest@yonex.com");
        dbHelper.insertItem(shuttleCock);

        InventoryItem badmintonNet = new InventoryItem(
                "Badminton Net",
                "25",
                50,
                "Yonex",
                "235 545 6577",
                "servicerequest@yonex.com");
        dbHelper.insertItem(badmintonNet);

        InventoryItem volleyBall = new InventoryItem(
                "Volley Ball",
                "15",
                80,
                "Wilson",
                "354 454 6666",
                "servicemail@wilson.com");
        dbHelper.insertItem(volleyBall);

        InventoryItem cricketBat = new InventoryItem(
                "Cricket Bat",
                "55",
                150,
                "English Willow",
                "335 657 6464",
                "mailman@englishwillow.net");
        dbHelper.insertItem(cricketBat);

        InventoryItem cricketBall = new InventoryItem(
                "Cricket Ball",
                "7",
                210,
                "Kookaburra",
                "235 555 4526",
                "mailtoservice@kookaburra.com");
        dbHelper.insertItem(cricketBall);

    }
}
