package ru.skillbranch.gameofthrones.data.local.database

import androidx.room.*
import ru.skillbranch.gameofthrones.data.local.entities.Character
import ru.skillbranch.gameofthrones.data.local.entities.CharacterFull
import ru.skillbranch.gameofthrones.data.local.entities.CharacterItem

@Dao
interface CharacterDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg character: Character)

    @Delete
    fun delete(character: Character)

    @Query("SELECT * FROM character")
    fun getAllCharacters(): List<Character>

    @Query("SELECT * FROM character WHERE id = :character_id LIMIT 1")
    fun getCharacterById(character_id: String): Character

    @Query("SELECT * FROM character_full WHERE id = :character_id LIMIT 1")
    fun getCharacterFullById(character_id: String): CharacterFull

    @Query("SELECT * FROM character_item WHERE houseId = :house_id")
    fun getCharactersByHouseName(house_id: String): List<CharacterItem>

    @Query("DELETE FROM character")
    fun deleteAllCharacters()
}
