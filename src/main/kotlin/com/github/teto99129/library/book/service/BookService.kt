package com.github.teto99129.library.book.service

import com.github.teto99129.library.book.model.Book
import com.github.teto99129.library.book.model.BookAuthors
import com.github.teto99129.library.book.model.PatchBookRequest
import com.github.teto99129.library.book.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
	private val repository: BookRepository
) {

	@Transactional
	fun registerBook(title: String, value: Int, authors: List<Int>, publicationStatus: Short): Book {
		return this.repository.insertBook(
			title = title,
			value = value,
			publicationStatus = publicationStatus,
			authors = authors
		)
	}

	@Transactional
	fun updateBook(bookId: Int, request: PatchBookRequest): Book {
		return this.repository.updateBook(
			bookId = bookId,
			title = request.title,
			value = request.value,
			publicationStatus = request.publicationStatus,
			authors = request.authors
		)
	}

	fun registerBookAuthors(bookId: Int, authors: List<Int>): BookAuthors {
		return this.repository.insertBookAuthors(
			bookId = bookId,
			authors = authors
		)
	}

	fun getBook(authId: Int?, authName: String?): List<Book> {
		return this.repository.getBook(
			authId = authId,
			authName = authName,
		)
	}

}
