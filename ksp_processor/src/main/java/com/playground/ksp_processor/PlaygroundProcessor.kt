package com.playground.ksp_processor

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.playground.ksp_annotation.AnnotateMe
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.writeTo

class PlaygroundProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(AnnotateMe::class.qualifiedName!!).toList()
        val (validated, unvalidated) = symbols.partition { it.validate() }
        val outputs = validated.filterIsInstance(KSClassDeclaration::class.java)
            .mapNotNull { it.accept(PlaygroundVisitor(environment), Unit) }
        environment.logger.warn("unvalidated: ${unvalidated.size}")
        environment.logger.warn("validated: ${validated.size}")
        environment.logger.warn("outputs: ${outputs.size}")

        if (outputs.isNotEmpty()) {
            val packageName = "com.playground.processor.generated"
            val objectBuilder = TypeSpec.objectBuilder("PlaygroundGenerated")
            val outClassesString = outputs.joinToString(",\n") {
                "\"${it.className}\""
            }
            environment.logger.warn(outClassesString)
//            outputs.forEach {
//                val propSpec = PropertySpec.builder("output${it.name}", STRING)
//                    .addOriginatingKSFile(it.originatingFile)
//                    .initializer("\"${it.className}\"")
//                objectBuilder.addProperty(propSpec.build()).addOriginatingKSFile(it.originatingFile)
//            }
            val propertySpec = PropertySpec
                .builder("validatedClass", LIST.parameterizedBy(STRING))
                .apply {
                    outputs.forEach {
                        addOriginatingKSFile(it.originatingFile)
                        objectBuilder.addOriginatingKSFile(it.originatingFile)
                    }
                }
                .initializer("listOf(\n$outClassesString\n)")
            objectBuilder.addProperty(propertySpec.build())


            FileSpec
                .builder(
                    packageName = packageName,
                    fileName = "PlaygroundGeneratedClass"
                )
                .addType(objectBuilder.build())
                .build()
                .writeTo(
                    codeGenerator = environment.codeGenerator,
                    aggregating = false
                )
        }

        return unvalidated.toList()
    }
}

internal const val appCompatActivityClassName = "androidx.appcompat.app.AppCompatActivity"

@AutoService(SymbolProcessorProvider::class)
class PlaygroundProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return PlaygroundProcessor(environment)
    }
}

data class Output(
    val originatingFile: KSFile,
    val className: String,
    val name: String,
)

class PlaygroundVisitor(private val environment: SymbolProcessorEnvironment) :
    KSDefaultVisitor<Unit, Output?>() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Output? {
        validateSubType(classDeclaration) ?: return null

        return Output(className = classDeclaration.qualifiedName?.asString()!!, originatingFile = classDeclaration.containingFile!!, name = classDeclaration.simpleName.asString())
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

    override fun defaultHandler(node: KSNode, data: Unit): Output? {
        return null
    }
}

internal infix fun KSClassDeclaration.hasSubType(qualifiedName: String) =
    superTypes.any { it.resolve().declaration.qualifiedName?.asString() == qualifiedName }