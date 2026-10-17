@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package com.example.storyround.testing

inline fun GIVEN(
    description: String,
    step: () -> Unit,
) = step()

inline fun WHEN(
    description: String,
    step: () -> Unit,
) = step()

inline fun THEN(
    description: String,
    step: () -> Unit,
) = step()

inline fun AND(
    description: String,
    step: () -> Unit,
) = step()
