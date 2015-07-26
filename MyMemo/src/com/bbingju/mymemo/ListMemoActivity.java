/*
 * "THE BEER-WARE LICENSE"
 * 
 * <pjhwang@gmail.com> wrote this file.  As long as you retain this
 * notice you can do whatever you want with this stuff. If we meet
 * some day, and you think this stuff is worth it, you can buy me a
 * beer in return.
 *
 * - Byung Ju Hwang.
 */

package com.bbingju.mymemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

import java.util.List;

/**
 * 메모 리스트를 보여주는 activity. 저장된 메모를 리스트 형식으로
 * 화면에 표시함.
 * <a href="https://parse.com/docs/android/api/">Parse</a>를 사용하여
 * 메모를 저장하며 다음과 같은 기능을 가짐.
 *
 *  <ol>
 *  <li>Add/Edit Memo
 *  <li>Delete Memo
 *  <li>Log In
 *  <li>Log Out
 *  </ol>
 */
public class ListMemoActivity extends Activity {

    private static final int LOGIN_REQUEST_CODE = 100;
    private static final int EDIT_REQUEST_CODE = 200;

    private LayoutInflater inflater;
    private ParseQueryAdapter<Memo> memoListAdapter;

    private ListView memoListView;
    private LinearLayout noMemosView;
    private TextView loggedInInfoView;

    private ParseUser currentUser;

    /**
     * 화면을 구성하는 각종 View를 초기화하고, List에 사용할 Adapter를
     * 설정하며, 아이템 클릭 시 호출 될 listener를 설정함.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_list);

        noMemosView = (LinearLayout) findViewById(R.id.no_memos_view);
        memoListView = (ListView) findViewById(R.id.memo_list_view);
        memoListView.setEmptyView(noMemosView);
        loggedInInfoView = (TextView) findViewById(R.id.loggedin_info);

        ParseQueryAdapter.QueryFactory<Memo> factory = new ParseQueryAdapter.QueryFactory<Memo>() {
            public ParseQuery<Memo> create() {
                ParseQuery<Memo> query = Memo.getQuery();
                query.orderByDescending("createdAt");
                query.fromLocalDatastore();
                return query;
            }
        };

        // Set up the adapter
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        memoListAdapter = new MemoListAdapter(this, factory);

        ListView memoListView = (ListView) findViewById(R.id.memo_list_view);
        memoListView.setAdapter(memoListAdapter);
        registerForContextMenu(memoListView);

        memoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Memo memo = memoListAdapter.getItem(position);
                openEditView(memo);
            }
        });

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    /**
     * {@link #onCreate}가 실행된 후에 호출되며,
     * <code>ParseUser</code>를 객체로 현재 사용자를 받아옴.
     */
    @Override
    protected void onStart() {
        super.onStart();

        currentUser = ParseUser.getCurrentUser();
    }

    /**
     *
     */
    @Override
    protected void onResume() {

        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);

        currentUser = ParseUser.getCurrentUser();

        if (!ParseAnonymousUtils.isLinked(currentUser)) {
            syncMemosToParse();
            updateLoggedInfo();
        }
    }

    /**
     *
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    /**
     *
     */
    private void updateLoggedInfo() {
        if (!ParseAnonymousUtils.isLinked(currentUser)) {
            loggedInInfoView.setText(getString(R.string.logged_in, currentUser.getString("name")));
        } else {
            loggedInInfoView.setText(getString(R.string.not_logged_in));
        }
    }

    /**
     *
     */
    private void openEditView(Memo memo) {
        Intent i = new Intent(this, EditMemoActivity.class);
        i.putExtra("ID", memo.getUuidString());
        startActivityForResult(i, EDIT_REQUEST_CODE);
    }

    /**
     * 다른 activity에서 돌아왔을 때 그 결과 값을 확인하여 적절히 처리함.
     *
     * @param requestCode 해당 activity를 호출했을 때 넘긴 code가 다시 넘어옴.
     * @param resultCode 값이 <code>Activity.RESULT_OK</code> 일 경우에만 값을 처리함.
     * @param data Intent에 붙어 있는 기타 데이터가 있을 경우 사용함. 여기서는 쓰지 않음.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_REQUEST_CODE) {
                memoListAdapter.loadObjects();
            } else if (requestCode == LOGIN_REQUEST_CODE) {
                currentUser = ParseUser.getCurrentUser();
                if (currentUser.isNew()) {
                    syncMemosToParse();
                } else {
                    loadFromParse();
                }
            }
        }
    }

    /**
     * Inner class. 
     */
    private class FindCallbackImplForLoad<T extends Memo> implements FindCallback<T> {

        @Override
        public void done(List<T> list, ParseException e) {
            if (e == null) {
                ParseObject.pinAllInBackground(list, new SaveCallback() {

                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            if (!isFinishing()) {
                                memoListAdapter.loadObjects();
                            } else {
                                Log.i("ListMemoActivity", "Error pinning memos: " + e.getMessage());
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     *
     */
    private void loadFromParse() {
        ParseQuery<Memo> query = Memo.getQuery();
        query.whereEqualTo("author", currentUser);
        query.findInBackground(new FindCallbackImplForLoad<>());
    }

    /**
     * 특정 리스트 아이템의 context menu dialog를 생성할 때 수행되는 callback 함수.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    /**
     * Context menu dialog를 통하여 아이템이 선택되었을 때 수행되는 callback 함수.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {

            case R.id.delete:
                memoListAdapter.getItem(info.position).deleteEventually();
                loadFromParse();
                return true;

            case R.id.edit:
                openEditView(memoListAdapter.getItem(info.position));
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_memo, menu);
        return true;
    }

    /**
     *
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean realUser = !ParseAnonymousUtils.isLinked(currentUser);
        menu.findItem(R.id.action_login).setVisible(!realUser);
        menu.findItem(R.id.action_logout).setVisible(realUser);
        return true;
    }

    /**
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_new:
                if (currentUser != null) {
                    startActivityForResult(new Intent(this, EditMemoActivity.class),
                            EDIT_REQUEST_CODE);
                }
                return true;

            case R.id.action_login:
                ParseLoginBuilder builder = new ParseLoginBuilder(this);
                startActivityForResult(builder.build(), LOGIN_REQUEST_CODE);
                return true;

            case R.id.action_logout:
                ParseUser.logOut();
                ParseAnonymousUtils.logIn(null);
                currentUser = ParseUser.getCurrentUser();

                updateLoggedInfo();
                memoListAdapter.clear();
                ParseObject.unpinAllInBackground(MemoApplication.MEMO_GROUP_NAME);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Inner class.
     */
    private class FindCallbackImplForSync<T extends Memo> implements FindCallback<T> {

        @Override
        public void done(List<T> list, ParseException e) {
            if (e == null) {
                for (final Memo memo : list) {
                    memo.setDraft(false);
                    memo.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                if (!isFinishing()) {
                                    memoListAdapter.notifyDataSetChanged();
                                }
                            } else {
                                memo.setDraft(true);
                            }
                        }
                    });
                }
            } else {
                Log.i("ListMemoActivity",
                        "syncMemosToParse: Error finding pinned memos: "
                                + e.getMessage());
            }
        }
    }

    /**
     *
     */
    private void syncMemosToParse() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni != null) && (ni.isConnected())) {
            if (!ParseAnonymousUtils.isLinked(currentUser)) {
                ParseQuery<Memo> query = Memo.getQuery();
                query.fromPin(MemoApplication.MEMO_GROUP_NAME);
                query.whereEqualTo("isDraft", true);
                query.findInBackground(new FindCallbackImplForSync<>());
            } else {
                // If we have a network connection but no logged in user, direct
                // the person to log in or sign up.
                ParseLoginBuilder builder = new ParseLoginBuilder(this);
                startActivityForResult(builder.build(), LOGIN_REQUEST_CODE);
            }
        } else {
            // If there is no connection, let the user know the sync didn't
            // happen
            Toast.makeText(getApplicationContext(),
                    "Your device appears to be offline. Some memos may not have been synced to Parse.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Inner class.
     */
    private static class ViewHolder {
        TextView memoText;
    }

    /**
     * Inner class.
     */
    private class MemoListAdapter extends ParseQueryAdapter<Memo> {

        public MemoListAdapter(Context context, ParseQueryAdapter.QueryFactory<Memo> queryFactory) {
            super(context, queryFactory);
        }

        @Override
        public View getItemView(Memo memo, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.list_item_memo, parent, false);
                holder = new ViewHolder();
                holder.memoText = (TextView) view.findViewById(R.id.memo_content);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            TextView content = holder.memoText;
            content.setText(memo.getMemo());
            return view;
        }
    }
}
