package com.github.teto99129.library.book.repository

import com.github.teto99129.library.book.model.Book
import com.github.teto99129.library.book.model.BookAuthors

interface BookRepository { 
	fun insertBook(title: String, value: Int, publicationStatus: Short, authors: List<Int>): Book
	fun insertBookAuthors(bookId: Int, authors: List<Int>): BookAuthors
	fun updateBook(bookId: Int, title: String?, value: Int?, publicationStatus: Short?, authors: List<Int>?): Book
	fun deleteBookAuthors(bookId: Int)
	fun getBook(authId: Int?, authName: String?): List<Book>
}
