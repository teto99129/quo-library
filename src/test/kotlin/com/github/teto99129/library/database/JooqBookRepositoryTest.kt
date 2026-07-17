package com.github.teto99129.library.database

import com.github.teto99129.library.book.model.PublicationStatus
import com.github.teto99129.library.common.exception.ResourceNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@Transactional
class JooqBookRepositoryTest(
	private val bookRepository: JooqBookRepository,
	private val authorRepository: JooqAuthorRepository,
) : DescribeSpec() {
	init {
		extension(SpringExtension)

		describe("insertBook") {
			it("正常 - 新規登録され、著者との紐付けも正しく行われていること") {
				val author = authorRepository.insertAuthor("夏目 漱石", LocalDate.of(1867, 2, 9))
				val title = "吾輩は猫である"
				val value = 1000
				val status = PublicationStatus.PUBLISHED

				val book = bookRepository.insertBook(title, value, status, listOf(author.authorId))

				book.bookID shouldNotBe null
				book.title shouldBe title
				book.value shouldBe value
				book.publicationStatus shouldBe status
				book.authors shouldHaveSize 1
				book.authors[0].authorId shouldBe author.authorId
			}
		}

		describe("updateBook") {
			it("正常 - タイトルのみ更新できること") {
				val author = authorRepository.insertAuthor("芥川 竜之介", LocalDate.of(1892, 3, 1))
				val original = bookRepository.insertBook("羅生門", 800, PublicationStatus.PUBLISHED, listOf(author.authorId))

				val updated = bookRepository.updateBook(original.bookID, "新・羅生門", null, null, null)

				updated.title shouldBe "新・羅生門"
				updated.value shouldBe original.value
				updated.publicationStatus shouldBe original.publicationStatus
				updated.authors shouldContainExactly original.authors
			}

			it("正常 - 著者のみ変更できること") {
				val author1 = authorRepository.insertAuthor("太宰 治", LocalDate.of(1909, 6, 19))
				val author2 = authorRepository.insertAuthor("坂口 安吾", LocalDate.of(1906, 10, 20))
				val original = bookRepository.insertBook("人間失格", 1200, PublicationStatus.PUBLISHED, listOf(author1.authorId))

				// 著者を author2 に変更
				val updated = bookRepository.updateBook(original.bookID, null, null, null, listOf(author2.authorId))

				updated.authors shouldHaveSize 1
				updated.authors[0].authorId shouldBe author2.authorId
			}

			it("正常 - すべて null で更新をかけた場合値が変わらないこと") {
				val author = authorRepository.insertAuthor("中島 敦", LocalDate.of(1909, 5, 5))
				val original = bookRepository.insertBook("山月記", 500, PublicationStatus.PUBLISHED, listOf(author.authorId))

				val updated = bookRepository.updateBook(original.bookID, null, null, null, null)

				updated.title shouldBe original.title
				updated.value shouldBe original.value
				updated.publicationStatus shouldBe original.publicationStatus
				updated.authors[0].authorId shouldBe author.authorId
			}

			it("異常 - 存在しないIDを指定して更新しようとした場合は例外が発生すること") {
				val nonExistentId = -9999
				shouldThrow<ResourceNotFoundException> {
					bookRepository.updateBook(nonExistentId, "新タイトル", null, null, null)
				}
			}
		}

		describe("getBook") {
			it("正常 - 条件なしで全件取得できること") {
				val author = authorRepository.insertAuthor("宮沢 賢治", LocalDate.of(1896, 8, 27))
				bookRepository.insertBook("銀河鉄道の夜", 900, PublicationStatus.PUBLISHED, listOf(author.authorId))

				val list = bookRepository.getBook(null, null)
				list.shouldNotBeEmpty()
			}

			it("正常 - 著者IDで絞り込めること") {
				val author1 = authorRepository.insertAuthor("梶井 基次郎", LocalDate.of(1901, 2, 17))
				val author2 = authorRepository.insertAuthor("三島 由紀夫", LocalDate.of(1925, 1, 14))

				val book1 = bookRepository.insertBook("檸檬", 600, PublicationStatus.PUBLISHED, listOf(author1.authorId))
				bookRepository.insertBook("金閣寺", 1500, PublicationStatus.PUBLISHED, listOf(author2.authorId))

				val result = bookRepository.getBook(author1.authorId, null)

				result shouldHaveSize 1
				result[0].bookID shouldBe book1.bookID
			}

			it("正常 - 著者名（あいまい検索）で絞り込めること") {
				val author1 = authorRepository.insertAuthor("川端 康成", LocalDate.of(1899, 6, 14))
				val author2 = authorRepository.insertAuthor("森 鴎外", LocalDate.of(1862, 2, 17))

				val book1 = bookRepository.insertBook("伊豆の踊子", 700, PublicationStatus.PUBLISHED, listOf(author1.authorId))
				bookRepository.insertBook("舞姫", 800, PublicationStatus.PUBLISHED, listOf(author2.authorId))

				// 「川端」で検索
				val result = bookRepository.getBook(null, "川端")

				result shouldHaveSize 1
				result[0].bookID shouldBe book1.bookID
			}
		}

		describe("getBookById") {
			it("正常 - 存在するIDで本を取得できること") {
				val author = authorRepository.insertAuthor("志賀 直哉", LocalDate.of(1883, 2, 20))
				val book = bookRepository.insertBook("暗夜行路", 2000, PublicationStatus.PUBLISHED, listOf(author.authorId))

				val result = bookRepository.getBookById(book.bookID)

				result shouldNotBe null
				result!!.title shouldBe "暗夜行路"
			}

			it("正常 - 存在しないIDの場合は null が返ること") {
				val result = bookRepository.getBookById(-9999)
				result shouldBe null
			}
		}
	}
}
