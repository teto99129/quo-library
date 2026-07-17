package com.github.teto99129.library.database

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@Transactional
class JooqAuthorRepositoryTest(
	private val repository: JooqAuthorRepository,
) : DescribeSpec() {
	init {
		extension(SpringExtension)

		describe("insertAuthor") {
			it("正常 - 新規著者が登録され、採番されたIDが返ってくること") {
				val name = "夏目 漱石"
				val birthday = LocalDate.of(1867, 2, 9)

				val author = repository.insertAuthor(name, birthday)

				author.authorId shouldNotBe null
				author.name shouldBe name
				author.birthday shouldBe birthday
			}
		}

		describe("findAuthorsByIds") {
			it("空リストを渡した場合は空のリストを返すこと") {
				val result = repository.findAuthorsByIds(emptyList())
				result.shouldBeEmpty()
			}

			it("存在するIDリストを渡した場合は該当する著者を返すこと") {
				val author1 = repository.insertAuthor("芥川 竜之介", LocalDate.of(1892, 3, 1))
				val author2 = repository.insertAuthor("太宰 治", LocalDate.of(1909, 6, 19))

				val result = repository.findAuthorsByIds(listOf(author1.authorId, author2.authorId))

				result shouldHaveSize 2
				result.find { it.authorId == author1.authorId }?.name shouldBe "芥川 竜之介"
				result.find { it.authorId == author2.authorId }?.name shouldBe "太宰 治"
			}
		}

		describe("updateAuthor") {
			it("正常 - 名前のみ更新（生年月日は元の値をキープ）") {
				val original = repository.insertAuthor("コナン ドイル", LocalDate.of(1859, 5, 22))

				val updated = repository.updateAuthor(original.authorId, "アーサー・コナン・ドイル", null)

				updated.authorId shouldBe original.authorId
				updated.name shouldBe "アーサー・コナン・ドイル"
				updated.birthday shouldBe original.birthday // 元の生年月日が残っていること
			}

			it("正常 - 生年月日のみ更新（名前は元の値をキープ）") {
				val original = repository.insertAuthor("アガサ クリスティ", LocalDate.of(1890, 9, 15))
				val newBirthday = LocalDate.of(1890, 9, 16)

				val updated = repository.updateAuthor(original.authorId, null, newBirthday)

				updated.authorId shouldBe original.authorId
				updated.name shouldBe original.name // 元の名前が残っていること
				updated.birthday shouldBe newBirthday
			}

			it("正常 - 両方とも null で更新（値は一切変わらない）") {
				val original = repository.insertAuthor("エドガー アラン ポー", LocalDate.of(1809, 1, 19))

				val updated = repository.updateAuthor(original.authorId, null, null)

				updated.authorId shouldBe original.authorId
				updated.name shouldBe original.name
				updated.birthday shouldBe original.birthday
			}

			it("正常 - 両方とも値を指定して更新") {
				val original = repository.insertAuthor("変更前 太郎", LocalDate.of(2000, 1, 1))
				val newName = "変更後 次郎"
				val newBirthday = LocalDate.of(2001, 2, 2)

				val updated = repository.updateAuthor(original.authorId, newName, newBirthday)

				updated.authorId shouldBe original.authorId
				updated.name shouldBe newName
				updated.birthday shouldBe newBirthday
			}

			it("異常 - 存在しないIDを指定した場合は例外が発生すること") {
				val nonExistentId = -9999

				shouldThrow<IllegalStateException> {
					repository.updateAuthor(nonExistentId, "幽霊 著者", LocalDate.now())
				}
			}
		}
	}
}
