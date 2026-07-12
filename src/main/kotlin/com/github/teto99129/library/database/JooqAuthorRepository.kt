package com.github.teto99129.library.database

import com.github.teto99129.library.author.model.Author
import com.github.teto99129.library.author.repository.AuthorRepository
import com.github.teto99129.library.jooq.tables.records.AuthorsRecord
import com.github.teto99129.library.jooq.tables.references.AUTHORS
import com.github.teto99129.library.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.jooq.Field
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class JooqAuthorRepository(private val dsl: DSLContext): AuthorRepository {
	override fun insertAuthor(name: String, birthday: LocalDate): Author {
		val record: AuthorsRecord = dsl.insertInto(AUTHORS)
			.columns(AUTHORS.NAME, AUTHORS.BIRTHDAY)
			.values(name, birthday)
			.returning()
			.fetchOne() ?: throw IllegalStateException("Failed to insert author")

		return Author(
			authorId = record.authorId!!,
			name = record.name!!,
			birthday = record.birthday!!
		)
	}

	override fun findAuthorsByIds(ids: List<Int>): List<Author> {
		if (ids.isEmpty()) {
			return emptyList()
		}
		return dsl.selectFrom(AUTHORS)
			.where(AUTHORS.AUTHOR_ID.`in`(ids))
			.fetch { record ->
				Author(
					authorId = record.authorId!!,
					name = record.name!!,
					birthday = record.birthday!!
				)
			}
	}

	override fun findAuthorsByBookId(bookId: Int): List<Author> {
		return dsl.select(*AUTHORS.fields())
			.from(AUTHORS)
			.join(BOOK_AUTHORS).on(AUTHORS.AUTHOR_ID.eq(BOOK_AUTHORS.AUTHOR_ID))
			.where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
			.fetch { record ->
				val authorsRecord = record.into(AUTHORS)
				Author(
					authorId = authorsRecord.authorId!!,
					name = authorsRecord.name!!,
					birthday = authorsRecord.birthday!!
				)
			}
	}

        override fun updateAuthor(authorId: Int, name: String?, birthday: LocalDate?): Author {
		val updateValues = mutableMapOf<Field<*>, Any?>()
		if (name != null) updateValues[AUTHORS.NAME] = name
		if (birthday != null) updateValues[AUTHORS.BIRTHDAY] = birthday

		val record = if (updateValues.isNotEmpty()) {
			dsl.update(AUTHORS)
				.set(updateValues)
				.where(AUTHORS.AUTHOR_ID.eq(authorId))
				.returning()
				.fetchOne() ?: throw IllegalStateException("Failed to update author with ID: $authorId")
		} else {
			dsl.selectFrom(AUTHORS)
				.where(AUTHORS.AUTHOR_ID.eq(authorId))
				.fetchOne() ?: throw IllegalStateException("Author not found with ID: $authorId")
		}
		return Author(
			authorId = record.authorId!!,
			name = record.name!!,
			birthday = record.birthday!!,
		)
	
        }
}
