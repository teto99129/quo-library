package com.github.teto99129.library.book.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class PublicationStatusTest : DescribeSpec() {
	init {
		describe("fromCode") {
			it("正常 - コード 0 の場合は UNPUBLISHED が返ること") {
				val result = PublicationStatus.fromCode(0)
				result shouldBe PublicationStatus.UNPUBLISHED
			}

			it("正常 - コード 1 の場合は PUBLISHED が返ること") {
				val result = PublicationStatus.fromCode(1)
				result shouldBe PublicationStatus.PUBLISHED
			}

			it("異常 - 定義されていないコードの場合は IllegalArgumentException が発生すること") {
				val invalidCode: Short = 99
				shouldThrow<IllegalArgumentException> {
					PublicationStatus.fromCode(invalidCode)
				}
			}
		}
	}
}
