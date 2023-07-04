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
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.util.SimpleDocTreeVisitor;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author njt
 */
public class AsciiDocDocTreeWalker extends SimpleDocTreeVisitor<Void, Void> {
  
  private static final Logger logger = LoggerFactory.getLogger(AsciiDocDocTreeWalker.class);
  
  private final AsciiDocOptions options;
  private final Writer writer;
  private final Reporter reporter;
  
  private Queue<Boolean> listOrderedStack = new ArrayDeque<>();
  
  public AsciiDocDocTreeWalker(AsciiDocOptions options, Writer writer, Reporter reporter) {
    this.options = options;
    this.writer = writer;
    this.reporter = reporter;
  }

  @Override
  public Void visitDocComment(DocCommentTree node, Void p) {
    logger.debug("visitDocComment({}, {})", node, p);
    for (DocTree tree : node.getFullBody()) {
      tree.accept(this, null);
    }
    return null;
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
    return null;
  }

  @Override
  public Void visitLink(LinkTree node, Void p) {
    logger.debug("visitLink({}, {})", node, p);
    Reference ref = Reference.parse(node.getReference());
    write(options.getLinkMaps().getLinkForReference(ref));
    return null;
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
    return null;
  }

  @Override
  public Void visitText(TextTree node, Void p) {
    logger.debug("visitText({}, {})", node, p);
//    if (node instanceof HTMLAnchorElement) {
//      HTMLAnchorElement ae = (HTMLAnchorElement) node;
//      write("link:");
//      write(ae.getHref());
//      if (node.getBody() != null && !node.getBody().isBlank()) {
//        write("[");
//        write(node.getBody());
//        write("] ");
//      }
//    }
    write(node.getBody().trim());
    write("\n");
    return null;
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
