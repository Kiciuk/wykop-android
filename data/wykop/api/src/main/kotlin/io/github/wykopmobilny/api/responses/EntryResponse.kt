package io.github.wykopmobilny.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.datetime.Instant

@JsonClass(generateAdapter = true)
data class EntryResponse(
    @field:Json(name = "id") val id: Long,
    @field:Json(name = "slug") val slug: String,
    @field:Json(name = "author") val author: AuthorResponse,
    @field:Json(name = "device") val device: String,
    @field:Json(name = "created_at") val created_at: Instant,
    @field:Json(name = "content") val content: String?,
    @field:Json(name = "media") val media: Boolean, //mediaresponse
    @field:Json(name = "adult") val adult: Boolean
    @field:Json(name = "tags") val tags: Boolean //array of string
    @field:Json(name = "favorite") val favorite: Boolean,
    @field:Json(name = "parent") val parent: Long, //shouldnt that be in comment_response?
    @field:Json(name = "votes") val votes: Int, //object UP:INT,DOWN,INT,USERS:ARRAY?
    @field:Json(name = "editable") val editable: Boolean,
    @field:Json(name = "deletable") val deletable: Boolean,
   
    @field:Json(name = "comments") val comments: List<EntryCommentResponse>?, //object items:array, count:int
    @field:Json(name = "resource") val resource: String,
    
    @field:Json(name = "actions") val actions: String, //object create,update,delete,vote_up,create_favourite,report:BOOL
    @field:Json(name = "archive") val archive: Boolean,
    @field:Json(name = "status") val status: String,
   
)
