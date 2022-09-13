package com.playground.ksp_processor

import com.google.devtools.ksp.symbol.KSClassDeclaration

internal infix fun KSClassDeclaration.hasSubType(qualifiedName: String) =
    superTypes.any { it.resolve().declaration.qualifiedName?.asString() == qualifiedName }