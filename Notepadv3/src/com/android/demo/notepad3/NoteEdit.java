/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.demo.notepad3;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class NoteEdit extends Activity {

    public static final String FULL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Notepad";
    private EditText mTitleText;
    private EditText mBodyText;
    private Long mRowId;
    private NotesDbAdapter mDbHelper;
    private Drawable error_indicator;

    public static void export(String fileName, String fileContent) {

        try {
            File dir = new File(FULL_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            OutputStream fOut = null;
            File file = new File(FULL_PATH, fileName + ".txt");

            file.createNewFile();
            fOut = new FileOutputStream(file);
            fOut.write(fileContent.getBytes());
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            Log.e("saveToExternalStorage()", e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        error_indicator = getResources().getDrawable(R.drawable.indicator_input_error);


//
//        mTitleText.setOnEditorActionListener(new EmptyTextListener(mTitleText));
//        mBodyText.setOnEditorActionListener(new EmptyTextListener(mBodyText));


        final Button confirmButton = (Button) findViewById(R.id.confirm);

        final Button shareButton = (Button) findViewById(R.id.share);

        mRowId = null;


        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mRowId = extras.getLong(NotesDbAdapter.KEY_ROWID);
            }
        }
//        mRowId = null;
//        Bundle extras = getIntent().getExtras();
//        if (extras != null) {
//            String title = extras.getString(NotesDbAdapter.KEY_TITLE);
//            String body = extras.getString(NotesDbAdapter.KEY_BODY);
//            mRowId = extras.getLong(NotesDbAdapter.KEY_ROWID);

//            if (title != null) {
//                mTitleText.setText(title);
//            }
//            if (body != null) {
//                mBodyText.setText(body);
//            }

        mTitleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mTitleText.getText().toString().length() == 0) {
                    // TODO to alert the user with error when title is empty
                    confirmButton.setEnabled(false);
                    shareButton.setEnabled(false);
                } else {
                    confirmButton.setEnabled(true);
                    shareButton.setEnabled(true);
                }


            }


        });

        if (mTitleText.getText().toString().length() == 0) {
            confirmButton.setEnabled(false);
            shareButton.setEnabled(false);
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {


                setResult(RESULT_OK);
                finish();

            }

        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");

                String shareBody = mBodyText.getText().toString();
                sharingIntent.putExtra(
                        android.content.Intent.EXTRA_SUBJECT, mTitleText.getText().toString());
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));

                setResult(RESULT_OK);
                finish();
            }
        });


    }


    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);

            mTitleText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)
            ));


            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)
            ));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        saveData();
        bundle.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveData() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();


        if (mRowId == null) {
            long id = mDbHelper.createNote(title, body);
            if (id > 0) {
                mRowId = id;
            }


        } else
            mDbHelper.updateNote(mRowId, title, body);
        export(title, body);
    }


}

//public class EmptyTextListener implements TextView.OnEditorActionListener {
//    private EditText et;
//
//    public EmptyTextListener(EditText editText) {
//        this.et = editText;
//    }
//
//    @Override
//    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//
//        if (actionId == EditorInfo.IME_ACTION_NEXT) {
//            // Called when user press Next button on the soft keyboard
//
//            if (et.getText().toString().equals(""))
//                et.setError("Oops! empty.", error_indicator);
//        }
//        return false;
//    }
//}

//public class InputValidator implements TextWatcher {
//    private EditText et;
//
//    private InputValidator(EditText editText) {
//        this.et = editText;
//    }
//
//    @Override
//    public void afterTextChanged(Editable s) {
//
//    }
//
//    @Override
//    public void beforeTextChanged(CharSequence s, int start, int count,
//                                  int after) {
//
//    }
//
//    @Override
//    public void onTextChanged(CharSequence s, int start, int before,
//                              int count) {
//        if (s.length() != 0) {
//            switch (et.getId()) {
//                case R.id.etUsername: {
//                    if (!Pattern.matches("^[a-z]{1,16}$", s)) {
//                        et.setError("Oops! Username must have only a-z");
//                    }
//                }
//                break;
//
//                case R.id.etPassword: {
//                    if (!Pattern.matches("^[a-zA-Z]{1,16}$", s)) {
//                        et.setError("Oops! Password must have only a-z and A-Z");
//                    }
//                }
//                break;
//            }
//        }
//    }
//}
