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

package org.jetbrains.kotlin.load.kotlin.reflect

import org.jetbrains.kotlin.builtins.ReflectionTypes
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.ModuleParameters
import org.jetbrains.kotlin.descriptors.SupertypeLoopChecker
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.load.java.components.*
import org.jetbrains.kotlin.load.java.lazy.JavaResolverComponents
import org.jetbrains.kotlin.load.java.lazy.LazyJavaPackageFragmentProvider
import org.jetbrains.kotlin.load.java.lazy.SingleModuleClassResolver
import org.jetbrains.kotlin.load.java.reflect.ReflectJavaClassFinder
import org.jetbrains.kotlin.load.kotlin.BinaryClassAnnotationAndConstantLoaderImpl
import org.jetbrains.kotlin.load.kotlin.DeserializationComponentsForJava
import org.jetbrains.kotlin.load.kotlin.DeserializedDescriptorResolver
import org.jetbrains.kotlin.load.kotlin.JavaClassDataFinder
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.JavaToKotlinClassMap
import org.jetbrains.kotlin.platform.JvmBuiltIns
import org.jetbrains.kotlin.resolve.jvm.JavaDescriptorResolver
import org.jetbrains.kotlin.serialization.deserialization.DeserializationComponents
import org.jetbrains.kotlin.serialization.deserialization.LocalClassifierResolver
import org.jetbrains.kotlin.serialization.deserialization.NotFoundClasses
import org.jetbrains.kotlin.storage.LockBasedStorageManager

class RuntimeModuleData private constructor(val deserialization: DeserializationComponents, val packageFacadeProvider: RuntimePackagePartProvider) {
    val module: ModuleDescriptor get() = deserialization.moduleDescriptor
    val localClassifierResolver: LocalClassifierResolver get() = deserialization.localClassifierResolver

    companion object {
        fun create(classLoader: ClassLoader): RuntimeModuleData {
            val storageManager = LockBasedStorageManager()
            val builtIns = JvmBuiltIns(storageManager)
            val module = ModuleDescriptorImpl(Name.special("<runtime module for $classLoader>"), storageManager,
                                              ModuleParameters(listOf(), JavaToKotlinClassMap.INSTANCE), builtIns)

            val reflectKotlinClassFinder = ReflectKotlinClassFinder(classLoader)
            val deserializedDescriptorResolver = DeserializedDescriptorResolver(RuntimeErrorReporter)
            val singleModuleClassResolver = SingleModuleClassResolver()
            val runtimePackageFacadeProvider = RuntimePackagePartProvider(classLoader)
            val javaResolverCache = JavaResolverCache.EMPTY
            val globalJavaResolverContext = JavaResolverComponents(
                    storageManager, ReflectJavaClassFinder(classLoader), reflectKotlinClassFinder, deserializedDescriptorResolver,
                    ExternalAnnotationResolver.EMPTY, SignaturePropagator.DO_NOTHING, RuntimeErrorReporter, javaResolverCache,
                    JavaPropertyInitializerEvaluator.DoNothing, SamConversionResolver, RuntimeSourceElementFactory, singleModuleClassResolver,
                    runtimePackageFacadeProvider, SupertypeLoopChecker.EMPTY, LookupTracker.DO_NOTHING, module, ReflectionTypes(module)
            )

            val lazyJavaPackageFragmentProvider = LazyJavaPackageFragmentProvider(globalJavaResolverContext)

            builtIns.setOwnerModuleDescriptor(module)

            val javaDescriptorResolver = JavaDescriptorResolver(lazyJavaPackageFragmentProvider, javaResolverCache)
            val javaClassDataFinder = JavaClassDataFinder(reflectKotlinClassFinder, deserializedDescriptorResolver)
            val notFoundClasses = NotFoundClasses(storageManager, module)
            val binaryClassAnnotationAndConstantLoader = BinaryClassAnnotationAndConstantLoaderImpl(
                    module, notFoundClasses, storageManager, reflectKotlinClassFinder
            )
            val deserializationComponentsForJava = DeserializationComponentsForJava(
                    storageManager, module, javaClassDataFinder, binaryClassAnnotationAndConstantLoader,
                    lazyJavaPackageFragmentProvider, notFoundClasses, RuntimeErrorReporter, LookupTracker.DO_NOTHING
            )
            singleModuleClassResolver.resolver = javaDescriptorResolver
            deserializedDescriptorResolver.setComponents(deserializationComponentsForJava)

            module.setDependencies(module, builtIns.builtInsModule)
            module.initialize(javaDescriptorResolver.packageFragmentProvider)

            return RuntimeModuleData(deserializationComponentsForJava.components, runtimePackageFacadeProvider)
        }
    }
}
