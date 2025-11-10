package umn.ac.id.musicplay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private EditText firstName, lastName, email;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        firstName = view.findViewById(R.id.firstNameEditText);
        lastName = view.findViewById(R.id.lastNameEditText);
        email = view.findViewById(R.id.emailAddressEditText);
        Button update = view.findViewById(R.id.updateButton);
        Button signOut = view.findViewById(R.id.signOutButton);

        loadUserData();

        update.setOnClickListener(v -> updateUserData());
        signOut.setOnClickListener(v -> signOut());

        return view;
    }

    private void loadUserData() {
        if (currentUser != null) {
            DocumentReference docRef = db.collection("users").document(currentUser.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    firstName.setText(documentSnapshot.getString("firstName"));
                    lastName.setText(documentSnapshot.getString("lastName"));
                    email.setText(documentSnapshot.getString("email"));
                }
            });
        }
    }

    private void updateUserData() {
        String firstNameInput = firstName.getText().toString().trim();
        String lastNameInput = lastName.getText().toString().trim();
        String emailInput = email.getText().toString().trim();

        if (firstNameInput.isEmpty()) {
            firstName.setError(getString(R.string.error_field_required, getString(R.string.field_first_name)));
            firstName.requestFocus();
            return;
        }

        if (emailInput.isEmpty()) {
            email.setError(getString(R.string.error_field_required, getString(R.string.field_email)));
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError(getString(R.string.error_invalid_email));
            email.requestFocus();
            return;
        }

        if (currentUser != null) {
            DocumentReference docRef = db.collection("users").document(currentUser.getUid());

            Map<String, Object> updates = new HashMap<>();
            updates.put("firstName", firstNameInput);
            updates.put("lastName", lastNameInput);
            updates.put("email", emailInput);

            docRef.update(updates)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), getString(R.string.resource_updated_successfully, getString(R.string.profile)), Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), getString(R.string.resource_update_failed, getString(R.string.profile)), Toast.LENGTH_SHORT).show());

            currentUser.verifyBeforeUpdateEmail(emailInput);
        }
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
