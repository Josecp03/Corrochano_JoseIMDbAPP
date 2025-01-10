package edu.pmdm.corrochano_josimdbapp.ui.favorites;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.corrochano_josimdbapp.R;
import edu.pmdm.corrochano_josimdbapp.adapters.MovieAdapter;
import edu.pmdm.corrochano_josimdbapp.database.FavoriteDatabaseHelper;
import edu.pmdm.corrochano_josimdbapp.databinding.FragmentGalleryBinding;
import edu.pmdm.corrochano_josimdbapp.models.Movie;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

public class FavoritesFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private Handler mainHandler;

    private boolean favoritos = true;
    private String idUsuario;
    private List<Movie> pelisFavoritas = new ArrayList<>();
    private FavoriteDatabaseHelper database;
    private MovieAdapter adapter;
    private static final int REQUEST_CODE_PERMISSIONS = 101;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        idUsuario = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        database = new FavoriteDatabaseHelper(getContext());
        mainHandler = new Handler(Looper.getMainLooper());

        // Configurar RecyclerView
        binding.recyclerViewFavoritos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new MovieAdapter(getContext(), pelisFavoritas, idUsuario, database, favoritos);
        binding.recyclerViewFavoritos.setAdapter(adapter);

        cargarPeliculasFavoritas();



        binding.buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions();
            }
        });



        return root;
    }

    private void requestPermissions() {

        String cadenaJSON = "";

        // Convertir el ArrayList a JSON
        Gson gson = new Gson();
        cadenaJSON = gson.toJson(pelisFavoritas);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                    },
                    REQUEST_CODE_PERMISSIONS
            );

            AlertDialog dialogoShare = crearDiaogoInstrucciones(cadenaJSON);
            dialogoShare.show();

        } else {
            AlertDialog dialogoShare = crearDiaogoInstrucciones(cadenaJSON);
            dialogoShare.show();
        }

    }

    private AlertDialog crearDiaogoInstrucciones(String listaPersonas) {

        // Inicializar Variables
        AlertDialog dialogo = null;

        // Convertir el archivo XML del diseño del diálogo en un objeto View para poder utilizarlo
        View alertCustomDialog = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog_compartir,null);

        // Constructor del diálogo
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());

        // Establecer la vista personalizada como el contenido del diálogo
        alertDialog.setView(alertCustomDialog);

        // Inicializar variables
        ImageButton cancelButton = alertCustomDialog.findViewById(R.id.cancelID);

        // Asignar valores al diálogo
        TextView txtTitle = alertCustomDialog.findViewById(R.id.TextViewTitle);
        TextView txtContenido = alertCustomDialog.findViewById(R.id.TextViewContent) ;
        txtContenido.setText(listaPersonas);
        txtTitle.setText("Películas favoritas en JSON");

        // Crear el Diálogo
        dialogo = alertDialog.create();

        // Establecer fondo del diálogo transparente
        dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Añadir evento cuando se pulsa el icono de salir
        AlertDialog finalDialogo = dialogo;
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialogo.cancel();
            }
        });

        // Devolver el dialogo creado
        return dialogo;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permisos otorgados", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permisos denegados", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void cargarPeliculasFavoritas() {
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