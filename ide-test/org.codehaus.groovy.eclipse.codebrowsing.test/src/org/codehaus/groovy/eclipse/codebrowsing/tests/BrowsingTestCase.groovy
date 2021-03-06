/*
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.codebrowsing.tests

import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite

import org.codehaus.groovy.eclipse.test.EclipseTestSetup
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.SourceRange

abstract class BrowsingTestCase extends TestCase {

    /**
     * Parent class should define:<pre>
     * public static Test suite() {
     *   return newTestSuite(Whatever.class);
     * }</pre>
     */
    protected static Test newTestSuite(Class test) {
        new EclipseTestSetup(new TestSuite(test))
    }

    @Override
    protected void tearDown() throws Exception {
        EclipseTestSetup.removeSources()
    }

    @Override
    protected void setUp() throws Exception {
        println '----------------------------------------'
        println 'Starting: ' + getName()
    }

    protected GroovyCompilationUnit addGroovySource(CharSequence contents, String name = nextFileName(), String pack = '') {
        EclipseTestSetup.addGroovySource(contents, name, pack)
    }

    protected void addJavaSource(CharSequence contents, String name = nextFileName(), String pack = '') {
        EclipseTestSetup.addJavaSource(contents, name, pack)
    }

    protected IJavaElement assertCodeSelect(Iterable<? extends CharSequence> sources, String target, String elementName = target) {
        def unit = null
        sources.each {
            unit = addGroovySource(it.toString(), nextFileName())
        }
        prepareForCodeSelect(unit)

        int offset = unit.source.lastIndexOf(target), length = target.length()
        assert offset >= 0 && length > 0 && offset + length <= unit.source.length()

        IJavaElement[] elems = unit.codeSelect(offset, length)
        if (!elementName) {
            assertEquals(0, elems.length)
        } else {
            assertEquals('Should have found a selection', 1, elems.length)
            assertEquals('Should have found reference to: ' + elementName, elementName, elems[0].elementName)
            assertTrue('Element should have existed in the model', elems[0].exists())
            return elems[0]
        }
    }

    protected IJavaElement assertCodeSelect(CharSequence source, SourceRange targetRange, String elementName) {
        GroovyCompilationUnit gunit = addGroovySource(source, nextFileName())
        prepareForCodeSelect(gunit)

        IJavaElement[] elems = gunit.codeSelect(targetRange.getOffset(), targetRange.getLength())
        if (!elementName) {
            assertEquals(0, elems.length)
        } else {
            assertEquals('Should have found a selection', 1, elems.length)
            assertEquals('Should have found reference to: ' + elementName, elementName, elems[0].elementName)
            assertTrue(elems[0].exists())
            return elems[0]
        }
    }

    private static Random salt = new Random(System.currentTimeMillis())

    protected static String nextFileName() {
        "File${salt.nextInt(999999)}"
    }

    protected static void prepareForCodeSelect(ICompilationUnit unit) {
        if (unit instanceof GroovyCompilationUnit) {
            def problems = unit.getModuleInfo(true).result.problems
            problems?.findAll { it.error }?.each { println it }
        }

        EclipseTestSetup.waitForIndex()
        EclipseTestSetup.openInEditor(unit)
    }
}
