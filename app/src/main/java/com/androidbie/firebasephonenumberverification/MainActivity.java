package com.androidbie.firebasephonenumberverification;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, FirebaseAuth.AuthStateListener {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "MainActivity>.TAG";

    private TextView tvDesc;
    private Button btnLogin;
    private Button btnLogout;
    private FirebaseUser user;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDesc = findViewById(R.id.textview_login_description);
        btnLogin = findViewById(R.id.btn_login);
        btnLogout = findViewById(R.id.btn_logout);

        btnLogin.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        firebaseAuth.addAuthStateListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.btn_login){
            signIn();
        }else if(id == R.id.btn_logout){
            signOut();
        }
    }

    private void signIn(){
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
                RC_SIGN_IN
        );
    }

    private void signOut(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Successfully Logout", Toast.LENGTH_SHORT).show();
                        onViewStateLogout();
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            Log.d(TAG, "onActivityResult: " + response.getEmail() + ", " + response.getIdpToken());
            if(resultCode==RESULT_OK){
                 user = FirebaseAuth.getInstance().getCurrentUser();
                 onViewStateLogin();
            }else{
                Toast.makeText(this, "SignIn Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onViewStateLogin(){
        btnLogin.setVisibility(View.GONE);
        btnLogout.setVisibility(View.VISIBLE);
        tvDesc.setText("Hello, " +firebaseAuth.getCurrentUser().getDisplayName());
    }

    private void onViewStateLogout(){
        btnLogin.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.GONE);
        tvDesc.setText(R.string.please_login_before_verification_your_phone);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if(firebaseAuth.getCurrentUser()!=null){
            onViewStateLogin();
        }else{
            onViewStateLogout();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(this);
    }
}
