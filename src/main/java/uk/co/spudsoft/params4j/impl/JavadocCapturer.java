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
 * Annotation processor for capturing Javadoc comments and converting them to annotations that can be understood by Params4J.
 * This provides a simple mechanism for document parameters both in Javadoc and on the command line in one go.
 * @author jtalbut
 */
@SupportedAnnotationTypes("uk.co.spudsoft.params4j.JavadocCapture")
public class JavadocCapturer extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annnotations, RoundEnvironment re) {
    for (TypeElement annotation : annnotations) {
      if (annotation.getQualifiedName().toString().equals(JavadocCapture.class.getName())) {
        for (Element e : re.getElementsAnnotatedWith(annotation)) {
          if (e.getKind() == ElementKind.CLASS) {
            generateCommentProperties(new HashSet<>(), (TypeElement) e);
          }
        }
      }
    }
    
    return true;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
  
  private void writeDocProperties(PackageElement packageElement, TypeElement classElement, Properties commentProps) {
    try {
      FileObject fo = processingEnv.getFiler().createResource(
              StandardLocation.CLASS_OUTPUT
              , packageElement.getQualifiedName()
              , classElement.getSimpleName() + "-doc.properties"
              , classElement
      );
      try (OutputStream out = fo.openOutputStream()) {
        commentProps.store(out, "Documentation properties for " + classElement.getQualifiedName());
      }
      processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, "Output written to " + fo.toUri());
    } catch (IOException ex) {
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

  /**
   * Convert the name of a setter to the name of the field.
   * Strips "set" from the beginning and lowercases the following character.
   * @param setterName The name of the bean field setter.
   * @return The name of setter with "set" removed and with a leading lowercase character.
   */
  public static String setterNameToVariableName(String setterName) {
    if (setterName == null) {
      return null;
    }
    if (setterName.startsWith("set") && setterName.length() > 3 && Character.isUpperCase(setterName.codePointAt(3))) {
      setterName = setterName.substring(3);
    }
    if (Character.isUpperCase(setterName.codePointAt(0))) {
      setterName = Character.toString(Character.toLowerCase(setterName.codePointAt(0))) + setterName.substring(setterName.offsetByCodePoints(0, 1));
    }
    return setterName;
  }
  /**
   * Convert the name of a getter to the name of the field.
   * Strips "get" or "is" from the beginning and lowercases the following character.
   * @param getterName The name of the bean field getter.
   * @return The name of getter with "get" or "is" removed and with a leading lowercase character.
   */
  public static String getterNameToVariableName(String getterName) {
    if (getterName == null) {
      return null;
    }
    if (getterName.startsWith("get") && getterName.length() > 3 && Character.isUpperCase(getterName.codePointAt(3))) {
      getterName = getterName.substring(3);
    }
    if (getterName.startsWith("is") && getterName.length() > 2 && Character.isUpperCase(getterName.codePointAt(2))) {
      getterName = getterName.substring(2);
    }
    if (Character.isUpperCase(getterName.codePointAt(0))) {
      getterName = Character.toString(Character.toLowerCase(getterName.codePointAt(0))) + getterName.substring(getterName.offsetByCodePoints(0, 1));
    }
    return getterName;
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
    return comment.trim();
  }
  
  private void generateCommentProperties(Set<String> elements, TypeElement element) {
    Properties commentProps = new Properties();
    if (elements.add(element.getQualifiedName().toString())) {
      for (Element child : element.getEnclosedElements()) {
        String childName = child.getSimpleName().toString();
        if (child.getKind() == ElementKind.FIELD) {
          if (!commentProps.containsKey(childName)) {
            // Add javadocs on fields, but only if the setter hasn't already written the comment
            String docComment = tidyComment(processingEnv.getElementUtils().getDocComment(child));
            if (docComment != null && !docComment.isEmpty()) {
              commentProps.put(childName, docComment);
            }
          }

          DeclaredType parameterClass = null;
          VariableElement variable = (VariableElement) child;
          if (variable.asType() instanceof DeclaredType) {
            parameterClass = (DeclaredType) variable.asType();
          }
          if (parameterClass != null) {
            generateCommentProperties(elements, (TypeElement) parameterClass.asElement());
          }                          
        } else if (child.getKind() == ElementKind.METHOD) {
          ExecutableElement method = (ExecutableElement) child;
          if (method.getParameters().size() == 1 && childName.startsWith("set")) {
            VariableElement parameter = method.getParameters().get(0);
            DeclaredType parameterClass = null;
            if (parameter.asType() instanceof DeclaredType) {
              parameterClass = (DeclaredType) parameter.asType();
            }

            String variableName = setterNameToVariableName(childName);
            String docComment = tidyComment(processingEnv.getElementUtils().getDocComment(method));
            if (docComment != null && !docComment.isEmpty()) {
              // Javadocs on setters take precedence over Javadocs on fields.
              commentProps.put(variableName, docComment);
            }

            if (parameterClass != null) {
              generateCommentProperties(elements, (TypeElement) parameterClass.asElement());
            }              
          }
        } else if (child.getKind() == ElementKind.CLASS) {
          generateCommentProperties(elements, (TypeElement) child);
        }
      }
      if (!commentProps.isEmpty()) {
        writeDocProperties(getPackage(element), element, commentProps);
      }
    }
  }

}
