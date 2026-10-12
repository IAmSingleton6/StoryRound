package com.example.storyround.protocol

import kotlinx.serialization.json.Json

fun protocolJson(): Json = Json { classDiscriminator = "type" }
