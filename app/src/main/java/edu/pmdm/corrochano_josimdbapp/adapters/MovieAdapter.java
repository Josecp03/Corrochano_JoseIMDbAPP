package edu.pmdm.corrochano_josimdbapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.pmdm.corrochano_josimdbapp.MovieDetailsActivity;
import edu.pmdm.corrochano_josimdbapp.R;
import edu.pmdm.corrochano_josimdbapp.models.Movie;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final Context context;
    private final List<Movie> movieList;

    public MovieAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
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

        // Cargar la imagen del poster usando Glide
        Glide.with(context)
                .load(movie.getPosterPath())
                .into(holder.posterImageView);

        // Listener para cuando se pulsa sobre la película
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, MovieDetailsActivity.class);
                i.putExtra("pelicula", movie);
                context.startActivity(i);
            }
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
}
