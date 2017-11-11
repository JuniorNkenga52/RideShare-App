package com.app.rideshare.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.app.rideshare.R;
import com.app.rideshare.adapter.ChooseGroupAdapter;
import com.app.rideshare.api.ApiServiceModule;
import com.app.rideshare.api.RestApiInterface;
import com.app.rideshare.api.response.GroupListResponce;
import com.app.rideshare.api.response.GroupResponce;
import com.app.rideshare.api.response.SignupResponse;
import com.app.rideshare.model.ChooseGroupModel;
import com.app.rideshare.notification.GCMRegistrationIntentService;
import com.app.rideshare.utils.AppUtils;
import com.app.rideshare.utils.PrefUtils;
import com.app.rideshare.utils.ToastUtils;
import com.app.rideshare.utils.TypefaceUtils;
import com.app.rideshare.view.CustomProgressDialog;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private CardView mFacebookCv;
    private CardView mGoogleCv;

    LoginButton loginButton;
    CallbackManager callbackManager;

    private static final int RC_SIGN_IN = 101;
    private GoogleApiClient mGoogleApiClient;

    private Typeface mRobotoMediam;

    private EditText mEmailEt;
    private EditText mPasswordEt;

    private TextView mLoginTv;
    private TextView mForgotPasswordTv;
    private TextView mSignUpTv;


    CustomProgressDialog mProgressDialog;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    String token;

    Spinner mchoose_group;
    ChooseGroupAdapter chooseGroupAdapter;
    ArrayList<ChooseGroupModel> listgroup = new ArrayList<>();
    ChooseGroupModel chooseGroupModel;
    Button create_group;
    PopupWindow popupWindow;
    private NiftyDialogBuilder dialogBuilder;
    String[] groupname = {"Choose Group", "Abc", "Pqr"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        PrefUtils.initPreference(this);


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {


            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)) {
                    token = intent.getStringExtra("token");
                    Log.d("token", token);
                } else if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)) {
                } else {
                }
            }
        };

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (ConnectionResult.SUCCESS != resultCode) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());
            } else {
            }
        } else {
            Intent itent = new Intent(this, GCMRegistrationIntentService.class);
            startService(itent);
        }

        mProgressDialog = new CustomProgressDialog(this);

        mRobotoMediam = TypefaceUtils.getTypefaceRobotoMediam(this);

        mEmailEt = (EditText) findViewById(R.id.username_et);
        mEmailEt.setTypeface(mRobotoMediam);
        mEmailEt.setText("");
        mPasswordEt = (EditText) findViewById(R.id.password_et);
        mLoginTv = (TextView) findViewById(R.id.login_tv);
        mForgotPasswordTv = (TextView) findViewById(R.id.forgot_password_tv);
        mSignUpTv = (TextView) findViewById(R.id.signup_tv);


        create_group = (Button) findViewById(R.id.create_group);
        create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();

            }
        });
        get_group_list_data();
        mchoose_group = (Spinner) findViewById(R.id.choose_group);
        chooseGroupAdapter = new ChooseGroupAdapter(this, listgroup);
        mchoose_group.setAdapter(chooseGroupAdapter);
        //mchoose_group.getSelectedItem().toString();
        mchoose_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView userid = (TextView) view.findViewById(R.id.txt_choose_group);
                if (listgroup.size() > 0) {
                    mchoose_group.setSelection(position);
                    mEmailEt.setText(chooseGroupModel.getGroup_name());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mPasswordEt.setTypeface(mRobotoMediam);
        mLoginTv.setTypeface(mRobotoMediam);
        mForgotPasswordTv.setTypeface(mRobotoMediam);
        mSignUpTv.setTypeface(mRobotoMediam);

        mSignUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(i);
                finish();
            }
        });

        mFacebookCv = (CardView) findViewById(R.id.facebook_card);
        mGoogleCv = (CardView) findViewById(R.id.google_cv);

        mFacebookCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.performClick();
            }
        });

        mGoogleCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_sign_in_key))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                try {
                                    Log.d("email", object.getString("email"));
                                    Log.d("id", object.getString("id"));
                                    Log.d("name", object.getString("name"));

                                    String mEmail = object.getString("email");
                                    String mId = object.getString("id");
                                    String mName = object.getString("name");

                                    try {
                                        String name[] = mName.split(" ");
                                        String mFiratName = name[0];
                                        String mLastName = name[1];

                                        loginfacebookuser(mId, mEmail, mFiratName, mLastName,String.valueOf(listgroup.get(1).getId()));

                                    } catch (Exception e) {

                                        loginfacebookuser(mId, mEmail, mName, mName,String.valueOf(listgroup.get(1).getId()));
                                    }

                                } catch (Exception e) {

                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d("error", "" + error);
            }
        });

        mLoginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmailEt.getText().toString().isEmpty()) {
                    ToastUtils.showShort(LoginActivity.this, "Please enter mobile number or email");
                } else if (mPasswordEt.getText().toString().isEmpty()) {
                    ToastUtils.showShort(LoginActivity.this, "Please enter password.");
                } else {
                    loginuser(mEmailEt.getText().toString(), mPasswordEt.getText().toString(), String.valueOf(mchoose_group.getSelectedItemId()));
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.d("email", acct.getEmail());
            Log.d("id", acct.getId());
            Log.d("name", acct.getDisplayName());
            Log.d("name", acct.getGivenName());

            logingoogle(acct.getId(), acct.getEmail(), acct.getDisplayName(), acct.getGivenName(),String.valueOf(listgroup.get(1).getId()));

        } else {
            Log.d("faile", "faile");
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("connection failed", connectionResult.getErrorMessage());
    }


    private void loginuser(final String mEmail, final String password, final String group_id) {
        mProgressDialog.show();
        ApiServiceModule.createService(RestApiInterface.class).login(mEmail, password, token, group_id).enqueue(new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!response.body().getmStatus().equals("error")) {
                        PrefUtils.addUserInfo(response.body().getMlist().get(0));
                        PrefUtils.putBoolean("islogin", true);
                        PrefUtils.putBoolean("isFriends", true);

                        PrefUtils.putString("loginwith", "normal");
                        PrefUtils.putString("gemail", mEmail);
                        PrefUtils.putString("gId", password);
                        PrefUtils.putString("group_id", group_id);

                        if (PrefUtils.getUserInfo().getmMobileNo().equals("")) {
                            Intent i = new Intent(getBaseContext(), MobileNumberActivity.class);
                            startActivity(i);
                            finish();
                        } else if (PrefUtils.getUserInfo().getmIsVerify().equals("0")) {
                            Intent i = new Intent(getBaseContext(), VerifyMobileNumberActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Intent i = new Intent(getBaseContext(), RideTypeActivity.class);
                            startActivity(i);
                            finish();
                        }
                    } else {
                        ToastUtils.showShort(LoginActivity.this, response.body().getmMessage());
                    }
                } else {

                }
                mProgressDialog.cancel();
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                t.printStackTrace();
                Log.d("error", t.toString());
                mProgressDialog.cancel();
            }
        });
    }


    private void loginfacebookuser(final String mFacebookId, final String mEmail, final String mFirstName, final String mLastName, final String group_id) {
        mProgressDialog.show();
        ApiServiceModule.createService(RestApiInterface.class).signfacebook(mFacebookId, mEmail, mFirstName, mLastName, token,group_id).enqueue(new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!response.body().getmStatus().equals("error")) {
                        PrefUtils.addUserInfo(response.body().getMlist().get(0));
                        PrefUtils.putBoolean("isFriends", true);
                        PrefUtils.putBoolean("islogin", true);

                        PrefUtils.putString("loginwith", "facebook");
                        PrefUtils.putString("gemail", mEmail);
                        PrefUtils.putString("gId", mFacebookId);
                        PrefUtils.putString("gfname", mFirstName);
                        PrefUtils.putString("glast", mLastName);

                        if (PrefUtils.getUserInfo().getmMobileNo().equals("")) {
                            Intent i = new Intent(getBaseContext(), MobileNumberActivity.class);
                            startActivity(i);
                            finish();
                        } else if (PrefUtils.getUserInfo().getmIsVerify().equals("0")) {
                            Intent i = new Intent(getBaseContext(), VerifyMobileNumberActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Intent i = new Intent(getBaseContext(), RideTypeActivity.class);
                            startActivity(i);
                            finish();
                        }

                    } else {
                        ToastUtils.showShort(LoginActivity.this, response.body().getmMessage());
                    }
                } else {

                }
                mProgressDialog.cancel();
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                t.printStackTrace();
                Log.d("error", t.toString());
                mProgressDialog.cancel();
            }
        });
    }


    private void logingoogle(final String mGoogleId, final String mEmail, final String mFirstName, final String mLastName, final String group_id) {
        mProgressDialog.show();
        ApiServiceModule.createService(RestApiInterface.class).signGoogleplus(mGoogleId, mEmail, mFirstName, mLastName, token,group_id).enqueue(new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!response.body().getmStatus().equals("error")) {
                        PrefUtils.addUserInfo(response.body().getMlist().get(0));
                        PrefUtils.putBoolean("islogin", true);
                        PrefUtils.putBoolean("isFriends", true);

                        PrefUtils.putString("loginwith", "google");
                        PrefUtils.putString("gemail", mEmail);
                        PrefUtils.putString("gId", mGoogleId);
                        PrefUtils.putString("gfname", mFirstName);
                        PrefUtils.putString("glast", mLastName);

                        if (PrefUtils.getUserInfo().getmMobileNo().equals("")) {
                            Intent i = new Intent(getBaseContext(), MobileNumberActivity.class);
                            startActivity(i);
                            finish();
                        } else if (PrefUtils.getUserInfo().getmIsVerify().equals("0")) {
                            Intent i = new Intent(getBaseContext(), VerifyMobileNumberActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Intent i = new Intent(getBaseContext(), RideTypeActivity.class);
                            startActivity(i);
                            finish();
                        }
                    } else {
                        ToastUtils.showShort(LoginActivity.this, response.body().getmMessage());
                    }
                } else {

                }
                mProgressDialog.cancel();
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                t.printStackTrace();
                Log.d("error", t.toString());
                mProgressDialog.cancel();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("MainActivity", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w("MainActivity", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    public void showPopup1() {
        dialogBuilder = NiftyDialogBuilder.getInstance(this);
        dialogBuilder.withTitleColor(getResources().getColor(R.color.colorPrimaryDark))
                .withTitle(getString(R.string.create_group_text)).withMessage(null)
                .withDividerColor(getResources().getColor(R.color.colorPrimary))
                .withDialogColor(getResources().getColor(R.color.TransWhite)).withDuration(200)
                .withEffect(Effectstype.SlideBottom).isCancelableOnTouchOutside(false)
                .setCustomView(R.layout.popup_group_layout, this);

        final EditText edt_grp_name = (EditText) dialogBuilder.findViewById(R.id.edt_group_name);
        final EditText edt_group_email_id = (EditText) dialogBuilder.findViewById(R.id.edt_group_email_id);

        TextView btn_create = (TextView) dialogBuilder.findViewById(R.id.btn_create);
        TextView btn_cancel = (TextView) dialogBuilder.findViewById(R.id.btn_cancel);


        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String group_name = edt_grp_name.getText().toString();
                String group_email = edt_group_email_id.getText().toString();
                /*chooseGroupModel = new ChooseGroupModel(2, group_name);
                listgroup.add(chooseGroupModel);*/
                create_group(group_name, group_email);
                dialogBuilder.dismiss();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.cancel();
            }
        });
        dialogBuilder.show();
    }

    @Override
    public void onClick(View v) {

    }

    private void create_group(final String group_name, final String group_email) {
        mProgressDialog.show();
        ApiServiceModule.createService(RestApiInterface.class).creategroup(group_name, group_email).enqueue(new Callback<GroupResponce>() {
            @Override
            public void onResponse(Call<GroupResponce> call, Response<GroupResponce> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!response.body().getStatus().equals("error")) {

                        PrefUtils.putString("group_email", group_email);
                        PrefUtils.putString("group_name", group_name);

                        ToastUtils.showShort(LoginActivity.this, response.body().getMessage());
                    } else {
                        ToastUtils.showShort(LoginActivity.this, response.body().getMessage());
                    }
                } else {

                }
                mProgressDialog.cancel();
            }

            @Override
            public void onFailure(Call<GroupResponce> call, Throwable t) {
                t.printStackTrace();
                Log.d("error", t.toString());
                mProgressDialog.cancel();
            }
        });
    }


    public void showPopup() {
        final Dialog dialog = new Dialog(this);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        window.setAttributes(wlp);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.popup_group_layout);


        CardView mllCustomDialogError = (CardView) dialog.findViewById(R.id.card_view_group);
        mllCustomDialogError.setLayoutParams(new LinearLayout.LayoutParams(
                (int) (AppUtils.getDeviceWidth(this) / 1.1),
                LinearLayout.LayoutParams.WRAP_CONTENT));

        final EditText edt_grp_name = (EditText) dialog.findViewById(R.id.edt_group_name);
        final EditText edt_group_email_id = (EditText) dialog.findViewById(R.id.edt_group_email_id);


        TextView btn_create = (TextView) dialog.findViewById(R.id.btn_create);
        TextView btn_cancel = (TextView) dialog.findViewById(R.id.btn_cancel);

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String group_name = edt_grp_name.getText().toString();
                String group_email = edt_group_email_id.getText().toString();
                create_group(group_name, group_email);
                dialog.dismiss();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void get_group_list_data() {
        mProgressDialog.show();
        ApiServiceModule.createService(RestApiInterface.class).getgrouplist().enqueue(new Callback<GroupListResponce>() {
            @Override
            public void onResponse(Call<GroupListResponce> call, Response<GroupListResponce> response) {
                if (response.isSuccessful() && response.body() != null) {

                    if (response.body().getResult().size() == 0) {
                        ToastUtils.showShort(LoginActivity.this, "Problem in Retriving data");
                    } else {
                        //ToastUtils.showShort(LoginActivity.this, "Data Received.!");
                        listgroup.clear();
                        for (int i = 0; i < response.body().getResult().size(); i++) {
                            int groupid = response.body().getResult().get(i).getId();
                            String groupname = response.body().getResult().get(i).getGroup_name();
                            chooseGroupModel = new ChooseGroupModel(groupid, groupname);
                            listgroup.add(chooseGroupModel);
                        }
                    }
                } else {

                }
                mProgressDialog.cancel();
            }

            @Override
            public void onFailure(Call<GroupListResponce> call, Throwable t) {
                t.printStackTrace();
                mProgressDialog.cancel();
            }
        });
    }
}
