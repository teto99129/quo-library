package com.github.teto99129.library.book.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PatchBookRequest(
	val title: String?,
	val value: Int?,
	@JsonProperty("publication_status")
	val publicationStatus: Short?,
	val authors: List<Int>?
) {
	fun hasAnyUpdate(): Boolean {
		return title != null || value != null || publicationStatus != null || authors != null
	}
}
