package com.github.teto99129.library.author.model

import jakarta.validation.constraints.Past
import java.time.LocalDate

data class PatchAuthorRequest(
	val name: String?,
	@field:Past(message = "生年月日は過去の日付を指定してください")
	val birthday: LocalDate?,
)
