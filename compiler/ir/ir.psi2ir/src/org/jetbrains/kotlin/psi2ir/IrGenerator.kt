/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.psi2ir

import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.tree.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext

interface IrGenerator {
    val context: IrGeneratorContext
}

interface IrDeclarationGenerator : IrGenerator {
    val irDeclaration: IrDeclaration
    val parent: IrDeclarationGenerator?
}

class IrModuleGenerator(override val context: IrGeneratorContext) : IrDeclarationGenerator {
    override val irDeclaration: IrModule get() = context.irModule
    override val parent: IrDeclarationGenerator? get() = null

    fun generateModuleContent() {
        for (ktFile in context.inputFiles) {
            val packageFragmentDescriptor = context.bindingContext[BindingContext.FILE_TO_PACKAGE_FRAGMENT, ktFile]
                                            ?: TODO("Handle unresolved code")
            val (fileEntry, irFile) = context.registerSourceFile(ktFile, packageFragmentDescriptor)
            irDeclaration.addFile(irFile)
            val generator = IrFileGenerator(context, ktFile, irFile, this, fileEntry)
            generator.generateFileContent()
        }
    }
}

abstract class IrDeclarationGeneratorBase(
        override val context: IrGeneratorContext,
        override val irDeclaration: IrDeclaration,
        override val parent: IrDeclarationGenerator,
        val containingFileEntry: PsiSourceManager.PsiFileEntry
) : IrDeclarationGenerator {
    fun generateAnnotationEntries(annotationEntries: List<KtAnnotationEntry>) {
        // TODO create IrAnnotation's for each KtAnnotationEntry
    }

    fun generateMemberDeclaration(ktDeclaration: KtDeclaration, containingDeclaration: IrCompoundDeclaration) {
        when (ktDeclaration) {
            is KtNamedFunction ->
                generateFunctionDeclaration(ktDeclaration, containingDeclaration)
            is KtProperty -> TODO("property")
            is KtClassOrObject -> TODO("classOrObject")
            is KtTypeAlias -> TODO("typealias")
        }
    }

    fun generateFunctionDeclaration(ktNamedFunction: KtNamedFunction, containingDeclaration: IrCompoundDeclaration) {
        val sourceLocation = containingFileEntry.getSourceLocationForElement(ktNamedFunction)
        val functionDescriptor = context.bindingContext[BindingContext.FUNCTION, ktNamedFunction]
                                 ?: TODO("handle unresolved code")
        val body = IrDummyFunctionBody(sourceLocation)
        val irFunction = IrFunctionImpl(sourceLocation, containingDeclaration, functionDescriptor, body)
        containingDeclaration.addChild(irFunction)
    }
}

class IrFileGenerator(
        context: IrGeneratorContext,
        val ktFile: KtFile,
        override val irDeclaration: IrFile,
        override val parent: IrModuleGenerator,
        fileEntry: PsiSourceManager.PsiFileEntry
) : IrDeclarationGeneratorBase(context, irDeclaration, parent, fileEntry) {
    fun generateFileContent() {
        generateAnnotationEntries(ktFile.annotationEntries)

        for (topLevelDeclaration in ktFile.declarations) {
            generateMemberDeclaration(topLevelDeclaration, irDeclaration)
        }
    }
}