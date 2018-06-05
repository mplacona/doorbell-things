package rocks.androidthings.doorbell;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.button.Button;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final String BASE_URL = "https://hesitant-beds-1131.twil.io";

    private Button button;
    private Doorbell doorbellApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialiseDoorbellButton();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        doorbellApi = retrofit.create(Doorbell.class);
    }

    /**
     * Initializes button driver, which will report physical button presses.
     */
    private void initialiseDoorbellButton() {
        try {
            String BUTTON_GPIO_PIN = "BCM5";
            button = new Button(BUTTON_GPIO_PIN,
                    Button.LogicState.PRESSED_WHEN_LOW);
            button.setOnButtonEventListener(mButtonCallback);
        } catch (IOException e) {
            Log.e(TAG, "button driver error", e);
        }
    }

    /**
     * Callback for button events.
     */
    private Button.OnButtonEventListener mButtonCallback =
            new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    if (pressed) {
                        // Doorbell rang!
                        Log.d(TAG, "button pressed");
                        Call<String> call = doorbellApi.startCall();
                        call.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                //Log.i(TAG, response.body());
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Log.e(TAG, t.getMessage());
                            }
                        });
                    }
                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (button != null) {
            try {
                button.close();
            } catch (IOException e) {
                Log.e(TAG, "button driver error", e);
            }
        }
    }
}
