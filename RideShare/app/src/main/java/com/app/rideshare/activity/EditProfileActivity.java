package com.app.rideshare.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.rideshare.R;
import com.app.rideshare.api.ApiServiceModule;
import com.app.rideshare.api.RestApiInterface;
import com.app.rideshare.api.RideShareApi;
import com.app.rideshare.api.response.SignupResponse;
import com.app.rideshare.model.User;
import com.app.rideshare.utils.AppUtils;
import com.app.rideshare.utils.PrefUtils;
import com.app.rideshare.utils.ToastUtils;
import com.app.rideshare.view.CustomProgressDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.tangxiaolv.telegramgallery.GalleryActivity;
import com.tangxiaolv.telegramgallery.GalleryConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EditProfileActivity extends AppCompatActivity {
    private ImageView mBackIv;
    private TextView mSaveTv;
    private TextView mUserNameTv;

    //private Typeface mRobotoMedium;

    User mUserBean;


    private EditText mFirstNameEt;
    private EditText mLastNameEt;
    private EditText mMobileEt;
    private EditText mEmailEt;
    private EditText mAddressEt;
    private EditText mVhmodel_Et;
    private EditText mVhtype_Et;
    private EditText mMaxpassenger_Et;
    private CheckBox mReqdriver_Ch;
    private LinearLayout layout_req_driver;
    private LinearLayout layout_other_op;

    private CircularImageView mProfileIv;
    //ArrayList<Image> images;

    private static final int REQUEST_CODE_CHOOSE = 101;
    CustomProgressDialog mProgressDialog;
    private int ch_val = 0;
    Activity activity;

    Uri imageUri = null;

    private int PICK_CAMERA = 1;
    private int PICK_GALLERY = 2;
    String imagePath="";
    //Button mprivileges;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_new);

        PrefUtils.initPreference(this);

        activity = this;
        mUserBean = PrefUtils.getUserInfo();
        mProgressDialog = new CustomProgressDialog(this);

        //mRobotoMedium= TypefaceUtils.getTypefaceRobotoMediam(this);
        mBackIv = (ImageView) findViewById(R.id.back_iv);
        mBackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mSaveTv = (TextView) findViewById(R.id.save_tv);
        //mSaveTv.setTypeface(mRobotoMedium);
        mSaveTv.setVisibility(View.INVISIBLE);

        mSaveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //updateProfile();
                if (mFirstNameEt.getText().toString().isEmpty()) {
                    ToastUtils.showShort(EditProfileActivity.this, "Please enter First Name");
                } else if (mLastNameEt.getText().toString().isEmpty()) {
                    ToastUtils.showShort(EditProfileActivity.this, "Please enter Last Name");
                } else if (mAddressEt.getText().toString().isEmpty()) {
                    ToastUtils.showShort(EditProfileActivity.this, "Please enter Home Address");
                } else if (!AppUtils.isEmail(mEmailEt.getText().toString()) || mEmailEt.getText().toString().isEmpty()) {
                    ToastUtils.showShort(EditProfileActivity.this, "Please enter valid Email Address");
                } else {
                    new AsyncUpdateUserProfile().execute();
                }

            }
        });
        // else if (!AppUtils.isEmail(email) || email.isEmpty())
        mUserNameTv = (TextView) findViewById(R.id.username_tv);
        // mUserNameTv.setTypeface(mRobotoMedium);
        mUserNameTv.setText(mUserBean.getmFirstName() + " " + mUserBean.getmLastName());

        mFirstNameEt = (EditText) findViewById(R.id.first_name_et);
        mLastNameEt = (EditText) findViewById(R.id.last_name_et);
        mMobileEt = (EditText) findViewById(R.id.mobile_et);
        mEmailEt = (EditText) findViewById(R.id.email_et);
        mAddressEt = (EditText) findViewById(R.id.address_name_et);

        mVhmodel_Et = (EditText) findViewById(R.id.vhmodel_et);
        mVhtype_Et = (EditText) findViewById(R.id.vhtype_et);
        mMaxpassenger_Et = (EditText) findViewById(R.id.maxpassenger_et);
        mReqdriver_Ch = (CheckBox) findViewById(R.id.reqdriver_ch);
        layout_req_driver = (LinearLayout) findViewById(R.id.layout_req_driver);
        layout_other_op = (LinearLayout) findViewById(R.id.layout_other_op);

        /*mFirstNameEt.setTypeface(mRobotoMedium);
        mLastNameEt.setTypeface(mRobotoMedium);
        mMobileEt.setTypeface(mRobotoMedium);
        mEmailEt.setTypeface(mRobotoMedium);

        mVhmodel_Et.setTypeface(mRobotoMedium);
        mVhtype_Et.setTypeface(mRobotoMedium);
        mMaxpassenger_Et.setTypeface(mRobotoMedium);*/

        mFirstNameEt.setText(mUserBean.getmFirstName());
        mLastNameEt.setText(mUserBean.getmLastName());
        mMobileEt.setText(mUserBean.getmMobileNo());
        mEmailEt.setText(mUserBean.getmEmail());
        mAddressEt.setText(mUserBean.getmAddress());

        mVhmodel_Et.setText(mUserBean.getMvehicle_model());
        mVhtype_Et.setText(mUserBean.getMvehicle_type());
        mMaxpassenger_Et.setText(mUserBean.getmMax_passengers());

        if (mUserBean.getMvehicle_model() != null) {
            if (mUserBean.getMvehicle_model().length() != 0) {
                mReqdriver_Ch.setChecked(true);
                layout_other_op.setVisibility(View.VISIBLE);
            }

        }

        /*if (mUserBean.getMrequested_as_driver() != null) {
            if (mUserBean.getMrequested_as_driver().equals("1")) {
                mReqdriver_Ch.setChecked(true);
                layout_other_op.setVisibility(View.VISIBLE);
            }

        }*/
        /*mprivileges= (Button) findViewById(R.id.btn_privileges);
        mprivileges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, DriverPrivileges.class);

                startActivity(intent);
            }
        });*/
        mReqdriver_Ch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ch_val = 1;
                    layout_other_op.setVisibility(View.VISIBLE);
                } else {
                    ch_val = 0;
                    layout_other_op.setVisibility(View.GONE);
                }
                mSaveTv.setVisibility(View.VISIBLE);
            }
        });


        mFirstNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mLastNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mAddressEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mMobileEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEmailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mVhmodel_Et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mVhtype_Et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mMaxpassenger_Et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mProfileIv = (CircularImageView) findViewById(R.id.circularImageView);

        if (!PrefUtils.getUserInfo().getProfile_image().equals("")) {
            /*Picasso.with(this)
                    .load(mUserBean.getProfile_image())
                    .resize(300, 300)
                    .rotate(90)
                    .centerCrop().into(mProfileIv);*/
            mProgressDialog.show();
            Glide.with(activity)
                    .load(mUserBean.getProfile_image())
                    .crossFade()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            mProgressDialog.dismiss();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            mProgressDialog.dismiss();
                            return false;
                        }
                    })
                    .into(mProfileIv);
        }


        mProfileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new TedPermission(EditProfileActivity.this)
                        .setPermissionListener(permissionlistener)
                        .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                        .check();

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        RideShareApp.mHomeTabPos = 4;

        Intent i = new Intent(EditProfileActivity.this, HomeNewActivity.class);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();

    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            /*ImagePicker.create(EditProfileActivity.this)
                    .folderMode(true)
                    .folderTitle("Folder")
                    .imageTitle("Tap to select")
                    .single()
                    .showCamera(true)
                    .imageDirectory("Camera")
                    .start(REQUEST_CODE_CHOOSE);*/
            selectImage();

        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {

        }
    };

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library"};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {

                    String fileName = "Camera_Example.jpg";

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, fileName);
                    values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");

                    imageUri = activity.getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    startActivityForResult(intent, PICK_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    GalleryConfig config = new GalleryConfig.Build()
                            .limitPickPhoto(1)
                            .singlePhoto(false)
                            .filterMimeTypes(new String[]{"image/gif"})
                            .hintOfPick("You can select max 1 photos")
                            .build();
                    GalleryActivity.openActivity(activity, PICK_GALLERY, config);
                }
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            try {
                images = data.getParcelableArrayListExtra(ImagePickerActivity.INTENT_EXTRA_SELECTED_IMAGES);
                mProfileIv.setImageURI(Uri.parse(images.get(0).getPath()));
                mSaveTv.setVisibility(View.VISIBLE);
            } catch (Exception e) {

            }
        }*/
        mSaveTv.setVisibility(View.VISIBLE);
        if (PICK_GALLERY == requestCode && resultCode == Activity.RESULT_OK) {
            ArrayList<String> photos = (ArrayList<String>) data.getSerializableExtra(GalleryActivity.PHOTOS);

            imagePath = photos.get(0);
            Uri uri = Uri.fromFile(new File(photos.get(0)));
            /*Picasso.with(activity)
                    .load(photos.get(0))
                    .into(mProfileIv);*/
            Glide.with(this).load(uri)
                    .error(R.drawable.icon_test)
                    .into(mProfileIv);
            /*Picasso.with(activity)
                    .load( uri)
                    .resize(300, 300)
                    .centerCrop()
                    .rotate(90)
                    .into(mProfileIv);*/

        } else if (PICK_CAMERA == requestCode && resultCode == Activity.RESULT_OK) {


            imagePath = convertImageUriToFile(imageUri, activity);
            //SignUpActivity.ProfilePhotoPath =  convertImageUriToFile(imageUri, activity);
            Glide.with(this)
                    .load("file://" + imagePath)
                    .error(R.drawable.icon_test)
                    .into(mProfileIv);

            /*Picasso.with(activity)
                    .load("file://" + imagePath)
                    .resize(300, 300)
                    .centerCrop()
                    .rotate(90)
                    .into(mProfileIv);*/
        }
    }

    public String convertImageUriToFile(Uri imageUri, Activity activity) {

        Cursor cursor = null;
        String Path = "";
        try {
            String[] proj = {
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Thumbnails._ID,
                    MediaStore.Images.ImageColumns.ORIENTATION
            };

            cursor = activity.getContentResolver().query(

                    imageUri,         //  Get data for specific image URI
                    proj,             //  Which columns to return
                    null,             //  WHERE clause; which rows to return (all rows)
                    null,             //  WHERE clause selection arguments (none)
                    null              //  Order-by clause (ascending by name)

            );

            int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            int size = cursor.getCount();

            if (size == 0) {
            } else {
                if (cursor.moveToFirst()) {
                    Path = cursor.getString(file_ColumnIndex);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return Path;
    }

    private void updateProfile() {
        mProgressDialog.show();
        RequestBody mfirstname = RequestBody.create(MultipartBody.FORM, mFirstNameEt.getText().toString());
        RequestBody mlatname = RequestBody.create(MultipartBody.FORM, mLastNameEt.getText().toString());
        RequestBody mMobile = RequestBody.create(MultipartBody.FORM, mMobileEt.getText().toString());
        RequestBody mUserId = RequestBody.create(MultipartBody.FORM, mUserBean.getmUserId());
        RequestBody mEmail = RequestBody.create(MultipartBody.FORM, mEmailEt.getText().toString());
        RequestBody mVh_Model = RequestBody.create(MultipartBody.FORM, mVhmodel_Et.getText().toString());
        RequestBody mVh_Type = RequestBody.create(MultipartBody.FORM, mVhtype_Et.getText().toString());
        RequestBody mMax_Passengers = RequestBody.create(MultipartBody.FORM, mMaxpassenger_Et.getText().toString());
        RequestBody mGroupid = RequestBody.create(MultipartBody.FORM, mUserBean.getmGroup_id());
        RequestBody mReq_driver = RequestBody.create(MultipartBody.FORM, String.valueOf(ch_val));

        RequestBody requestFile = null;
        MultipartBody.Part body = null;

        /*if (images != null) {
            requestFile = RequestBody.create(RestApiInterface.MULTIPART, new File(images.get(0).getPath()));
            body = MultipartBody.Part.createFormData("profile_image", images.get(0).getName(), requestFile);
        }*/

        ApiServiceModule.createService(RestApiInterface.class).updateProfile(mUserId, mfirstname, mlatname, mMobile, body, mEmail, mVh_Model, mVh_Type, mMax_Passengers, mReq_driver, mGroupid).enqueue(new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                mProgressDialog.cancel();
                if (response.isSuccessful() && response.body() != null) {
                    PrefUtils.addUserInfo(response.body().getMlist().get(0));
                    ToastUtils.showShort(EditProfileActivity.this, response.body().getmMessage());
                    finish();
                }
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                t.printStackTrace();
                Log.d("error", t.toString());
                mProgressDialog.cancel();
            }
        });
    }

    private class AsyncUpdateUserProfile extends AsyncTask<Objects, Void, String> {

        CustomProgressDialog mProgressDialog;
        String mVh_Model = "";
        String mVh_Type = "";
        String mMax_Passengers = "";
        String mReq_driver = "";

        public AsyncUpdateUserProfile() {

            mProgressDialog = new CustomProgressDialog(EditProfileActivity.this);
            mProgressDialog.show();

            /*if (images != null) {
                imagePath = images.get(0).getPath();
                //requestFile = RequestBody.create(RestApiInterface.MULTIPART, new File(images.get(0).getPath()));
                //body = MultipartBody.Part.createFormData("profile_image", images.get(0).getName(), requestFile);
            }*/

            if (ch_val == 1) {
                mVh_Model = mVhmodel_Et.getText().toString();
                mVh_Type = mVhtype_Et.getText().toString();
                mMax_Passengers = mMaxpassenger_Et.getText().toString();
                mReq_driver = String.valueOf(ch_val);
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(Objects... param) {
            try {
                return RideShareApi.updateProfileNew(mUserBean.getmUserId(), mFirstNameEt.getText().toString(),
                        mLastNameEt.getText().toString(), mAddressEt.getText().toString(),
                        mEmailEt.getText().toString(), imagePath,
                        mVh_Model, mVh_Type, mMax_Passengers);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            mProgressDialog.dismiss();

            try {

                if (result != null) {

                    JSONObject jObj = new JSONObject(result);

                    if (jObj.getString("status").equals("success")) {

                        JSONArray jArrayResult = new JSONArray(jObj.getString("result"));

                        JSONObject jObjResult = jArrayResult.getJSONObject(0);

                        User beanUser = new User();

                        beanUser.setmUserId(jObjResult.getString("u_id"));
                        beanUser.setmGroup_id(jObjResult.getString("group_id"));
                        beanUser.setmFirstName(jObjResult.getString("u_firstname"));
                        beanUser.setmLastName(jObjResult.getString("u_lastname"));
                        beanUser.setmEmail(jObjResult.getString("u_email"));
                        beanUser.setmDescription(jObjResult.getString("description"));
                        beanUser.setmAddress(jObjResult.getString("address"));
                        beanUser.setProfile_image(jObjResult.getString("profile_image"));
                        beanUser.setmMobileNo(jObjResult.getString("u_mo_number"));
                        beanUser.setmLatitude(jObjResult.getString("u_lat"));
                        beanUser.setmLongitude(jObjResult.getString("u_long"));
                        beanUser.setMu_type(jObjResult.getString("u_type"));
                        beanUser.setMtoken(jObjResult.getString("token"));
                        beanUser.setmMobileNumber(jObjResult.getString("mobile_verify_number"));
                        beanUser.setmIsVerify(jObjResult.getString("verify_mobile"));
                        beanUser.setmRideType(jObjResult.getString("u_ride_type"));
                        beanUser.setmStatus(jObjResult.getString("u_status"));
                        beanUser.setmRidestatus(jObjResult.getString("ride_status"));
                        beanUser.setContact_sync(jObjResult.getString("contact_sync"));
                        beanUser.setmIs_rider(jObjResult.getString("is_rider"));
                        beanUser.setmUpdatedDate(jObjResult.getString("update_date"));
                        beanUser.setmCreatedDate(jObjResult.getString("create_date"));

                        beanUser.setMvehicle_model(jObjResult.optString("vehicle_model"));
                        beanUser.setMvehicle_type(jObjResult.optString("vehicle_type"));
                        beanUser.setmMax_passengers(jObjResult.optString("max_passengers"));

                        PrefUtils.addUserInfo(beanUser);

                        RideShareApp.mHomeTabPos = 4;

                        Intent i = new Intent(EditProfileActivity.this, HomeNewActivity.class);
                        startActivity(i);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                        finishAffinity();

                    } else {
                        Toast.makeText(EditProfileActivity.this, "Please try again", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Please try again", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(EditProfileActivity.this, "Please try again", Toast.LENGTH_LONG).show();
            }
        }
    }

}
