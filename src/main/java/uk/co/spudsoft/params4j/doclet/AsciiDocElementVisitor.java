/*
 * Copyright (C) 2023 jtalbut
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

import com.sun.source.doctree.DocCommentTree;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import uk.co.spudsoft.params4j.impl.JavadocCapturer;

/**
 * {@link ElementVisitor} for generating AsciiDocs from JavaDocs.
 * 
 * This is used by the AsciiDocDoclet to walk the Java language elements.
 * 
 * @author njt
 */
public class AsciiDocElementVisitor implements ElementVisitor<Void, Boolean> {
  
  private final DocletEnvironment environment;
  private final AsciiDocOptions options;
  private final Reporter reporter;
  private final boolean verbose;

  private Writer writer;
  private TypeWriter typeWriter;
  private final Set<String> fields = new HashSet<>();
  private final Map<String, String> capturedDocs = new HashMap<>();
  
  /**
   * Constructor.
   * @param environment The DocletEnvironment, provided by the Doclet processor, via the Doclet.
   * @param options Options found in the pom configuration.
   * @param reporter Doclet diagnostics reporter - the only mechanism for logging issues.
   */
  public AsciiDocElementVisitor(DocletEnvironment environment, AsciiDocOptions options, Reporter reporter) {
    this.environment = environment;
    this.options = options;
    this.reporter = reporter;
    this.verbose = Boolean.getBoolean("uk.co.spudsoft.doclet.verbose");
  }
  
  @Override
  public Void visit(Element e, Boolean p) {
    return null;
  }

  @Override
  public Void visitPackage(PackageElement e, Boolean p) {
    return visitStandard(e, p);
  }
  
  private boolean include(TypeElement e) {
    if (options.getIncludeClasses() == null || options.getIncludeClasses().isEmpty()) {
      return true;
    }
    return options.getIncludeClasses().contains(e.getQualifiedName().toString());
  }

  @Override
  @SuppressFBWarnings(value = "PATH_TRAVERSAL_IN", justification = "The point of this class is to write to that file, ensure it is correct")
  public Void visitType(TypeElement e, Boolean p) {
    if (verbose) {
      reporter.print(Diagnostic.Kind.NOTE, "Visiting Type " + e.getQualifiedName());
    }
    
    if (include(e)) {  
      File dir = new File(options.getDestDirName());
      if (!dir.exists()) {
        if (!dir.mkdirs()) {
          reporter.print(Diagnostic.Kind.ERROR, "Failed to create directory " + dir.getAbsolutePath());
          return null;
        }
      }
      fields.clear();
      File output = new File(dir, e.getQualifiedName() + ".adoc");
      reporter.print(Diagnostic.Kind.NOTE, "Writing file " + output.getAbsolutePath());
      try (Writer newWriter = new FileWriter(output, StandardCharsets.UTF_8)) {
        this.writer = newWriter;
        this.typeWriter = new TypeWriter(writer, reporter, options.getIncludeClasses(), options.getLinkMaps());
        
        try {
          writer.write("= " + e.getSimpleName());
          writer.write("\n\n");
          
          AsciiDocDocTreeWalker docTreeWalker = new AsciiDocDocTreeWalker(environment, options, writer, reporter, environment.getDocTrees().getPath(e));
          docTreeWalker.scan();
          writer.write("\n");
          writer.write("\n");

          writer.write("[cols=\"1,1a,4a\",stripes=even]\n");
          writer.write("|===\n");
          writer.write("| Name\n");
          writer.write("| Type\n");
          writer.write("| Details\n");
          writer.write("\n\n");

          documentFields(e);

          for (TypeMirror superMirror = e.getSuperclass(); superMirror != null;) {

            Element superElement = environment.getTypeUtils().asElement(superMirror);
            if (superElement instanceof TypeElement) {
              TypeElement superTypeElement = (TypeElement) superElement;
              documentFields(superTypeElement);
              superMirror = superTypeElement.getSuperclass();
            } else {
              superMirror = null;
            }
          }
          
          writer.write("|===\n");
        } catch (IOException ex) {
          reporter.print(Diagnostic.Kind.ERROR, "Failed to write to file: " + ex.getMessage());
        }
      } catch (IOException ex) {
        reporter.print(Diagnostic.Kind.ERROR, "Failed to open file " + output.getAbsolutePath());
        return null;
      } finally {
        writer = null;
      }
    }
    
    return null;
  }
  
  private void documentFields(TypeElement type) {
    type.getEnclosedElements().forEach(enclosed -> {
      if (enclosed.getKind() == ElementKind.FIELD) {
        enclosed.accept(this, Boolean.FALSE);
      }
    });
    type.getEnclosedElements().forEach(enclosed -> {
      if (enclosed.getKind() == ElementKind.METHOD) {
        enclosed.accept(this, Boolean.TRUE);
      }
    });
    type.getEnclosedElements().forEach(enclosed -> {
      if (enclosed.getKind() == ElementKind.METHOD) {
        enclosed.accept(this, Boolean.FALSE);
      }
    });
    type.getEnclosedElements().forEach(enclosed -> {
      if (enclosed.getKind() == ElementKind.FIELD) {
        enclosed.accept(this, Boolean.TRUE);
      }
    });    
  }
  
  /**
   * For fields, parameter TRUE means gather private field comments into capturedDocs.
   * @param e The element.
   * @param p If TRUE gather private field comments into capturedDocs, otherwise output them into the file using the current writer. 
   * @return Null
   */
  @Override
  public Void visitVariable(VariableElement e, Boolean p) {
    if (p == null || p) {
      return visitVariableAndOutputNonPrivate(e);
    } else {
      return visitVariableAndCapturePrivate(e);
    }    
  }

  /**
   * For fields, parameter TRUE means gather setters, FALSE means gather getters.
   * @param e The element.
   * @param p If TRUE gather write setters, if FALSE write getters using the current writer. 
   * @return Null
   */
    @Override
  public Void visitExecutable(ExecutableElement e, Boolean p) {
    if (p == null || p) {
      return visitExecutableAndOutputSetters(e);
    } else {
      return visitExecutableAndOutputGetters(e);
    }
  }
    
  public Void visitVariableAndCapturePrivate(VariableElement e) {
    try {
      String fieldName = e.getSimpleName().toString();
      // Capture docs on all variables, even if they are private
      Writer stringWriter = new StringWriter();
      AsciiDocDocTreeWalker docTreeWalker = new AsciiDocDocTreeWalker(environment, options, stringWriter, reporter, environment.getDocTrees().getPath(e));
      DocCommentTree docCommentTree = environment.getDocTrees().getDocCommentTree(e);
      if (docCommentTree != null) {
        docTreeWalker.scan();
      }
      String doc = stringWriter.toString();
      if (doc != null && !doc.isBlank()) {
        capturedDocs.put(fieldName, doc);
      }
    } catch (Throwable ex) {
      reporter.print(Diagnostic.Kind.ERROR, "Failed to process field: " + ex.getMessage());
    }
    return null;
  }

  public Void visitVariableAndOutputNonPrivate(VariableElement e) {
    try {
      String fieldName = e.getSimpleName().toString();
      if (!this.fields.contains(fieldName)) {
        if (!e.getModifiers().contains(Modifier.PRIVATE)) {
          this.fields.add(fieldName);

          writer.write("\n| [[");
          writer.write(fieldName);
          writer.write("]]");
          writer.write(fieldName);
          writer.write("\n");

          writer.write("| ");         
          VariableElement variableElement = e;

          DeclaredType declaredType = getDeclaredType(variableElement);
          Element declaredTypeElement = declaredType == null ? null : declaredType.asElement();
          TypeElement typeElement = declaredTypeElement instanceof TypeElement ? (TypeElement) declaredTypeElement : null;
          if (typeElement == null) {
            writer.write(variableElement.asType().toString());
          } else {
            typeWriter.writeDeclaredType(declaredType);
          }
          writer.write("\n");

          writer.write("| ");
          AsciiDocDocTreeWalker docTreeWalker = new AsciiDocDocTreeWalker(environment, options, writer, reporter, environment.getDocTrees().getPath(e));
          DocCommentTree docCommentTree = environment.getDocTrees().getDocCommentTree(e);
          if (docCommentTree == null) {
            reporter.print(Diagnostic.Kind.WARNING, "No doc comment for " + e.getSimpleName());
          } else {
            docTreeWalker.scan();
            // environment.getDocTrees().getDocCommentTree(e).accept(docTreeWalker, null);
          }
          writer.write("\n\n");
        }
      }
    } catch (IOException ex) {
      reporter.print(Diagnostic.Kind.ERROR, "Failed to write to file: " + ex.getMessage());
    }
    return null;
  }

  private DeclaredType getDeclaredType(VariableElement variableElement) {
    DeclaredType declaredType = variableElement.asType().accept(new SimpleTypeVisitor14<DeclaredType, Void>(){
      @Override
      public DeclaredType visitDeclared(DeclaredType t, Void p) {
        return t;
      }
    }, null);
    return declaredType;
  }

  public Void visitExecutableAndOutputSetters(ExecutableElement e) {

    if (!e.getModifiers().contains(Modifier.PRIVATE) 
            && e.getSimpleName().toString().startsWith("set") && e.getParameters().size() == 1) {
      try {
        String fieldName = JavadocCapturer.setterNameToVariableName(e.getSimpleName().toString());
        if (!this.fields.contains(fieldName)) {
          this.fields.add(fieldName);

          writer.write("\n| [[");
          writer.write(fieldName);
          writer.write("]]");
          writer.write(fieldName);
          writer.write("\n");

          writer.write("| ");
          VariableElement variableElement = (VariableElement) e.getParameters().get(0);

          DeclaredType declaredType = getDeclaredType(variableElement);
          Element declaredTypeElement = declaredType == null ? null : declaredType.asElement();
          TypeElement typeElement = declaredTypeElement instanceof TypeElement ? (TypeElement) declaredTypeElement : null;
          if (typeElement == null) {
            writer.write(variableElement.asType().toString());
          } else {
            typeWriter.writeDeclaredType(declaredType);
          }
          writer.write("\n");

          writer.write("| ");
          Writer stringWriter = new StringWriter();
          AsciiDocDocTreeWalker docTreeWalker = new AsciiDocDocTreeWalker(environment, options, stringWriter, reporter, environment.getDocTrees().getPath(e));
          DocCommentTree docCommentTree = environment.getDocTrees().getDocCommentTree(e);
          if (docCommentTree == null) {
            reporter.print(Diagnostic.Kind.WARNING, "No doc comment for " + e.getSimpleName());
          } else {
            docTreeWalker.scan();
            // environment.getDocTrees().getDocCommentTree(e).accept(docTreeWalker, null);
          }
          String doc = stringWriter.toString();
          if (doc == null || doc.isBlank()) {
            doc = capturedDocs.get(fieldName);
          }
          if (doc != null) {
            writer.write(doc);
          }
          writer.write("\n");
        }
      } catch (IOException ex) {
        reporter.print(Diagnostic.Kind.ERROR, "Failed to write to file: " + ex.getMessage());
      }
      e.getEnclosedElements().forEach(enclosed -> enclosed.accept(this, null));      
    }
    return null;
  }
  

  public Void visitExecutableAndOutputGetters(ExecutableElement e) {

    if (!e.getModifiers().contains(Modifier.PRIVATE) 
            && (e.getSimpleName().toString().startsWith("get") || e.getSimpleName().toString().startsWith("is"))
            && e.getParameters().isEmpty()
            && !e.getEnclosingElement().getSimpleName().toString().equals("Object")
            ) {
      try {
        String fieldName = JavadocCapturer.getterNameToVariableName(e.getSimpleName().toString());
        if (!this.fields.contains(fieldName)) {
          this.fields.add(fieldName);

          writer.write("\n| [[");
          writer.write(fieldName);
          writer.write("]]");
          writer.write(fieldName);
          writer.write("\n");

          writer.write("| ");
          TypeMirror returnType = e.getReturnType();

          DeclaredType declaredType = returnType.accept(new SimpleTypeVisitor14<DeclaredType, Void>(){
            @Override
            public DeclaredType visitDeclared(DeclaredType t, Void p) {
              return t;
            }

          }, null);
          Element declaredTypeElement = declaredType == null ? null : declaredType.asElement();
          TypeElement typeElement = declaredTypeElement instanceof TypeElement ? (TypeElement) declaredTypeElement : null;
          if (typeElement == null) {
            writer.write(returnType.toString());
          } else {
            typeWriter.writeDeclaredType(declaredType);
          }
          writer.write("\n");

          writer.write("| ");
          Writer stringWriter = new StringWriter();
          AsciiDocDocTreeWalker docTreeWalker = new AsciiDocDocTreeWalker(environment, options, stringWriter, reporter, environment.getDocTrees().getPath(e));
          DocCommentTree docCommentTree = environment.getDocTrees().getDocCommentTree(e);
          if (docCommentTree == null) {
            reporter.print(Diagnostic.Kind.WARNING, "No doc comment for " + e.getSimpleName());
          } else {
            docTreeWalker.scan();
            // environment.getDocTrees().getDocCommentTree(e).accept(docTreeWalker, null);
          }
          String doc = stringWriter.toString();
          if (doc == null || doc.isBlank()) {
            doc = capturedDocs.get(fieldName);
          }
          if (doc != null) {
            writer.write(doc);
          }
          writer.write("\n");
        }
      } catch (IOException ex) {
        reporter.print(Diagnostic.Kind.ERROR, "Failed to write to file: " + ex.getMessage());
      }
      e.getEnclosedElements().forEach(enclosed -> enclosed.accept(this, null));      
    }
    return null;
  }  
  @Override
  public Void visitTypeParameter(TypeParameterElement e, Boolean p) {
    return visitStandard(e, p);
  }

  @Override
  public Void visitUnknown(Element e, Boolean p) {
    return visitStandard(e, p);
  }
  
  private <T extends Element> Void visitStandard(T e, Boolean p) {
    e.getEnclosedElements().forEach(enclosed -> enclosed.accept(this, null));
    return null;
  }
  
}
