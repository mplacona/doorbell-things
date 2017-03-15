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

    private final String BUTTON_GPIO_PIN = "BCM6";
    private static final String TAG = "MainActivity";
    private static final String BASE_URL = "http://06551871.ngrok.io";

    private Button mButton;
    private Doorbell mDoorbellApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseDoorbellButton();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        mDoorbellApi = retrofit.create(Doorbell.class);
    }

    /**
     * Initializes button driver, which will report physical button presses.
     */
    private void initialiseDoorbellButton() {
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
            new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    if (pressed) {
                        // Doorbell rang!
                        Log.d(TAG, "button pressed");
                        Call<String> call = mDoorbellApi.startCall();
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
        if (mButton != null) {
            try {
                mButton.close();
            } catch (IOException e) {
                Log.e(TAG, "button driver error", e);
            }
        }
    }
}
