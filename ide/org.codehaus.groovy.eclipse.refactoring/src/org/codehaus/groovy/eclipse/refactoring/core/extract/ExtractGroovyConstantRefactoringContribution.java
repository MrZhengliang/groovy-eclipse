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
package org.codehaus.groovy.eclipse.refactoring.core.extract;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.scripting.JavaUIRefactoringContribution;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class ExtractGroovyConstantRefactoringContribution extends JavaUIRefactoringContribution {

    public RefactoringDescriptor createDescriptor() {
        return RefactoringSignatureDescriptorFactory.createExtractConstantDescriptor();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map arguments, int flags) {
        return RefactoringSignatureDescriptorFactory.createExtractConstantDescriptor(project, description, comment, arguments, flags);
    }

    @Override
    public Refactoring createRefactoring(JavaRefactoringDescriptor descriptor, RefactoringStatus status) throws CoreException {
        JavaRefactoringArguments arguments = new JavaRefactoringArguments(descriptor.getProject(), retrieveArgumentMap(descriptor));
        return new ExtractGroovyConstantRefactoring(arguments, status);
    }
}
