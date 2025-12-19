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
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.TreePath;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 *
 * @author njt
 */
public class AsciiDocDocTreeWalker extends DocTreePathScanner<Void, Void> {
    
  private enum TableState {
    table
    , caption
    , header
    , headerRow
    , headerCell
    , body
    , bodyRow
    , bodyCell
  }
  
  private final DocletEnvironment environment;  
  private final Writer writer;
  private final Reporter reporter;
  private final TreePath path;
  private DocCommentTree dc;
  private final TypeWriter typeWriter;
  
  private final Queue<Boolean> listOrderedStack = new ArrayDeque<>();
  
  private boolean inPara;
  private boolean inSource;
  
  private TableState tableState;
  
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "The writer should be considered dedicated to this purpose whilst this walker is in operation.")  
  public AsciiDocDocTreeWalker(DocletEnvironment environment, AsciiDocOptions options, Writer writer, Reporter reporter, TreePath path) {
    this.environment = environment;
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
    } else if (name.equalsIgnoreCase("em")) {
      write("_");
    } else if (name.equalsIgnoreCase("a")) {
      write("] ");
    } else if (name.equalsIgnoreCase("table") && tableState == TableState.table) {
      tableState = null;
      write("\n!===\n");
    } else if (name.equalsIgnoreCase("caption") && tableState == TableState.caption) {
      tableState = TableState.table;
    } else if (name.equalsIgnoreCase("thead") && tableState == TableState.header) {
      tableState = TableState.table;
      write("\n");
    } else if (name.equalsIgnoreCase("tr") && tableState == TableState.headerRow) {
      tableState = TableState.header;
      write("\n");
    } else if (name.equalsIgnoreCase("th") && tableState == TableState.headerCell) {
      tableState = TableState.headerRow;
      write(" ");
    } else if (name.equalsIgnoreCase("tbody") && tableState == TableState.body) {
      tableState = TableState.table;
    } else if (name.equalsIgnoreCase("tr") && tableState == TableState.bodyRow) {
      tableState = TableState.body;
      write("\n");
    } else if (name.equalsIgnoreCase("td") && tableState == TableState.bodyCell) {
      write("\n");
      tableState = TableState.bodyRow;
    } else if (name.equalsIgnoreCase("pre") || name.equalsIgnoreCase("code")) {
      if (inSource) {
        inSource = false;
        write("\n----\n");
      }
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
    } else if (name.equalsIgnoreCase("table")) {
      tableState = TableState.table;
      write("\n\n!===\n");
    } else if (name.equalsIgnoreCase("caption") && tableState == TableState.table) {
      tableState = TableState.caption;
    } else if (name.equalsIgnoreCase("thead") && tableState == TableState.table) {
      tableState = TableState.header;
    } else if (name.equalsIgnoreCase("tr") && tableState == TableState.header) {
      tableState = TableState.headerRow;
    } else if (name.equalsIgnoreCase("th") && tableState == TableState.headerRow) {
      tableState = TableState.headerCell;
      write("! ");
    } else if (name.equalsIgnoreCase("tbody") && tableState == TableState.table) {
      tableState = TableState.body;
    } else if (name.equalsIgnoreCase("tr") && tableState == TableState.body) {
      tableState = TableState.bodyRow;
    } else if (name.equalsIgnoreCase("td") && tableState == TableState.bodyRow) {
      tableState = TableState.bodyCell;
      write("! ");
    } else if (name.equalsIgnoreCase("p")) {
      write("\n\n");
    } else if (name.equalsIgnoreCase("em")) {
      write("_");
    } else if (name.equalsIgnoreCase("a")) {
      write("link:");
      String attributeValue = findAttribute(node, "href");
      if (attributeValue != null) {
        write(attributeValue);
      }
      write("[");
      return null;
    } else if (name.equalsIgnoreCase("pre") || name.equalsIgnoreCase("code")) {
      if (!inSource) {
        inSource = true;
        write("\n[source");
        String attributeValue = findAttribute(node, "data-lang");
        if (attributeValue != null) {
          write(",");
          write(attributeValue);
        }
        write("]\n");
        write("----\n");
        return null;
      }      
    } else {
      reporter.print(Diagnostic.Kind.WARNING, new DocTreePath(path, dc), "Unrecognised HTML tag (" + name + ").");
      write(node.toString());
    }
    return super.visitStartElement(node, p);
  }

  @Override
  public Void visitAttribute(AttributeTree node, Void p) {
    if (tableState == null) {
      return super.visitAttribute(node, p);
    }
    return null;
  }
  
  String findAttribute(StartElementTree node, String requiredAttribute) {
    for (DocTree attribute : node.getAttributes()) {
      if (attribute instanceof AttributeTree) {
        AttributeTree attributeTree = (AttributeTree) attribute;
        if (attributeTree.getName().toString().equalsIgnoreCase(requiredAttribute)) {
          return attributeTree.getValue().toString();
        }
      }
    }
    return null;
  }

  @Override
  public Void visitText(TextTree node, Void p) {
    if ((tableState != TableState.table) && (tableState != TableState.caption)) {
      String text = node.getBody();
      if (!inPara || tableState != null) {
        text = text.stripLeading();
      }

      write(text);
    }
    return super.visitText(node, p);
  }

  @Override
  public Void visitLiteral(LiteralTree node, Void p) {
    write("`+");
    super.visitLiteral(node, p);
    write("+`");
    return null;
  }

  private static final Pattern NUMERIC_ENTITY = Pattern.compile("&#([0-9]{1,6});");
  private static final Pattern TEXT_ENTITY = Pattern.compile("&([a-zA-Z]{1,6});");
  
  @Override
  public Void visitEntity(EntityTree node, Void p) {
    String entityRef = node.toString();
    
    try {
      Matcher matcher = NUMERIC_ENTITY.matcher(entityRef);
      if (matcher.matches()) {
        String ref = matcher.group(1);
        int code = Integer.parseInt(ref);
        write(Character.toString(code));
        return null;
      }
      matcher = TEXT_ENTITY.matcher(entityRef);
      if (matcher.matches()) {
        String ref = matcher.group(1).toLowerCase();        
        String character = HtmlEntities.lookupEntity(ref);
        if (character != null) {
          write(character);
          return null;
        }
      }
    } catch (Throwable ex) {
      reporter.print(Diagnostic.Kind.ERROR, new DocTreePath(path, dc), "Failed to process entity: " + node.toString());
    }
    
    write(node.toString());
    return null;
  }
  
  @Override
  public Void visitSee(SeeTree node, Void p) {
    write("\n\nSee: ");
    return super.visitSee(node, p);
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
      inPara = !s.endsWith("\n");
      try {
        writer.write(s);
      } catch (IOException ex) {
        reporter.print(Diagnostic.Kind.ERROR, new DocTreePath(path, dc), "Failed to write: " + ex.getMessage());
      }
    }
  }  
 
  
}
