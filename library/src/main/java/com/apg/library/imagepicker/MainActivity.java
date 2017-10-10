package com.apg.library.imagepicker;

import android.Manifest;
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

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 15200;

    private ArrayList<Uri> images = new ArrayList<>();
    private ArrayList<Integer> selected = new ArrayList<>();
    private GridView gridView;
    private boolean isNeverShowAgain = false;
    private boolean isMultipleSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = (GridView) findViewById(R.id.grid);
        isMultipleSelected = getIntent().getBooleanExtra("isMultipleSelected", false);

        gridView.postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaImagePicker.startPickImage(MainActivity.this, 11, false);
            }
        }, 5000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isMultipleSelected) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuOk) {
            ArrayList<Uri> results = new ArrayList<>();
            for (Integer position : selected) {
                results.add(images.get(position));
            }
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra("result", results);
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
        if(isMultipleSelected) {
            setTitle(getString(R.string.selected_item, selected.size()));
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(MainActivity.this, R.layout.listitem_image, null);
                new ViewHolder(view);
            }
            ViewHolder vh = (ViewHolder) view.getTag();
            vh.position = i;
            Picasso.with(MainActivity.this)
                    .load(getItem(i))
                    .fit()
                    .centerCrop()
                    .into(vh.imageView);
            boolean isSelected = selected.contains(i);
            vh.checkbox.setChecked(isSelected);
            vh.imageView.setScaleY(isSelected ? 0.6f : 1.0f);
            vh.imageView.setScaleX(isSelected ? 0.6f : 1.0f);
            return view;
        }

        class ViewHolder {
            private ImageView imageView;
            private CompoundButton checkbox;
            private int position = 0;

            ViewHolder(View itemView) {
                imageView = itemView.findViewById(R.id.imageview);
                checkbox = itemView.findViewById(R.id.checkbox);
                checkbox.setVisibility(isMultipleSelected ? View.VISIBLE : View.GONE);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (selected.contains(position)) {
                            Iterator<Integer> iterator = selected.iterator();
                            while (iterator.hasNext()) {
                                Integer next = iterator.next();
                                if (next == position) iterator.remove();
                            }
                        } else {
                            selected.add(position);
                            if (!isMultipleSelected) {
                                ArrayList<Uri> results = new ArrayList<>();
                                for (Integer position : selected) {
                                    results.add(images.get(position));
                                }
                                Intent intent = new Intent();
                                intent.putParcelableArrayListExtra("result", results);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                        notifyDataSetChanged();
                        setTitle(getString(R.string.selected_item, selected.size()));
                    }
                });
                itemView.setTag(this);
            }
        }
    }
}
