package br.com.mimoapp.mimo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
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
import com.google.firebase.database.DatabaseReference;

import java.util.concurrent.TimeUnit;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.persistence.UserPersist;

public class ValidationActivity extends AppCompatActivity {

    // [ FireBase authentication ]

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private DatabaseReference fireBaseReference;
    private String mVerificationId;
    private String userName;
    private String userPhone;
    private PhoneAuthCredential myCredential;

    // [ Inputs ]
    private EditText validationCode;
    private Button validationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation);

        validationCode = findViewById(R.id.userName);
        validationBtn = findViewById(R.id.validationBtn);

        if (savedInstanceState == null) {

            Bundle extras = getIntent().getExtras();
            userName = extras.getString("USER_NAME");
            userPhone = extras.getString("USER_PHONE");

        } else {

            userName = (String) savedInstanceState.getSerializable("USER_NAME");
            userPhone = (String) savedInstanceState.getSerializable("USER_PHONE");

        }

        verifyPhoneNumber(userPhone);

        validationCode = findViewById(R.id.activationCode);
        validationBtn = findViewById(R.id.validationBtn);

        SimpleMaskFormatter maskPhone = new SimpleMaskFormatter("NNNNNN");
        MaskTextWatcher validationCodeWatcher = new MaskTextWatcher(validationCode, maskPhone);
        validationCode.addTextChangedListener(validationCodeWatcher);

        validationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validationCode.length() < 6) {
                    Toast.makeText(ValidationActivity.this, "Please enter with activation code!", Toast.LENGTH_SHORT).show();
                } else {
                    signInWithPhoneAuthCredential(myCredential);
                }

            }
        });

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    private void verifyPhoneNumber(String phone) {

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                myCredential = credential;
                validationCode.setText(credential.getSmsCode());

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(ValidationActivity.this, R.string.phone_invalid, Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(ValidationActivity.this, R.string.server_error, Toast.LENGTH_LONG).show();
                }
                Toast.makeText(ValidationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,              // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            UserPersist userPersist = new UserPersist( getApplicationContext() );
                            User mUser = new User();
                            FirebaseUser user = task.getResult().getUser();

                            mUser.setId(user.getUid());
                            mUser.setName(userName);
                            mUser.setPhone(userPhone);

                            userPersist.saveInSharedPreferences( mUser );
                            userPersist.saveInFireBase( mUser );

                            //


                            Intent iMain = new Intent(ValidationActivity.this, MainActivity.class);
                            startActivity(iMain);

                            finish();

                        } else {

                            Intent iLogin = new Intent(ValidationActivity.this, LoginActivity.class);
                            startActivity(iLogin);
                            finish();
                        }
                    }
                });
    }

}
