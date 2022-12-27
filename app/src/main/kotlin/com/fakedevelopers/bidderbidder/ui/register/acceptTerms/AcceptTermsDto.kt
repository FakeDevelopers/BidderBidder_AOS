package com.fakedevelopers.bidderbidder.ui.register.acceptTerms

data class TermListDto(
    val optional: List<TermItemDto>,
    val required: List<TermItemDto>
)
data class TermItemDto(
    val id: Long,
    val name: String
)
