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
import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 *
 * @author njt
 */
public class AsciiDocDocTreeWalker extends DocTreePathScanner<Void, Void> {
    
  private final DocletEnvironment environment;  
  private final AsciiDocOptions options;
  private final Writer writer;
  private final Reporter reporter;
  private final TreePath path;
  private DocCommentTree dc;
  private final TypeWriter typeWriter;
  
  private final Queue<Boolean> listOrderedStack = new ArrayDeque<>();
  
  private boolean inMacro;
  
  public AsciiDocDocTreeWalker(DocletEnvironment environment, AsciiDocOptions options, Writer writer, Reporter reporter, TreePath path) {
    this.environment = environment;
    this.options = options;
    this.writer = writer;
    this.reporter = reporter;
    this.path = path;
    this.typeWriter = new TypeWriter(writer, reporter, options.getIncludeClasses(), options.getLinkMaps());
  }
  
  public void scan() {
    dc = environment.getDocTrees().getDocCommentTree(path);
    scan(new DocTreePath(path, dc), null);    
  }

  @Override
  public Void visitEndElement(EndElementTree node, Void p) {
    String name = node.getName().toString();
    if (name.equalsIgnoreCase("ul")) {
      listOrderedStack.poll();
      write("\n");
    } else if (name.equalsIgnoreCase("ol")) {
      listOrderedStack.poll();
      write("\n");
    } else if (name.equalsIgnoreCase("a")) {
      inMacro = false;
      write("] ");
    } else {
      write(node.toString());
      write(" ");
    }
    return super.visitEndElement(node, p);
  }
  
  @Override
  public Void visitLink(LinkTree node, Void p) {
    scan(node.getReference(), null);

    ReferenceTree refTree = node.getReference();
    typeWriter.writeReferenceTree(environment, this.getCurrentPath(), refTree);
    return super.visitLink(node, p);
  }

  @Override
  public Void visitStartElement(StartElementTree node, Void p) {
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
      inMacro = true;
      return null;
    } else {
      write(node.toString());
    }
    return super.visitStartElement(node, p);
  }

  @Override
  public Void visitText(TextTree node, Void p) {
    write(node.getBody().trim());
    if (!inMacro) {
      write("\n");
    }
    return super.visitText(node, p);
  }

  @Override
  public Void visitSee(SeeTree node, Void p) {
    write("\n\nSee: ");
    return super.visitSee(node, p); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
  }

  @Override
  public Void visitParam(ParamTree node, Void p) {
    return null;
  }

  @Override
  public Void visitReturn(ReturnTree node, Void p) {
    return null;
  }

  @Override
  public Void visitAuthor(AuthorTree node, Void p) {
    return null;
  }

  private void write(String s) {
    if (s != null) {
      s = s.replaceAll("\n ", " ");

      try {
        writer.write(s);
      } catch (IOException ex) {
        reporter.print(Diagnostic.Kind.ERROR, new DocTreePath(path, dc), "Failed to write: " + ex.getMessage());
      }
    }
  }  
 
  
}
