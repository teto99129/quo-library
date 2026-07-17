package com.github.teto99129.library.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String?>> {
		// "birthday" to "生年月日は過去の日付を指定してください" のようなマップを生成
		val errors =
			ex.bindingResult.fieldErrors.associate {
				it.field to it.defaultMessage
			}
		return ResponseEntity(errors, HttpStatus.BAD_REQUEST)
	}
}
