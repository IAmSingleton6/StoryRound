package com.example.storyround.identity

import java.util.UUID

internal class UuidIdentifierGenerator : IdentifierGenerator {
    override fun generate(): String = UUID.randomUUID().toString()
}
