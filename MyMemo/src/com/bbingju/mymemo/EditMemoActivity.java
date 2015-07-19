package com.bbingju.mymemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by bbingju on 15. 7. 14.
 */
public class EditMemoActivity extends Activity {

    private Button saveButton;
    private Button deleteButton;
    private EditText memoText;
    private Memo memo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String memoId = null;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_edit);

        if (getIntent().hasExtra("ID")) {
            memoId = getIntent().getExtras().getString("ID");
        }

        memoText = (EditText) findViewById(R.id.memo_text);
        saveButton = (Button) findViewById(R.id.saveButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);

        if (memoId == null) {
            memo = new Memo();
            memo.setUuidString();
        } else {
            // Load existing item
            ParseQuery<Memo> query = Memo.getQuery();
            query.fromLocalDatastore();
            query.whereEqualTo("uuid", memoId);
            query.getFirstInBackground(new GetCallback<Memo>() {
                @Override
                public void done(Memo object, ParseException e) {
                    if (!isFinishing()) {
                        memo = object;
                        memoText.setText(memo.getTitle());
                        deleteButton.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                memo.setTitle(memoText.getText().toString());
                memo.setAuthor(ParseUser.getCurrentUser());
                memo.setDraft(true);
                memo.pinInBackground(MemoApplication.MEMO_GROUP_NAME, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (isFinishing()) {
                            return;
                        }

                        if (e == null) {
                            setResult(Activity.RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                memo.deleteEventually();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }
}
