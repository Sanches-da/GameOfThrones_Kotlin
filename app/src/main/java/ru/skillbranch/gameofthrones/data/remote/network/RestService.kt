package ru.skillbranch.gameofthrones.data.remote.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.skillbranch.gameofthrones.data.remote.res.CharacterRes
import ru.skillbranch.gameofthrones.data.remote.res.HouseRes

interface RestService {
    @GET("houses/")
    suspend fun getAllHouses(@Query("pageSize")pageSize:Int, @Query("page")page:Int) : List<HouseRes>

    @GET("characters/{id}")
    suspend fun getCharacter(@Path("id")characterId:String) : CharacterRes

    @GET("houses")
    suspend fun getHousesByName(@Query("name") name: String): List<HouseRes>
}