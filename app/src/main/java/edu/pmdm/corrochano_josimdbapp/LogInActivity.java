package edu.pmdm.corrochano_josimdbapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LogInActivity extends AppCompatActivity {

    private SignInButton btnGoogle;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                            auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseAuth auth = FirebaseAuth.getInstance();
                                        String nombre = auth.getCurrentUser().getDisplayName();
                                        String email = auth.getCurrentUser().getEmail();
                                        Uri imagen = auth.getCurrentUser().getPhotoUrl();
                                        navigateToMainActivity(nombre, email, imagen);
                                    } else {
                                        btnGoogle.setEnabled(true);
                                        Toast.makeText(LogInActivity.this, "Failed to sign in.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (ApiException e) {
                            e.printStackTrace();
                            btnGoogle.setEnabled(true);
                            Toast.makeText(LogInActivity.this, "Failed to sign in.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        btnGoogle.setEnabled(true);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        FirebaseApp.initializeApp(this);

        btnGoogle = findViewById(R.id.sign_in_button);
        auth = FirebaseAuth.getInstance();

        // Configurar Google Sign-In
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);

        // Verificar si el usuario ya está autenticado
        if (auth.getCurrentUser() != null) {
            String nombre = auth.getCurrentUser().getDisplayName();
            String email = auth.getCurrentUser().getEmail();
            Uri imagen = auth.getCurrentUser().getPhotoUrl();
            navigateToMainActivity(nombre, email, imagen);
        }

        // Personalizar texto del botón
        for (int i = 0; i < btnGoogle.getChildCount(); i++) {
            View v = btnGoogle.getChildAt(i);
            if (v instanceof TextView) {
                ((TextView) v).setText("Sign in with Google");
                break;
            }
        }

        btnGoogle.setOnClickListener(v -> {
            btnGoogle.setEnabled(false);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(signInIntent);
        });
    }

    private void navigateToMainActivity(String nombre, String email, Uri imagen) {
        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
        intent.putExtra("nombre", nombre);
        intent.putExtra("email", email);
        intent.putExtra("imagen", imagen.toString());
        startActivity(intent);
        finish();
    }



}
