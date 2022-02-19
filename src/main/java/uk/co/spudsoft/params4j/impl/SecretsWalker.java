/*
 * Copyright (C) 2022 jtalbut
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
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileVisitor used by SecretsGatherer to construct a JSON ObjectNode containing files from the root directory.
 * @author jtalbut
 */
public class SecretsWalker extends SimpleFileVisitor<Path> {

  private static final Logger logger = LoggerFactory.getLogger(SecretsWalker.class);

  private final Path root;
  private final ObjectMapper objectMapper;
  private final long fileSizeLimit;
  private final int fileCountLimit;
  private final Charset charset;
  private final Consumer<Path> dirHandler;

  private final ObjectNode objectNode;
  private final Map<Path, ObjectNode> nodes = new HashMap<>();
  private ObjectNode current;
  private int fileCount;

  /**
   * Constructor.
   * @param root The base path from which to start searching, typically something like "/etc/[service name]/conf.d"
   * @param objectMapper The ObjectMapper to use to build the resulting ObjectNode.
   * @param fileSizeLimit The maximum size of file to attempt to load.
   * The recommendation is to set this to slightly larger than your standard secret length.
   * @param fileCountLimit The maximum number of files to attempt to load.
   * The recommendation is to set this to one more than the number of secrets in your parameters hierarchy.
   * @param charset The charset to use when reading the file.
   * @param dirHandler Optional consumer called for each directory that is entered.
   */
  public SecretsWalker(Path root, ObjectMapper objectMapper, int fileSizeLimit, int fileCountLimit, Charset charset, Consumer<Path> dirHandler) {
    this.root = root;
    this.objectMapper = objectMapper;
    this.objectNode = objectMapper.createObjectNode();
    this.current = this.objectNode;
    this.fileSizeLimit = fileSizeLimit;
    this.fileCountLimit = fileCountLimit;
    this.charset = charset;
    this.dirHandler = dirHandler;
  }

  /**
   * Get the ObjectNode that reflects the directory/file structure underneath the root path.
   * @return the ObjectNode constructed by walking the file tree.
   */
  public ObjectNode getObjectNode() {
    return objectNode;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    logger.trace("Leaving dir: {}", dir);
    current = nodes.get(dir);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    logger.trace("File: {}", file);
    if (attrs.isDirectory()) {
      logger.trace("Ignoring file {} because it is a directory (can be caused by max depth being exceeded)", file);
    } else {
      String filename = file.getFileName().toString();
      if (filename.startsWith(".")) {
        logger.trace("Ignoring file {} because it is hidden", file);
      } else if (attrs.size() > fileSizeLimit) {
        logger.trace("Ignoring file {} because it is larger than the maximum size ({} > {})", file, attrs.size(), fileSizeLimit);
      } else if (++fileCount > fileCountLimit) {
        logger.trace("Ignoring file {} because too many files have been processed ({} > {})", file, fileCount, fileCountLimit);
      } else {
        try (FileInputStream fis = new FileInputStream(file.toFile())) {
          String value = new String(fis.readAllBytes(), charset);
          current.put(filename, value);
        } catch (Throwable ex) {
          logger.trace("Ignoring file {} because it could not be read: ", file, ex);
        }
      }
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    String dirname = dir.getFileName().toString();
    if (dir.equals(root)) {
      return FileVisitResult.CONTINUE;
    } else if (dirname.startsWith(".") || dir.equals(root)) {
      logger.trace("Ignoring dir {} because it is hidden", dir);
      return FileVisitResult.SKIP_SUBTREE;
    } else {
      logger.trace("Entering dir: {}", dir);
      if (dirHandler != null) {
        dirHandler.accept(dir);
      }
      ObjectNode dirNode = objectMapper.createObjectNode();
      current.set(dirname, dirNode);
      nodes.put(dir, current);
      current = dirNode;
      return FileVisitResult.CONTINUE;
    }
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    logger.trace("Failed to visit: {}", file);
    return FileVisitResult.CONTINUE;
  }
}
