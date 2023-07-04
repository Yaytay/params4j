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
import com.sun.source.doctree.CommentTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocRootTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.doctree.DocTreeVisitor;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.ErroneousTree;
import com.sun.source.doctree.IdentifierTree;
import com.sun.source.doctree.InheritDocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SerialDataTree;
import com.sun.source.doctree.SerialFieldTree;
import com.sun.source.doctree.SerialTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UnknownInlineTagTree;
import com.sun.source.doctree.ValueTree;
import com.sun.source.doctree.VersionTree;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.html.HTMLAnchorElement;

/**
 *
 * @author njt
 */
public class AsciiDocDocTreeWalker implements DocTreeVisitor<Void, Void> {
  
  private static final Logger logger = LoggerFactory.getLogger(AsciiDocDocTreeWalker.class);
  
  private final AsciiDocOptions options;
  private final Writer writer;
  private final Reporter reporter;
  
  private Queue<Character> listTypes = new ArrayDeque<>();
  
  public AsciiDocDocTreeWalker(AsciiDocOptions options, Writer writer, Reporter reporter) {
    this.options = options;
    this.writer = writer;
    this.reporter = reporter;
  }

  @Override
  public Void visitAttribute(AttributeTree node, Void p) {
    logger.debug("visitAttribute({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitAuthor(AuthorTree node, Void p) {
    logger.debug("visitAuthor({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitComment(CommentTree node, Void p) {
    logger.debug("visitComment({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitDeprecated(DeprecatedTree node, Void p) {
    logger.debug("visitDeprecated({}, {})", node, p);
    return null;
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
  public Void visitDocRoot(DocRootTree node, Void p) {
    logger.debug("visitDocRoot({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitEndElement(EndElementTree node, Void p) {
    logger.debug("visitEndElement({}, {})", node, p);
    String name = node.getName().toString();
    if (name.equalsIgnoreCase("ul")) {
      if (listTypes.peek() == 'U') {
        listTypes.remove();
      }
      write("\n");
    } else if (name.equalsIgnoreCase("ol")) {
      if (listTypes.peek() == 'O') {
        listTypes.remove();
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
  public Void visitEntity(EntityTree node, Void p) {
    logger.debug("visitEntity({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitErroneous(ErroneousTree node, Void p) {
    logger.debug("visitErroneous({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitIdentifier(IdentifierTree node, Void p) {
    logger.debug("visitIdentifier({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitInheritDoc(InheritDocTree node, Void p) {
    logger.debug("visitInheritDoc({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitLink(LinkTree node, Void p) {
    logger.debug("visitLink({}, {})", node, p);
    ReferenceTree refTree = node.getReference();
    if (Kind.REFERENCE == refTree.getKind()) {
      Reference ref = Reference.parse(node.getReference());
      write(options.getLinkMaps().getLinkForReference(ref));
    } else {
      logger.debug("visitLink({})", node.getLabel());
      logger.debug("visitLink({})", node.getReference().getKind());
      logger.debug("visitLink({})", node.getReference().getSignature());
    }
    return null;
  }

  @Override
  public Void visitLiteral(LiteralTree node, Void p) {
    logger.debug("visitLiteral({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitParam(ParamTree node, Void p) {
    logger.debug("visitParam({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitReference(ReferenceTree node, Void p) {
    logger.debug("visitReference({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitReturn(ReturnTree node, Void p) {
    logger.debug("visitReturn({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitSee(SeeTree node, Void p) {
    logger.debug("visitSee({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitSerial(SerialTree node, Void p) {
    logger.debug("visitSerial({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitSerialData(SerialDataTree node, Void p) {
    logger.debug("visitSerialData({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitSerialField(SerialFieldTree node, Void p) {
    logger.debug("visitSerialField({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitSince(SinceTree node, Void p) {
    logger.debug("visitSince({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitStartElement(StartElementTree node, Void p) {
    logger.debug("visitStartElement({}, {})", node, p);
    String name = node.getName().toString();
    if (name.equalsIgnoreCase("ul")) {
      listTypes.add('U');
    } else if (name.equalsIgnoreCase("ol")) {
      listTypes.add('O');      
    } else if (name.equalsIgnoreCase("li")) {
      if (listTypes.isEmpty() || listTypes.peek() == 'U') {
        write("* ");
      } else {
        write(". ");        
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
    if (node instanceof HTMLAnchorElement) {
      HTMLAnchorElement ae = (HTMLAnchorElement) node;
      write(ae.getHref());
      if (node.getBody() != null && !node.getBody().isBlank()) {
        write("{");
        write(node.getBody());
        write("] ");
      }
    }
    write(node.getBody().trim());
    write("\n");
    return null;
  }

  @Override
  public Void visitThrows(ThrowsTree node, Void p) {
    logger.debug("visitThrows({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitUnknownBlockTag(UnknownBlockTagTree node, Void p) {
    logger.debug("visitUnknownBlockTag({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitUnknownInlineTag(UnknownInlineTagTree node, Void p) {
    logger.debug("visitUnknownInlineTag({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitValue(ValueTree node, Void p) {
    logger.debug("visitValue({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitVersion(VersionTree node, Void p) {
    logger.debug("visitVersion({}, {})", node, p);
    return null;
  }

  @Override
  public Void visitOther(DocTree node, Void p) {
    logger.debug("visitOther({}, {})", node, p);
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
