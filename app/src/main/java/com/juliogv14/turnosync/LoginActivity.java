package com.juliogv14.turnosync;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.juliogv14.turnosync.databinding.ActivityLoginBinding;
import com.juliogv14.turnosync.utils.LoginUtils;

/**
 * Created by Julio on 14/11/2017.
 * LoginActivity.java
 */

public class LoginActivity extends AppCompatActivity {

    //Constants
    private final String TAG = this.getClass().getSimpleName();
    private static final int RC_GOOGLE_SIGN_IN = 0;

    //View Binding
    private ActivityLoginBinding mViewBinding;
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
        mViewBinding.buttonGoogleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(googleSignInIntent, RC_GOOGLE_SIGN_IN);
            }
        });

        //Forgot Password
        mViewBinding.actionResetPassword.setMovementMethod(LinkMovementMethod.getInstance());

        mViewBinding.actionResetPassword.setTextColor(ContextCompat.getColorStateList(this, R.color.selector_link));

        Spannable span = (Spannable) mViewBinding.actionResetPassword.getText();
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                //TODO: Forgot password handle
                Toast.makeText(LoginActivity.this, "Forgot password", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(getResources().getColor(android.R.color.holo_blue_light));
                super.updateDrawState(ds);
            }
        }, 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_register) {
            Toast.makeText(this, "Go to register activity", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, RegisterActivity.class));
        }
        return super.onOptionsItemSelected(item);
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
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            R.string.login_error_auth_failed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    private void attemptLogin() {

        //Reset error indicator
        mViewBinding.editTextLayoutEmail.setError(null);
        mViewBinding.editTextLayoutPassword.setError(null);

        String email = mViewBinding.editTextEmail.getText().toString();
        String password = mViewBinding.editTextPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        /*Check for a valid email address.*/
        if (TextUtils.isEmpty(email)) {
            mViewBinding.editTextLayoutEmail.
                    setError(getString(R.string.login_error_field_required));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        } else if (!LoginUtils.isEmailValid(email)) {
            mViewBinding.editTextLayoutEmail.
                    setError(getString(R.string.login_error_invalid_email));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        }

        /*Check for a valid password.*/
        if (!TextUtils.isEmpty(password) && !LoginUtils.isLoginPasswordValid(password)) {
            mViewBinding.editTextLayoutPassword.
                    setError(getString(R.string.login_error_invalid_password));
            focusView = mViewBinding.editTextPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            //Firebase signin with email and password
            LoginUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(), true);
            mFirebaseAuth.signInWithEmailAndPassword(email, password).
                    addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            LoginUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(),
                                    false);
                            if (task.isSuccessful()) {
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        R.string.login_error_auth_failed, Toast.LENGTH_SHORT).show();
                                mViewBinding.editTextLayoutPassword.
                                        setError(getString(R.string.login_error_incorrect_password));
                                mViewBinding.editTextPassword.requestFocus();

                            }


                        }
                    });
        }
    }
}
