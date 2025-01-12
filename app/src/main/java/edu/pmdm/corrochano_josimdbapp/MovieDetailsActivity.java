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

        // Título con valor predeterminado
        String title = (pelicula != null && pelicula.getTitle() != null && !pelicula.getTitle().isEmpty())
                ? pelicula.getTitle()
                : "Título no disponible";
        txtTitle.setText(title);

        // Cargar la imagen de la portada usando Glide con manejo de valores nulos
        String posterPath = (pelicula != null) ? pelicula.getPosterPath() : "";
        if (posterPath != null && !posterPath.endsWith("No+Image") && !posterPath.isEmpty()) {
            Glide.with(this)
                    .load(pelicula.getPosterPath())
                    .placeholder(R.mipmap.placeholderportada) // Imagen de placeholder
                    .error(R.mipmap.placeholderportada) // Imagen en caso de error
                    .into(imagen);
        } else {
            // Si no hay portada, cargar una imagen predeterminada
            Glide.with(this)
                    .load(R.mipmap.placeholderportada)
                    .into(imagen);
        }

        // Configuración de OkHttpClient con encabezados para la API
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request modifiedRequest = chain.request().newBuilder()
                            .addHeader("X-RapidAPI-Key", "1d6b8bf5bemsh13bad6e5b669b95p146504jsnaa743711d880")
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

        if (pelicula != null && pelicula.getId() != null && !pelicula.getId().isEmpty() && !pelicula.getId().equals("ID no disponible")) {
            Call<MovieOverviewResponse> call = imdbApiService.obtenerDatos(pelicula.getId());
            call.enqueue(new Callback<MovieOverviewResponse>() {
                @Override
                public void onResponse(Call<MovieOverviewResponse> call, Response<MovieOverviewResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        MovieOverviewResponse.Data data = response.body().getData();
                        if (data != null) {
                            MovieOverviewResponse.Title titleData = data.getTitle();
                            if (titleData != null) {
                                // Descripción con valor predeterminado
                                String descripcion = "Descripción no disponible";
                                if (titleData.getPlot() != null &&
                                        titleData.getPlot().getPlotText() != null &&
                                        titleData.getPlot().getPlotText().getPlainText() != null &&
                                        !titleData.getPlot().getPlotText().getPlainText().isEmpty()) {
                                    descripcion = titleData.getPlot().getPlotText().getPlainText();
                                }
                                txtDescription.setText(descripcion);

                                // Fecha de lanzamiento con valor predeterminado
                                String formattedDate = "Fecha no disponible";
                                MovieOverviewResponse.ReleaseDate releaseDate = titleData.getReleaseDate();
                                if (releaseDate != null) {
                                    formattedDate = String.format("%d-%02d-%02d", releaseDate.getYear(), releaseDate.getMonth(), releaseDate.getDay());
                                }
                                txtDate.setText("Release Date: " + formattedDate);

                                // Rating con valor predeterminado
                                String ratingText = "Rating: No disponible";
                                MovieOverviewResponse.RatingsSummary ratingsSummary = titleData.getRatingsSummary();
                                if (ratingsSummary != null) {
                                    rating = ratingsSummary.getAggregateRating();
                                    ratingText = "Rating: " + String.format("%.1f", rating);
                                }
                                TextView ratingView = findViewById(R.id.TextViewRating);
                                ratingView.setText(ratingText);
                            } else {
                                // Asignar valores predeterminados si titleData es null
                                txtDescription.setText("Descripción no disponible");
                                txtDate.setText("Release Date: No disponible");
                                TextView ratingView = findViewById(R.id.TextViewRating);
                                ratingView.setText("Rating: No disponible");
                                Toast.makeText(MovieDetailsActivity.this, "Información de la película no disponible.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Asignar valores predeterminados si data es null
                            txtDescription.setText("Descripción no disponible");
                            txtDate.setText("Release Date: No disponible");
                            TextView ratingView = findViewById(R.id.TextViewRating);
                            ratingView.setText("Rating: No disponible");
                            Toast.makeText(MovieDetailsActivity.this, "Información de la película no disponible.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Asignar valores predeterminados en caso de fallo en la respuesta
                        txtDescription.setText("Descripción no disponible");
                        txtDate.setText("Release Date: No disponible");
                        TextView ratingView = findViewById(R.id.TextViewRating);
                        ratingView.setText("Rating: No disponible");
                        Toast.makeText(MovieDetailsActivity.this, "Error al obtener detalles de la película.", Toast.LENGTH_SHORT).show();
                        Log.e("MovieDetailsActivity", "Respuesta no exitosa: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<MovieOverviewResponse> call, Throwable t) {
                    Log.e("MovieDetailsActivity", "Error en la llamada API: " + t.getMessage());
                    // Asignar valores predeterminados en caso de fallo
                    txtDescription.setText("Descripción no disponible");
                    txtDate.setText("Release Date: No disponible");
                    TextView ratingView = findViewById(R.id.TextViewRating);
                    ratingView.setText("Rating: No disponible");
                    Toast.makeText(MovieDetailsActivity.this, "Error al obtener detalles de la película.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Asignar valores predeterminados si la película es nula o no tiene ID válido
            txtDescription.setText("Descripción no disponible");
            txtDate.setText("Release Date: No disponible");
            TextView ratingView = findViewById(R.id.TextViewRating);
            ratingView.setText("Rating: No disponible");
        }

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
