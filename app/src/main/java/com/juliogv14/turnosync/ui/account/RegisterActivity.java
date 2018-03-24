package com.juliogv14.turnosync.ui.account;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.User;
import com.juliogv14.turnosync.databinding.ActivityRegisterBinding;
import com.juliogv14.turnosync.ui.drawerlayout.DrawerActivity;
import com.juliogv14.turnosync.utils.FormUtils;

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
                    FormUtils.closeKeyboard(RegisterActivity.this, textView);
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mViewBinding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FormUtils.closeKeyboard(RegisterActivity.this, v);
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
                    .setError(getString(R.string.form_error_field_required));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        } else if (!FormUtils.isEmailValid(mEmail)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.login_error_invalid_email));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        }

        /*Check for a valid displayName*/
        if (TextUtils.isEmpty(mDisplayName)) {
            mViewBinding.editTextLayoutName
                    .setError(getString(R.string.form_error_field_required));
            focusView = mViewBinding.editTextName;
            cancel = true;
        } else if (!FormUtils.isDisplayNameValid(mDisplayName)) {
            mViewBinding.editTextLayoutName
                    .setError(getString(R.string.form_error_name));
            focusView = mViewBinding.editTextName;
            cancel = true;
        }

        /*Check for a valid password.*/
        if (TextUtils.isEmpty(mPassword)) {
            mViewBinding.editTextLayoutPassword
                    .setError(getString(R.string.form_error_field_required));
            focusView = mViewBinding.editTextPassword;
            cancel = true;
        } else if (!FormUtils.isRegisterPasswordValid(mPassword)) {
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
            FormUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(), true);
            mFirebaseAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FormUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(),
                                    false);
                            if (task.isSuccessful()) {

                                FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

                                if (firebaseUser != null) {
                                    //Add display name to firebase user profile
                                    Task<Void> updateTask = firebaseUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(mDisplayName).build())
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Exception error: " + e.getMessage());
                                                    Toast.makeText(RegisterActivity.this,
                                                            R.string.toast_generic_error + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                    CollectionReference usersReference = FirebaseFirestore.getInstance().collection(getString(R.string.data_ref_users));
                                    User user = new User(firebaseUser.getEmail(), mDisplayName);

                                    //Add user to database
                                    Task<Void> databaseTask = usersReference.document(firebaseUser.getUid()).set(user)
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Exception error: " + e.getMessage());
                                                    Toast.makeText(RegisterActivity.this,
                                                            R.string.toast_generic_error + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                    //When all tasks are done
                                    Task<Void> alltasks = Tasks.whenAll(updateTask, databaseTask);
                                    alltasks.addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent startMainIntent = new Intent(RegisterActivity.this, DrawerActivity.class);
                                            startMainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(startMainIntent);
                                            finish();
                                        }
                                    });
                                }
                            } else {
                                if (task.getException() != null) {
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthUserCollisionException e) {
                                        Log.d(TAG, "Register failed: " + e.getMessage());
                                        Toast.makeText(RegisterActivity.this,
                                                R.string.toast_register_failed, Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Log.e(TAG, "Exception error: " + e.getMessage());
                                        Toast.makeText(RegisterActivity.this,
                                                R.string.toast_generic_error + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                        }
                    });
        }
    }
}