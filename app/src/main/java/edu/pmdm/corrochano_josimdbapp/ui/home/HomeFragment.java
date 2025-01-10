package edu.pmdm.corrochano_josimdbapp.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.pmdm.corrochano_josimdbapp.adapters.MovieAdapter;
import edu.pmdm.corrochano_josimdbapp.api.IMDBApiService;
import edu.pmdm.corrochano_josimdbapp.databinding.FragmentHomeBinding;
import edu.pmdm.corrochano_josimdbapp.database.FavoriteDatabaseHelper;
import edu.pmdm.corrochano_josimdbapp.models.Movie;
import edu.pmdm.corrochano_josimdbapp.models.PopularMoviesResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private IMDBApiService imdbApiService;
    private List<Movie> movieList = new ArrayList<>();
    private MovieAdapter adapter;
    private RecyclerView re;
    private FavoriteDatabaseHelper databaseHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Obtener el idUsuario desde Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();  // Obtienes la instancia de FirebaseAuth
        String idUsuario = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;  // Obtener el ID del usuario autenticado

        // Verificar si el usuario está autenticado
        if (idUsuario == null) {
            Log.e("HomeFragment", "No hay usuario autenticado.");
            return null;  // Si no está autenticado, puedes salir del fragmento o mostrar un mensaje
        }

        // Inicialización de la base de datos y demás componentes
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        databaseHelper = new FavoriteDatabaseHelper(getContext());

        // Configurar RecyclerView
        re = binding.recycler;
        re.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columnas
        adapter = new MovieAdapter(getContext(), movieList, idUsuario, databaseHelper, false); // Pasa idUsuario al adaptador
        re.setAdapter(adapter);

        // Configuración de la API
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

        // Llamada a la API
        Call<PopularMoviesResponse> call = imdbApiService.obtenerTop10("US");
        call.enqueue(new Callback<PopularMoviesResponse>() {
            @Override
            public void onResponse(Call<PopularMoviesResponse> call, Response<PopularMoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PopularMoviesResponse.Edge> edges = response.body().getData().getTopMeterTitles().getEdges();
                    if (edges != null && !edges.isEmpty()) {
                        movieList.clear();  // Limpiar la lista antes de agregar los nuevos elementos
                        for (int i = 0; i < Math.min(edges.size(), 10); i++) {
                            PopularMoviesResponse.Edge edge = edges.get(i);
                            PopularMoviesResponse.Node node = edge.getNode();
                            Movie movie = new Movie();
                            movie.setId(node.getId());
                            movie.setTitle(node.getTitleText().getText());
                            movie.setReleaseDate(node.getPrimaryImage().getUrl());
                            movie.setPosterPath(node.getPrimaryImage().getUrl());
                            movieList.add(movie); // Agregar la película a la lista
                        }
                        adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar películas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PopularMoviesResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error en la llamada API: " + t.getMessage());
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
