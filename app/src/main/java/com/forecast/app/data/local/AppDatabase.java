package com.forecast.app.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.forecast.app.models.PomodoroSession;
import com.forecast.app.models.Task;

@Database(
    entities = {Task.class, PomodoroSession.class},
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "productivity_db";
    private static volatile AppDatabase instance;

    // ── Singleton ─────────────────────────────────────────────────────────────

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return instance;
    }

    // ── DAO access ────────────────────────────────────────────────────────────

    public abstract TaskDao taskDao();
    public abstract SessionDao sessionDao();
}
