package com.apg.library.imagepicker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class PickerActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 15200;

    private ArrayList<Uri> images = new ArrayList<>();
    private ArrayList<Uri> selected = new ArrayList<>();
    private GridView gridView;
    private boolean isNeverShowAgain = false;
    private boolean isMultipleSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AppCompat_Light);
        setContentView(R.layout.alphaimagepicker_activity_picker);
        setTitle(R.string.alphaimagepicker__app_name);
        gridView = (GridView) findViewById(R.id.grid);
        isMultipleSelected = getIntent().getBooleanExtra("isMultipleSelected", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isMultipleSelected) {
            getMenuInflater().inflate(R.menu.alphaimagepicker_menu_main, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuOk) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra("result", selected);
            setResult(RESULT_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                isNeverShowAgain = false;
                render();
            } else if (isNeverShowAgain) {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("Permission granted Fail")
                        .setMessage("The application would like to access storage. " +
                                "Please granted access storage permission by yourself in Setting")
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                finish();
                            }
                        })
                        .setPositiveButton("Go to Setting", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_STORAGE);
            }
        } else {
            isNeverShowAgain = false;
            render();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_STORAGE &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    isNeverShowAgain = !shouldShowRequestPermissionRationale(permission);
                }
            }
        }
    }

    private void render() {
        if (gridView.getAdapter() == null) {
            LocalAdapter adapter = new LocalAdapter();
            gridView.setAdapter(adapter);

            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    proj, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        images.add(Uri.fromFile(new File(cursor.getString(dataColumnIndex))));
                    }
                }
                cursor.close();
                adapter.notifyDataSetChanged();
            }
        }
        if (isMultipleSelected) {
            setTitle(getString(R.string.alphaimagepicker_selected_item, selected.size()));
        }
    }

    private class LocalAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Uri getItem(int i) {
            return images.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(PickerActivity.this, R.layout.alphaimagepicker_listitem_image, null);
                new ViewHolder(view);
            }
            Uri item = getItem(position);
            ViewHolder vh = (ViewHolder) view.getTag();
            vh.position = position;
            Picasso.with(PickerActivity.this)
                    .load(item)
                    .fit()
                    .centerCrop()
                    .into(vh.imageView);
            boolean isSelected = false;
            String sequence = "";
            for (int i = 0; i < selected.size(); i++) {
                if (selected.get(i) == item) {
                    isSelected = true;
                    sequence = (i + 1) + "";
                    break;
                }
            }
            vh.tvSequence.setText(sequence);
            vh.checkbox.setChecked(isSelected);
            vh.imageView.setScaleY(isSelected ? 0.6f : 1.0f);
            vh.imageView.setScaleX(isSelected ? 0.6f : 1.0f);
            return view;
        }

        class ViewHolder {
            private ImageView imageView;
            private CompoundButton checkbox;
            private TextView tvSequence;
            private int position = 0;

            ViewHolder(View itemView) {
                imageView = itemView.findViewById(R.id.imageview);
                tvSequence = itemView.findViewById(R.id.tvSequence);
                checkbox = itemView.findViewById(R.id.checkbox);
                checkbox.setVisibility(isMultipleSelected ? View.VISIBLE : View.GONE);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri item = images.get(position);
                        if (selected.contains(item)) {
                            selected.remove(item);
                        } else {
                            selected.add(item);
                            if (!isMultipleSelected) {
                                ArrayList<Uri> results = new ArrayList<>();
                                results.add(item);
                                Intent intent = new Intent();
                                intent.putParcelableArrayListExtra("result", results);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                        notifyDataSetChanged();
                        setTitle(getString(R.string.alphaimagepicker_selected_item, selected.size()));
                    }
                });
                itemView.setTag(this);
            }
        }
    }


    public static ArrayList<Uri> getResultFromIntent(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra("result")) {
            return data.getParcelableArrayListExtra("result");
        }
        return new ArrayList<>();
    }

    public static Intent createIntent(Context context, boolean isMultipleSelected) {
//        Intent intent = new Intent("com.apg.library.imagepicker.PICKIMAGE");
        Intent intent = new Intent(context, PickerActivity.class);
        intent.putExtra("isMultipleSelected", isMultipleSelected);
        return intent;
    }
}
