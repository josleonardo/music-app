package umn.ac.id.musicplay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private EditText firstName, lastName, email, password, confirmPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        firstName = findViewById(R.id.firstNameField);
        lastName = findViewById(R.id.lastNameField);
        email = findViewById(R.id.emailAddressField);
        password = findViewById(R.id.passwordEditText);
        confirmPassword = findViewById(R.id.confirmEditText);
        Button signUp = findViewById(R.id.signUpButton);
        Button signIn = findViewById(R.id.signInButton);
        progressBar = findViewById(R.id.progressBar);

        signUp.setOnClickListener(view -> createAccount());

        signIn.setOnClickListener(view -> startActivity(new Intent(SignUpActivity.this, SignInActivity.class)));
    }

    private void createAccount() {
        String firstNameInput = firstName.getText().toString().trim();
        String lastNameInput = lastName.getText().toString().trim();
        String emailInput = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();
        String confirmPasswordInput = confirmPassword.getText().toString().trim();

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

        if (passwordInput.isEmpty()) {
            password.setError(getString(R.string.error_field_required, getString(R.string.field_password)));
            password.requestFocus();
            return;
        }

        if (passwordInput.length() < 8) {
            password.setError(getString(R.string.error_password_too_short));
            password.requestFocus();
            return;
        }

        if (!confirmPasswordInput.equals(passwordInput)) {
            confirmPassword.setError(getString(R.string.error_passwords_do_not_match));
            confirmPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        saveUserData(user, firstNameInput, lastNameInput);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignUpActivity.this, getString(R.string.resource_failed, getString(R.string.authentication)), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(FirebaseUser firebaseUser, String firstName, String lastName) {
        String userId = firebaseUser.getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("email", firebaseUser.getEmail());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignUpActivity.this, getString(R.string.resource_created_successfully, getString(R.string.account)), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignUpActivity.this, getString(R.string.error_saving_user_data), Toast.LENGTH_SHORT).show();
                });
    }
}
