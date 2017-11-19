package com.juliogv14.turnosync;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.juliogv14.turnosync.databinding.ActivityRegisterBinding;
import com.juliogv14.turnosync.utils.LoginUtils;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding mViewBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
        String displayName = mViewBinding.editTextName.getText().toString();
        String password = mViewBinding.editTextPassword.getText().toString();
        String passwordRepeat = mViewBinding.editTextPasswordRepeat.getText().toString();
        ;


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

        /*Check for valid displayName*/
    }
}
