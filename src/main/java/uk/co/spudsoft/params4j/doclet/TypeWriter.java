/*
 * Copyright (C) 2023 njt
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
package uk.co.spudsoft.params4j.doclet;

import com.sun.source.doctree.ReferenceTree;
import com.sun.source.util.DocTreePath;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 * Write out AsciiDoc of types found.
 * @author njt
 */
public class TypeWriter {
  
  private final Writer writer;
  private final Reporter reporter;
  private final Set<String> includedClasses;
  private final AsciiDocLinkMaps linkMaps;
  
  public void writeReferenceTree(DocletEnvironment environment, DocTreePath currentPath, ReferenceTree refTree) {
    Element element = environment.getDocTrees().getElement(new DocTreePath(currentPath, refTree));
    if (element instanceof ExecutableElement) {      
      TypeElement typeElement = (TypeElement) element.getEnclosingElement();
      ExecutableElement methodElement = (ExecutableElement) element;
      
      String url = linkMaps.getUrlForType(reporter, TypeWriter.getPackage(typeElement), TypeWriter.getClassName(typeElement));
      if (url == null) {
        if (includedClasses.contains(typeElement.getQualifiedName().toString())) {
          write("xref:");
          write(typeElement.getQualifiedName().toString());
          write(".adoc[");
          // For method links javadoc removes leading packages and type parameters        
          write(TypeWriter.simplifySignature(refTree.getSignature()));
          write("] ");
        } else {
          write(refTree.getSignature());
          write(" ");
        }
      } else {
        write("link:");
        write(linkMaps.getUrlForType(reporter, TypeWriter.getPackage(typeElement), TypeWriter.getClassName(typeElement)));
        write(TypeWriter.getMethodAnchor(methodElement));
        write("[");
        // For method links javadoc removes leading packages and type parameters        
        write(TypeWriter.simplifySignature(refTree.getSignature()));
        write("] ");
      }
    } else if (element instanceof TypeElement) {
      TypeElement typeElement = (TypeElement) element;
      
      String url = linkMaps.getUrlForType(reporter, TypeWriter.getPackage(typeElement), TypeWriter.getClassName(typeElement));
      if (url == null) {
        if (includedClasses.contains(typeElement.getQualifiedName().toString())) {
          write("xref:");
          write(typeElement.getQualifiedName().toString());
          write(".adoc[");
          // For method links javadoc removes leading packages and type parameters        
          write(refTree.getSignature());
          write("] ");
        } else {
          write(refTree.getSignature());
          write(" ");
        }
      } else {
        write("link:");
        write(linkMaps.getUrlForType(reporter, TypeWriter.getPackage(element), TypeWriter.getClassName(element)));
        write("[");
        write(refTree.getSignature());
        write("] ");
      }
    }
  }
  
  
  public void writeDeclaredType(DeclaredType declaredType) {
    
    TypeElement typeElement = (TypeElement) declaredType.asElement();    
    
    String url = linkMaps.getUrlForType(reporter, TypeWriter.getPackage(typeElement), TypeWriter.getClassName(typeElement));

    if (url != null) {
      write("link:");
      write(url);
      write("[");
      write(typeElement.getSimpleName().toString());
      write("]");
    } else if (includedClasses.contains(typeElement.getQualifiedName().toString())) {
      write("xref:");
      write(typeElement.getQualifiedName().toString());
      write(".adoc[");
      write(typeElement.getSimpleName().toString());
      write("]");
    } else {
      write(declaredType.toString());
    }
    
    if (!declaredType.getTypeArguments().isEmpty()) {
      write("<");
      boolean first = true;
      for (TypeMirror ta : declaredType.getTypeArguments()) {
        if (!first) {
          write(", ");
        }
        first = false;
        if (ta instanceof DeclaredType) {
          writeDeclaredType((DeclaredType) ta);
        } else if (ta != null) {
          write(ta.toString());
        }
      }
      write(">");
    }
  }      
  
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Objects are not POJOs and may be accessed (and modified) during the life of this object")
  public TypeWriter(Writer writer, Reporter reporter, Set<String> includedClasses, AsciiDocLinkMaps linkMaps) {
    super();
    this.writer = writer;
    this.reporter = reporter;
    this.includedClasses = includedClasses;
    this.linkMaps = linkMaps;
  }

  public static String getPackage(Element elem) {
    while (elem != null && !(elem instanceof PackageElement)) {
      elem = elem.getEnclosingElement();
    }
    if (elem != null) {
      return ((PackageElement) elem).getQualifiedName().toString();
    }
    
    return null;
  }
  
  public static String getMethodAnchor(ExecutableElement methodElement) {
    StringBuilder builder = new StringBuilder();
    builder.append("#");
    builder.append(methodElement.getSimpleName());
    builder.append("(");
    boolean first = true;
    for (VariableElement ve : methodElement.getParameters()) {
      if (!first) {
        builder.append(",");
      }
      first = false;
      String type = ve.asType().accept(new SimpleTypeVisitor14<String, Void>(){
        @Override
        public String visitDeclared(DeclaredType t, Void p) {
          return t.asElement().toString();
        }        
        @Override
        public String visitArray(ArrayType t, Void p) {
          return t.toString().replaceAll("\\[\\]", "%5B%5D");
        }
        @Override
        public String visitPrimitive(PrimitiveType t, Void p) {
          return t.toString();
        }
      }, null);
      builder.append(type);
    }
    builder.append(")");
    return builder.toString();
  }
  
  public static String getBaseClassName(Element elem) {
    Element lastElem = elem;
    while (elem instanceof TypeElement) {
      elem = elem.getEnclosingElement();
      if (elem instanceof TypeElement) {
        lastElem = elem;
      }
    }    
    return lastElem.getSimpleName().toString();
  }
  
  public static String getClassName(Element elem) {
    StringBuilder builder = new StringBuilder();
    while (elem instanceof TypeElement) {
      if (!builder.isEmpty()) {
        builder.insert(0, ".");
      }
      builder.insert(0, elem.getSimpleName());
      elem = elem.getEnclosingElement();
    }    
    return builder.toString();
  }
  
  /**
   * Remove, using regexes, leading packages and type parameters.
   * @param signature The signature to be simplified.
   * @return A  simpler signature.
   */
  private static final Pattern LEADING_PACKAGE = Pattern.compile("^([a-z][a-zA-Z0-9]+\\.)");
  private static final Pattern TYPE_PARAMS = Pattern.compile("(<[^<]+>)");
  private static final Pattern SQUARE_BRACKET = Pattern.compile("\\]");
  private static final Pattern CONSTRUCTOR = Pattern.compile("^([a-zA-Z0-9]+)#\\1");
  public static String simplifySignature(String signature) {
    Matcher matcher;
    while ((matcher = LEADING_PACKAGE.matcher(signature)).find()) {
      signature = matcher.replaceAll("");
    }
    
    Matcher tpm = TYPE_PARAMS.matcher(signature);
    while (tpm.find()) {
      signature = tpm.replaceAll("");
      tpm = TYPE_PARAMS.matcher(signature);
    }
    
    signature = SQUARE_BRACKET.matcher(signature).replaceAll("\\\\]");
    
    signature = CONSTRUCTOR.matcher(signature).replaceAll(mr -> mr.group(1));
    
    return signature.trim();
  }

  private void write(String s) {
    s = s.replaceAll("\n ", "\n");
    
    try {
      writer.write(s);
    } catch (IOException ex) {
      reporter.print(Diagnostic.Kind.ERROR, "Failed to write: " + ex.getMessage());
    }
  }

}
