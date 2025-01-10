package edu.pmdm.corrochano_josimdbapp.api;

import edu.pmdm.corrochano_josimdbapp.models.GeneroResponse;
import edu.pmdm.corrochano_josimdbapp.models.MovieSearchResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TMDbApiService {

    @GET("movie/changes")
    Call<MovieSearchResponse> getMovieChanges(@Query("page") int page);

    @GET("genre/movie/list")
    Call<GeneroResponse> getGenres(@Query("language") String language);
}
