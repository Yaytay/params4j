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

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor14;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author njt
 */
public class TypeWriter extends SimpleTypeVisitor14<Void, TypeMirror> {
  
  private static final Logger logger = LoggerFactory.getLogger(TypeWriter.class);
  
  private final Writer writer;
  private final Reporter reporter;
  private final Set<String> includedClasses;
  private final AsciiDocLinkMaps linkMaps;
  
  // inlink preventes recursive calls from outputting links
  private boolean inlink;

  public static void write(Writer writer, Reporter reporter, Set<String> includedClasses, AsciiDocLinkMaps linkMaps, TypeElement typeElement, ExecutableElement methodElement) {
    
    
    String primitiveName = typeElement.asType().accept(new SimpleTypeVisitor14<String, Void>() {
      @Override
      public String visitPrimitive(PrimitiveType t, Void p) {
        return t.toString();
      }
    }, null);

    if (primitiveName != null) {
      logger.debug("Primitive: {}", primitiveName);
      return ;
    }
    
    String packageName =  getPackage(typeElement);    
    String baseClassName = getBaseClassName(typeElement);
    String fullClassName = getClassName(typeElement);
    String typeParameters = getTypeParameters(typeElement);

    
    String methodName = null;
    String methodParameters = null;
    if (methodElement != null) {
      ExecutableType methodType = methodElement.asType().accept(new SimpleTypeVisitor14<ExecutableType, Void>(){
        @Override
        public ExecutableType visitExecutable(ExecutableType t, Void p) {
          return t;
        }

        @Override
        protected ExecutableType defaultAction(TypeMirror t, Void p) {
          logger.debug("Got a {}: {}", t.getClass(), t);
          return super.defaultAction(t, p); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
        }
        
      }, null);
      methodName = methodElement.getSimpleName().toString();
    }
    
    //            new TypeWriter(writer, reporter, includedClasses, linkMaps), method);
  }
    
  public TypeWriter(Writer writer, Reporter reporter, Set<String> includedClasses, AsciiDocLinkMaps linkMaps) {
    super();
    this.writer = writer;
    this.reporter = reporter;
    this.includedClasses = includedClasses;
    this.linkMaps = linkMaps;
  }

//  @Override
//  public Void visitIntersection(IntersectionType t, Void p) {
//    write("visitIntersection(" + t.toString() + ")");
//    return super.visitIntersection(t, p);
//  }
//
//  @Override
//  public Void visitUnion(UnionType t, Void p) {
//    write("visitUnion(" + t.toString() + ")");
//    return super.visitUnion(t, p); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
//  }
//
//  @Override
//  public Void visitNoType(NoType t, Void p) {
//    write("visitNoType(" + t.toString() + ")");
//    return super.visitNoType(t, p); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
//  }
//
  @Override
  public Void visitExecutable(ExecutableType t, TypeMirror method) {
    logger.debug("visitExecutable({})", t);
    logger.debug("Receiver type: {}", t.getReceiverType());
    write("(");
    boolean first = true;
    for (TypeMirror param : t.getParameterTypes()) {
      if (!first) {
        write(", ");
      }
      first = false;
      String paramString = param.toString();
      if (inlink) {
        paramString = paramString.replaceAll("\\]", "\\\\]");
      }
      write(paramString);
    } 
    write(")");
    return super.visitExecutable(t, method); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
  }
//
//  @Override
//  public Void visitWildcard(WildcardType t, Void p) {
//    write("visitWildcard(" + t.toString() + ")");
//    return super.visitWildcard(t, p); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
//  }
//
//  @Override
//  public Void visitTypeVariable(TypeVariable t, Void p) {
//    write("visitTypeVariable(" + t.toString() + ")");
//    return super.visitTypeVariable(t, p); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
//  }
//
//  @Override
//  public Void visitError(ErrorType t, Void p) {
//    write("visitError(" + t.toString() + ")");
//    return super.visitError(t, p); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
//  }

  private static String getPackage(Element elem) {
    while (elem != null && !(elem instanceof PackageElement)) {
      elem = elem.getEnclosingElement();
    }
    if (elem instanceof PackageElement) {
      return ((PackageElement) elem).getQualifiedName().toString();
    }
    
    return null;
  }
  
  private static String getBaseClassName(Element elem) {
    Element lastElem = elem;
    while (elem instanceof TypeElement) {
      elem = elem.getEnclosingElement();
      if (elem instanceof TypeElement) {
        lastElem = elem;
      }
    }    
    return lastElem.getSimpleName().toString();
  }
  
  private static String getClassName(Element elem) {
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
  
  private static class TypeParametersExtractor extends ElementKindVisitor14<Void, Void> {
    private final StringBuilder builder;
    private boolean first = true;

    public TypeParametersExtractor(StringBuilder builder) {
      this.builder = builder;
    }
    
    public String get() {
      return builder.toString();
    }
    
    @Override
    public Void visitTypeParameter(TypeParameterElement e, Void p) {
      if (!first) {
        builder.append(", ");
      }
      first = false;
      builder.append(e.getSimpleName().toString());
      return super.visitTypeParameter(e, p);
    }

    @Override
    protected Void defaultAction(Element e, Void p) {
      logger.debug("Got a {}: {}", e.getClass(), e);
      return super.defaultAction(e, p); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

  }
  
  private static String getTypeParameters(TypeElement elem) {
    if (elem.getTypeParameters().isEmpty()) {
      return null;
    }
    StringBuilder builder = new StringBuilder();    
    builder.append("<");
    TypeParametersExtractor extractor = new TypeParametersExtractor(builder);
    for (TypeParameterElement tpe : elem.getTypeParameters()) {
      tpe.accept(extractor, null);
    }
    builder.append(">");
    return builder.toString();
  }
  
  
  @Override
  public Void visitDeclared(DeclaredType t, TypeMirror method) {
    logger.debug("visitDeclared({})", t);
    Element elem = t.asElement();
    if (elem instanceof TypeElement) {
      TypeElement te = (TypeElement) elem;
      String url = linkMaps.getLinkForType(getPackage(te), getClassName(te), null);
      
      if (method != null) {
        url = url + '#' + method.toString();
      }
      
      boolean opened = false;
      
      if (!inlink) {
        if (includedClasses.contains(te.getQualifiedName().toString())) {
          write("xref:");
          write(te.getQualifiedName().toString());
          write(".adoc[");
          opened = true;
          inlink = true;
        } else if (url != null) {
          write("link:");
          write(url);
          write("[");
          opened = true;
          inlink = true;
        }
      }
      
      write(te.getSimpleName().toString());
      List<? extends TypeMirror> typeArguments = t.getTypeArguments();
      if (typeArguments != null && !typeArguments.isEmpty()) {
        write("<");
        boolean first = true;
        for (TypeMirror child : t.getTypeArguments()) {
          if (!first) {
            write(", ");
          } 
          first = false;
          child.accept(this, null);
        }
        write(">");
      }
      
      if (method != null) {
        write(".");
        method.accept(this, null);
      }
      
      if (opened) {
        write("]");
        inlink = false;
      }
    }
    return super.visitDeclared(t, method); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
  }

//  @Override
//  public Void visitArray(ArrayType t, Void p) {
//    write("visitArray(" + t.toString() + ")");
//    return super.visitArray(t, p); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
//  }
//
//  @Override
//  public Void visitNull(NullType t, Void p) {
//    write("visitNull(" + t.toString() + ")");
//    return super.visitNull(t, p); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
//  }

  @Override
  public Void visitPrimitive(PrimitiveType t, TypeMirror method) {
    write(t.toString());
    return super.visitPrimitive(t, method); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
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
