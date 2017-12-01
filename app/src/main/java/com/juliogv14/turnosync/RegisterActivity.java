package com.juliogv14.turnosync;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.juliogv14.turnosync.databinding.ActivityRegisterBinding;
import com.juliogv14.turnosync.utils.LoginUtils;

public class RegisterActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private ActivityRegisterBinding mViewBinding;
    private FirebaseAuth mFirebaseAuth;

    private String mEmail;
    private String mDisplayName;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();

        //EditText done listener, attempt register
        mViewBinding.editTextPasswordRepeat.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    LoginUtils.closeKeyboard(RegisterActivity.this, textView);
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mViewBinding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUtils.closeKeyboard(RegisterActivity.this, v);
                attemptRegister();
            }
        });
    }

    private void attemptRegister() {
        /*Reset error on textInputLayouts*/
        mViewBinding.editTextLayoutEmail.setError(null);
        mViewBinding.editTextLayoutName.setError(null);
        mViewBinding.editTextLayoutPassword.setError(null);
        mViewBinding.editTextLayoutPasswordRepeat.setError(null);

        /*get strings from editTexts*/
        mEmail = mViewBinding.editTextEmail.getText().toString();
        mDisplayName = mViewBinding.editTextName.getText().toString();
        mPassword = mViewBinding.editTextPassword.getText().toString();
        String passwordRepeat = mViewBinding.editTextPasswordRepeat.getText().toString();

        boolean cancel = false;
        View focusView = null;

        /*Check for a valid email address.*/
        if (TextUtils.isEmpty(mEmail)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.login_error_field_required));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        } else if (!LoginUtils.isEmailValid(mEmail)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.login_error_invalid_email));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        }

        /*Check for a valid displayName*/
        if (TextUtils.isEmpty(mDisplayName)) {
            mViewBinding.editTextLayoutName
                    .setError(getString(R.string.login_error_field_required));
            focusView = mViewBinding.editTextName;
            cancel = true;
        } else if (!LoginUtils.isDisplayNameValid(mDisplayName)) {
            mViewBinding.editTextLayoutName
                    .setError(getString(R.string.register_error_name));
            focusView = mViewBinding.editTextName;
            cancel = true;
        }

        /*Check for a valid password.*/
        if (TextUtils.isEmpty(mPassword)) {
            mViewBinding.editTextLayoutPassword
                    .setError(getString(R.string.login_error_field_required));
            focusView = mViewBinding.editTextPassword;
            cancel = true;
        } else if (!LoginUtils.isRegisterPasswordValid(mPassword)) {
            mViewBinding.editTextLayoutPassword.
                    setError(getString(R.string.register_error_password));
            focusView = mViewBinding.editTextPassword;
            cancel = true;
        }

        /*Check for passwords match*/
        if (!TextUtils.equals(mPassword, passwordRepeat)) {
            mViewBinding.editTextLayoutPasswordRepeat.
                    setError(getString(R.string.register_error_repeat));
            focusView = mViewBinding.editTextPasswordRepeat;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            LoginUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(), true);
            mFirebaseAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            LoginUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(),
                                    false);
                            if (task.isSuccessful()) {

                                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                if (user != null) {
                                    user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(mDisplayName).build())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Intent startMainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                    startMainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                    startActivity(startMainIntent);
                                                    finish();
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this,
                                        R.string.login_error_auth_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
