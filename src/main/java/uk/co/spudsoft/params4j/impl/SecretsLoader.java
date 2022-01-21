/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jtalbut
 */
public class SecretsLoader {
  
  private static final Logger logger = LoggerFactory.getLogger(SecretsLoader.class);
  
  public static ObjectNode gather(ObjectMapper mapper, Path root, int fileSizeLimit, int fileCountLimit, int fileDepthLimit, Charset charset, Consumer<Path> dirHandler) throws IOException {
    SecretsWalker visitor = new SecretsWalker(root, mapper, fileSizeLimit, fileCountLimit, charset, dirHandler);
    Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), fileDepthLimit, visitor);
    return visitor.getObjectNode();
  }
  
  private static class SecretsWalker  extends SimpleFileVisitor<Path> {
    
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

    SecretsWalker(Path root, ObjectMapper objectMapper, int fileSizeLimit, int fileCountLimit, Charset charset, Consumer<Path> dirHandler) {
      this.root = root;
      this.objectMapper = objectMapper;
      this.objectNode = objectMapper.createObjectNode();
      this.current = this.objectNode;
      this.fileSizeLimit = fileSizeLimit;
      this.fileCountLimit = fileCountLimit;
      this.charset = charset;
      this.dirHandler = dirHandler;
    }

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
          } catch(Throwable ex) {
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
  
}
