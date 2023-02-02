package com.fakedevelopers.data.service

import com.fakedevelopers.domain.model.TermListDto
import retrofit2.http.GET
import retrofit2.http.Path

interface RegistrationTermService {
    @GET("term/list")
    suspend fun getRegistrationTermList(): TermListDto

    @GET("term/{id}")
    suspend fun getRegistrationTermContents(
        @Path("id") id: Long
    ): String
}
