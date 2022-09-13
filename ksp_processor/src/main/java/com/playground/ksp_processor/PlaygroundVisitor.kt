package com.playground.ksp_processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.playground.ksp_annotation.AnnotateMe

class PlaygroundVisitor(private val environment: SymbolProcessorEnvironment) :
    KSDefaultVisitor<Unit, PlaygroundOutput?>() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): PlaygroundOutput? {
        validateSubType(classDeclaration) ?: return null

        return PlaygroundOutput(
            className = classDeclaration.qualifiedName?.asString()!!,
            originatingFile = classDeclaration.containingFile!!,
            name = classDeclaration.simpleName.asString()
        )
    }

    private fun validateSubType(classDeclaration: KSClassDeclaration): KSClassDeclaration? {
        return validate(
            classDeclaration = classDeclaration,
            errorMessage = "Invalid annotation (${classDeclaration.qualifiedName}): ${AnnotateMe::class.qualifiedName} needs to be a subtype of $appCompatActivityClassName"
        ) { it hasSubType appCompatActivityClassName }
    }

    private fun validate(
        classDeclaration: KSClassDeclaration,
        errorMessage: String,
        predicate: (KSClassDeclaration) -> Boolean
    ): KSClassDeclaration? {
        return if (!predicate(classDeclaration)) {
            environment.logger.error(errorMessage)
            null
        } else classDeclaration
    }

    override fun defaultHandler(node: KSNode, data: Unit): PlaygroundOutput? {
        return null
    }
}