package edu.pmdm.corrochano_josimdbapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.pmdm.corrochano_josimdbapp.MovieDetailsActivity;
import edu.pmdm.corrochano_josimdbapp.R;
import edu.pmdm.corrochano_josimdbapp.database.FavoriteDatabaseHelper;
import edu.pmdm.corrochano_josimdbapp.models.Movie;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final Context context;
    private final List<Movie> movieList;
    private final String idUsuario;
    private final FavoriteDatabaseHelper databaseHelper;
    private final boolean favoritos;

    public MovieAdapter(Context context, List<Movie> movieList, String idUsuario, FavoriteDatabaseHelper databaseHelper, boolean favoritos) {
        this.context = context;
        this.movieList = movieList;
        this.idUsuario = idUsuario;
        this.databaseHelper = databaseHelper;
        this.favoritos = favoritos;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        Glide.with(context).load(movie.getPosterPath()).into(holder.posterImageView);

        // Listener para cuando hago click sobre una película
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailsActivity.class);
            intent.putExtra("pelicula", movie);
            context.startActivity(intent);
        });

        // Listener para cuando hago longClick sobre una película
        holder.itemView.setOnLongClickListener(v -> {
            if (!favoritos) {
                agregarFavorito(movie, holder.getAdapterPosition());
            } else {
                eliminarFavorito(movie, holder.getAdapterPosition());
            }
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImageView;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.ImageViewPelicula);
        }
    }

    private void agregarFavorito(Movie movie, int position) {

        SQLiteDatabase dbWrite = databaseHelper.getWritableDatabase();

        long result = databaseHelper.insertarFavorito(
                dbWrite,
                idUsuario,
                movie.getId(),
                movie.getTitle(),
                movie.getPosterPath()
        );

        dbWrite.close();

        if (result != -1) {
            Toast.makeText(context, "Agregada a favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error al agregar a favoritos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarFavorito(Movie movie, int position) {
        SQLiteDatabase dbWrite = databaseHelper.getWritableDatabase();
        int rowsDeleted = dbWrite.delete(
                FavoriteDatabaseHelper.TABLE_FAVORITOS,
                "idUsuario=? AND idPelicula=?",
                new String[]{idUsuario, movie.getId()}
        );
        dbWrite.close();

        if (rowsDeleted > 0) {
            Toast.makeText(context, movie.getTitle() + " eliminado de favoritos", Toast.LENGTH_SHORT).show();
            movieList.remove(position);
            notifyItemRemoved(position);
        } else {
            Toast.makeText(context, "Error al eliminar de favoritos.", Toast.LENGTH_SHORT).show();
        }
    }
}
