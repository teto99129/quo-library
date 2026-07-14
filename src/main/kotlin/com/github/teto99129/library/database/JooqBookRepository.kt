package com.github.teto99129.library.database

import com.github.teto99129.library.author.model.Author
import com.github.teto99129.library.book.model.Book
import com.github.teto99129.library.book.model.BookAuthors
import com.github.teto99129.library.book.repository.BookRepository
import com.github.teto99129.library.jooq.tables.records.BooksRecord
import com.github.teto99129.library.jooq.tables.references.AUTHORS
import com.github.teto99129.library.jooq.tables.references.BOOKS
import com.github.teto99129.library.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
import org.jooq.impl.DSL.multiset
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class JooqBookRepository(private val dsl: DSLContext): BookRepository {
	override fun insertBook(title: String, value: Int, publicationStatus: Short, authors: List<Int>): Book {
		val record: BooksRecord =  dsl.insertInto(BOOKS)
			.columns(BOOKS.TITLE, BOOKS.VALUE, BOOKS.PUBLICATION_STATUS, BOOKS.CREATED_AT)
			.values(title, value, publicationStatus, OffsetDateTime.now())
			.returning()
			.fetchOne() ?: throw IllegalStateException("Failed to insert book")

		val bookId = record.bookId!!
		insertBookAuthors(bookId, authors)

		return getBookById(bookId) ?: throw IllegalStateException("Failed to fetch inserted book with ID: $bookId")
	}

	override fun insertBookAuthors(bookId: Int, authors: List<Int>): BookAuthors {
		if (authors.isEmpty()) {
			return BookAuthors(bookId, emptyList())
		}

		val queries = authors.map { authorId ->
			dsl.insertInto(BOOK_AUTHORS)
				.columns(BOOK_AUTHORS.BOOK_ID, BOOK_AUTHORS.AUTHOR_ID)
				.values(bookId, authorId)
		}
		dsl.batch(queries).execute()

		return BookAuthors(bookId, authors)
	}

	override fun updateBook(bookId: Int, title: String?, value: Int?, publicationStatus: Short?, authors: List<Int>?): Book {
		val updateValues = mutableMapOf<Field<*>, Any?>()
		if (title != null) updateValues[BOOKS.TITLE] = title
		if (value != null) updateValues[BOOKS.VALUE] = value
		if (publicationStatus != null) updateValues[BOOKS.PUBLICATION_STATUS] = publicationStatus

		if (updateValues.isNotEmpty()) {
			dsl.update(BOOKS)
				.`set`(updateValues)
				.where(BOOKS.BOOK_ID.eq(bookId))
				.execute()
		}

		if (authors != null) {
			deleteBookAuthors(bookId)
			insertBookAuthors(bookId, authors)
		}

		return getBookById(bookId) ?: throw IllegalStateException("Failed to fetch updated book with ID: $bookId")
	}

	override fun deleteBookAuthors(bookId: Int) {
		dsl.deleteFrom(BOOK_AUTHORS)
			.where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
			.execute()
	}

	override fun getBook(authId: Int?, authName: String?): List<Book> {
		val authCondition = if (authId != null || authName != null) {
			DSL.exists(
				select(DSL.one())
					.from(BOOK_AUTHORS)
					.join(AUTHORS).on(BOOK_AUTHORS.AUTHOR_ID.eq(AUTHORS.AUTHOR_ID))
					.where(BOOK_AUTHORS.BOOK_ID.eq(BOOKS.BOOK_ID))
					.and(if (authId != null) BOOK_AUTHORS.AUTHOR_ID.eq(authId) else DSL.noCondition())
					.and(if (authName != null) AUTHORS.NAME.like("%$authName%") else DSL.noCondition())
			)
		} else {
			DSL.noCondition()
		}

		val authorsField = multiset(
			select(AUTHORS.AUTHOR_ID, AUTHORS.NAME, AUTHORS.BIRTHDAY)
				.from(AUTHORS)
				.join(BOOK_AUTHORS).on(AUTHORS.AUTHOR_ID.eq(BOOK_AUTHORS.AUTHOR_ID))
				.where(BOOK_AUTHORS.BOOK_ID.eq(BOOKS.BOOK_ID))
		).convertFrom { r ->
			r.map { record ->
				Author(
					authorId = record.get(AUTHORS.AUTHOR_ID)!!,
					name = record.get(AUTHORS.NAME)!!,
					birthday = record.get(AUTHORS.BIRTHDAY)!!
				)
			}
		}.`as`("authors")

		return dsl.select(
			BOOKS.BOOK_ID,
			BOOKS.TITLE,
			BOOKS.VALUE,
			BOOKS.PUBLICATION_STATUS,
			BOOKS.CREATED_AT,
			authorsField
		)
		.from(BOOKS)
		.where(authCondition)
		.fetch { record ->
			Book(
				bookID = record.get(BOOKS.BOOK_ID)!!,
				title = record.get(BOOKS.TITLE)!!,
				value = record.get(BOOKS.VALUE)!!,
				publicationStatus = record.get(BOOKS.PUBLICATION_STATUS)!!,
				createdAt = record.get(BOOKS.CREATED_AT)!!,
				authors = record.get(authorsField) ?: emptyList()
			)
		}
	}

	private fun getBookById(bookId: Int): Book? {
		val authorsField = multiset(
			select(AUTHORS.AUTHOR_ID, AUTHORS.NAME, AUTHORS.BIRTHDAY)
				.from(AUTHORS)
				.join(BOOK_AUTHORS).on(AUTHORS.AUTHOR_ID.eq(BOOK_AUTHORS.AUTHOR_ID))
				.where(BOOK_AUTHORS.BOOK_ID.eq(BOOKS.BOOK_ID))
		).convertFrom { r ->
			r.map { record ->
				Author(
					authorId = record.get(AUTHORS.AUTHOR_ID)!!,
					name = record.get(AUTHORS.NAME)!!,
					birthday = record.get(AUTHORS.BIRTHDAY)!!
				)
			}
		}.`as`("authors")

		return dsl.select(
			BOOKS.BOOK_ID,
			BOOKS.TITLE,
			BOOKS.VALUE,
			BOOKS.PUBLICATION_STATUS,
			BOOKS.CREATED_AT,
			authorsField
		)
		.from(BOOKS)
		.where(BOOKS.BOOK_ID.eq(bookId))
		.fetchOne { record ->
			Book(
				bookID = record.get(BOOKS.BOOK_ID)!!,
				title = record.get(BOOKS.TITLE)!!,
				value = record.get(BOOKS.VALUE)!!,
				publicationStatus = record.get(BOOKS.PUBLICATION_STATUS)!!,
				createdAt = record.get(BOOKS.CREATED_AT)!!,
				authors = record.get(authorsField) ?: emptyList()
			)
		}
	}
}
