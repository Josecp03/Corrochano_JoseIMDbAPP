package edu.pmdm.corrochano_josimdbapp.ui.favorites;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.corrochano_josimdbapp.R;
import edu.pmdm.corrochano_josimdbapp.adapters.MovieAdapter;
import edu.pmdm.corrochano_josimdbapp.database.FavoriteDatabaseHelper;
import edu.pmdm.corrochano_josimdbapp.databinding.FragmentFavoritesBinding;
import edu.pmdm.corrochano_josimdbapp.models.Movie;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;
    private Handler mainHandler;

    private boolean favoritos = true;
    private String idUsuario;
    private List<Movie> pelisFavoritas = new ArrayList<>();
    private FavoriteDatabaseHelper database;
    private MovieAdapter adapter;

    private ActivityResultLauncher<String[]> requestPermissionsLauncher;
    private ActivityResultLauncher<Intent> enableBluetoothLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
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

        // Registrar lanzadores para permisos y activar Bluetooth
        registerLaunchers();

        binding.buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissionsAndShare();
            }
        });

        return root;
    }

    private void registerLaunchers() {
        // Lanzador para solicitar permisos
        requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Boolean granted : result.values()) {
                        allGranted = allGranted && granted;
                    }
                    if (allGranted) {
                        // Permisos otorgados, verificar Bluetooth
                        checkAndEnableBluetooth();
                    } else {
                        Toast.makeText(getContext(), "Permisos denegados", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Lanzador para solicitar activar Bluetooth
        enableBluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        // Bluetooth activado, mostrar diálogo
                        showShareDialog();
                    } else {
                        Toast.makeText(getContext(), "Bluetooth no fue activado", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void requestPermissionsAndShare() {
        // Verificar si ya se tienen los permisos
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            // Solicitar permisos
            requestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            });

        } else {
            // Permisos ya otorgados, verificar Bluetooth
            checkAndEnableBluetooth();
        }
    }

    private void checkAndEnableBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                // Solicitar al usuario que active Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBluetoothLauncher.launch(enableBtIntent);
            } else {
                // Bluetooth ya está activado, mostrar diálogo
                showShareDialog();
            }
        } else {
            Toast.makeText(getContext(), "Bluetooth no está soportado en este dispositivo.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showShareDialog() {

        if (pelisFavoritas.isEmpty()) {

            Toast.makeText(getContext(), "Error al compartir. No hay películas en favoritos", Toast.LENGTH_SHORT).show();

        } else {

            // Convertir el ArrayList a JSON
            Gson gson = new Gson();
            String cadenaJSON = gson.toJson(pelisFavoritas);

            AlertDialog dialogoShare = crearDiaogoInstrucciones(cadenaJSON);
            dialogoShare.show();

        }


    }

    private AlertDialog crearDiaogoInstrucciones(String listaPeliculasJSON) {

        // Inicializar Variables
        AlertDialog dialogo = null;

        // Convertir el archivo XML del diseño del diálogo en un objeto View para poder utilizarlo
        View alertCustomDialog = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog_compartir, null);

        // Constructor del diálogo
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());

        // Establecer la vista personalizada como el contenido del diálogo
        alertDialog.setView(alertCustomDialog);

        // Inicializar variables
        ImageButton cancelButton = alertCustomDialog.findViewById(R.id.cancelID);

        // Asignar valores al diálogo
        TextView txtTitle = alertCustomDialog.findViewById(R.id.TextViewTitle);
        TextView txtContenido = alertCustomDialog.findViewById(R.id.TextViewContent);
        txtContenido.setText(listaPeliculasJSON);
        txtTitle.setText("Películas favoritas en JSON");

        // Crear el Diálogo
        dialogo = alertDialog.create();

        // Establecer fondo del diálogo transparente
        if (dialogo.getWindow() != null) {
            dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Añadir evento cuando se pulsa el icono de salir
        ImageButton finalCancelButton = cancelButton;
        AlertDialog finalDialogo = dialogo;
        finalCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialogo.cancel();
            }
        });

        // Devolver el dialogo creado
        return dialogo;
    }

    private void cargarPeliculasFavoritas() {
        new Thread(() -> {

            SQLiteDatabase db = database.getReadableDatabase();

            Cursor cursor = db.rawQuery(
                    "SELECT idPelicula, nombrePelicula, portadaURL FROM " + FavoriteDatabaseHelper.TABLE_FAVORITOS + " WHERE idUsuario=?",
                    new String[]{idUsuario}
            );

            pelisFavoritas.clear();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String idPelicula = cursor.getString(cursor.getColumnIndex("idPelicula"));
                    @SuppressLint("Range") String titulo = cursor.getString(cursor.getColumnIndex("nombrePelicula"));
                    @SuppressLint("Range") String portada = cursor.getString(cursor.getColumnIndex("portadaURL"));

                    Movie movie = new Movie();
                    movie.setId(idPelicula);
                    movie.setTitle(titulo);
                    movie.setPosterPath(portada);

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
