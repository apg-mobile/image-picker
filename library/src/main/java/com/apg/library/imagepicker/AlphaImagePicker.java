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
        Intent intent = new Intent("com.apg.library.imagepicker.PICKIMAGE");
        intent.putExtra("isMultipleSelected", isMultipleSelected);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivityForResult(intent, requestCode);
    }
}
