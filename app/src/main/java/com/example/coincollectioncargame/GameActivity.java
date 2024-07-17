package com.example.coincollectioncargame;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class GameActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "GameActivity";

    private ImageView heart1, heart2, heart3, cheese, stone, car, buttonLeft, buttonRight;
    private TextView odometer, coinCounter;
    private Handler handler;
    private Handler odometerHandler;
    private Toast currentToast;

    private Runnable runnable;
    private Runnable odometerRunnable;
    private int laneWidth;
    private int rowHeight;
    private int cheeseSpeed = 1;
    private int stoneSpeed = 1;
    private int lives = 3;
    private int score = 0;
    private int distanceTraveled = 0;
    private int coinsCollected = 0;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private boolean sensorMode = false;
    private boolean sensorModeFast = false;
    private final int totalLanes = 5;
    private final int totalRows = 10;

    private int carColumn;
    private int cheeseColumn;
    private int cheeseRow;
    private int stoneColumn;
    private int stoneRow;

    private float accelerationFactor = 4.0f;
    private DatabaseReference scoresRef;
    private String userId;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        heart1 = findViewById(R.id.heart1);
        heart2 = findViewById(R.id.heart2);
        heart3 = findViewById(R.id.heart3);
        cheese = findViewById(R.id.cheese);
        stone = findViewById(R.id.stone);
        car = findViewById(R.id.car);
        buttonLeft = findViewById(R.id.button_left);
        buttonRight = findViewById(R.id.button_right);
        odometer = findViewById(R.id.odometer);
        coinCounter = findViewById(R.id.coinCounter);

        Intent locationIntent = getIntent();
        latitude = locationIntent.getDoubleExtra("latitude", 0.0);
        longitude = locationIntent.getDoubleExtra("longitude", 0.0);

        scoresRef = FirebaseDatabase.getInstance().getReference("scores");

        userId = "user" + System.currentTimeMillis(); // Using timestamp for unique id

        laneWidth = getResources().getDisplayMetrics().widthPixels / totalLanes;
        rowHeight = getResources().getDisplayMetrics().heightPixels / totalRows;

        handler = new Handler();
        startObjectFall();

        odometerHandler = new Handler();
        startOdometerUpdate();

        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCarLeft();
            }
        });

        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCarRight();
            }
        });

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        if (mode != null) {
            switch (mode) {
                case "sensorSlow":
                    sensorMode = true;
                    cheeseSpeed = 1;
                    stoneSpeed = 1;
                    accelerationFactor = 3.0f;
                    break;
                case "sensorFast":
                    sensorMode = true;
                    sensorModeFast = true;
                    cheeseSpeed = 1;
                    stoneSpeed = 1;
                    accelerationFactor = 4.0f;
                    break;
                default:
                    sensorMode = false;
            }
        }

        if (sensorMode) {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            }
            buttonLeft.setVisibility(View.GONE);
            buttonRight.setVisibility(View.GONE);
        }

        carColumn = totalLanes / 2; // Start in the middle lane
        cheeseRow = 0;
        cheeseColumn = new Random().nextInt(totalLanes);
        stoneRow = 0;
        stoneColumn = new Random().nextInt(totalLanes);

        Log.d(TAG, "Initial carColumn: " + carColumn);
        Log.d(TAG, "Initial cheeseColumn: " + cheeseColumn);
        Log.d(TAG, "Initial stoneColumn: " + stoneColumn);

        car.post(new Runnable() {
            @Override
            public void run() {
                car.setX(carColumn * laneWidth);
                car.setY(getResources().getDisplayMetrics().heightPixels - rowHeight - car.getHeight());
                Log.d(TAG, "Car X: " + car.getX() + ", Car Y: " + car.getY());
            }
        });

        updateCarPosition();
        updateCheesePosition();
        updateStonePosition();
    }

    private void startObjectFall() {
        runnable = new Runnable() {
            @Override
            public void run() {
                moveObjects();
                handler.postDelayed(this, 100);
            }
        };
        handler.post(runnable);
    }

    private void startOdometerUpdate() {
        odometerRunnable = new Runnable() {
            @Override
            public void run() {
                distanceTraveled++; //  distance traveled counter
                odometer.setText("km: " + distanceTraveled); // Update odometer display
                odometerHandler.postDelayed(this, 2000); // Run every 2 seconds
            }
        };
        odometerHandler.post(odometerRunnable);
    }

    private void moveObjects() {
        moveCheese();
        moveStone();
    }

    private void moveCheese() {
        cheeseRow += cheeseSpeed;
        if (cheeseRow >= totalRows) {
            cheeseRow = 0;
            cheeseColumn = new Random().nextInt(totalLanes);
        }
        updateCheesePosition();
        checkCollisionWithCheese();
    }

    private void moveStone() {
        stoneRow += stoneSpeed;
        if (stoneRow >= totalRows) {
            stoneRow = 0;
            stoneColumn = new Random().nextInt(totalLanes);
        }
        updateStonePosition();
        checkCollisionWithStone();
    }

    private void checkCollisionWithCheese() {
        if (cheeseRow == totalRows - 1 && cheeseColumn == carColumn) {
            cheeseRow = 0;
            cheeseColumn = new Random().nextInt(totalLanes);
            vibrateAndToast("dont feed the rat");
            reduceLives();
            updateCheesePosition();
        }
    }

    private void checkCollisionWithStone() {
        if (stoneRow == totalRows - 1 && stoneColumn == carColumn) {
            coinsCollected++;
            coinCounter.setText("Coins: " + coinsCollected);
            stoneRow = 0;
            stoneColumn = new Random().nextInt(totalLanes);
            updateStonePosition();
        }
    }

    private void vibrateAndToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (vibrator != null && checkSelfPermission(Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                vibrator.vibrate(300);
            }
        }

        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private void reduceLives() {
        if (lives > 0) {
            lives--;
            updateHearts(lives);
        } else {
            vibrateAndToast("Game Over! Score: " + score + " Distance: " + distanceTraveled);
            endgame();
        }
    }

    private void moveCarLeft() {
        if (carColumn > 0) {
            carColumn--;
            updateCarPosition();
        }
    }

    private void moveCarRight() {
        if (carColumn < totalLanes - 1) {
            carColumn++;
            updateCarPosition();
        }
    }

    private void updateCarPosition() {
        car.setX(carColumn * laneWidth);
        car.setY(getResources().getDisplayMetrics().heightPixels - rowHeight - car.getHeight());
    }

    private void updateCheesePosition() {
        cheese.setX(cheeseColumn * laneWidth);
        cheese.setY(cheeseRow * rowHeight);
    }

    private void updateStonePosition() {
        stone.setX(stoneColumn * laneWidth);
        stone.setY(stoneRow * rowHeight);
    }

    public void updateHearts(int lives) {
        if (lives < 3) {
            heart3.setVisibility(View.INVISIBLE);
        }
        if (lives < 2) {
            heart2.setVisibility(View.INVISIBLE);
        }
        if (lives < 1) {
            heart1.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sensorMode && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            updateCarPositionTilt(x);
        }
    }

    private void updateCarPositionTilt(float tiltX) {
        float carSpeedX = -tiltX * accelerationFactor;
        float carX = car.getX() + carSpeedX;

        if (carX < 0) carX = 0;
        if (carX > laneWidth * (totalLanes - 1)) carX = laneWidth * (totalLanes - 1);

        car.setX(carX);
        carColumn = Math.round(carX / laneWidth);
        Log.d(TAG, "Updated Car X: " + car.getX() + ", Car Y: " + car.getY() + ", Car Column: " + carColumn);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        handler.removeCallbacks(runnable); // Stop object movement
        odometerHandler.removeCallbacks(odometerRunnable); // Stop odometer update
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        handler.post(runnable); // Resume object movement
        odometerHandler.post(odometerRunnable); // Resume odometer update
    }

    @Override
    public void onBackPressed() {
        // Save the score
        scoresRef.child(userId).setValue(new Score(score, latitude, longitude));
        vibrateAndToast("Game ended with " + coinsCollected + " distance: " + distanceTraveled);
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        handler.removeCallbacks(runnable); // Stop object movement
        odometerHandler.removeCallbacks(odometerRunnable); // Stop odometer update
        super.onBackPressed();
    }

    private void endgame() {
        // Save the score
        scoresRef.child(userId).setValue(new Score(score, latitude, longitude));
        // Show toast message
        vibrateAndToast("Game ended with " + coinsCollected + " distance: " + distanceTraveled);
        // Stop all  tasks
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        handler.removeCallbacks(runnable); // Stop object movement
        odometerHandler.removeCallbacks(odometerRunnable); // Stop odometer update

        finish(); //return to MainActivity
    }
}

