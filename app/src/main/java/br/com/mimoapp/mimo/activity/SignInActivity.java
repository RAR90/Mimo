package br.com.mimoapp.mimo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.model.Worker;
import br.com.mimoapp.mimo.persistence.UserPersist;
import br.com.mimoapp.mimo.persistence.WorkerPersist;

public class SignInActivity extends AppCompatActivity {

    private FloatingActionButton fabSend;

    private EditText etName;
    private EditText etLastName;
    private EditText etBirthDate;
    private EditText etEmail;

    private RadioButton rbMale;
    private RadioButton rbFemale;

    private TextView signInText;

    private String gender;

    private Worker worker;

    private Boolean isWorker;

    private static final String KEY_ISWORKER = "is_worker";
    private static final String KEY_WORKERS_NODE = "workers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //TODO: extrair strings

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Get bundle of recipient information
        Bundle extra = getIntent().getExtras();

        // Get isWorker Flag to handle the UI
        isWorker = false;
        if ( extra != null ) {
            isWorker = (Boolean) extra.get(KEY_ISWORKER);
        }

        // Get inputs
        etName = findViewById(R.id.et_my_name);
        etLastName = findViewById(R.id.et_my_second_name);
        etBirthDate = findViewById(R.id.et_my_birthday);
        etEmail = findViewById(R.id.et_my_email);
        rbMale = findViewById( R.id.rd_male );
        rbFemale = findViewById( R.id.rd_female );

        // Set field mask
        SimpleMaskFormatter birthDayMask = new SimpleMaskFormatter( "NN/NN/NNNN" );
        MaskTextWatcher birthDayMaskWatcher = new MaskTextWatcher( etBirthDate, birthDayMask );
        etBirthDate.addTextChangedListener( birthDayMaskWatcher );

        gender = null;

        // Get signIn text
        signInText = findViewById( R.id.sign_text );

        // Show SignIn Text if is not Worker
        if ( !isWorker ) {
            signInText.setVisibility( View.VISIBLE );
        }

        //Fill fields if user is worker
        if ( isWorker ) {

            //TODO: se tiver conexão, pega do firebase e atualiza o device, senão tiver conexão, pega do device
            // WorkerPersist workerPersist = new WorkerPersist( getApplicationContext() );
            // worker = workerPersist.getFromSharedPreferences();

            UserPersist userPersist = new UserPersist( getApplicationContext() );
            User user = userPersist.getUser();

            DatabaseReference fireBaseReference = FireBaseConfig.getFirebase().child(KEY_WORKERS_NODE).child( user.getId() );

            // Set listener of data change in FireBase
            fireBaseReference.addListenerForSingleValueEvent( new ValueEventListener() {

                /**
                 * Get result of search in FireBase then export to
                 * array ArrayList<Service> ( services ) to serve the adapter to compose the list
                 * @param dataSnapshot FireBase retrieved data
                 */
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Worker worker = dataSnapshot.getValue( Worker.class );

                    etName.setText( worker.getName() );
                    etLastName.setText( worker.getLastName() );
                    etBirthDate.setText( worker.getBirthDate() );
                    etEmail.setText( worker.getEmail() );
                    gender = worker.getGender();

                    //Set gender
                    if ( gender != null && gender.equals("Masculino") ) {
                        rbMale.setChecked( true );
                        rbFemale.setChecked( false );
                    } else if ( gender != null && gender.equals("Feminino") ) {
                        rbMale.setChecked( false );
                        rbFemale.setChecked( true );
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }




        // Set next button
        fabSend = findViewById(R.id.fab_my_send);

        // Set save icon if user is Worker
        if ( isWorker ) {
            fabSend.setImageResource( R.drawable.ic_save );
        }

        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ( etName.getText().length() <= 0 ) {
                    Toast.makeText(SignInActivity.this, "O campo 'Nome' é obrigatório", Toast.LENGTH_SHORT).show();
                    etName.requestFocus();
                }else if ( etLastName.getText().length() <= 0 ) {
                    Toast.makeText(SignInActivity.this, "O campo 'Sobrenome' é obrigatório", Toast.LENGTH_SHORT).show();
                    etLastName.requestFocus();
                }else if ( etBirthDate.getText().length() <= 0 ) {
                    Toast.makeText(SignInActivity.this, "O campo 'Data de nascimento' é obrigatório", Toast.LENGTH_SHORT).show();
                    etBirthDate.requestFocus();
                }else if ( etEmail.getText().length() <= 0 ) {
                    Toast.makeText(SignInActivity.this, "O campo 'Email' é obrigatório", Toast.LENGTH_SHORT).show();
                    etEmail.requestFocus();
                }else {
                    signIn();
                }
            }
        });

        // Toolbar configuration
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        if ( toolbar != null ) {
            if ( isWorker ) {
                toolbar.setTitle("Atualizar meus dados");
            } else {
                toolbar.setTitle("Ofereça serviços!");
            }
        }
    }

    public void signIn() {

        WorkerPersist workerPersist = new WorkerPersist( getApplicationContext() );
        Worker worker = new Worker();

        UserPersist userPersist = new UserPersist( getApplicationContext() );
        User user = userPersist.getUser();

        worker.setId( user.getId() );

        worker.setName( etName.getText().toString() );
        worker.setLastName( etLastName.getText().toString() );
        worker.setBirthDate( etBirthDate.getText().toString() );
        worker.setEmail( etEmail.getText().toString() );
        worker.setGender( gender );
        worker.setStatus( "1" );

        user.setName( etName.getText().toString() );

        workerPersist.saveInFireBase( worker );
        workerPersist.saveInSharedPreferences( worker );

        userPersist.saveInSharedPreferences( user );
        userPersist.saveInFireBase( user );

        if ( isWorker ) {
            Intent nextStep = new Intent( getApplicationContext(), MainActivity.class );
            startActivity( nextStep );
            Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Intent nextStep = new Intent( getApplicationContext(), UserImageUploadActivity.class );
            startActivity( nextStep );
            finish();
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {

            case R.id.rd_male:
                if (checked)
                    gender = "Masculino";
            break;

            case R.id.rd_female:
                if (checked)
                    gender = "Feminino";
            break;
        }
    }


    @Override
    public void onBackPressed() {
        if ( isWorker ) {
            Intent i = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        } else {
            promptToBack();
        }
    }

    /**
     * <h2>Show confirmation dialog before to back<h2/>
     */
    public void promptToBack() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);

        new ContextThemeWrapper(SignInActivity.this, R.style.MyDialog);

        builder.setTitle(R.string.attention);
        builder.setMessage(R.string.quit_confirm);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent i = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
        builder.setNegativeButton(android.R.string.no, null);
        builder.show();
    }

    public void promptToFinish() {

        final String KEY_EXTRA_SERVICES_FLAG = "services_flag";

        AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
        builder.setTitle(R.string.congrat);
        builder.setMessage(R.string.congrat_text);
        builder.setPositiveButton(R.string.choose_services, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent i = new Intent(SignInActivity.this, MainActivity.class);
                i.putExtra(KEY_EXTRA_SERVICES_FLAG, true);
                startActivity(i);
                finish();
            }
        });
        //builder.setNegativeButton(android.R.string.no, null);
        builder.show();
    }
}
