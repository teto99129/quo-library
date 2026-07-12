package com.github.teto99129.library.book.service

import com.github.teto99129.library.author.repository.AuthorRepository
import com.github.teto99129.library.book.model.Book
import com.github.teto99129.library.book.model.BookAuthors
import com.github.teto99129.library.book.model.PatchBookRequest
import com.github.teto99129.library.book.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
	private val repository: BookRepository,
	private val authorRepository: AuthorRepository
) {

	@Transactional
	fun registerBook(title: String, value: Int, authors: List<Int>, publicationStatus: Short): Book {
		val book = this.repository.insertBook(
			title = title,
			value = value,
			publicationStatus = publicationStatus
		)
		this.repository.insertBookAuthors(
			bookId = book.bookID,
			authors = authors
		)
		val authorDetails = this.authorRepository.findAuthorsByIds(authors)
		return book.copy(authors = authorDetails)
	}

	@Transactional
	fun updateBook(bookId: Int, request: PatchBookRequest): Book {
		val book = this.repository.updateBook(
			bookId = bookId,
			title = request.title,
			value = request.value,
			publicationStatus = request.publicationStatus
		)

		if (request.authors != null) {
			this.repository.deleteBookAuthors(bookId)
			this.repository.insertBookAuthors(bookId, request.authors)
		}

		val authorDetails = this.authorRepository.findAuthorsByBookId(bookId)
		return book.copy(authors = authorDetails)
	}

	fun registerBookAuthors(bookId: Int, authors: List<Int>): BookAuthors {
		return this.repository.insertBookAuthors(
			bookId = bookId,
			authors = authors
		)
	}

}
