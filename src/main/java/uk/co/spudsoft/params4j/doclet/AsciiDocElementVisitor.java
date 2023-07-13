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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import uk.co.spudsoft.params4j.impl.JavadocCapturer;

/**
 *
 * @author njt
 */
public class AsciiDocElementVisitor implements ElementVisitor<Void, Void> {
  
  private final DocletEnvironment environment;
  private final AsciiDocOptions options;
  private final Reporter reporter;

  private Writer writer;
  private TypeWriter typeWriter;
  
  public AsciiDocElementVisitor(DocletEnvironment environment, AsciiDocOptions options, Reporter reporter) {
    this.environment = environment;
    this.options = options;
    this.reporter = reporter;
  }
  
  @Override
  public Void visit(Element e, Void p) {
    return null;
  }

  @Override
  public Void visitPackage(PackageElement e, Void p) {
    reporter.print(Diagnostic.Kind.NOTE, "Visiting Package " + e.getQualifiedName());
    e.getEnclosedElements().forEach(enclosed -> enclosed.accept(this, null));
    return null;
  }
  
  private boolean include(TypeElement e) {
    if (options.getIncludeClasses() == null || options.getIncludeClasses().isEmpty()) {
      return true;
    }
    return options.getIncludeClasses().contains(e.getQualifiedName().toString());
  }

  @Override
  public Void visitType(TypeElement e, Void p) {
    reporter.print(Diagnostic.Kind.NOTE, "Visiting Type " + e.getQualifiedName());
    
    if (include(e)) {  
      File dir = new File(options.getDestDirName());
      if (!dir.exists()) {
        if (!dir.mkdirs()) {
          reporter.print(Diagnostic.Kind.ERROR, "Failed to create directory " + dir.getAbsolutePath());
          return null;
        }
      }
      File output = new File(dir, e.getQualifiedName() + ".adoc");
      reporter.print(Diagnostic.Kind.NOTE, "Writing file " + output.getAbsolutePath());
      try (Writer newWriter = new FileWriter(output)) {
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

          e.getEnclosedElements().forEach(enclosed -> enclosed.accept(this, null));

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

  @Override
  public Void visitVariable(VariableElement e, Void p) {
    e.getEnclosedElements().forEach(enclosed -> enclosed.accept(this, null));
    return null;
  }

  @Override
  public Void visitExecutable(ExecutableElement e, Void p) {
    if (e.getSimpleName().toString().startsWith("set") && e.getParameters().size() == 1) {
      try {
        writer.write("| ");
        writer.write(JavadocCapturer.setterNameToVariableName(e.getSimpleName().toString()));
        writer.write("\n");
        
        writer.write("| ");
        VariableElement variableElement = (VariableElement) e.getParameters().get(0);
                
        DeclaredType declaredType = variableElement.asType().accept(new SimpleTypeVisitor14<DeclaredType, Void>(){
          @Override
          public DeclaredType visitDeclared(DeclaredType t, Void p) {
            return t;
          }
          
        }, null);
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
        writer.write("\n");
      } catch (IOException ex) {
        reporter.print(Diagnostic.Kind.ERROR, "Failed to write to file: " + ex.getMessage());
      }
      
      e.getEnclosedElements().forEach(enclosed -> enclosed.accept(this, null));      
    }
    return null;
  }
  
  @Override
  public Void visitTypeParameter(TypeParameterElement e, Void p) {
    e.getEnclosedElements().forEach(enclosed -> enclosed.accept(this, null));
    return null;
  }

  @Override
  public Void visitUnknown(Element e, Void p) {
    e.getEnclosedElements().forEach(enclosed -> enclosed.accept(this, null));
    return null;
  }
  
}
