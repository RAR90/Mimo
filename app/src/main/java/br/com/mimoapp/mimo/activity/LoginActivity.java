package br.com.mimoapp.mimo.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;

import br.com.mimoapp.mimo.R;

public class LoginActivity extends AppCompatActivity {

    // Inputs
    private EditText inName;
    private EditText inPhone;
    private Button btnRegister;
    private Button btnTerms;

    private boolean isConnected = false;

    // Constants
    private String[] appPermissions = new String[] {
        Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);




        setContentView(R.layout.activity_login);

        // PermissionsHelper
        // PermissionsHelper.validatePermissions(1, this, appPermissions);

        // Bind inputs and buttons
        inName = findViewById(R.id.userName);
        inPhone = findViewById(R.id.userPhone);
        btnRegister = findViewById(R.id.userRegisterBtn);
        btnTerms = findViewById(R.id.termsButton);

        // Set field mask
        SimpleMaskFormatter maskPhone = new SimpleMaskFormatter("NN NNNNN-NNNN");
        MaskTextWatcher maskWatcherPhone = new MaskTextWatcher(inPhone, maskPhone);
        inPhone.addTextChangedListener(maskWatcherPhone);

        // Button submit action
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strName = inName.getText().toString();
                String myPhone = inPhone.getText().toString();

                // Start verification and sign in process
                if (strName.length() < 2) {

                    inName.requestFocus();
                    Toast.makeText(LoginActivity.this, R.string.name_error_msg, Toast.LENGTH_SHORT).show();

                } else if (myPhone.length() < 13) {

                    inPhone.requestFocus();
                    Toast.makeText(LoginActivity.this, R.string.email_error_msg, Toast.LENGTH_SHORT).show();

                } else {

                    ConnectivityManager cm =
                            (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                    if (cm != null) {
                        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                    }

                    if ( isConnected ) {

                        myPhone = myPhone.replace("-", "").replace(" ", "");
                        String strPhone = "+55" + myPhone;

                        Intent ValidationActivity = new Intent(LoginActivity.this, ValidationActivity.class);

                        ValidationActivity.putExtra("USER_NAME", strName);
                        ValidationActivity.putExtra("USER_PHONE", strPhone);

                        startActivity(ValidationActivity);

                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Button terms action
        btnTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_link)));
                startActivity(intent);
            }
        });
    }

    // [ start PermissionsHelper treatment ]

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                permissionsAlert();
            }
        }
    }

    private void permissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle(R.string.no_permission);
        builder.setMessage(R.string.no_permission_text);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // [ end PermissionsHelper treatment ]

}
