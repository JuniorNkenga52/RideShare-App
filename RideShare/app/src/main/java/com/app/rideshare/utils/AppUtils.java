package com.app.rideshare.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.app.rideshare.R;
import com.app.rideshare.model.ContactBean;
import com.app.rideshare.model.Rider;
import com.app.rideshare.model.User;
import com.app.rideshare.view.CustomProgressDialog;
import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.LOCATION_SERVICE;

public class AppUtils {

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static int getDeviceWidth(Context context) {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Point point = new Point();
            wm.getDefaultDisplay().getSize(point);
            return point.x;
        } else {
            return wm.getDefaultDisplay().getWidth();
        }
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected() && netInfo.isAvailable());
    }

    public static boolean isEmail(String email) {
        Pattern emailPattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        return emailPattern.matcher(email).find();
    }

    public static boolean isMobileNumber(String mno) {
        Pattern emailPattern = Pattern.compile("^[0-9]*$");
        return emailPattern.matcher(mno).find();
    }


    public static void showMessage(Activity activity, boolean isSuccess, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        //showCustomToast(activity_waiting, message, R.drawable.ic_camera);
    }

    public static int dp2px(int dp, Context ctx) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
    }

    public static ArrayList<ContactBean> readContacts(Context mContext) {
        ArrayList<ContactBean> mlist = new ArrayList<>();

        ContentResolver cr = mContext.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        String phone = null;


        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    ContactBean bean = new ContactBean();
                    bean.setName(name);

                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        bean.setMobile(phone);
                    }

                    mlist.add(bean);
                    pCur.close();
                }
            }
        }
        return mlist;
    }

    public static String dateformat(String time) {
        String inputPattern = "yyyy-MM-dd hh:mm:ss";
        String outputPattern = "EEE, dd MMM, yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static Bitmap getMarkerBitmapFromView(Activity activity, String userImage) {

        View customMarkerView = activity.getLayoutInflater().inflate(R.layout.item_custom_marker, null);

        CircleImageView markerImageView = customMarkerView.findViewById(R.id.user_dp);

        Glide.with(activity).load(userImage).error(R.drawable.icon_test).placeholder(R.drawable.icon_test).into(markerImageView);

        customMarkerView.setDrawingCacheEnabled(true);

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();

        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);

        Drawable drawable = customMarkerView.getBackground();

        if (drawable != null)
            drawable.draw(canvas);

        customMarkerView.draw(canvas);

        return returnedBitmap;
    }

    public static Bitmap getMarkerBitmapFromViewUser(Activity activity, User user) {

        View customMarkerView = activity.getLayoutInflater().inflate(R.layout.item_custom_marker, null);

        CircleImageView markerImageView = customMarkerView.findViewById(R.id.user_dp);

        Glide.with(activity).load(user.getProfile_image()).error(R.drawable.icon_test).placeholder(R.drawable.icon_test).into(markerImageView);

        customMarkerView.setDrawingCacheEnabled(true);

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();

        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);

        Drawable drawable = customMarkerView.getBackground();

        if (drawable != null)
            drawable.draw(canvas);

        customMarkerView.draw(canvas);

        return returnedBitmap;
    }

    public static void composeEmail(Context context, String subject, String shareData) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
//        intent.putExtra(Intent.EXTRA_EMAIL, addresses); String[] addresses,
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, shareData);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public static void showNoInternetAvailable(Activity activity) {
        Toast.makeText(activity, R.string.txt_msg_no_internet_available, Toast.LENGTH_SHORT).show();
    }

    public static  String downloadUrl(String strUrl,CustomProgressDialog progressDialog) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
            progressDialog.dismiss();
        }
        return data;
    }
}