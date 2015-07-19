package com.bbingju.mymemo.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.bbingju.mymemo.EditMemoActivity;
import com.bbingju.mymemo.R;

/**
 * Created by bbingju on 15. 7. 19.
 */
public class EditMemoActivityTest extends ActivityInstrumentationTestCase2<EditMemoActivity> {

    private EditMemoActivity mEditMemoActivity;
    private EditText mMemoText;
    private Button mSaveButton;
    private Button mDeleteButton;

    public EditMemoActivityTest() {
        super(EditMemoActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(true);

        mEditMemoActivity = getActivity();
        mMemoText = (EditText) mEditMemoActivity.findViewById(R.id.memo_text);
        mSaveButton = (Button) mEditMemoActivity.findViewById(R.id.saveButton);
        mDeleteButton = (Button) mEditMemoActivity.findViewById(R.id.deleteButton);
    }

    @MediumTest
    public void testPreconditions() {
        assertNotNull("mEditMemoActivity is null", mEditMemoActivity);
        assertNotNull("mMemoText is null", mMemoText);
        assertNotNull("mSaveButton is null", mSaveButton);
        assertNotNull("mDeleteButton is null", mDeleteButton);
    }

    @MediumTest
    public void testSaveButton_layout() {
        // Retrieve the top-level window decor view
        final View decorView = mEditMemoActivity.getWindow().getDecorView();

        // Verify that the mSaveButton is on screen
        ViewAsserts.assertOnScreen(decorView, mSaveButton);

        // Verify width and height
        final ViewGroup.LayoutParams layoutParams = mSaveButton.getLayoutParams();
        assertNotNull(layoutParams);
        assertEquals(layoutParams.width, WindowManager.LayoutParams.MATCH_PARENT);
        assertEquals(layoutParams.height, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @MediumTest
    public void testDeleteButton_layout() {
        // Retrieve the top-level window decor view
        final View decorView = mEditMemoActivity.getWindow().getDecorView();

        // Verify that the mSaveButton is on screen
        ViewAsserts.assertOnScreen(decorView, mDeleteButton);

        // Verify width and height
        final ViewGroup.LayoutParams layoutParams = mDeleteButton.getLayoutParams();
        assertNotNull(layoutParams);
        assertEquals(layoutParams.width, WindowManager.LayoutParams.MATCH_PARENT);
        assertEquals(layoutParams.height, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Tests the correctness of the initial text.
     */
    @MediumTest
    public void testEditMemoTestSaveButton_labelText() {
        final String expected = mEditMemoActivity.getString(R.string.save);
        final String actual = mSaveButton.getText().toString();
        assertEquals(expected, actual);
    }

    /**
     *
     */
    @MediumTest
    public void testEditMemoTestDeleteButton_labelText() {
        final String expected = mEditMemoActivity.getString(R.string.delete);
        final String actual = mDeleteButton.getText().toString();
        assertEquals(expected, actual);
    }
}
