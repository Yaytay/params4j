/*
 * Copyright (C) 2022 jtalbut
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.spudsoft.params4j.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import uk.co.spudsoft.params4j.JavadocCapture;


/**
 *
 * @author jtalbut
 */
@SupportedAnnotationTypes("uk.co.spudsoft.params4j.JavadocCapture")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class JavadocCapturer extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annnotations, RoundEnvironment re) {
    for (TypeElement annotation : annnotations) {
      if (annotation.getQualifiedName().toString().equals(JavadocCapture.class.getName())) {
        for (Element e : re.getElementsAnnotatedWith(annotation)) {
          if (e.getKind() == ElementKind.CLASS) {
            generateCommentProperties(new HashSet<>(), e);
          }
        }
      }
    }
    
    return true;
  }
  
  private void writeDocProperties(PackageElement packageElement, Element classElement, Properties commentProps) {
    try {
      FileObject fo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, packageElement.getQualifiedName(), classElement.getSimpleName() + "-doc.properties", classElement);
      try (OutputStream out = fo.openOutputStream()) {
        commentProps.store(out, "Documentation properties for " + ((TypeElement) classElement).getQualifiedName());
      }
      processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, "Output written to " + fo.toUri());
    } catch(IOException ex) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to write to resource file: " + ex.getMessage());
    }
  }
  
  private PackageElement getPackage(Element element) {
    if (element.getKind() == ElementKind.PACKAGE) {
      return (PackageElement) element;
    } else {
      Element parent = element.getEnclosingElement();
      if (parent != null) {
        return getPackage(parent);
      } else {
        return null;
      }
    }
  }

  public static String setterNameToVariableName(String setterName) {
    if (setterName == null) {
      return null;
    }
    if (setterName.startsWith("set") && setterName.length() > 3 && Character.isUpperCase(setterName.codePointAt(3))) {
      setterName = setterName.substring(3);
    }
    if (Character.isUpperCase(setterName.codePointAt(0))) {
      setterName = Character.toString(Character.toLowerCase(setterName.codePointAt(0))) + setterName.substring(1);
    }
    return setterName;
  }

  static String tidyComment(String comment) {
    if (comment == null) {
      return null;
    }
    comment = comment.split("\n")[0].trim();
    if (comment.startsWith("The ") || comment.startsWith("the ")) {
      comment = comment.substring(4);
    }
    if (comment.endsWith(".")) {
      comment = comment.substring(0, comment.length() - 1);
    }
    return comment;
  }
  
  private void generateCommentProperties(Set<String> elements, Element element) {
    ElementKind kind = element.getKind();
    Properties commentProps = new Properties();
    if (kind == ElementKind.CLASS) {
      if (elements.add(((TypeElement) element).getQualifiedName().toString())) {
        for (Element child : element.getEnclosedElements()) {
          if (child.getKind() == ElementKind.METHOD) {
            ExecutableElement method = (ExecutableElement) child;
            String childName = child.getSimpleName().toString();
            if (method.getParameters().size() == 1 && childName.startsWith("set")) {
              VariableElement parameter = method.getParameters().get(0);
              DeclaredType parameterClass = null;
              if (parameter.asType() instanceof DeclaredType) {
                parameterClass = (DeclaredType) parameter.asType();
              }

              String variableName = setterNameToVariableName(childName);
              String docComment = tidyComment(processingEnv.getElementUtils().getDocComment(method));
              if (docComment != null && !docComment.isEmpty()) {
                commentProps.put(variableName, docComment);
              }

              if (parameterClass != null) {
                generateCommentProperties(elements, parameterClass.asElement());
              }              
            }
          } else if (child.getKind() == ElementKind.CLASS) {
            generateCommentProperties(elements, child);
          }
        }
        if (!commentProps.isEmpty()) {
          writeDocProperties(getPackage(element), element, commentProps);
        }
      }
    }
  }

}
