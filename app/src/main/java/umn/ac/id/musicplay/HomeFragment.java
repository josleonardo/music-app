package umn.ac.id.musicplay;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView textGreeting;
    private ImageView profilePicture;
    private LocalMusicAdapter localMusicAdapter;
    private List<Music> localMusicList;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchLocalMusics();
                } else {
                    Toast.makeText(getContext(), "Permission denied. Cannot load local musics.", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textGreeting = view.findViewById(R.id.textGreeting);
        profilePicture = view.findViewById(R.id.profilePicture);

        RecyclerView recyclerLocal = view.findViewById(R.id.recyclerLocal);
        recyclerLocal.setLayoutManager(new LinearLayoutManager(getContext()));
        localMusicList = new ArrayList<>();
        localMusicAdapter = new LocalMusicAdapter(getContext(), localMusicList);
        recyclerLocal.setAdapter(localMusicAdapter);
        recyclerLocal.setNestedScrollingEnabled(false);

        loadUserData();
        checkPermissionAndLoadMusics();

        return view;
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(uid);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String firstName = document.getString("firstName");

                        updateGreeting(firstName);

                        if (document.contains("profilePictureUrl")) {
                            String imageUrl = document.getString("profilePictureUrl");
                            Glide.with(this).load(imageUrl).into(profilePicture);
                        } else {
                            profilePicture.setImageResource(R.drawable.ic_profile);
                        }

                    } else {
                        updateGreeting(null);
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.failed_to_load_user), Toast.LENGTH_SHORT).show();
                    updateGreeting(null);
                }
            });
        } else {
            updateGreeting(null);
        }
    }

    private void updateGreeting(String userFirstName) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        String finalGreeting = greeting + (userFirstName != null ? ", " + userFirstName : "User");
        textGreeting.setText(finalGreeting);
    }

    private void checkPermissionAndLoadMusics() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            fetchLocalMusics();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void fetchLocalMusics() {
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + MediaStore.Audio.Media.DURATION + ">= 15000";

        try (Cursor cursor = requireContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE + " ASC"
        )) {
            if (cursor != null) {
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

                while (cursor.moveToNext()) {
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    int durationMs = cursor.getInt(durationColumn);
                    long albumId = cursor.getLong(albumIdColumn);
                    long minutes = (durationMs / 1000) / 60;
                    long seconds = (durationMs / 1000) % 60;

                    String durationFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds);

                    Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/album-art"), albumId);

                    localMusicList.add(new Music(title, artist, durationFormatted, albumArtUri));
                }

                localMusicAdapter.notifyDataSetChanged();
            }
        }
    }
}