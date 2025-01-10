package edu.pmdm.corrochano_josimdbapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class FavoriteDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NOMBRE = "peliculas.db";
    public static final String TABLE_FAVORITOS = "t_favoritos";

    public FavoriteDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NOMBRE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FAVORITOS + "(" +
                "idPelicula TEXT NOT NULL," +
                "idUsuario TEXT NOT NULL," +
                "nombrePelicula TEXT NOT NULL ," +
                "descripcionPelicula TEXT NOT NULL," +
                "fechaLanzamiento TEXT NOT NULL," +
                "rankingPelicula TEXT NOT NULL," + // Guardamos el ranking como texto
                "portadaURL TEXT NOT NULL," +
                "PRIMARY KEY (idUsuario, idPelicula))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_FAVORITOS);
        onCreate(db);
    }

    public long insertarFavorito(SQLiteDatabase db,
                                 String idUsuario,
                                 String idPelicula,
                                 String nombrePelicula,
                                 String descripcionPelicula,
                                 String fechaLanzamiento,
                                 String rankingPelicula,
                                 String portadaURL) {

        ContentValues valores = new ContentValues();

        valores.put("idPelicula", idPelicula);
        valores.put("idUsuario", idUsuario);  // Asegúrate de que este valor no sea null ni incorrecto
        valores.put("nombrePelicula", nombrePelicula);
        valores.put("descripcionPelicula", descripcionPelicula);
        valores.put("fechaLanzamiento", fechaLanzamiento);
        valores.put("rankingPelicula", rankingPelicula);  // Asegúrate de que sea un String
        valores.put("portadaURL", portadaURL);

        return db.insert(TABLE_FAVORITOS, null, valores);  // Este valor es el ID de la fila insertada, debería ser > 0 si la inserción fue exitosa
    }

    public int borrarTodosLosFavoritos(SQLiteDatabase db) {
        return db.delete(TABLE_FAVORITOS, null, null);
    }

}