package com.juliogv14.turnosync;

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
import com.google.firebase.auth.FirebaseAuth;
import com.juliogv14.turnosync.databinding.ActivityResetPasswordBinding;
import com.juliogv14.turnosync.utils.LoginUtils;

public class ResetPasswordActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private ActivityResetPasswordBinding mViewBinding;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mViewBinding.editTextEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptSendEmail();
                    LoginUtils.closeKeyboard(getApplicationContext(), textView);
                    return true;
                }
                return false;
            }
        });

        mViewBinding.buttonSendemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSendEmail();
                LoginUtils.closeKeyboard(getApplicationContext(), v);
            }
        });

    }

    private void attemptSendEmail() {

        mViewBinding.editTextEmail.setError(null);
        String email = mViewBinding.editTextEmail.getText().toString();

        boolean cancel = false;

        if (TextUtils.isEmpty(email)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.login_error_field_required));
            cancel = true;
        } else if (!LoginUtils.isEmailValid(email)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.login_error_invalid_email));
            cancel = true;
        }

        if (cancel) {
            mViewBinding.editTextEmail.requestFocus();
        } else {

            LoginUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(), true);
            mFirebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            LoginUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(), false);
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Sent successful");
                                Toast.makeText(ResetPasswordActivity.this, R.string.toast_resetpassword_successfully, Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, "Failed to send email" + task.getException().getMessage());
                                Toast.makeText(ResetPasswordActivity.this, R.string.toast_resetpassword_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
