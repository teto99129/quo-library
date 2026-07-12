package com.github.teto99129.library.author.model

import java.time.LocalDate

data class PatchAuthorRequest(
	val name: String?,
	val birthday: LocalDate?
)
