package com.github.teto99129.library.author.repository

import com.github.teto99129.library.author.model.Author
import java.time.LocalDate

interface AuthorRepository {
	fun insertAuthor(name: String, birthday: LocalDate): Author
	fun findAuthorsByIds(ids: List<Int>): List<Author>
	fun findAuthorsByBookId(bookId: Int): List<Author>
	fun updateAuthor(authorId: Int, name: String?, birthday: LocalDate?): Author
}
