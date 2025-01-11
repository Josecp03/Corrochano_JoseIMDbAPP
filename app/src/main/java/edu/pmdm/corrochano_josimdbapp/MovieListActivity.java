package edu.pmdm.corrochano_josimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.corrochano_josimdbapp.adapters.MovieAdapter;
import edu.pmdm.corrochano_josimdbapp.api.TMDbApiService;
import edu.pmdm.corrochano_josimdbapp.database.FavoriteDatabaseHelper;
import edu.pmdm.corrochano_josimdbapp.models.Movie;
import edu.pmdm.corrochano_josimdbapp.models.MovieSearchResponse;
import edu.pmdm.corrochano_josimdbapp.models.TMDBMovie;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Movie> movieList = new ArrayList<>();
    private MovieAdapter adapter;
    private FavoriteDatabaseHelper databaseHelper;

    // Clave API de TMDb
    private static final String TMDB_API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJmMDE2ZGRjOGRhYWZmYzUyYmM1MmUxN2I1MTQ2ZTk3MSIsIm5iZiI6MTczNjUzOTU1MC43NjksInN1YiI6IjY3ODE3ZDllYzVkMmU5NmUyNjdiNGMwZiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.cP-LiqfqCtg1E7xRX6nPOT3cdttykNkk95N3dvGxkbA"; // Reemplaza con tu nueva clave API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Referenciar el RecyclerView por su ID
        recyclerView = findViewById(R.id.recyclerView);

        // Inicializar la base de datos
        databaseHelper = new FavoriteDatabaseHelper(this);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas

        // Obtener el UID del usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String idUsuario = (currentUser != null) ? currentUser.getUid() : null;

        if (idUsuario == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            // Opcional: Redirigir al usuario a la pantalla de login
            finish();
            return;
        }

        adapter = new MovieAdapter(this, movieList, idUsuario, databaseHelper, false);
        recyclerView.setAdapter(adapter);

        // Obtener los extras del Intent
        Intent intent = getIntent();
        if (intent != null) {
            String yearStr = intent.getStringExtra("year");
            int genreId = intent.getIntExtra("genreId", -1);

            // Validar los datos recibidos
            if (yearStr != null && genreId != -1) {
                try {
                    int year = Integer.parseInt(yearStr);
                    buscarPeliculas(year, genreId);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Año de publicación inválido.", Toast.LENGTH_SHORT).show();
                    Log.e("MovieListActivity", "Error al convertir el año: " + e.getMessage());
                }
            } else {
                Toast.makeText(this, "Datos incompletos recibidos.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No se recibieron datos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void buscarPeliculas(int year, int genreId) {
        // Configurar Retrofit con Interceptor para añadir headers
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + TMDB_API_KEY)
                            .addHeader("accept", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .build();

        // Inicializar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApiService tmdbApiService = retrofit.create(TMDbApiService.class);

        // Realizar la llamada a la API
        Call<MovieSearchResponse> call = tmdbApiService.searchMovies(year, genreId, "es-ES", "popularity.desc", 1);
        call.enqueue(new Callback<MovieSearchResponse>() {
            @Override
            public void onResponse(Call<MovieSearchResponse> call, Response<MovieSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Log de la respuesta para debugging
                    Log.d("MovieListActivity", "Respuesta de la API: " + new com.google.gson.Gson().toJson(response.body()));

                    List<TMDBMovie> results = response.body().getResults();
                    if (results != null && !results.isEmpty()) {
                        movieList.clear();
                        for (TMDBMovie tmdbMovie : results) {
                            Movie movie = new Movie();
                            movie.setId(String.valueOf(tmdbMovie.getId()));
                            movie.setTitle(tmdbMovie.getTitle());
                            movie.setOriginalTitle(tmdbMovie.getOriginal_title());
                            movie.setReleaseDate(tmdbMovie.getRelease_date());
                            movie.setDescripcion(tmdbMovie.getOverview());
                            movie.setRating(String.valueOf(tmdbMovie.getVote_average()));

                            // Construir la URL completa de la imagen
                            String posterPath = tmdbMovie.getPoster_path();
                            if (posterPath != null && !posterPath.isEmpty()) {
                                String posterUrl = "https://image.tmdb.org/t/p/w500" + posterPath;
                                movie.setPosterPath(posterUrl);
                            } else {
                                // URL por defecto si no hay poster
                                movie.setPosterPath("https://via.placeholder.com/500x750?text=No+Image");
                            }

                            // Llamada para obtener el ID de IMDB
                            obtenerImdbId(String.valueOf(tmdbMovie.getId()), movie);

                            // Agregar la película a la lista
                            movieList.add(movie);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MovieListActivity.this, "No se encontraron películas para los parámetros especificados.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MovieListActivity.this, "Error al cargar películas: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("MovieListActivity", "Respuesta no exitosa: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieSearchResponse> call, Throwable t) {
                Toast.makeText(MovieListActivity.this, "Error en la llamada API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("MovieListActivity", "Error en la llamada API: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Aquí puedes limpiar recursos si es necesario
    }

    private void obtenerImdbId(String tmdbId, Movie movie) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApiService tmdbApiService = retrofit.create(TMDbApiService.class);

        Call<JsonObject> call = tmdbApiService.getExternalIds(tmdbId, "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJmMDE2ZGRjOGRhYWZmYzUyYmM1MmUxN2I1MTQ2ZTk3MSIsIm5iZiI6MTczNjUzOTU1MC43NjksInN1YiI6IjY3ODE3ZDllYzVkMmU5NmUyNjdiNGMwZiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.cP-LiqfqCtg1E7xRX6nPOT3cdttykNkk95N3dvGxkbA");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imdbId = response.body().get("imdb_id").getAsString();
                    movie.setId(imdbId); // Guarda el ID de IMDB en el objeto Movie
                } else {
                    Log.e("MovieListActivity", "No se pudo obtener el ID de IMDB");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MovieListActivity", "Error en la llamada a TMDB: " + t.getMessage());
            }
        });
    }

}