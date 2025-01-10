package edu.pmdm.corrochano_josimdbapp.ui.favorites;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.corrochano_josimdbapp.adapters.MovieAdapter;
import edu.pmdm.corrochano_josimdbapp.database.FavoriteDatabaseHelper;
import edu.pmdm.corrochano_josimdbapp.databinding.FragmentGalleryBinding;
import edu.pmdm.corrochano_josimdbapp.models.Movie;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;

public class FavoritesFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private Handler mainHandler;

    private boolean favoritos = true;
    private String idUsuario;
    private List<Movie> pelisFavoritas = new ArrayList<>();
    private FavoriteDatabaseHelper database;
    private MovieAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        idUsuario = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (idUsuario == null) {
            Toast.makeText(getContext(), "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return root;
        }

        database = new FavoriteDatabaseHelper(getContext());
        mainHandler = new Handler(Looper.getMainLooper());

        // Configurar RecyclerView
        binding.recyclerViewFavoritos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new MovieAdapter(getContext(), pelisFavoritas, idUsuario, database, favoritos);
        binding.recyclerViewFavoritos.setAdapter(adapter);

        cargarFavoritosDesdeBD();
        return root;
    }

    private void cargarFavoritosDesdeBD() {
        new Thread(() -> {
            SQLiteDatabase db = database.getReadableDatabase();

            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + FavoriteDatabaseHelper.TABLE_FAVORITOS + " WHERE idUsuario=?",
                    new String[]{idUsuario}
            );

            pelisFavoritas.clear();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String idPelicula = cursor.getString(cursor.getColumnIndex("idPelicula"));
                    @SuppressLint("Range") String titulo = cursor.getString(cursor.getColumnIndex("nombrePelicula"));
                    @SuppressLint("Range") String descripcion = cursor.getString(cursor.getColumnIndex("descripcionPelicula"));
                    @SuppressLint("Range") String fecha = cursor.getString(cursor.getColumnIndex("fechaLanzamiento"));
                    @SuppressLint("Range") String ranking = cursor.getString(cursor.getColumnIndex("rankingPelicula"));
                    @SuppressLint("Range") String caratula = cursor.getString(cursor.getColumnIndex("portadaURL"));

                    Movie movie = new Movie();
                    movie.setId(idPelicula);
                    movie.setTitle(titulo);
                    movie.setDescripcion(descripcion);
                    movie.setReleaseDate(fecha);
                    movie.setRating(ranking);
                    movie.setPosterPath(caratula);
                    pelisFavoritas.add(movie);
                } while (cursor.moveToNext());
            }

            if (cursor != null) {
                cursor.close();
            }
            db.close();

            mainHandler.post(() -> {
                if (!pelisFavoritas.isEmpty()) {
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "No tienes películas favoritas guardadas", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}