package ru.skillbranch.gameofthrones.data.local.database

import androidx.room.*
import ru.skillbranch.gameofthrones.data.local.entities.House

@Dao
interface HouseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg house: House)

    @Delete
    fun delete(house: House)

    @Query("SELECT * FROM house")
    fun getAllHouses(): List<House>

    @Query("DELETE FROM house")
    fun deleteAllHouses()

//    @Query("SELECT * FROM character WHERE id = :character_id LIMIT 1")
//    fun getCharacterById(character_id: String): Character
//
//    @Query("SELECT * FROM character WHERE houseId = :house_id LIMIT 1")
//    fun getCharactersByHouseName(house_id: String): List<Character>

}