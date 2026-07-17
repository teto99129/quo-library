package com.github.teto99129.library.book.controller

import com.github.teto99129.library.book.model.Book
import com.github.teto99129.library.book.model.PatchBookRequest
import com.github.teto99129.library.book.model.PublicationStatus
import com.github.teto99129.library.book.service.BookService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.mockito.Mockito.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.OffsetDateTime

@WebMvcTest(BookController::class)
class BookControllerTest : DescribeSpec() {
	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	@MockitoBean
	private lateinit var service: BookService

	init {
		extension(SpringExtension)

		describe("POST /book") {
			it("正常 - リクエスト") {
				val title = "Test Book"
				val value = 1500
				val authors = listOf(1)
				val status = PublicationStatus.PUBLISHED
				val createdBook =
					Book(
						bookID = 1,
						title = title,
						value = value,
						publicationStatus = status,
						createdAt = OffsetDateTime.now(),
						authors = emptyList(),
					)

				`when`(service.registerBook(title, value, authors, status)).thenReturn(createdBook)

				val requestBody =
					mapOf(
						"title" to title,
						"value" to value,
						"publication_status" to status.code.toInt(),
						"authors" to authors,
					)

				mockMvc
					.perform(
						post("/book")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestBody)),
					).andExpect(status().isOk)
					.andExpect(jsonPath("$.bookID").value(1))
					.andExpect(jsonPath("$.title").value(title))
					.andExpect(jsonPath("$.value").value(value))
			}

			it("価格が0円の場合400エラー") {
				val requestBody =
					mapOf(
						"title" to "Invalid Value Book",
						"value" to 0, // Min(1) violation
						"publication_status" to PublicationStatus.PUBLISHED.code.toInt(),
						"authors" to listOf(1),
					)

				mockMvc
					.perform(
						post("/book")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestBody)),
					).andExpect(status().isBadRequest)
			}

			it("著者の指定がない場合400エラー") {
				val requestBody =
					mapOf(
						"title" to "No Authors Book",
						"value" to 1000,
						"publication_status" to PublicationStatus.PUBLISHED.code.toInt(),
						"authors" to emptyList<Int>(), // NotEmpty violation
					)

				mockMvc
					.perform(
						post("/book")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestBody)),
					).andExpect(status().isBadRequest)
			}
		}

		describe("PATCH /book/{bookId}") {
			it("正常- 本更新") {
				val bookId = 1
				val expectedRequest =
					PatchBookRequest(
						title = "Updated Title",
						value = 2000,
						publicationStatus = null,
						authors = null,
					)
				val updatedBook =
					Book(
						bookID = bookId,
						title = "Updated Title",
						value = 2000,
						publicationStatus = PublicationStatus.PUBLISHED,
						createdAt = OffsetDateTime.now(),
						authors = emptyList(),
					)

				`when`(service.updateBook(bookId, expectedRequest)).thenReturn(updatedBook)

				val requestBody =
					mapOf(
						"title" to "Updated Title",
						"value" to 2000,
					)

				mockMvc
					.perform(
						patch("/book/$bookId")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestBody)),
					).andExpect(status().isOk)
					.andExpect(jsonPath("$.bookID").value(bookId))
					.andExpect(jsonPath("$.title").value("Updated Title"))
					.andExpect(jsonPath("$.value").value(2000))
			}

			it("異常 - リクエストボディ何もなしの場合400エラー") {
				val bookId = 1
				val requestBody = emptyMap<String, Any>()

				mockMvc
					.perform(
						patch("/book/$bookId")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestBody)),
					).andExpect(status().isBadRequest)
			}

			it("異常 - 価格を0円に更新する場合400エラー") {
				val bookId = 1
				val requestBody =
					mapOf(
						"value" to 0, // Min(1) violation
					)

				mockMvc
					.perform(
						patch("/book/$bookId")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestBody)),
					).andExpect(status().isBadRequest)
			}

			it("異常 - 著者なしで更新する場合400エラー") {
				val bookId = 1
				val requestBody =
					mapOf(
						"authors" to emptyList<Int>(), // Size(min = 1) violation
					)

				mockMvc
					.perform(
						patch("/book/$bookId")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestBody)),
					).andExpect(status().isBadRequest)
			}
		}

		describe("GET /book") {
			it("正常 - 本のリストを取得") {
				val books =
					listOf(
						Book(
							bookID = 1,
							title = "Book 1",
							value = 1000,
							publicationStatus = PublicationStatus.PUBLISHED,
							createdAt = OffsetDateTime.now(),
						),
						Book(
							bookID = 2,
							title = "Book 2",
							value = 1200,
							publicationStatus = PublicationStatus.UNPUBLISHED,
							createdAt = OffsetDateTime.now(),
						),
					)

				`when`(service.getBook(any(), any())).thenReturn(books)

				mockMvc
					.perform(
						get("/book")
							.param("auth_id", "1")
							.param("auth_name", "Author"),
					).andExpect(status().isOk)
					.andExpect(jsonPath("$.length()").value(2))
					.andExpect(jsonPath("$[0].bookID").value(1))
					.andExpect(jsonPath("$[0].title").value("Book 1"))
					.andExpect(jsonPath("$[1].bookID").value(2))
					.andExpect(jsonPath("$[1].title").value("Book 2"))
			}
		}
	}
}
