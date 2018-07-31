package com.juliogv14.turnosync.ui.account;

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
import android.util.Log;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.databinding.ActivityLoginBinding;
import com.juliogv14.turnosync.ui.drawerlayout.DrawerActivity;
import com.juliogv14.turnosync.utils.AnimationViewUtils;
import com.juliogv14.turnosync.utils.FormUtils;

/**
 * La clase LoginActivity es responsable de manejar el inicio de sesion del usuario y responder a
 * la navegación despues de iniciar sesión, pantalla de registro y de restablecer contraseña.
 * Extiende AppCompatActivity.
 * Implementa la interfaz de escucha del cuadro de dialogo ResetPasswordDialog.
 *
 * @author Julio García
 * @see AppCompatActivity
 * @see ResetPasswordDialog.ResetPasswordListener
 * @see FirebaseAuth
 */
public class LoginActivity extends AppCompatActivity implements ResetPasswordDialog.ResetPasswordListener {

    /** Tag de clase */
    private final String TAG = this.getClass().getSimpleName();
    /** Codigo del intent en el inicio de sesion con google */
    private static final int RC_GOOGLE_SIGN_IN = 0;

    /** Referencia a la vista con databinding */
    private ActivityLoginBinding mViewBinding;
    /** Referencia al servicio de autenticación de Firebase */
    private FirebaseAuth mFirebaseAuth;

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Se establecen las escuchas de finalizado en el teclado, botón de login, botón de login con google
     * enlace de recuperacion de contraseña.
     */
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
                    FormUtils.closeKeyboard(LoginActivity.this, textView);
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
                FormUtils.closeKeyboard(LoginActivity.this, v);
                attemptLogin();
            }
        });

        //Login with google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_token))
                .requestEmail()
                .build();

        final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        mViewBinding.buttonGoogleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent googleSignInIntent = googleSignInClient.getSignInIntent();
                AnimationViewUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(), true);
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
                ResetPasswordDialog dialog = new ResetPasswordDialog();
                dialog.show(getSupportFragmentManager(), "resetPassword");
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(getResources().getColor(android.R.color.holo_blue_light));
                super.updateDrawState(ds);
            }
        }, 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * {@inheritDoc}
     * Callback del ciclo de vida
     * Infla la vista del menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Responde cuando se selecciona un elemento del menu.
     * Redirecciona a la pantalla de registro
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_register) {
            startActivity(new Intent(this, RegisterActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc} <br>
     * Llamado cuando finaliza el inicio de sesion con google.
     */
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
                                AnimationViewUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(), false);
                                if (task.isSuccessful()) {
                                    Intent signCompleteIntent = new Intent(getBaseContext(), DrawerActivity.class);
                                    signCompleteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    signCompleteIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(signCompleteIntent);
                                } else {
                                    Log.d(TAG, "login failed: " + task.getException().getMessage());
                                    Toast.makeText(LoginActivity.this,
                                            R.string.toast_sign_in_failed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                AnimationViewUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(), false);
            }
        }
    }


    /**
     * ntento de inicio de sesión comprobando si las cadenas introducidas son validas
     */
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
                    setError(getString(R.string.form_error_field_required));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        } else if (!FormUtils.isEmailValid(email)) {
            mViewBinding.editTextLayoutEmail.
                    setError(getString(R.string.login_error_invalid_email));
            focusView = mViewBinding.editTextEmail;
            cancel = true;
        }

        /*Check for a valid password.*/
        if (TextUtils.isEmpty(password)) {
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
            AnimationViewUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(), true);
            mFirebaseAuth.signInWithEmailAndPassword(email, password).
                    addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            AnimationViewUtils.showLoadingIndicator(mViewBinding.layoutProgressbar.getRoot(),
                                    false);
                            if (!task.isSuccessful()) {
                                if (task.getException() != null) {
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthInvalidUserException e) {
                                        Log.d(TAG, "Register failed: " + e.getMessage());
                                        Toast.makeText(LoginActivity.this,
                                                R.string.toast_sign_in_failed, Toast.LENGTH_SHORT).show();
                                        mViewBinding.editTextLayoutEmail.
                                                setError(getString(R.string.login_error_incorrect_email));
                                        mViewBinding.editTextPassword.requestFocus();
                                    } catch (FirebaseAuthInvalidCredentialsException e) {
                                        Log.d(TAG, "Register failed: " + e.getMessage());
                                        Toast.makeText(LoginActivity.this,
                                                R.string.toast_sign_in_failed, Toast.LENGTH_SHORT).show();
                                        mViewBinding.editTextLayoutPassword.
                                                setError(getString(R.string.login_error_incorrect_password));
                                    } catch (Exception e) {
                                        Log.e(TAG, "Exception error: " + e.getMessage());
                                        Toast.makeText(LoginActivity.this,
                                                R.string.toast_generic_error + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });
        }

        mFirebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null){
                    if(firebaseUser.isEmailVerified()){
                        Intent signCompleteIntent = new Intent(getBaseContext(), DrawerActivity.class);
                        signCompleteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        signCompleteIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(signCompleteIntent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "This account's email hasn't been verified, please verify before sign in.", Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                    }
                }
            }
        });
    }

    /**
     * Callback del dialogo de restablecer contraseña. Envia el email con en enlace para restablecer la contraseña
     *
     *  @param email email introducido en el dialogo
     */
    @Override
    public void onResetPassword(String email) {
        mFirebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Sent successful");
                            Toast.makeText(LoginActivity.this, R.string.toast_resetpassword_successfully, Toast.LENGTH_LONG).show();
                        } else {
                            Log.d(TAG, "Failed to send email" + task.getException().getMessage());
                            Toast.makeText(LoginActivity.this, R.string.toast_resetpassword_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
