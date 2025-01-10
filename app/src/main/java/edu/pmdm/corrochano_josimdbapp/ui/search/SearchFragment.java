package edu.pmdm.corrochano_josimdbapp.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.corrochano_josimdbapp.api.TMDbApiService;
import edu.pmdm.corrochano_josimdbapp.databinding.FragmentSearchBinding;
import edu.pmdm.corrochano_josimdbapp.models.Genero;
import edu.pmdm.corrochano_josimdbapp.models.GeneroResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private TMDbApiService tmdbApiService;
    private Spinner spinnerGeneros;
    private List<Genero> generosList = new ArrayList<>();
    private static final String TMDB_API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJmMDE2ZGRjOGRhYWZmYzUyYmM1MmUxN2I1MTQ2ZTk3MSIsIm5iZiI6MTczNjUzOTU1MC43NjksInN1YiI6IjY3ODE3ZDllYzVkMmU5NmUyNjdiNGMwZiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.cP-LiqfqCtg1E7xRX6nPOT3cdttykNkk95N3dvGxkbA";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        spinnerGeneros = binding.spinner;

        // Configurar Retrofit con Interceptor para añadir headers
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(new okhttp3.OkHttpClient.Builder()
                        .addInterceptor(chain -> {
                            okhttp3.Request request = chain.request().newBuilder()
                                    .addHeader("Authorization", "Bearer " + TMDB_API_KEY)
                                    .addHeader("accept", "application/json")
                                    .build();
                            return chain.proceed(request);
                        })
                        .build())
                .build();

        tmdbApiService = retrofit.create(TMDbApiService.class);

        // Obtener los géneros y poblar el spinner
        getGenres();

        // Listener para cuando se pulsa el botón de buscar
        binding.buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding.editTextNumberDate.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "El año no puede estar vacío", Toast.LENGTH_SHORT).show();
                } else {



                }

            }
        });


        return root;
    }

    private void getGenres() {
        Call<GeneroResponse> call = tmdbApiService.getGenres("en-US");
        call.enqueue(new Callback<GeneroResponse>() {
            @Override
            public void onResponse(Call<GeneroResponse> call, Response<GeneroResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    generosList = response.body().getGenres();
                    List<String> generoNames = new ArrayList<>();
                    for (Genero genero : generosList) {
                        generoNames.add(genero.getNombre());
                    }

                    // Configurar el adaptador para el spinner
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            generoNames
                    );

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerGeneros.setAdapter(adapter);

                } else {
                    Toast.makeText(getContext(), "Error al obtener los géneros", Toast.LENGTH_SHORT).show();
                    Log.e("SearchFragment", "Respuesta no exitosa: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GeneroResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error al conectar con la API", Toast.LENGTH_SHORT).show();
                Log.e("SearchFragment", "Error en la llamada API: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
