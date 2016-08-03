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
import org.jetbrains.kotlin.utils.Printer

fun IrDeclaration.dump(): String {
    val sb = StringBuilder()
    dump(sb)
    return sb.toString()
}

fun IrDeclaration.dump(out: Appendable) {
    val printer = Printer(out)
    val renderer = RenderIrElementVisitor()
    dumpRecursively(printer, renderer)
}

private fun IrDeclaration.dumpRecursively(printer: Printer, renderer: RenderIrElementVisitor) {
    printer.println(accept(renderer, null))
    printer.pushIndent()
    forEachChild {
        dumpRecursively(printer, renderer)
    }
    printer.popIndent()
}
