package edu.pmdm.corrochano_josimdbapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LogInActivity extends AppCompatActivity {

    private SignInButton btnGoogle = null;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private String nombre, email;
    private Uri imagen;


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
                                        auth =  FirebaseAuth.getInstance();
                                        imagen = auth.getCurrentUser().getPhotoUrl();
                                        nombre = auth.getCurrentUser().getDisplayName();
                                        email = auth.getCurrentUser().getEmail();
                                        // Glide.with(LogInActivity.this).load(auth.getCurrentUser().getPhotoUrl()).into(imageView);
                                        Toast.makeText(LogInActivity.this, "Signed in succesfuly", Toast.LENGTH_SHORT).show();
                                    } else {

                                        Toast.makeText(LogInActivity.this, "Failed to sign in: ", Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Asignar valores XML
        btnGoogle = findViewById(R.id.sign_in_button);

        // Recorrer las vistas hijas que están contenidas dentro del botón
        for (int i = 0; i < btnGoogle.getChildCount(); i++) {

            // Obtiene la vista hija en la posición i. Cada hijo es un componente interno del botón, como texto, íconos o contenedores
            View v = btnGoogle.getChildAt(i);

            // Comrpobar si el componente actual es un TextView
            if (v instanceof TextView) {

                // Cambiar el texto del botón
                ((TextView) v).setText("Sign in with Google");

                // Salirse cuando realice la acción
                break;

            }

        }

        FirebaseApp.initializeApp(this);
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(LogInActivity.this, options);
        auth = FirebaseAuth.getInstance();


        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(i);

                Intent i2 = new Intent(LogInActivity.this, MainActivity.class);
                i2.putExtra("nombre", nombre);
                i2.putExtra("email", email);
                i2.putExtra("imagen", imagen);
                startActivity(i2);

            }
        });



    }
}