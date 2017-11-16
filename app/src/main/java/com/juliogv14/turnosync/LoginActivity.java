package com.juliogv14.turnosync;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.juliogv14.turnosync.databinding.ActivityLoginBinding;
import com.juliogv14.turnosync.utils.AnimationViewUtils;
import com.juliogv14.turnosync.utils.LoginUtils;

/**
 * Created by Julio on 14/11/2017.
 * LoginActivity.java
 */

public class LoginActivity extends AppCompatActivity {

    public final String TAG = this.getClass().getSimpleName();
    public static final int RC_GOOGLE_SIGN_IN = 0;

    //View Binding
    ActivityLoginBinding mViewBinding;
    //Login Async task
    UserLoginTask mLoginTask;
    //Firebase auth
    private FirebaseAuth mFirebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mFirebaseAuth = FirebaseAuth.getInstance();


        //EditText done listener, attempt login
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
        //Login button listener, attempt login
        mViewBinding.buttonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        //Login with google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_token))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mViewBinding.buttonGoogleSignin.setSize(SignInButton.SIZE_WIDE);
        mViewBinding.buttonGoogleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(googleSignInIntent, RC_GOOGLE_SIGN_IN);
            }
        });


    }

    /*Google sign in*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                //Firebase sign in with google
                mFirebaseAuth.signInWithCredential(credential).
                        addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, R.string.login_error_auth_failed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        }
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
            mViewBinding.editTextPassword.
                    setError(getString(R.string.login_error_invalid_password));
            focusView = mViewBinding.editTextPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mViewBinding.editTextEmail.
                    setError(getString(R.string.login_error_field_required));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        } else if (!LoginUtils.isEmailValid(email)) {
            mViewBinding.editTextEmail.
                    setError(getString(R.string.login_error_invalid_email));
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
            AnimationViewUtils.animateView(mViewBinding.layoutProgressbar.getRoot(),
                    View.VISIBLE, 0.4f, 200);
        } else {
            AnimationViewUtils.animateView(mViewBinding.layoutProgressbar.getRoot(),
                    View.VISIBLE, 0, 200);
        }
    }

    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private boolean success = true;

        UserLoginTask(String email, String password) {
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
            // Simulate network access.
            mFirebaseAuth.signInWithEmailAndPassword(mEmail, mPassword).
                    addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, R.string.login_error_auth_failed, Toast.LENGTH_SHORT).show();
                                success = false;
                            }
                        }
                    });

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            showLoadingIndicator(false);

            if (success) {
                finish();
            } else {
                mViewBinding.editTextPassword.
                        setError(getString(R.string.login_error_incorrect_password));
                mViewBinding.editTextPassword.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mLoginTask = null;
            showLoadingIndicator(false);
        }
    }


}
