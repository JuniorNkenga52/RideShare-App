package com.app.rideshare.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.rideshare.R;
import com.app.rideshare.api.RideShareApi;
import com.app.rideshare.model.GroupList;
import com.app.rideshare.utils.AppUtils;
import com.app.rideshare.utils.PrefUtils;
import com.app.rideshare.view.CustomProgressDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class MyGroupSelectionActivity extends AppCompatActivity {

    ArrayList<GroupList> mListGroup = new ArrayList<>();
    ArrayList<GroupList> mSearchListGroup = new ArrayList<>();
    Context context;
    ListView mLvMyGroup;
    GroupAdapter groupAdapter;
    EditText tvSearchGroup;
    //String adminid;
    CustomProgressDialog mProgressDialog;
    TextView txtskip;
    SwipeRefreshLayout swipeRefreshRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_group_selection);
        context = this;
        PrefUtils.initPreference(this);
        mProgressDialog = new CustomProgressDialog(context);

        //adminid = PrefUtils.getString("AdminID");

        mLvMyGroup = findViewById(R.id.mLvMyGroup);
        txtskip = findViewById(R.id.txtskip);
        swipeRefreshRequests = findViewById(R.id.swipeRefreshRequests);

        new AsyncMyGroup().execute();

        tvSearchGroup = (EditText) findViewById(R.id.txtSearchGroup);

        tvSearchGroup.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                String text = tvSearchGroup.getText().toString().toLowerCase(Locale.getDefault());
                if (groupAdapter != null)
                    groupAdapter.filter(text.trim());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            }
        });
        txtskip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, RideTypeActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        swipeRefreshRequests.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (AppUtils.isInternetAvailable(context)) {
                    new AsyncMyGroup().execute();
                } else {
                    swipeRefreshRequests.setRefreshing(false);
                }
            }
        });
        swipeRefreshRequests.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
    }

    public class AsyncMyGroup extends AsyncTask<Object, Integer, Object> {

        //CustomProgressDialog mProgressDialog;

        AsyncMyGroup() {

            //mProgressDialog = new CustomProgressDialog(context);
            mProgressDialog.show();
            swipeRefreshRequests.setRefreshing(false);
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        public Object doInBackground(Object... params) {
            try {
                return RideShareApi.mygroups(PrefUtils.getUserInfo().getmUserId());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public void onPostExecute(Object result) {
            super.onPostExecute(result);

            mProgressDialog.dismiss();

            try {
                Log.e("AsyncAllGroup", "onPostExecute: result>> " + result.toString());
                JSONObject jsonObject = new JSONObject(result.toString());

                if (jsonObject.getString("status").equalsIgnoreCase("success")) {

                    JSONArray jArrayResult = new JSONArray(jsonObject.getString("result"));

                    mListGroup = new ArrayList<>();

                    if (jArrayResult.length() == 0) {
                        PrefUtils.putString("isBlank", "true");
                        Intent i = new Intent(MyGroupSelectionActivity.this, HomeNewActivity.class);
                        startActivity(i);
                    } else {
                        for (int i = 0; i < jArrayResult.length(); i++) {
                            JSONObject jObjResult = jArrayResult.getJSONObject(i);

                            GroupList bean = new GroupList();

                            bean.setId(jObjResult.getString("id"));
                            bean.setGroup_name(jObjResult.getString("group_name"));
                            bean.setGroup_description(jObjResult.getString("group_description"));
                            bean.setCategory_name(jObjResult.getString("category_name"));
                            bean.setCategory_image(jObjResult.getString("category_image"));
                            bean.setIs_joined(jObjResult.optString("is_joined"));
                            bean.setShareLink(jObjResult.optString("share_link"));
                            bean.setCategory_id(jObjResult.optString("category_id"));
                            bean.setIs_assigned(jObjResult.optString("is_assigned"));

                            mListGroup.add(bean);
                            if (bean.getIs_assigned().equals("1")) {
                                mListGroup.remove(i);
                                mListGroup.add(0, bean);
                            }
                        }
                        mSearchListGroup.clear();
                        mSearchListGroup.addAll(mListGroup);

                        groupAdapter = new GroupAdapter();
                        mLvMyGroup.setAdapter(groupAdapter);
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class GroupAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        private GroupAdapter() {

            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mSearchListGroup.size();
        }

        @Override
        public GroupList getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            ImageView imgGroup;
            TextView txtGroupName;
            TextView txtGroupDescription;
            TextView txtJoin;
        }


        @SuppressLint("InflateParams")
        @Override
        public View getView(final int pos, View vi, ViewGroup parent) {

            final GroupAdapter.ViewHolder holder;

            if (vi == null) {

                vi = mInflater.inflate(R.layout.item_group, null);

                holder = new GroupAdapter.ViewHolder();

                holder.imgGroup = vi.findViewById(R.id.imgGroup);

                holder.txtGroupName = vi.findViewById(R.id.txtGroupName);

                holder.txtGroupDescription = vi.findViewById(R.id.txtGroupDescription);

                holder.txtJoin = vi.findViewById(R.id.txtJoin);
                holder.txtJoin.setTextColor(Color.WHITE);
                holder.txtJoin.setText("Select");
                vi.setTag(holder);
            } else {
                holder = (GroupAdapter.ViewHolder) vi.getTag();
            }

            final GroupList bean = mSearchListGroup.get(pos);

            holder.txtGroupName.setText(bean.getGroup_name());
            holder.txtGroupDescription.setText(bean.getGroup_description());

            Picasso.with(context).load(bean.getCategory_image()).error(R.drawable.user_icon).into(holder.imgGroup);

            /*0 = None (Join)
            1 = Requested
            2 = Accept Request(Joined)
            3 = Decline
            4 = Confirm*/

            if (bean.getIs_assigned().equalsIgnoreCase("0")) {
                holder.txtJoin.setText("Select");
                holder.txtJoin.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.btn_join));
                holder.txtJoin.setTextColor(context.getResources().getColor(R.color.white));
            } else if (bean.getIs_assigned().equalsIgnoreCase("1")) {
                holder.txtJoin.setText("Selected");
                holder.txtJoin.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.btn_requested));
                holder.txtJoin.setTextColor(context.getResources().getColor(R.color.white));
                txtskip.setVisibility(View.VISIBLE);
            }

            holder.txtJoin.setTag(pos);
            holder.txtJoin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int poss = (int) v.getTag();
                    //PrefUtils.putString("MyID","");
                    new AsyncJoinGroup(poss).execute();
                }
            });

            return vi;
        }

        // Filter Class
        public void filter(String charText) {
            try {
                charText = charText.toLowerCase(Locale.getDefault());
                mSearchListGroup.clear();
                if (charText.length() == 0) {
                    mSearchListGroup.addAll(mListGroup);
                } else {
                    for (GroupList gp : mListGroup) {
                        if (gp.getGroup_name().toLowerCase(Locale.getDefault()).contains(charText)) {
                            mSearchListGroup.add(gp);
                        }
                    }
                }
                notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class AsyncJoinGroup extends AsyncTask<Object, Integer, Object> {

        //private CustomProgressDialog mProgressDialog;
        int poss;
        TextView txtJoin;

        AsyncJoinGroup(int poss) {
            //mProgressDialog = new CustomProgressDialog(context);
            this.poss = poss;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        public Object doInBackground(Object... params) {
            try {
                return RideShareApi.getUpdateUserGroup(PrefUtils.getUserInfo().getmUserId(), mSearchListGroup.get(poss).getId());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public void onPostExecute(Object result) {
            super.onPostExecute(result);
            try {

                JSONObject jsonObject = new JSONObject(result.toString());

                if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                    Intent i = new Intent(context, RideTypeActivity.class);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                    groupAdapter.notifyDataSetChanged();

                }
                mProgressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }
}