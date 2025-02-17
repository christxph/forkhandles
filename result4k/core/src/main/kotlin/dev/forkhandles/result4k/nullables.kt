package dev.forkhandles.result4k

/*
 * Translate between the `Result` and Nullable/Optional/Maybe monads
 */

/**
 * Convert a nullable value to a `Result`, using the result of [failureDescription] as the failure reason
 * if the value is `null`.
 */
inline fun <T, E> T?.asResultOr(failureDescription: () -> E): Result<T & Any, E> =
    if (this != null) Success(this) else Failure(failureDescription())

/**
 * Convert a `Success` of a nullable value to a `Success` of a non-null value or a `Failure`,
 * using the result of [failureDescription] as the failure reason, if the value is `null`.
 */
inline fun <T : Any, E> Result<T?, E>.filterNotNull(failureDescription: () -> E): Result<T, E> =
    flatMap { it.asResultOr(failureDescription) }

/**
 * Returns the success value, or `null` if the `Result` is a failure.
 */
fun <T, E> Result<T, E>.valueOrNull(): T? = when (this) {
    is Success<T> -> value
    is Failure<E> -> null
}

/**
 * Returns the failure reason, or `null` if the `Result` is a success.
 */
fun <T, E> Result<T, E>.failureOrNull(): E? = when (this) {
    is Success<T> -> null
    is Failure<E> -> reason
}

/**
 * Convert a `Success` of a nullable value to a `Success` of a non-null value, or calling `block` to abort from
 * the current function if the value is `null`
 */
inline fun <T, E> Result<T?, E>.onNull(block: () -> Nothing): Result<T, E> =
    flatMap { if (it != null) Success(it) else block() }
