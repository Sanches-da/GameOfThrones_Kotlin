package ru.skillbranch.gameofthrones.data.local.entities

import androidx.room.*
import ru.skillbranch.gameofthrones.data.local.database.Converters

@Entity
@TypeConverters(Converters::class)
data class Character(
    @PrimaryKey
    val id: String,
    val name: String,
    val gender: String,
    val culture: String,
    val born: String,
    val died: String,
    val titles: List<String> = listOf(),
    val aliases: List<String> = listOf(),
    val father: String, //rel
    val mother: String, //rel
    val spouse: String, //rel
    val houseId: String//rel
)

@DatabaseView(
    value="SELECT character.id, character.houseId, character.name, character.titles, character.aliases FROM character",
    viewName = "character_item"
)
@TypeConverters(Converters::class)
data class CharacterItem(
    val id: String,
    @ColumnInfo(name="houseId")
    val house: HouseType, //rel
    val name: String,
    val titles: List<String>,
    val aliases: List<String>
)

@DatabaseView(
    value = """SELECT character.id, character.name, character.born, character.died, character.titles, character.aliases, character.houseId, house.words, mother.name AS m_name, mother.id AS m_id , mother.houseId AS m_house , father.name AS f_name, father.id AS f_id, father.houseId AS f_house  FROM character
    LEFT JOIN character AS mother ON character.mother = mother.id
    LEFT JOIN character AS father ON character.father = father.id
    INNER JOIN house ON character.houseId = house.id""",
    viewName = "character_full"
)
@TypeConverters(Converters::class)
data class CharacterFull(
    val id: String,
    val name: String,
    val words: String,
    val born: String,
    val died: String,
    val titles: List<String>,
    val aliases: List<String>,
    @ColumnInfo(name="houseId")
    val house:String,
    @Embedded(prefix = "f_")
    val father: RelativeCharacter?,
    @Embedded(prefix = "m_")
    val mother: RelativeCharacter?
)

data class RelativeCharacter(
    val id: String,
    val name: String,
    val house: String
)
