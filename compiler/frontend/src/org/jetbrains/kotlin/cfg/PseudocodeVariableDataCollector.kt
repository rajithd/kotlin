/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.cfg

import org.jetbrains.kotlin.cfg.pseudocode.Pseudocode
import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.BlockScope
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.VariableDeclarationInstruction
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.Edges
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.TraversalOrder
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.collectData
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.traverse
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.resolve.BindingContext
import java.util.ArrayList
import java.util.HashMap

class PseudocodeVariableDataCollector(
        private val bindingContext: BindingContext,
        private val pseudocode: Pseudocode
) {
    val blockScopeVariableInfo = computeBlockScopeVariableInfo(pseudocode)

    fun <I : ControlFlowInfo<*>> collectData(
            traversalOrder: TraversalOrder,
            mergeDataWithLocalDeclarations: Boolean,
            initialInfo: I,
            instructionDataMergeStrategy: (Instruction, Collection<I>) -> Edges<I>
    ): Map<Instruction, Edges<I>> {
        return pseudocode.collectData(
                traversalOrder, mergeDataWithLocalDeclarations,
                instructionDataMergeStrategy,
                { from, to, info -> filterOutVariablesOutOfScope(from, to, info) },
                initialInfo
        )
    }

    private fun <I : ControlFlowInfo<*>> filterOutVariablesOutOfScope(
            from: Instruction,
            to: Instruction,
            info: I
    ): I {
        // If an edge goes from deeper scope to a less deep one, this means that it points outside of the deeper scope.
        val toDepth = to.blockScope.depth
        if (toDepth >= from.blockScope.depth) return info

        // Variables declared in an inner (deeper) scope can't be accessed from an outer scope.
        // Thus they can be filtered out upon leaving the inner scope.
        @Suppress("UNCHECKED_CAST")
        return info.copy().retainAll { variable ->
            val blockScope = blockScopeVariableInfo.declaredIn[variable]
            // '-1' for variables declared outside this pseudocode
            val depth = blockScope?.depth ?: -1
            depth <= toDepth
        } as I
    }

    fun computeBlockScopeVariableInfo(pseudocode: Pseudocode): BlockScopeVariableInfo {
        val blockScopeVariableInfo = BlockScopeVariableInfoImpl()
        pseudocode.traverse(TraversalOrder.FORWARD, { instruction ->
            if (instruction is VariableDeclarationInstruction) {
                val variableDeclarationElement = instruction.variableDeclarationElement
                val descriptor = bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, variableDeclarationElement)
                if (descriptor != null) {
                    blockScopeVariableInfo.registerVariableDeclaredInScope(
                            descriptor, instruction.blockScope
                    )
                }
            }
        })
        return blockScopeVariableInfo
    }
}

interface BlockScopeVariableInfo {
    val declaredIn : Map<DeclarationDescriptor, BlockScope>
    val scopeVariables : Map<BlockScope, Collection<DeclarationDescriptor>>
}

class BlockScopeVariableInfoImpl : BlockScopeVariableInfo {
    override val declaredIn = HashMap<DeclarationDescriptor, BlockScope>()
    override val scopeVariables = HashMap<BlockScope, MutableCollection<DeclarationDescriptor>>()

    fun registerVariableDeclaredInScope(variable: DeclarationDescriptor, blockScope: BlockScope) {
        declaredIn[variable] = blockScope
        val variablesInScope = scopeVariables.getOrPut(blockScope, { ArrayList<DeclarationDescriptor>() })
        variablesInScope.add(variable)
    }
}
