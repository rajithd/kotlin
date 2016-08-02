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

package org.jetbrains.kotlin.ir.visitors

import org.jetbrains.kotlin.ir.declarations.*

interface IrDeclarationVisitor<out R, in D> {
    fun visitDeclaration(declaration: IrDeclaration, data: D): R
    fun visitCompoundDeclaration(declaration: IrCompoundDeclaration, data: D): R = visitDeclaration(declaration, data)
    fun visitModule(declaration: IrModule, data: D): R = visitCompoundDeclaration(declaration, data)
    fun visitFile(declaration: IrFile, data: D): R = visitCompoundDeclaration(declaration, data)
    fun visitClass(declaration: IrClass, data: D): R = visitCompoundDeclaration(declaration, data)
    fun visitFunction(declaration: IrFunction, data: D): R = visitDeclaration(declaration, data)
    fun visitPropertyGetter(declaration: IrPropertyGetter, data: D): R = visitFunction(declaration, data)
    fun visitPropertySetter(declaration: IrPropertySetter, data: D): R = visitFunction(declaration, data)
    fun visitProperty(declaration: IrProperty, data: D): R = visitDeclaration(declaration, data)
}
