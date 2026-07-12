package com.github.teto99129.library.book.controller

import com.github.teto99129.library.book.model.BookResponse
import com.github.teto99129.library.book.model.PatchBookRequest
import com.github.teto99129.library.book.model.PostBookRequest
import com.github.teto99129.library.book.service.BookService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class BookController(private val _service: BookService) {

	@PostMapping("/book")
	fun registerBook(@RequestBody body: PostBookRequest): BookResponse {
		val book = this._service.registerBook(body.title, body.value, body.authors, body.publicationStatus)
		return BookResponse.from(book)
	}

	@PatchMapping("/book/{bookId}")
	fun updateBook(
		@PathVariable bookId: Int,
		@RequestBody body: PatchBookRequest
	): BookResponse {
		if (!body.hasAnyUpdate()) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field must be provided for update")
		}
		val book = this._service.updateBook(bookId, body)
		return BookResponse.from(book)
	}

}
