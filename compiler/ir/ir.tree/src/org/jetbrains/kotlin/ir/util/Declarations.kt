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

package org.jetbrains.kotlin.ir.util

import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.visitors.IrDeclarationVisitor
import org.jetbrains.kotlin.ir.visitors.IrDeclarationVisitorVoid

inline fun IrDeclaration.forEachChild(crossinline block: IrDeclaration.() -> Unit) {
    acceptChildren(object : IrDeclarationVisitorVoid {
        override fun visitDeclaration(declaration: IrDeclaration) =
                declaration.block()
    }, null)
}

inline fun <T> IrDeclaration.foldByChildren(initial: T, crossinline block: IrDeclaration.(T) -> T): T {
    val visitor = object : IrDeclarationVisitorVoid {
        var state = initial
        override fun visitDeclaration(declaration: IrDeclaration) {
            state = declaration.block(state)
        }
    }
    acceptChildren(visitor, null)
    return visitor.state
}

fun <D> IrDeclarationVisitor<Unit, D>.visitEachChild(declaration: IrDeclaration, data: D) =
        declaration.acceptChildren(this, data)

fun <D> IrDeclarationVisitor<Unit, D>.visitInPrefixOrder(declaration: IrDeclaration, data: D) {
    declaration.accept(this, data)
    declaration.forEachChild {
        this@visitInPrefixOrder.visitInPrefixOrder(this, data)
    }
}

fun <D> IrDeclarationVisitor<Unit, D>.visitInPostfixOrder(declaration: IrDeclaration, data: D) {
    declaration.forEachChild {
        this@visitInPostfixOrder.visitInPostfixOrder(this, data)
    }
    declaration.accept(this, data)
}
