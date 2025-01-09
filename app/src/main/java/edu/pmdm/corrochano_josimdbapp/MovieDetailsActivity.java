package edu.pmdm.corrochano_josimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
    private TextView txt;
    private IMDBApiService imdbApiService;
    private TextView txt2;
    private ImageView imagen;

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

        txt = findViewById(R.id.TextViewTitle);
        txt2 = findViewById(R.id.TextViewDescription);
        TextView releaseDateView = findViewById(R.id.TextViewDate); // Referencia al TextView de la fecha
        txt.setText(pelicula.getTitle());
        imagen = findViewById(R.id.ImageViewPortada);

        // Cargar la imagen del poster usando Glide
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
                    txt2.setText(descripcion);

                    // Obtener y formatear la fecha de lanzamiento
                    MovieOverviewResponse.ReleaseDate releaseDate = response.body().getData().getTitle().getReleaseDate();
                    if (releaseDate != null) {
                        String formattedDate = String.format("%d-%02d-%02d", releaseDate.getYear(), releaseDate.getMonth(), releaseDate.getDay());
                        releaseDateView.setText("Release Date: " + formattedDate);
                    }

                    // Obtener y mostrar el rating
                    MovieOverviewResponse.RatingsSummary ratingsSummary = response.body().getData().getTitle().getRatingsSummary();
                    if (ratingsSummary != null) {
                        double rating = ratingsSummary.getAggregateRating();
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

    }
}