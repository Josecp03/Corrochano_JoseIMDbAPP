package edu.pmdm.corrochano_josimdbapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.util.concurrent.TimeUnit;

import edu.pmdm.corrochano_josimdbapp.api.IMDBApiService;
import edu.pmdm.corrochano_josimdbapp.models.Movie;
import edu.pmdm.corrochano_josimdbapp.models.MovieOverviewResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MovieDetailsActivity extends AppCompatActivity {

    private Movie pelicula;
    private TextView txtTitle = null;
    private TextView txtDescription = null;
    private TextView txtDate = null;
    private IMDBApiService imdbApiService;
    private ImageView imagen;
    private Button btnSMS = null;
    private double rating;

    private static final int PERMISSION_REQUEST_CODE_CONTACTS = 101;
    private static final int PERMISSION_REQUEST_CODE_SMS = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent i = getIntent();
        pelicula = i.getParcelableExtra("pelicula");

        txtTitle = findViewById(R.id.TextViewTitle);
        txtDescription = findViewById(R.id.TextViewDescription);
        txtDate = findViewById(R.id.TextViewDate);
        btnSMS = findViewById(R.id.btnSendSms);
        imagen = findViewById(R.id.ImageViewPortada);

        txtTitle.setText(pelicula.getTitle());

        // Cargar la imagen de la portada usando Glide
        Glide.with(this)
                .load(pelicula.getPosterPath())
                .into(imagen);

        // Configuración de OkHttpClient con encabezados para la API
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request modifiedRequest = chain.request().newBuilder()
                            .addHeader("X-RapidAPI-Key", "440d48ca01mshb9178145c398148p1c905ajsn498799d4ab35")
                            .addHeader("X-RapidAPI-Host", "imdb-com.p.rapidapi.com")
                            .build();
                    return chain.proceed(modifiedRequest);
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Inicialización de Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://imdb-com.p.rapidapi.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        imdbApiService = retrofit.create(IMDBApiService.class);

        Call<MovieOverviewResponse> call = imdbApiService.obtenerDatos(pelicula.getId());
        call.enqueue(new Callback<MovieOverviewResponse>() {
            @Override
            public void onResponse(Call<MovieOverviewResponse> call, Response<MovieOverviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String descripcion = response.body().getData().getTitle().getPlot().getPlotText().getPlainText();
                    txtDescription.setText(descripcion);

                    // Obtener y formatear la fecha de lanzamiento
                    MovieOverviewResponse.ReleaseDate releaseDate = response.body().getData().getTitle().getReleaseDate();
                    if (releaseDate != null) {
                        String formattedDate = String.format("%d-%02d-%02d", releaseDate.getYear(), releaseDate.getMonth(), releaseDate.getDay());
                        txtDate.setText("Release Date: " + formattedDate);
                    }

                    // Obtener y mostrar el rating
                    MovieOverviewResponse.RatingsSummary ratingsSummary = response.body().getData().getTitle().getRatingsSummary();
                    if (ratingsSummary != null) {
                        rating = ratingsSummary.getAggregateRating();
                        TextView ratingView = findViewById(R.id.TextViewRating);
                        ratingView.setText("Rating: " + String.format("%.1f", rating));
                    }

                }
            }

            @Override
            public void onFailure(Call<MovieOverviewResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error en la llamada API: " + t.getMessage());
            }
        });

        btnSMS.setOnClickListener(v -> {

            // Verificar permisos de contactos
            if (ContextCompat.checkSelfPermission(MovieDetailsActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MovieDetailsActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_CODE_CONTACTS);
            } else if (ContextCompat.checkSelfPermission(MovieDetailsActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                // Verificar permisos de SMS
                ActivityCompat.requestPermissions(MovieDetailsActivity.this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE_SMS);
            } else {
                // Lanzar el método cuando ambos permisos están concedidos
                sendSms();
            }

        });

    }

    private void sendSms() {
        // Crear y lanzar el intent
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PERMISSION_REQUEST_CODE_CONTACTS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE_SMS);
            } else {
                Toast.makeText(this, "Permiso de contactos denegado.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSms();
            } else {
                Toast.makeText(this, "Permiso para enviar SMS denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_REQUEST_CODE_CONTACTS && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            String idContacto = obtenerIdContacto(contactUri);
            if (idContacto != null) {
                String numTelefono = obtenerTelefono(idContacto);
                if (numTelefono != null && !numTelefono.isEmpty()) {

                    // Construir el mensaje para el SMS con el rating formateado
                    String textoSMS = "Esta película te gustará: " + txtTitle.getText().toString() +
                            " Rating: " + String.format("%.1f", rating);

                    // Enviar el SMS con los detalles de la película
                    enviarSMS(numTelefono, textoSMS);
                } else {
                    Toast.makeText(this, "El contacto no tiene número de teléfono.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void enviarSMS(String numero, String texto) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE_SMS);
        } else {
            abrirAppSMS(numero, texto);
        }
    }

    private void abrirAppSMS(String numero, String texto) {
        if (numero == null || texto == null || numero.isEmpty() || texto.isEmpty()) {
            Toast.makeText(this, "No se tiene número o texto para enviar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + numero));
        smsIntent.putExtra("sms_body", texto);
        startActivity(smsIntent);
    }

    private String obtenerIdContacto(Uri contactUri) {
        String idContacto = null;
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            idContacto = cursor.getString(idIndex);
        }
        if (cursor != null) {
            cursor.close();
        }
        return idContacto;
    }

    private String obtenerTelefono(String contactId) {
        String numTelefono = null;
        Cursor cursorTelefono = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );
        if (cursorTelefono != null && cursorTelefono.moveToFirst()) {
            int numberIndex = cursorTelefono.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            numTelefono = cursorTelefono.getString(numberIndex);
        }
        if (cursorTelefono != null) {
            cursorTelefono.close();
        }
        return numTelefono;
    }
}
