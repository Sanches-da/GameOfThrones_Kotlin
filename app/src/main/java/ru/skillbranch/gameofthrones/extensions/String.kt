package ru.skillbranch.gameofthrones.extensions

fun String.getIdfromURL() : String?{
    return this.split("/").lastOrNull()
}