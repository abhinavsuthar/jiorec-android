package com.hackathon.iitb.model

import java.io.Serializable

data class Show(val genres: List<Genre>, val id: Long, val image_url: String, val name: String) : Serializable
data class Genre(val id: Int, val name: String) : Serializable

data class Product(
    val brand: String,
    val id: Int,
    val link: String,
    val name: String,
    val price: Int,
    val tags: String
) : Serializable

const val username = "suthar"
const val password = "admin@123"
const val server = "10.197.4.171"