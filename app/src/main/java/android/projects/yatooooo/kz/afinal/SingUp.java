package android.projects.yatooooo.kz.afinal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class SingUp extends AppCompatActivity {

    private EditText suTeEmail;
    private EditText suTeUsername;
    private EditText suTePassword;
    private Button suBtnSingUp;
    private Button suBtnSingIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        initUI();
    }

    private void initUI() {
        suTeEmail = (EditText) findViewById(R.id.suTeEmail);
        suTeUsername = (EditText) findViewById(R.id.suTeUsername);
        suTePassword = (EditText) findViewById(R.id.suTePassword);

        suBtnSingUp = (Button) findViewById(R.id.suBtnSingUp);
        suBtnSingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSingUpButttonClicked();
            }
        });
    }

    private void onSingUpButttonClicked() {

        Toast.makeText(SingUp.this, "Loading...", Toast.LENGTH_SHORT).show();

        if (suTeEmail.getText().toString().equals("")) {
            suTeEmail.setError(getString(R.string.error_email));
            return;
        }
        if (suTePassword.getText().toString().equals("")) {
            suTePassword.setError(getString(R.string.error_pass));
            return;
        }

        BackendlessUser newUser = new BackendlessUser();
        newUser.setEmail(suTeEmail.getText().toString());
        newUser.setProperty("name", suTeUsername.getText().toString());
        newUser.setPassword(suTePassword.getText().toString());

        Backendless.UserService.register(newUser, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                Toast.makeText(SingUp.this, R.string.register_success,
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SingUp.this, SingIn.class));
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                System.out.println(fault.getCode());
                System.out.println(fault.getMessage());
                System.out.println(fault.getDetail());
                Toast.makeText(SingUp.this, R.string.register_fail,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
