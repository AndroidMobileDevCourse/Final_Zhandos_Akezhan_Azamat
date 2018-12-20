package android.projects.yatooooo.kz.afinal;

import android.app.Application;
import android.projects.yatooooo.kz.afinal.backendless.BackendlessApplication;

public class ApplicationStart extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        BackendlessApplication.init(getApplicationContext());

    }
}
