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
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.LinkTree;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    Element element = environment.getDocTrees().getElement(new DocTreePath(this.getCurrentPath(), node.getReference()));
    TypeMirror type = environment.getDocTrees().getType(new DocTreePath(this.getCurrentPath(), node.getReference()));
    logger.debug("element: {}", element);
    logger.debug("type: {}", type);
    if (element instanceof ExecutableElement) {
      TypeElement typeElement = (TypeElement) element.getEnclosingElement();
      ExecutableElement methodElement = (ExecutableElement) element;
      TypeWriter.write(writer, reporter, options.getIncludeClasses(), options.getLinkMaps(), typeElement, methodElement);
    } else if (element instanceof TypeElement) {
      TypeElement typeElement = (TypeElement) element;
      TypeWriter.write(writer, reporter, options.getIncludeClasses(), options.getLinkMaps(), typeElement, null);
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
    } else if (name.equalsIgnoreCase("a")) {
      write("link:");
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
    s = s.replaceAll("\n ", "\n");
    
    try {
      writer.write(s);
    } catch (IOException ex) {
      reporter.print(Diagnostic.Kind.ERROR, "Failed to write: " + ex.getMessage());
    }
  }
  
  
}
