package com.normno.travelassistant.core.common.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Состояние загрузки, которое экран может показать целиком: своё для «грузится»,
 * своё для «ошибка». Kotlin `Result` третьего состояния не даёт, а ошибку
 * оборачивает в исключение, из которого в UI уже ничего не достать.
 */
sealed interface Outcome<out T> {
    data object Loading : Outcome<Nothing>
    data class Success<T>(val data: T) : Outcome<T>
    data class Failure(val cause: Throwable, val message: String? = null) : Outcome<Nothing>
}

fun <T> Flow<T>.asOutcome(): Flow<Outcome<T>> =
    map<T, Outcome<T>> { Outcome.Success(it) }
        .onStart { emit(Outcome.Loading) }
        .catch { emit(Outcome.Failure(it)) }

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(data))
    is Outcome.Failure -> this
    Outcome.Loading -> Outcome.Loading
}

val <T> Outcome<T>.dataOrNull: T? get() = (this as? Outcome.Success)?.data
