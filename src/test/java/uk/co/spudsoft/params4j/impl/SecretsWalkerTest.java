/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author jtalbut
 */
public class SecretsWalkerTest {
  
  private static final Logger logger = LoggerFactory.getLogger(SecretsWalkerTest.class);
  
  @Test
  public void testGather() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    
    Path root = new File(Helpers.getResourcePath("/secrets")).toPath();
    
    
    SecretsWalker visitor = new SecretsWalker(root, mapper, 100, 100, StandardCharsets.UTF_8, null);
    Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 4, visitor);
    ObjectNode result = visitor.getObjectNode();
    
    assertNotNull(result);
    logger.debug("Result: {}", result);
    assertThat(result.has(".hidden"), is(false));
    assertThat(result.get("bad").get("nonexistent").textValue(), equalTo("This field does not exist in the parameters object"));
    assertThat(result.get("child").has(".thisfileishidden"), is(false));
    assertThat(result.get("child").has("toolong"), is(false));
    assertThat(result.get("child").get("username").textValue(), equalTo("user"));
    assertThat(result.get("child").get("password").textValue(), equalTo("pass"));
    assertThat(result.get("this").get("dir").get("structure").has("is"), is(false));
  }
  
}
