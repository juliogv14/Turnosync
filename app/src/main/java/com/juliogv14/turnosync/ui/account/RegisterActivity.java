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
import com.juliogv14.turnosync.utils.AnimationViewUtils;
import com.juliogv14.turnosync.utils.FormUtils;

/**
 * La clase RegisterActivity es responsable de manejar el registro del usuario.
 * Extiende AppCompatActivity
 *
 * @author Julio García
 * @see AppCompatActivity
 * @see FirebaseAuth
 */
public class RegisterActivity extends AppCompatActivity {

    /** Tag de clase */
    private final String TAG = this.getClass().getSimpleName();

    /** Referencia a la vista con databinding */
    private ActivityRegisterBinding mViewBinding;
    /** Referencia al servicio de autenticación de Firebase */
    private FirebaseAuth mFirebaseAuth;


    /** {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Se establecen las esuchas de finalizado en el teclado y el boton de registrar
     */
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
                if ((id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) && keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    FormUtils.closeKeyboard(RegisterActivity.this, textView);
                    attemptRegister();
                    return true;
                } else return keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN;

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

    /** Intento de registro comprobando si las cadenas introducidas son validas
     */
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
                    .setError(getString(R.string.form_error_field_required));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        } else if (!FormUtils.isEmailValid(email)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.login_error_invalid_email));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        }

        /*Check for a valid displayName*/
        if (TextUtils.isEmpty(displayName)) {
            mViewBinding.editTextLayoutName
                    .setError(getString(R.string.form_error_field_required));
            focusView = mViewBinding.editTextName;
            cancel = true;
        } else if (!FormUtils.isDisplayNameValid(displayName)) {
            mViewBinding.editTextLayoutName
                    .setError(getString(R.string.form_error_name));
            focusView = mViewBinding.editTextName;
            cancel = true;
        }

        /*Check for a valid password.*/
        if (TextUtils.isEmpty(password)) {
            mViewBinding.editTextLayoutPassword
                    .setError(getString(R.string.form_error_field_required));
            focusView = mViewBinding.editTextPassword;
            cancel = true;
        } else if (!FormUtils.isRegisterPasswordValid(password)) {
            mViewBinding.editTextLayoutPassword.
                    setError(getString(R.string.register_error_password));
            focusView = mViewBinding.editTextPassword;
            cancel = true;
        }

        /*Check for passwords match*/
        if (!TextUtils.equals(password, passwordRepeat)) {
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
            AnimationViewUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(), true);
            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            AnimationViewUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(),
                                    false);
                            if (task.isSuccessful()) {

                                final FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

                                if (firebaseUser != null) {
                                    //Add display name to firebase user profile
                                    Task<Void> updateTask = firebaseUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(displayName).build())
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Exception error: " + e.getMessage());
                                                    Toast.makeText(RegisterActivity.this,
                                                            R.string.toast_generic_error + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                    CollectionReference usersReference = FirebaseFirestore.getInstance().collection(getString(R.string.data_ref_users));
                                    User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), displayName);

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
                                            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Log.d(TAG, "Email verification sent");
                                                    }
                                                }
                                            });
                                            Toast.makeText(RegisterActivity.this, "Email verification sent, please proceed to verify before sing in.", Toast.LENGTH_LONG).show();
                                            Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(loginIntent);
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
