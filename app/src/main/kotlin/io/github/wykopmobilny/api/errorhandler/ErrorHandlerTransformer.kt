package io.github.wykopmobilny.api.errorhandler

import io.github.wykopmobilny.api.responses.WykopApiResponse
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.SingleTransformer

class ErrorHandlerTransformer<T : Any> : SingleTransformer<WykopApiResponse<T>, T> {

    override fun apply(upstream: Single<WykopApiResponse<T>>): SingleSource<T> {
        return upstream.flatMap {
            val exception = WykopExceptionParser.getException(it)
            if (exception != null) {
                Single.error(exception)
            } else {
                Single.just(it.data!!)
            }
        }
    }
}

suspend fun <T> unwrapping(block: suspend () -> WykopApiResponse<T>): T {
    val response = block()
    val exception = WykopExceptionParser.getException(response)

    return if (exception != null) {
        throw exception
    } else {
        response.data!!
    }
}
