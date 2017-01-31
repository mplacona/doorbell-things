package rocks.androidthings.doorbell;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.button.Button;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class MainActivity extends AppCompatActivity {

    /**
     * The GPIO pin to activate for button presses.
     */
    private final String BUTTON_GPIO_PIN = "BCM6";

    /**
     * Driver for the doorbell button
     */
    private Button mButton;

    /**
     * Doorbell API
     */
    private Doorbell doorbellApi;

    private static final String TAG = "MainActivity";

    private static final String BASE_URL = "http://6e9c6cf2.ngrok.io/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeDoorbellButton();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        doorbellApi = retrofit.create(Doorbell.class);
    }

    /**
     * Initializes button driver, which will report physical button presses.
     */
    private void initializeDoorbellButton() {
        try {
            mButton = new Button(BUTTON_GPIO_PIN,
                    Button.LogicState.PRESSED_WHEN_LOW);
            mButton.setOnButtonEventListener(mButtonCallback);
        } catch (IOException e) {
            Log.e(TAG, "button driver error", e);
        }
    }

    /**
     * Callback for button events.
     */
    private Button.OnButtonEventListener mButtonCallback =
            (button, pressed) -> {
                if (pressed) {
                    // Doorbell rang!
                    Log.d(TAG, "button pressed");
                    Call<String> call = doorbellApi.startCall();
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Log.i(TAG, response.body());
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.e(TAG, t.getMessage());
                        }
                    });

                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mButton != null) {
            try {
                mButton.close();
            } catch (IOException e) {
                Log.e(TAG, "button driver error", e);
            }
        }
    }
}
