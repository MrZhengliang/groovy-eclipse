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
package org.codehaus.groovy.eclipse.quickfix.test.resolvers;

import static org.codehaus.groovy.eclipse.core.model.GroovyRuntime.removeGroovyClasspathContainer;
import static org.junit.Assume.assumeTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.quickfix.proposals.AddClassCastResolver;
import org.codehaus.groovy.eclipse.quickfix.proposals.AddClassCastResolver.AddClassCastProposal;
import org.codehaus.groovy.eclipse.quickfix.proposals.AddGroovyRuntimeResolver;
import org.codehaus.groovy.eclipse.quickfix.proposals.AddMissingGroovyImportsResolver;
import org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver;
import org.codehaus.groovy.eclipse.quickfix.proposals.ProblemType;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

/**
 * Tests Groovy quick fixes in a Groovy file contained in a Groovy Project
 *
 * @author Nieraj Singh
 */
public class GroovyProjectGroovyQuickFixTest extends GroovyProjectQuickFixHarness {

    private static final String SUBTEST = "com.test.subtest";
    private static final String SUBSUBTEST = "com.test.subtest.subtest";
    private static final String TOP_LEVEL_TYPE = "class TopLevelType { class InnerType { class InnerInnerType { } } }";

    private ICompilationUnit topLevelUnit;

    protected void setUp() throws Exception {
        super.setUp();
        IPackageFragment subtestPackFrag = testProject.createPackage(SUBTEST);
        topLevelUnit = createGroovyType(subtestPackFrag, "TopLevelType.groovy", TOP_LEVEL_TYPE);
    }

    public void testAddImportField() throws Exception {
        String typeToImport = "TopLevelType";
        String typeToAddImport = "BarField";
        String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
        String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
        String typeToAddImportContent = "class BarField { TopLevelType typeVar }";

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent);
    }

    public void testBasicAddImportInnerType() throws Exception {
        // When an InnerType is referenced with its declaring type, for example,
        // Map.Entry,
        // "Map" is imported. When the InnerType is referenced by it's simple
        // name, there may
        // be further suggestions as other top level types might have inner
        // types with the same name
        // therefore "Inner" is imported and the actual fully qualified top
        // level is shown within parenthesis

        // This tests the inner type reference by itself: InnerType
        String typeToImport = "InnerType";
        String typeToAddImport = "BarUsingInner";
        String innerFullyQualified = "com.test.subtest.TopLevelType.InnerType";
        String expectedQuickFixDisplay = "Import 'InnerType' (com.test.subtest.TopLevelType)";
        String typeToAddImportContent = "class BarUsingInner { InnerType innerTypeVar }";

        testSelectImportGroovyTypeFromNewPackage(typeToImport, innerFullyQualified, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent);
    }

    public void testBasicAddImportInnerType2() throws Exception {
        // When an InnerType is referenced with its declaring type, for example,
        // Map.Entry,
        // "Map" is imported. When the InnerType is referenced by it's simple
        // name, there may
        // be further suggestions as other top level types might have inner
        // types with the same name
        // therefore "Inner" is imported and the actual fully qualified top
        // level is shown within parenthesis

        // This tests the inner type when it also contains the top level type:
        // TopLevelType.InnerType
        String typeToImport = "TopLevelType";
        String typeToAddImport = "BarUsingInnerB";
        String typeToImportFullyQualified = "com.test.subtest.TopLevelType";
        String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
        String typeToAddImportContent = "class BarUsingInnerB { TopLevelType.InnerType innerTypeVar }";

        testSelectImportGroovyTypeFromNewPackage(typeToImport, typeToImportFullyQualified, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent);
    }

    public void testBasicAddImportInnerInnerType() throws Exception {
        String typeToImport = "InnerInnerType";
        String typeToAddImport = "BarUsingInnerInner";
        String typeToImportFullyQualified = "com.test.subtest.TopLevelType.InnerType.InnerInnerType";
        String expectedQuickFixDisplay = "Import 'InnerInnerType' (com.test.subtest.TopLevelType.InnerType)";
        String typeToAddImportContent = "class BarUsingInnerInner { InnerInnerType innerTypeVar }";

        testSelectImportGroovyTypeFromNewPackage(typeToImport, typeToImportFullyQualified, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent);
    }

    public void testAddImportReturnType() throws Exception {
        String typeToImport = "TopLevelType";
        String typeToAddImport = "BarReturnType";
        String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
        String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
        String typeToAddImportContent = "class BarReturnType { public TopLevelType doSomething() { \n return null \n } }";

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent);
    }

    /**
     * Tests if an add import resolver can be found if the unresolved type is in a local variable declaration
     */
    public void testAddImportMethodParameter() throws Exception {
        String typeToImport = "TopLevelType";
        String typeToAddImport = "BarMethodParameter";
        String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
        String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
        String typeToAddImportContent = "class BarMethodParameter { public void doSomething(TopLevelType ttI) {  } }";

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent);
    }

    /**
     * Tests if an add import resolver can be found if the unresolved type is a generic
     */
    public void testAddImportGeneric() throws Exception {
        String typeToImport = "TopLevelType";
        String typeToAddImport = "BarGeneric";
        String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
        String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
        String typeToAddImportContent = "class BarGeneric { List<TopLevelType> aList }";

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent);
    }

    /**
     * Tests if an add import resolver can be found if a class is extending an unresolved type
     */
    public void testAddImportSubclassing() throws Exception {
        String typeToImport = "TopLevelType";
        String typeToAddImport = "BarSubclassing";
        String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
        String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
        String typeToAddImportContent = "class BarSubclassing extends TopLevelType {  }";

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent);
    }

    /**
     * Tests if an add import resolver can be found if the unresolved type is in a local variable declaration
     */
    public void testAddImportLocalVariable() throws Exception {
        String typeToImport = "TopLevelType";
        String typeToAddImport = "BarLocalVariable";
        String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
        String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
        String typeToAddImportContent = "class BarLocalVariable  { public void doSomething () { TopLevelType localVar  }  }";

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent);
    }

    /**
     * Tests that a Groovy add import quick fix resolver can be obtained when
     * the unresolved type is encountered in multiple places in the code.
     */
    public void testAddImportMultipleLocations() throws Exception {
        String typeToImport = "TopLevelType";
        String typeToAddImport = "BarMultipleLocations";
        String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
        String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
        String typeToAddImportContent = "class BarMultipleLocations extends TopLevelType { public List<TopLevelType> doSomething () {\n TopLevelType localVar \n return null }  }";

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent);
    }

    /**
     * Tests if a Groovy add import quick fix can be obtained when other
     * unresolved types exist in the Groovy file
     */
    public void testAddImportMultipleUnresolved() throws Exception {
        String typeToImport = "TopLevelType";
        String typeToAddImport = "BarMultipleUnresolved";
        String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
        String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
        String typeToAddImportContent = "class BarMultipleUnresolved extends TopLevelType { \n CSS css \n HTML val = new Entry() \n  }";

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent);
    }

    /**
     * Tests if a Groovy add import resolver has multiple suggestions for the
     * same unresolved simple name.
     */
    public void testAddImportMultipleProposalsForSameType() throws Exception {
        IPackageFragment subsubPackFrag = testProject.createPackage(SUBSUBTEST);
        createGroovyType(subsubPackFrag, "TopLevelType.groovy", TOP_LEVEL_TYPE);

        String typeToImport = "TopLevelType";
        String typeToAddImport = "BarLocalMultipleSameType";
        String typeToAddImportContent = "class BarLocalMultipleSameType { public void doSomething () { TopLevelType localVar } }";

        Map<String, String> expectedQuickFixes = new HashMap<String, String>();
        expectedQuickFixes.put("Import 'TopLevelType' (" + SUBTEST + ")", SUBTEST + ".TopLevelType");
        expectedQuickFixes.put("Import 'TopLevelType' (" + SUBSUBTEST + ")", SUBSUBTEST + ".TopLevelType");
        testMultipleProposalsSameTypeName(typeToImport, expectedQuickFixes, typeToAddImport, typeToAddImportContent);
    }

    /**
     * Tests that no Groovy add import quick fix resolvers are obtained for an
     * unresolved type that does not exist.
     */
    public void testAddImportNoProposals() throws Exception {
        String typeToAddImport = "BarAddImportNoProposal";
        String nonExistantType = "DoesNotExistTopLevelType";

        String typeToAddImportContent = "class BarAddImportNoProposal  { public void doSomething () { DoesNotExistTopLevelType localVar  }  }";
        ICompilationUnit unit = createGroovyTypeInTestPackage(typeToAddImport + ".groovy", typeToAddImportContent);
        AddMissingGroovyImportsResolver resolver = getAddMissingImportsResolver(nonExistantType, unit);

        assertNull("Expected no resolver for nonexistant type: " + nonExistantType, resolver);
    }

    /**
     * Tests if Groovy add import quick fix resolvers are obtained for an annotation.
     */
    public void testAddImportAnnotation() throws Exception {
        String typeToImport = "Target";

        String expectedQuickFixDisplay = "Import 'Target' (java.lang.annotation)";
        String fullQualifiedTypeToImport = "java.lang.annotation.Target";
        String typeToAddImport = "Test";

        String typeToAddImportContent = "@Target() public @interface Test {}";

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent);
    }

    public void testAddGroovyRuntime() throws Exception {
        assumeTrue("Project lacks Groovy nature", hasGroovyNature());
        removeGroovyClasspathContainer(testProject.getJavaProject());
        buildAll();

        IMarker[] markers = getProjectJDTFailureMarkers();
        assumeTrue("Should have found problems in this project", markers != null && markers.length > 0);

        List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(markers, ProblemType.MISSING_CLASSPATH_CONTAINER_TYPE, topLevelUnit);
        assertEquals("Should have found exactly one resolver", 1, resolvers.size());
        assertEquals("Wrong type of resolver", AddGroovyRuntimeResolver.class, resolvers.get(0).getClass());
        resolvers.get(0).getQuickFixProposals().get(0).apply(null);
        testProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertNull("Should not have found problems in this project", testProject.getProblems());
    }

    /**
     * Tests that no Groovy quick fix resolvers are encountered for unrecognised
     * errors
     */
    public void testUnrecognisedErrorNoProposals() throws Exception {

        String typeToAddImport = "BarUnrecognisedError";

        String typeToAddImportContent = "class BarUnrecognisedError  { public void doSomething () { 222  }  }";
        ICompilationUnit unit = createGroovyTypeInTestPackage(typeToAddImport
                + ".groovy", typeToAddImportContent);

        IMarker[] markers = getCompilationUnitJDTFailureMarkers(unit);

        ProblemType[] knownProblemTypes = getGroovyProblemTypes();

        assertTrue("No Groovy problem types to test", knownProblemTypes != null
                && knownProblemTypes.length > 0);

        for (ProblemType type : getGroovyProblemTypes()) {
            List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(
                    markers, type, unit);
            assertTrue(
                    "Encountered resolvers for unknown compilation error. None expected.",
                    resolvers == null || resolvers.isEmpty());
        }
    }

    public void testAddImportGRECLIPSE1612() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        testProject.createJavaTypeAndPackage("other", "FooJava.java",
                "public class FooJava {\n" +
                "    public static String getProperty() {\n" +
                "        return \"sad\";\n" +
                "    }\n" +
                "}");

        String typeToAddImport = "FooGroovy";
        String typeToAddImportContent = "@groovy.transform.TypeChecked\nclass FooGroovy {\n def main() { FooJava.getProperty() } }";
        ICompilationUnit unit = createGroovyTypeInTestPackage(typeToAddImport + ".groovy", typeToAddImportContent);
        IMarker[] markers = getCompilationUnitJDTFailureMarkers(unit);
        List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(markers, ProblemType.MISSING_IMPORTS_TYPE, unit);

        assertEquals("Should have found exactly one resolver", 1, resolvers.size());
        assertEquals("Wrong type of resolver", AddMissingGroovyImportsResolver.class, resolvers.get(0).getClass());
        IJavaCompletionProposal proposal = resolvers.get(0).getQuickFixProposals().get(0);
        assertEquals("Import 'FooJava' (other)", proposal.getDisplayString());
    }

    public void testGRECLIPSE1777() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21) return;

        ICompilationUnit unit = createGroovyTypeInTestPackage("D.groovy",
                "import groovy.transform.CompileStatic\n" +
                "@CompileStatic\n" +
                "class D {\n" +
                "    Number foo() {\n" +
                "        new Integer(1)\n" +
                "    }\n" +
                "    Integer bar() {\n" +
                "        Integer result = foo()\n" +
                "        result\n" +
                "    }\n" +
                "}");

        String expectedQuickFixDisplay = "Add cast to Integer";
        AddClassCastResolver resolver = getAddClassCastResolver(unit);
        assertNotNull("Expected a resolver for " + unit, resolver);
        AddClassCastProposal proposal = getAddClassCastProposal(expectedQuickFixDisplay, resolver);
        assertNotNull("Expected a quick fix proposal for " + unit, proposal);
        assertEquals("Actual quick fix display expression should be: " + expectedQuickFixDisplay, expectedQuickFixDisplay, proposal.getDisplayString());
    }
}
