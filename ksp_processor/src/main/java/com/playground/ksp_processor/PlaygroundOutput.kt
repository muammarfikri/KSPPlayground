package com.playground.ksp_processor

import com.google.devtools.ksp.symbol.KSFile

data class PlaygroundOutput(
    val originatingFile: KSFile,
    val className: String,
    val name: String,
)