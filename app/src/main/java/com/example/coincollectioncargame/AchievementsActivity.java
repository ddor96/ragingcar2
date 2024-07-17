package com.example.coincollectioncargame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity implements OnMapReadyCallback, ScoreAdapter.OnScoreItemClickListener {

    private RecyclerView recyclerView;
    private ScoreAdapter scoreAdapter;
    private List<Score> scoreList;
    private DatabaseReference databaseReference;

    private MapView mapView;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        scoreList = new ArrayList<>();
        scoreAdapter = new ScoreAdapter(scoreList, getApplicationContext(), this); // Pass listener
        recyclerView.setAdapter(scoreAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("scores");

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fetchScores();
    }

    private void fetchScores() {
        Query query = databaseReference.orderByChild("score").limitToLast(10); // Get top ten scores
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                scoreList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Score score = postSnapshot.getValue(Score.class);
                    scoreList.add(score);
                }
                Collections.sort(scoreList); // Sort the scores
                scoreAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onScoreItemClick(Score score) {
        LatLng location = new LatLng(score.getLatitude(), score.getLongitude());

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(location).title("Score: " + score.getScore()));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(15)
                .tilt(30)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng location = new LatLng(0.0, 0.0);
        googleMap.addMarker(new MarkerOptions().position(location).title("Score: 0"));


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(15)
                .tilt(30)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}