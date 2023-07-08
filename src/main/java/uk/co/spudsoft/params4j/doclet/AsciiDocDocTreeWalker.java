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

import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author njt
 */
public class AsciiDocDocTreeWalker extends DocTreePathScanner<Void, Void> {
  
  private static final Logger logger = LoggerFactory.getLogger(AsciiDocDocTreeWalker.class);
  
  private final DocletEnvironment environment;  
  private final AsciiDocOptions options;
  private final Writer writer;
  private final Reporter reporter;
  private final TreePath path;
  private DocCommentTree dc;
  
  private final Queue<Boolean> listOrderedStack = new ArrayDeque<>();
  
  public AsciiDocDocTreeWalker(DocletEnvironment environment, AsciiDocOptions options, Writer writer, Reporter reporter, TreePath path) {
    this.environment = environment;
    this.options = options;
    this.writer = writer;
    this.reporter = reporter;
    this.path = path;
  }
  
  public void scan() {
    dc = environment.getDocTrees().getDocCommentTree(path);
    scan(new DocTreePath(path, dc), null);    
  }

  @Override
  public Void visitDocComment(DocCommentTree node, Void p) {
    logger.debug("visitDocComment({}, {})", node, p);
    return super.visitDocComment(node, p);
  }

  @Override
  public Void visitEndElement(EndElementTree node, Void p) {
    logger.debug("visitEndElement({}, {})", node, p);
    String name = node.getName().toString();
    if (name.equalsIgnoreCase("ul")) {
      if (!listOrderedStack.peek()) {
        listOrderedStack.remove();
      }
      write("\n");
    } else if (name.equalsIgnoreCase("ol")) {
      if (listOrderedStack.peek()) {
        listOrderedStack.remove();
      }
      write("\n");
    } else if (name.equalsIgnoreCase("a")) {
      write("] ");
    } else {
      write(node.toString());
      write(" ");
    }
    return super.visitEndElement(node, p);
  }
  
  @Override
  public Void visitLink(LinkTree node, Void p) {
    logger.debug("visitLink({}, {})", node, p);
    scan(node.getReference(), null);
    logger.debug("path: {} ({})", this.getCurrentPath());

    ReferenceTree refTree = node.getReference();
    Element element = environment.getDocTrees().getElement(new DocTreePath(this.getCurrentPath(), refTree));
    TypeMirror type = environment.getDocTrees().getType(new DocTreePath(this.getCurrentPath(), refTree));
    logger.debug("element: {}", element);
    logger.debug("type: {}", type);
    if (element instanceof ExecutableElement) {      
      TypeElement typeElement = (TypeElement) element.getEnclosingElement();
      ExecutableElement methodElement = (ExecutableElement) element;
      
      String url = options.getLinkMaps().getUrlForType(TypeWriter.getPackage(typeElement), TypeWriter.getClassName(typeElement));
      if (url == null) {
        if (options.getIncludeClasses().contains(typeElement.getQualifiedName().toString())) {
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
        write(options.getLinkMaps().getUrlForType(TypeWriter.getPackage(typeElement), TypeWriter.getClassName(typeElement)));
        write(TypeWriter.getMethodAnchor(methodElement));
        write("[");
        // For method links javadoc removes leading packages and type parameters        
        write(TypeWriter.simplifySignature(refTree.getSignature()));
        write("] ");
      }
    } else if (element instanceof TypeElement) {
      TypeElement typeElement = (TypeElement) element;
      
      String url = options.getLinkMaps().getUrlForType(TypeWriter.getPackage(typeElement), TypeWriter.getClassName(typeElement));
      if (url == null) {
        if (options.getIncludeClasses().contains(typeElement.getQualifiedName().toString())) {
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
        write(options.getLinkMaps().getUrlForType(TypeWriter.getPackage(element), TypeWriter.getClassName(element)));
        write("[");
        write(refTree.getSignature());
        write("] ");
      }
    }
    return super.visitLink(node, p);
  }

  @Override
  public Void visitStartElement(StartElementTree node, Void p) {
    logger.debug("visitStartElement({}, {})", node, p);
    String name = node.getName().toString();
    if (name.equalsIgnoreCase("ul")) {
      listOrderedStack.add(Boolean.FALSE);
    } else if (name.equalsIgnoreCase("ol")) {
      listOrderedStack.add(Boolean.TRUE);      
    } else if (name.equalsIgnoreCase("li")) {
      if (!listOrderedStack.isEmpty() && listOrderedStack.peek()) {
        write(". ");        
      } else {
        write("* ");
      }
    } else if (name.equalsIgnoreCase("p")) {
      write("\n\n");
    } else if (name.equalsIgnoreCase("a")) {
      write("link:");
      for (DocTree attribute : node.getAttributes()) {
        if (attribute instanceof AttributeTree) {
          AttributeTree attributeTree = (AttributeTree) attribute;
          if (attributeTree.getName().toString().equalsIgnoreCase("href")) {
            write(attributeTree.getValue().toString());
            break;
          }
        }
      }
      write("[");
      return null;
    } else {
      write(node.toString());
    }
    return super.visitStartElement(node, p);
  }

  @Override
  public Void visitText(TextTree node, Void p) {
    logger.debug("visitText({}, {})", node, p);
    write(node.getBody().trim());
    write("\n");
    return super.visitText(node, p);
  }

  private void write(String s) {
    if (s != null) {
      s = s.replaceAll("\n ", " ");

      try {
        writer.write(s);
      } catch (IOException ex) {
        reporter.print(Diagnostic.Kind.ERROR, "Failed to write: " + ex.getMessage());
      }
    }
  }
  
  
}
