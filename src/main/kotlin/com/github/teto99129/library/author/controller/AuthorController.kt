package com.github.teto99129.library.author.controller

import com.github.teto99129.library.author.model.AuthorResponse
import com.github.teto99129.library.author.model.PatchAuthorRequest
import com.github.teto99129.library.author.model.PostAuthorRequest
import com.github.teto99129.library.author.service.AuthorService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthorController(
	private val service: AuthorService,
) {
	@PostMapping("/author")
	fun registerAuthor(
		@RequestBody
		@Valid body: PostAuthorRequest,
	): AuthorResponse {
		val author = this.service.registerAuthor(body.name, body.birthday)
		return AuthorResponse(
			authorId = author.authorId,
			name = author.name,
			birthday = author.birthday,
		)
	}

	@PatchMapping("/author/{authorId}")
	fun updateAuthor(
		@PathVariable authorId: Int,
		@RequestBody
		@Valid body: PatchAuthorRequest,
	): AuthorResponse {
		val author = this.service.updateAuthor(authorId, body.name, body.birthday)
		return AuthorResponse(
			authorId = author.authorId,
			name = author.name,
			birthday = author.birthday,
		)
	}
}
