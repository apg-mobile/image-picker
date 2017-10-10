package com.apg.library.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by siwarats on 10/10/2560.
 */

public class AlphaImagePicker {

    public static ArrayList<Uri> getResultFromIntent(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra("result")) {
            return data.getParcelableArrayListExtra("result");
        }
        return new ArrayList<>();
    }

    public static void startPickImage(Activity activity, int requestCode,
                                      boolean isMultipleSelected) {
//        Intent intent = new Intent(activity, PickerActivity.class);
//        intent.putExtra("isMultipleSelected", isMultipleSelected);
//        activity.startActivityForResult(intent, requestCode);
        Intent intent = new Intent("com.apg.library.imagepicker.PICKIMAGE");
        intent.addCategory("com.apg.library.imagepicker");
        intent.putExtra("isMultipleSelected", isMultipleSelected);
        activity.startActivityForResult(intent, requestCode);
    }
}
