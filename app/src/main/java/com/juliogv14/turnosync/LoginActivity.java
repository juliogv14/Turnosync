package com.juliogv14.turnosync;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.juliogv14.turnosync.databinding.ActivityLoginBinding;
import com.juliogv14.turnosync.utils.AnimationViewUtils;
import com.juliogv14.turnosync.utils.LoginUtils;

/**
 * Created by Julio on 14/11/2017.
 * LoginActivity.java
 */

public class LoginActivity extends AppCompatActivity {


    ActivityLoginBinding mViewBinding;
    UserLoginTask mLoginTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mViewBinding.editTextPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mViewBinding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mLoginTask != null) {
            mLoginTask.cancel(true);
        }
        super.onBackPressed();
    }

    private void attemptLogin() {
        if (mLoginTask != null) {
            return;
        }

        //Reset error indicator
        mViewBinding.editTextEmail.setError(null);
        mViewBinding.editTextPassword.setError(null);

        String email = mViewBinding.editTextEmail.getText().toString();
        String password = mViewBinding.editTextPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !LoginUtils.isLoginPasswordValid(password)) {
            //mViewBinding.editTextPassword.setError(getString(R.string.error_invalid_password));
            focusView = mViewBinding.editTextPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            //mViewBinding.editTextEmail.setError(getString(R.string.login_error_field_required));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        } else if (!LoginUtils.isEmailValid(email)) {
            //mViewBinding.editTextEmail.setError(getString(R.string.error_invalid_email));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginTask = new UserLoginTask(email, password);
            mLoginTask.execute((Void) null);
        }
    }

    void showLoadingIndicator(boolean show) {
        if (show) {
            mViewBinding.buttonLogin.setEnabled(false);
            AnimationViewUtils.animateView(mViewBinding.layoutProgressbar.getRoot(),
                    View.VISIBLE, 0.4f, 200);
        } else {
            mViewBinding.buttonLogin.setEnabled(true);
            AnimationViewUtils.animateView(mViewBinding.layoutProgressbar.getRoot(),
                    View.VISIBLE, 0, 200);
        }
    }

    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        public UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingIndicator(true);

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //TODO authentication

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            showLoadingIndicator(false);

            if (success) {
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mLoginTask = null;
            showLoadingIndicator(false);
        }
    }



}
