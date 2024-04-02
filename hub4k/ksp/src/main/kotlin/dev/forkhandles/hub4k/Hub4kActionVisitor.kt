package dev.forkhandles.hub4k

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.ClassKind.OBJECT
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import java.util.Locale

class Hub4kActionVisitor(private val log: (Any?) -> Unit) :
    KSEmptyVisitor<KSClassDeclaration, Sequence<FunSpec>>() {
    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: KSClassDeclaration
    ): Sequence<FunSpec> {
        log("Processing " + classDeclaration.asStarProjectedType().declaration.qualifiedName!!.asString())

        return classDeclaration.getConstructors().flatMap { ctr ->
            listOfNotNull(
                generateActionExtension(classDeclaration, data, ctr),
            )
        }
    }

    override fun defaultHandler(node: KSNode, data: KSClassDeclaration) = error("unsupported")
}

private fun generateActionExtension(
    actionClass: KSClassDeclaration,
    adapterClazz: KSClassDeclaration,
    ctr: KSFunctionDeclaration
) = generateExtensionFunction(
    actionClass, adapterClazz, ctr, "",
    CodeBlock.of(
        when (actionClass.classKind) {
            OBJECT -> "return invoke(%T)"
            else -> "return invoke(%T(${ctr.parameters.joinToString(", ") { it.name!!.asString() }}))"
        },
        actionClass.asType(emptyList()).toTypeName()
    ),
    actionClass.getAllFunctions()
        .first { it.simpleName.getShortName() == "toResult" }.returnType!!.toTypeName()
)

private fun generateExtensionFunction(
    actionClazz: KSClassDeclaration,
    adapterClazz: KSClassDeclaration,
    ctr: KSFunctionDeclaration,
    suffix: String,
    codeBlock: CodeBlock,
    returnType: TypeName
): FunSpec {
    val baseFunction = FunSpec.builder(
        actionClazz.simpleName.asString().replaceFirstChar { it.lowercase(Locale.getDefault()) } + suffix)
        .addKdoc("@see ${actionClazz.qualifiedName!!.asString().replace('/', '.')}")
        .receiver(adapterClazz.toClassName())
        .returns(returnType)
        .addCode(codeBlock)

    ctr.parameters.forEach {
        val base = ParameterSpec.builder(it.name!!.asString(), it.type.toTypeName())
        with(it.type.resolve()) {
            if (isMarkedNullable) base.defaultValue(CodeBlock.of("null"))
            else if (it.hasDefault) {
                if (starProjection().toString() == "Map<*, *>") base.defaultValue(CodeBlock.of("emptyMap()"))
                else if (starProjection().toString() == "List<*>") base.defaultValue(CodeBlock.of("emptyList()"))
                else if (starProjection().toString() == "Set<*>") base.defaultValue(CodeBlock.of("emptySet()"))
                else {
                }
            } else {
            }
        }
        baseFunction.addParameter(base.build())
    }
    return baseFunction.build()
}
