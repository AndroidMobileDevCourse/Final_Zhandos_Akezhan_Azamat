package android.projects.yatooooo.kz.afinal.backendless;

import android.content.Context;
import android.location.Location;
import android.projects.yatooooo.kz.afinal.model.Image;
import android.projects.yatooooo.kz.afinal.service.LocationService;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BackendlessApplication {
    private static String url = "https://api.backendless.com";
    public static String applicationId = "97FA41ED-75DE-E4F0-FF14-29ABE6F5E900";
    private static String androidApiKey = "8F257A87-B0E0-C07F-FFB9-E64BFF98C000";


    public static void init(Context applicationContext) {
        Backendless.setUrl(url);
        Backendless.initApp(applicationContext, applicationId, androidApiKey);
    }

    public static void saveImage(final Context applicationContext, byte[] bytes) {
        String currentDateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String path = "images";
        String name = "IMAGE_" + currentDateTime + "_.jpg";
        Backendless.Files.saveFile(path, name, bytes, true);

        final Image image = new Image();
        image.setUserId(Backendless.UserService.CurrentUser().getObjectId());
        image.setName(name);
        image.setPath(path);

        Location deviceLocation = LocationService.location;
        System.out.println("deviceLocation: " + (deviceLocation == null));

        if (deviceLocation != null) {
            image.setLatitude(deviceLocation.getLatitude() + "");
            image.setLongitude(deviceLocation.getLongitude() + "");
        }

        Backendless.Persistence.save(image, new AsyncCallback<Image>() {
            @Override
            public void handleResponse(Image response) {
                Toast.makeText(applicationContext, "Saved at Backendless", Toast.LENGTH_SHORT);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(applicationContext, "Fault during saving", Toast.LENGTH_SHORT);
            }
        });
    }
}
