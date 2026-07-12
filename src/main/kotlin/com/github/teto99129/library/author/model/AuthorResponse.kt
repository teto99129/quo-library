package com.github.teto99129.library.author.model

import java.time.LocalDate

data class AuthorResponse(
	val authorId: Int,
	val name: String,
	val birthday: LocalDate
)
