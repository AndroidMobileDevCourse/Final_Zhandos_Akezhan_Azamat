package android.projects.yatooooo.kz.afinal;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.HashMap;

public class Home extends AppCompatActivity {

    private TextView tvEmail;
    private TextView tvName;
    private TextView tvLastLogin;
    private Button hBtnCamera;
    private Button hBtnMap;
    private Button hBtnSingOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);



        initUI();
    }

    private void initUI() {
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvName = (TextView) findViewById(R.id.tvName);
        tvLastLogin = (TextView) findViewById(R.id.tvLastLogin);
        hBtnCamera = (Button) findViewById(R.id.hBtnCamera);
        hBtnMap = (Button) findViewById(R.id.hBtnMap);
        hBtnSingOut = (Button) findViewById(R.id.hBtnSingOut);

        hBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this, Camera.class));
            }
        });

        hBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this, Maps.class));
            }
        });

        hBtnSingOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Home.this, "Loading...", Toast.LENGTH_SHORT).show();
                Backendless.UserService.logout(new AsyncCallback<Void>() {
                    @Override
                    public void handleResponse(Void response) {
                        Toast.makeText(Home.this, "You logged out!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Home.this, SingIn.class));
                    }
                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(Home.this, "Failed to log out!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        HashMap currentUser = (HashMap) Backendless.UserService.CurrentUser().getProperties();
        if (currentUser != null) {
            tvEmail.setText("Your email: " + currentUser.get("email"));
            tvName.setText("Your name: " + currentUser.get("name"));
            tvLastLogin.setText("Last login: " + currentUser.get("lastLogin"));
        }
    }
}
