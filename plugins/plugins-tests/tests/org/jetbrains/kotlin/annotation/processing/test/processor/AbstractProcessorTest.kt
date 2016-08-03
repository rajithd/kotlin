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

package org.jetbrains.kotlin.annotation.processing.test.processor

import com.intellij.testFramework.registerServiceInstance
import org.jetbrains.kotlin.android.synthetic.AndroidConfigurationKeys
import org.jetbrains.kotlin.android.synthetic.AndroidExtensionPropertiesComponentContainerContributor
import org.jetbrains.kotlin.android.synthetic.codegen.AndroidExpressionCodegenExtension
import org.jetbrains.kotlin.android.synthetic.codegen.AndroidOnDestroyClassBuilderInterceptorExtension
import org.jetbrains.kotlin.android.synthetic.res.AndroidLayoutXmlFileManager
import org.jetbrains.kotlin.android.synthetic.res.AndroidVariant
import org.jetbrains.kotlin.android.synthetic.res.CliAndroidLayoutXmlFileManager
import org.jetbrains.kotlin.android.synthetic.res.CliAndroidPackageFragmentProviderExtension
import org.jetbrains.kotlin.annotation.AbstractAnnotationProcessingExtension
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.codegen.AbstractBytecodeTextTest
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.lang.resolve.android.test.createTestEnvironment
import org.jetbrains.kotlin.lang.resolve.android.test.getResPaths
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisCompletedHandlerExtension
import org.jetbrains.kotlin.resolve.jvm.extensions.PackageFragmentProviderExtension
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.TestJdkKind
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import java.io.File
import javax.annotation.processing.Processor

class AnnotationProcessingExtensionForTests(
        val processors: List<Processor>,
        generatedSourcesOutputDir: File,
        classesOutputDir: File,
        javaSourceRoots: List<File>
) : AbstractAnnotationProcessingExtension(generatedSourcesOutputDir, classesOutputDir, javaSourceRoots) {
    override fun loadAnnotationProcessors() = processors
}

abstract class AbstractAndroidProcessorTest : AbstractBytecodeTextTest() {

    private fun createEnvironment(path: String) {
        return createEnvironmentForConfiguration(KotlinTestUtils.newConfiguration(ConfigurationKind.ALL, TestJdkKind.MOCK_JDK), path)
    }

    private fun createEnvironmentForConfiguration(configuration: CompilerConfiguration, path: String) {
        val layoutPaths = getResPaths(path)
        myEnvironment = createTestEnvironment(configuration, layoutPaths)
    }

    private fun KtUsefulTestCase.createTestEnvironment(configuration: CompilerConfiguration): KotlinCoreEnvironment {
        val myEnvironment = KotlinCoreEnvironment.createForTests(testRootDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
        val project = myEnvironment.project

        val apExtension = AnnotationProcessingExtensionForTests(listOf(), )
        AnalysisCompletedHandlerExtension.registerExtension(project, apExtension)

        return myEnvironment
    }

    override fun doTest(path: String) {
        val fileName = path + getTestName(true) + ".kt"
        createEnvironment(path)
        loadFileByFullPath(fileName)
        val expected = readExpectedOccurrences(fileName)
        val actual = generateToText()




        checkGeneratedTextAgainstExpectedOccurrences(actual, expected)
    }
}