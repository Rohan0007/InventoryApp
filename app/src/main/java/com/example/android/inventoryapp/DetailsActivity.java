package com.example.android.inventoryapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.android.inventoryapp.data.InventoryDbHelper;
import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryItem;

/**
 * Created by Rohan on 10/20/17.
 */

public class DetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailsActivity.class.getCanonicalName();
    private InventoryDbHelper dbHelper;
    EditText name;
    EditText price;
    EditText quant;
    EditText supplierName;
    EditText supplierPhone;
    EditText supplierEmail;
    long currentItemId;
    ImageButton decreaseQuantity;
    ImageButton increaseQuantity;
    Boolean infoItemHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        name = (EditText) findViewById(R.id.product_name_edit);
        price = (EditText) findViewById(R.id.price_edit);
        quant = (EditText) findViewById(R.id.quantity_edit);
        supplierName = (EditText) findViewById(R.id.supplier_name_edit);
        supplierPhone = (EditText) findViewById(R.id.supplier_phone_edit);
        supplierEmail = (EditText) findViewById(R.id.supplier_email_edit);
        decreaseQuantity = (ImageButton) findViewById(R.id.decrease_quantity);
        increaseQuantity = (ImageButton) findViewById(R.id.increase_quantity);

        dbHelper = new InventoryDbHelper(this);
        currentItemId = getIntent().getLongExtra("itemId", 0);

        if (currentItemId == 0) {
            setTitle(getString(R.string.editor_activity_title_new_item));
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));
            addValuesToEditItem(currentItemId);
        }

        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subtractOneFromQuantity();
                infoItemHasChanged = true;
            }
        });

        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOneToQuantity();
                infoItemHasChanged = true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!infoItemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialogBox(discardButtonClickListener);
    }

    private void showUnsavedChangesDialogBox(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void subtractOneFromQuantity() {
        String previousVal = quant.getText().toString();
        int previousValue;
        if (previousVal.isEmpty()) {
            return;
        } else if (previousVal.equals("0")) {
            return;
        } else {
            previousValue = Integer.parseInt(previousVal);
            quant.setText(String.valueOf(previousValue - 1));
        }
    }

    private void addOneToQuantity() {
        String previousVal = quant.getText().toString();
        int previousValue;
        if (previousVal.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousVal);
        }
        quant.setText(String.valueOf(previousValue + 1));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentItemId == 0) {
            MenuItem deleteOne = menu.findItem(R.id.action_delete_item);
            MenuItem deleteAll = menu.findItem(R.id.action_delete_all_data);
            MenuItem orderMenuItem = menu.findItem(R.id.action_order);
            deleteOne.setVisible(false);
            deleteAll.setVisible(false);
            orderMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (!addItemToDb()) {
                    return true;
                }
                finish();
                return true;

            case android.R.id.home:
                if (!infoItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                            }
                        };
                showUnsavedChangesDialogBox(discardButtonClickListener);
                return true;

            case R.id.action_order:
                showOrderConfirmationDialogBox();
                return true;

            case R.id.action_delete_item:
                showDeleteConfirmationDialogBox(currentItemId);
                return true;

            case R.id.action_delete_all_data:
                showDeleteConfirmationDialogBox(0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean addItemToDb() {
        boolean Ok = true;
        if (!checkIfValueSet(name, "name")) {
            Ok = false;
        }
        if (!checkIfValueSet(price, "price")) {
            Ok = false;
        }
        if (!checkIfValueSet(quant, "quantity")) {
            Ok = false;
        }
        if (!checkIfValueSet(supplierName, "supplier name")) {
            Ok = false;
        }
        if (!checkIfValueSet(supplierPhone, "supplier phone")) {
            Ok = false;
        }
        if (!checkIfValueSet(supplierEmail, "supplier email")) {
            Ok = false;
        }
        if (!Ok) {
            return false;
        }

        if (currentItemId == 0) {
            InventoryItem item = new InventoryItem(
                    name.getText().toString().trim(),
                    price.getText().toString().trim(),
                    Integer.parseInt(quant.getText().toString().trim()),
                    supplierName.getText().toString().trim(),
                    supplierPhone.getText().toString().trim(),
                    supplierEmail.getText().toString().trim());
            dbHelper.insertItem(item);
        } else {
            int quantity = Integer.parseInt(quant.getText().toString().trim());
            dbHelper.updateItem(currentItemId, quantity);
        }
        return true;
    }

    private boolean checkIfValueSet(EditText text, String description) {
        if (TextUtils.isEmpty(text.getText())) {
            text.setError("Missing " + description);
            return false;
        } else {
            text.setError(null);
            return true;
        }
    }

    private void addValuesToEditItem(long itemId) {
        Cursor cursor = dbHelper.readItem(itemId);
        cursor.moveToFirst();
        name.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.StockEntry.COLUMN_NAME)));
        price.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.StockEntry.COLUMN_PRICE)));
        quant.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.StockEntry.COLUMN_QUANTITY)));
        supplierName.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.StockEntry.COLUMN_SUPPLIER_NAME)));
        supplierPhone.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.StockEntry.COLUMN_SUPPLIER_PHONE)));
        supplierEmail.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.StockEntry.COLUMN_SUPPLIER_EMAIL)));
        name.setEnabled(false);
        price.setEnabled(false);
        supplierName.setEnabled(false);
        supplierPhone.setEnabled(false);
        supplierEmail.setEnabled(false);
    }

    private void showOrderConfirmationDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.order_message);
        builder.setPositiveButton(R.string.phone, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + supplierPhone.getText().toString().trim()));
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.email, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + supplierEmail.getText().toString().trim()));
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "New order");
                String bodyMessage = "Need More Items" +
                        name.getText().toString().trim();
                intent.putExtra(android.content.Intent.EXTRA_TEXT, bodyMessage);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private int deleteAllRowsFromTable() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return database.delete(InventoryContract.StockEntry.TABLE_NAME, null, null);
    }

    private int deleteOneItemFromTable(long itemId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String selection = InventoryContract.StockEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(itemId)};
        int rowsDeleted = database.delete(
                InventoryContract.StockEntry.TABLE_NAME, selection, selectionArgs);
        return rowsDeleted;
    }

    private void showDeleteConfirmationDialogBox(final long itemId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (itemId == 0) {
                    deleteAllRowsFromTable();
                } else {
                    deleteOneItemFromTable(itemId);
                }
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
