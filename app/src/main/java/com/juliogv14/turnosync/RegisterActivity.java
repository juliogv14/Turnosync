package com.juliogv14.turnosync;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

    public final String TAG = this.getClass().getSimpleName();

    ActivityRegisterBinding mViewBinding;
    FirebaseAuth mFirebaseAuth;

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
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mViewBinding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        String email = mViewBinding.editTextEmail.getText().toString();
        final String displayName = mViewBinding.editTextName.getText().toString();
        String password = mViewBinding.editTextPassword.getText().toString();
        String passwordRepeat = mViewBinding.editTextPasswordRepeat.getText().toString();

        boolean cancel = false;
        View focusView = null;

        /*Check for a valid email address.*/
        if (TextUtils.isEmpty(email)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.login_error_field_required));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        } else if (!LoginUtils.isEmailValid(email)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.login_error_invalid_email));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        }

        /*Check for a valid displayName*/
        if (TextUtils.isEmpty(displayName)) {
            mViewBinding.editTextLayoutName
                    .setError(getString(R.string.login_error_field_required));
            focusView = mViewBinding.editTextName;
            cancel = true;
        } else if (!LoginUtils.isDisplayNameValid(displayName)) {
            mViewBinding.editTextLayoutName
                    .setError(getString(R.string.register_error_name));
            focusView = mViewBinding.editTextName;
            cancel = true;
        }

        /*Check for a valid password.*/
        if (TextUtils.isEmpty(password)) {
            mViewBinding.editTextLayoutPassword
                    .setError(getString(R.string.login_error_field_required));
            focusView = mViewBinding.editTextPassword;
            cancel = true;
        } else if (!LoginUtils.isRegisterPasswordValid(password)) {
            mViewBinding.editTextLayoutPassword.
                    setError(getString(R.string.login_error_invalid_password));
            focusView = mViewBinding.editTextPassword;
            cancel = true;
        }

        /*Check for passwords match*/
        if (!TextUtils.equals(password, passwordRepeat)) {
            mViewBinding.editTextPasswordRepeat.
                    setError(getString(R.string.login_error_invalid_password));
            focusView = mViewBinding.editTextPasswordRepeat;
            cancel = true;
        }

        LoginUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(), true);
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        LoginUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(),
                                false);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(displayName).build());
                            Intent startMainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                            startMainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(startMainIntent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    R.string.login_error_auth_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
