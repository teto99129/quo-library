package com.github.teto99129.library.author.service

import com.github.teto99129.library.author.model.Author
import com.github.teto99129.library.author.repository.AuthorRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AuthorService(private val _repository: AuthorRepository) {
        fun registerAuthor(name: String, birthday: LocalDate): Author {
		return this._repository.insertAuthor(
			name = name,
			birthday = birthday
		)
        }

	fun updateAuthor(authorId: Int, name: String?, birthday: LocalDate?): Author {
		return this._repository.updateAuthor(
			authorId = authorId,
			name = name,
			birthday = birthday
		)
	}
}
