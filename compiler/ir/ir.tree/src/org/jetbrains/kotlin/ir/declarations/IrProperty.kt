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

package org.jetbrains.kotlin.ir.declarations

import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.SourceLocation
import org.jetbrains.kotlin.ir.visitors.IrDeclarationVisitor

interface IrProperty : IrDeclarationNonRoot {
    override val descriptor: PropertyDescriptor
    val getter: IrPropertyGetter?
    val setter: IrPropertySetter?
}

class IrPropertyImpl(
        sourceLocation: SourceLocation,
        containingDeclaration: IrDeclaration,
        override val descriptor: PropertyDescriptor,
        override val getter: IrPropertyGetter?,
        override val setter: IrPropertySetter?
) : IrDeclarationNonRootBase(sourceLocation, containingDeclaration), IrProperty {
    override fun <R, D> accept(visitor: IrDeclarationVisitor<R, D>, data: D): R =
            visitor.visitProperty(this, data)

    override fun <D> acceptChildren(visitor: IrDeclarationVisitor<Unit, D>, data: D) {
        getter?.accept(visitor, data)
        setter?.accept(visitor, data)
    }
}