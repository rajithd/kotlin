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

@file:JvmName("KClassesJvm")
package kotlin.reflect.jvm

import kotlin.reflect.KClass

/**
 * Returns the JVM name of the class represented by this [KClass] instance.
 *
 * @see [Class.getName]
 */
val KClass<*>.jvmName: String
    get() = java.name

/**
 * TODO: which class loader exactly?
 *
 * Returns the [KClass] instance which has the given JVM name,
 * or throws an exception if such class does not exist, according to the class loader which loaded the [KClass] interface.
 *
 * This function is different from [KClass.byQualifiedName] in that ... TODO
 *
 * @see [Class.forName]
 */
fun KClass.Companion.byJvmName(jvmName: String): KClass<*> =
        Class.forName(jvmName, true, KClass::class.java.classLoader).kotlin
