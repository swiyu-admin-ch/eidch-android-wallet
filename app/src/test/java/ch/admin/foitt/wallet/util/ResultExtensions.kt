@file:OptIn(UnsafeResultValueAccess::class, UnsafeResultErrorAccess::class)

package ch.admin.foitt.wallet.util

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.annotation.UnsafeResultErrorAccess
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.github.michaelbull.result.asErr
import com.github.michaelbull.result.asOk
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import kotlin.reflect.KClass

fun <T, V> Result<T, V>.assertOk(): T {
    assertTrue(isOk) { "an error occurred: ${asErr<T, V, V>().error}" }
    return get()!!
}

fun <T, V> Result<T, V>.assertOkNullable(): T? {
    assertTrue(isOk) { "an error occurred: ${asErr<T, V, V>().error}" }
    return get()
}

inline fun <T : Any, V, reified U : T> Result<T, V>.assertSuccessType(type: KClass<U>): U {
    val success = assertOk()
    assertTrue(type.isInstance(success)) { "the success is not of the right type: ${success::class.simpleName}" }
    return success as U
}

fun <T, V> Result<T, V>.assertErr(): V {
    assertTrue(isErr) { "an unexpected success occurred: ${(asOk<T, V, T>()).value}" }
    return getError()!!
}

inline fun <T, V : Any, reified U : V> Result<T, V>.assertErrorType(type: KClass<U>): U {
    val error = assertErr()
    assertTrue(type.isInstance(error)) { "the error is not of the right type: ${error::class.simpleName}" }
    return error as U
}
