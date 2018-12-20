package android.projects.yatooooo.kz.afinal;

import android.app.Dialog;
import android.content.Intent;
import android.projects.yatooooo.kz.afinal.service.LocationService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class SingIn extends AppCompatActivity {

    private EditText siTeEmail;
    private EditText siTePassword;
    private CheckBox siCbRemember;
    private Button siBtnSingIn;
    private TextView siTvSingUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_in);

        LocationService.getLocationPermission( this, getApplicationContext());
        LocationService.init(this, getApplicationContext());

        initView();

        if (Backendless.UserService.CurrentUser() != null)
            Toast.makeText(SingIn.this, Backendless.UserService.CurrentUser().getUserId(),
                    Toast.LENGTH_SHORT).show();
    }

    private void initView() {
        siTeEmail = (EditText) findViewById(R.id.siTeEmail);
        siTePassword = (EditText) findViewById(R.id.siTePassword);
        siCbRemember = (CheckBox) findViewById(R.id.siCbRemember);
        siBtnSingIn = (Button) findViewById(R.id.siBtnSingIn);
        siTvSingUp = (TextView) findViewById(R.id.siTvSingUp);


        siBtnSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSingInButtonClicked();
            }
        });
        siTvSingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SingIn.this, SingUp.class));
            }
        });
    }

    private void singInSuccess() {
        startActivity(new Intent(SingIn.this, Home.class));
    }

    private void onSingInButtonClicked() {
        Toast.makeText(SingIn.this, "Loading...", Toast.LENGTH_SHORT).show();
        Backendless.UserService.login(siTeEmail.getText().toString(),
                siTePassword.getText().toString(),
                new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser response) {
                        Toast.makeText(SingIn.this, R.string.email_login_success,
                                Toast.LENGTH_SHORT).show();
                        singInSuccess();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(SingIn.this, getString(R.string.login_fail) + fault.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }, siCbRemember.isChecked());
    }
}
