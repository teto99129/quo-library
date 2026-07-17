package com.github.teto99129.library.author.controller

import com.github.teto99129.library.author.model.Author
import com.github.teto99129.library.author.service.AuthorService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate

@WebMvcTest(AuthorController::class)
class AuthorControllerTest : DescribeSpec() {
	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	@MockitoBean
	private lateinit var service: AuthorService

	init {
		extension(SpringExtension)

		describe("POST /author") {
			it("正常 - 著者登録") {
				val name = "東野 みゆき"
				val birthday = LocalDate.of(1990, 1, 1)
				val registeredAuthor = Author(authorId = 1, name = name, birthday = birthday)

				`when`(service.registerAuthor(name, birthday)).thenReturn(registeredAuthor)

				val requestBody =
					mapOf(
						"name" to name,
						"birthday" to birthday.toString(),
					)

				mockMvc
					.perform(
						post("/author")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestBody)),
					).andExpect(status().isOk)
					.andExpect(jsonPath("$.authorId").value(1))
					.andExpect(jsonPath("$.name").value(name))
					.andExpect(jsonPath("$.birthday").value(birthday.toString()))
			}

			it("異常 - 生年月日が現在日より未来の場合400エラー") {
				val name = "東野 みゆき"
				val birthday = LocalDate.now().plusDays(1)

				val requestBody =
					mapOf(
						"name" to name,
						"birthday" to birthday.toString(),
					)

				mockMvc
					.perform(
						post("/author")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestBody)),
					).andExpect(status().isBadRequest)
			}
		}

		describe("PATCH /author/{authorId}") {
			it("正常 - 著者更新") {
				val authorId = 1
				val name = "コナン ドイル"
				val birthday = LocalDate.of(1995, 5, 5)
				val updatedAuthor = Author(authorId = authorId, name = name, birthday = birthday)

				`when`(service.updateAuthor(authorId, name, birthday)).thenReturn(updatedAuthor)

				val requestBody =
					mapOf(
						"name" to name,
						"birthday" to birthday.toString(),
					)

				mockMvc
					.perform(
						patch("/author/$authorId")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestBody)),
					).andExpect(status().isOk)
					.andExpect(jsonPath("$.authorId").value(authorId))
					.andExpect(jsonPath("$.name").value(name))
					.andExpect(jsonPath("$.birthday").value(birthday.toString()))
			}

			it("正常 - 著者名のみ更新（生年月日バリデーションが動かない確認）") {
				val authorId = 1
				val name = "コナン ドイル"
				val birthday = LocalDate.of(1995, 5, 5)
				val updatedAuthor = Author(authorId = authorId, name = name, birthday = birthday)

				`when`(service.updateAuthor(authorId, name, null)).thenReturn(updatedAuthor)

				val requestBody =
					mapOf(
						"name" to name,
					)

				mockMvc
					.perform(
						patch("/author/$authorId")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestBody)),
					).andExpect(status().isOk)
					.andExpect(jsonPath("$.authorId").value(authorId))
					.andExpect(jsonPath("$.name").value(name))
					.andExpect(jsonPath("$.birthday").value(birthday.toString()))
			}

			it("異常 - 生年月日が現在日より未来の場合400エラー") {
				val name = "コナン ドイル"
				val birthday = LocalDate.now().plusDays(1)

				val requestBody =
					mapOf(
						"birthday" to birthday.toString(),
					)

				mockMvc
					.perform(
						patch("/author/1")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestBody)),
					).andExpect(status().isBadRequest)
			}
		}
	}
}
