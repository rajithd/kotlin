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

import org.jetbrains.kotlin.ir.tree.IrDummyExpression
import org.jetbrains.kotlin.ir.tree.IrExpression

interface IrExpressionVisitorVoid : IrExpressionVisitor<Unit, Nothing?> {
    fun visitExpression(expression: IrExpression)
    fun visitDummyExpression(expression: IrDummyExpression) = visitExpression(expression)

    // Delegating methods from IrExpressionVisitor
    override fun visitExpression(expression: IrExpression, data: Nothing?) = visitExpression(expression)
    override fun visitDummyExpression(expression: IrDummyExpression, data: Nothing?) = visitDummyExpression(expression)
}