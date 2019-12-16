package ru.skillbranch.gameofthrones.repositories

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import ru.skillbranch.gameofthrones.App
import ru.skillbranch.gameofthrones.AppConfig
import ru.skillbranch.gameofthrones.data.local.entities.Character
import ru.skillbranch.gameofthrones.data.local.entities.CharacterFull
import ru.skillbranch.gameofthrones.data.local.entities.CharacterItem
import ru.skillbranch.gameofthrones.data.local.entities.House
import ru.skillbranch.gameofthrones.data.remote.network.NetworkService
import ru.skillbranch.gameofthrones.data.remote.res.CharacterRes
import ru.skillbranch.gameofthrones.data.remote.res.HouseRes
import ru.skillbranch.gameofthrones.extensions.getIdfromURL
import ru.skillbranch.gameofthrones.extensions.mutableLiveData
import java.util.logging.Logger.global

object RootRepository {

    /**
     * Получение данных о всех домах из сети
     * @param result - колбек содержащий в себе список данных о домах
     */
    //@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getAllHouses(result: (houses: List<HouseRes>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val resList: List<HouseRes> = listOf()
            var iter = 0
            do {
                val tmpList = NetworkService.api.getAllHouses(50, iter)
                if (tmpList.isEmpty()) break
                resList.plus(tmpList)
                iter++
            }while (true)

            result(resList)
        }
    }

    /**
     * Получение данных о требуемых домах по их полным именам из сети
     * @param houseNames - массив полных названий домов (смотри AppConfig)
     * @param result - колбек содержащий в себе список данных о домах
     */
    //@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getNeedHouses(vararg houseNames: String, result: (houses: List<HouseRes>) -> Unit) {
        getAllHouses { houses ->  result(houses.filter { houseNames.contains(it.name) })}
    }


    suspend fun getCharacterRes(characterId: String) : CharacterRes {
        return NetworkService.api.getCharacter(characterId)
    }

    /**
     * Получение данных о требуемых домах по их полным именам и персонажах в каждом из домов из сети
     * @param houseNames - массив полных названий домов (смотри AppConfig)
     * @param result - колбек содержащий в себе список данных о доме и персонажей в нем (Дом - Список Персонажей в нем)
     */
    //@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun getNeedHouseWithCharacters(
        vararg houseNames: String,
        result: (houses: List<Pair<HouseRes, List<CharacterRes>>>) -> Unit
    ) {
            getNeedHouses(*houseNames){houses ->
                val res: MutableList<Pair<HouseRes, List<CharacterRes>>> = mutableListOf()
                houses.forEach {
                    val characters: MutableList<CharacterRes> = mutableListOf()
                    res.add(it to characters)
                    it.swornMembers
                    .map { it_char -> it_char.getIdfromURL() ?: "" }
                    .filter { it_id -> it_id.isNotEmpty() }
                    .forEach{ it_id ->
                        GlobalScope.launch(Dispatchers.IO) {
                            getCharacterRes(it_id)
                                .apply {houseId = getHouseShortName(it.name)}
                                .also { chr -> characters.add(chr) }
                        }
                    }
                }
                result(res)

            }
    }

    /**
     * Запись данных о домах в DB
     * @param houses - Список персонажей (модель HouseRes - модель ответа из сети)
     * необходимо произвести трансформацию данных
     * @param complete - колбек о завершении вставки записей db
     */
    //@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun insertHouses(houses: List<HouseRes>, complete: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val dao = App.getDatabase().houseDao()
            houses.forEach {
                dao.insertAll(it.toHouse())
            }
            complete()
        }
    }

    /**
     * Запись данных о пересонажах в DB
     * @param Characters - Список персонажей (модель CharacterRes - модель ответа из сети)
     * необходимо произвести трансформацию данных
     * @param complete - колбек о завершении вставки записей db
     */
    //@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun insertCharacters(characters: List<CharacterRes>, complete: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val dao = App.getDatabase().characterDao()
            characters.forEach {
                dao.insertAll(it.toCharacter())
            }
            complete()
        }
    }

    /**
     * При вызове данного метода необходимо выполнить удаление всех записей в db
     * @param complete - колбек о завершении очистки db
     */
    //@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun dropDb(complete: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            App.getDatabase().characterDao().deleteAllCharacters()
            App.getDatabase().houseDao().deleteAllHouses()
            complete()
        }
    }

    /**
     * Поиск всех персонажей по имени дома, должен вернуть список краткой информации о персонажах
     * дома - смотри модель CharacterItem
     * @param name - краткое имя дома (его первычный ключ)
     * @param result - колбек содержащий в себе список краткой информации о персонажах дома
     */
    // @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun findCharactersByHouseName(name: String, result: (characters: List<CharacterItem>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            result(
                App.getDatabase().characterDao().getCharactersByHouseName(
                    name
                )
            )
        }
    }

    /**
     * Поиск персонажа по его идентификатору, должен вернуть полную информацию о персонаже
     * и его родственных отношения - смотри модель CharacterFull
     * @param id - идентификатор персонажа
     * @param result - колбек содержащий в себе полную информацию о персонаже
     */
    //@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun findCharacterFullById(id: String, result: (character: CharacterFull) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            result(
                App.getDatabase().characterDao().getCharacterFullById(
                    id
                )
            )
        }
    }

    /**
     * Метод возвращет true если в базе нет ни одной записи, иначе false
     * @param result - колбек о завершении очистки db
     */

    fun isNeedUpdate(result: (isNeed: Boolean) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) { result(App.getDatabase().houseDao().getAllHouses().isEmpty()) }
    }

    suspend fun isNeedUpdate(): Boolean {
        return App.getDatabase().houseDao().getAllHouses().isEmpty()
    }

    fun getCharacter(characterId: String): LiveData<CharacterFull> {
        val result = MutableLiveData<CharacterFull>()
        GlobalScope.launch(Dispatchers.IO) {
            result.postValue(App.getDatabase().characterDao().getCharacterFullById(characterId))
        }
        return result
    }

    suspend fun sync() {
        getAllHouses { housesList ->
            housesList.filter { AppConfig.NEED_HOUSES.contains(it.name) }

            val res: Map<String, CharacterRes> = HashMap()
            val relatives: ArrayList<String> = ArrayList()

            val houseDAO = App.getDatabase().houseDao()
            val characterDAO = App.getDatabase().characterDao()

            housesList.forEach {
                houseDAO.insertAll(it.toHouse())
                it.swornMembers
                    .map { it_char -> it_char.getIdfromURL() ?: "" }
                    .filter { it_id -> it_id.isNotEmpty() }
                    .forEach { it_id ->
                        if (res[it_id] == null) {
                            GlobalScope.launch(Dispatchers.IO) {
                                getCharacterRes(it_id)
                                    .apply { houseId = getHouseShortName(it.name) }
                                    .also { chr ->
                                        characterDAO.insertAll(chr.toCharacter())
                                        val fatherId = chr.father.getIdfromURL()
                                        if (!fatherId.isNullOrEmpty() && !relatives.contains(
                                                fatherId
                                            ) && res[fatherId] == null
                                        ) relatives.add(
                                            fatherId
                                        )
                                        val motherId = chr.mother.getIdfromURL()
                                        if (!motherId.isNullOrEmpty() && !relatives.contains(
                                                motherId
                                            ) && res[motherId] == null
                                        ) relatives.add(
                                            motherId
                                        )
                                        val spouseId = chr.spouse.getIdfromURL()
                                        if (!spouseId.isNullOrEmpty() && !relatives.contains(
                                                spouseId
                                            ) && res[spouseId] == null
                                        ) relatives.add(
                                            spouseId
                                        )
                                    }
                            }
                        }
                    }
                relatives.filter { it_id -> res[it_id] == null }
                    .forEach { it_id ->
                        GlobalScope.launch(Dispatchers.IO) {
                            characterDAO.insertAll(getCharacterRes(it_id).apply { houseId = "" }.toCharacter())
                        }
                    }
            }
        }

    }

    fun findCharacters(houseName: String): LiveData<List<CharacterItem>> {
        val result = MutableLiveData<List<CharacterItem>>()
        GlobalScope.launch (Dispatchers.IO){
            result.postValue(App.getDatabase().characterDao().getCharactersByHouseName(houseName))
        }
        return result
    }

    fun getHouseShortName(fullName:String):String {
        val tmp = fullName.split(" ")
        return tmp.getOrNull(tmp.indexOf("of")-1)?:"Stark"
    }


}
