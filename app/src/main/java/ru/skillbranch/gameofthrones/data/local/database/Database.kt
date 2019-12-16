package ru.skillbranch.gameofthrones.data.local.database

import android.content.Context
import androidx.room.*
import ru.skillbranch.gameofthrones.data.local.entities.*

@Database(
    entities = [Character::class, House::class],
    views = [CharacterFull::class, CharacterItem::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun houseDao(): HouseDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) : AppDatabase {
            val db = Room.databaseBuilder(context.applicationContext,
                AppDatabase::class.java, "got_db.db")
            return db.build()
        }
    }
}

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",")?.toList()?:ArrayList()
    }

    @TypeConverter
    fun fromArrayList(list: List<String>?): String {
        return list?.run{
           this.joinToString(",")
        }?:""
    }

    @TypeConverter
    fun houseFromString(value: String?): HouseType {
        return HouseType.fromString(value?:"")
    }

    @TypeConverter
    fun houseToString(value: HouseType?): String {
        return value?.title ?: ""
    }

}