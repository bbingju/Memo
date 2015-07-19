package com.bbingju.mymemo.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bbingju.mymemo.ListMemoActivity;
import com.bbingju.mymemo.R;

/**
 * Created by goldmund on 15. 7. 19.
 */
public class ListMemoActivityTest extends ActivityInstrumentationTestCase2<ListMemoActivity> {

    private ListMemoActivity mListMemoActivity;
    private ListView mMemoListView;
    private LinearLayout mNoMemosView;
    private TextView mLoggedInInfoView;

    public ListMemoActivityTest(Class<ListMemoActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mListMemoActivity = getActivity();
        mMemoListView = (ListView) mListMemoActivity.findViewById(R.id.memo_list_view);
        mNoMemosView = (LinearLayout) mListMemoActivity.findViewById(R.id.no_memos_view);
        mLoggedInInfoView = (TextView) mListMemoActivity.findViewById(R.id.loggedin_info);
    }

    public void testPreconditions() {
        assertNotNull("mListMemoActivity is null.", mListMemoActivity);
        assertNotNull("mMemoListView is null", mMemoListView);
        assertNotNull("mNoMemosView is null", mNoMemosView);
        assertNotNull("mLoggedInInfoView is null", mLoggedInInfoView);
    }

}
