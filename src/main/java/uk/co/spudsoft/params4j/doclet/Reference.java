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

import com.sun.source.doctree.ReferenceTree;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.type.TypeMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author njt
 */
public class Reference {
  
  private static final Logger logger = LoggerFactory.getLogger(Reference.class);
  
  private final String classFQName;
  private final String packageName;
  private final String className;
  private final String signature;
  
  private final String methodSignature;
  private final String simpleMethodSignature;

  private Reference(String classFQName, String packageName, String simpleName, String signature, String methodSignature, String simpleMethodSignature) {
    this.classFQName = classFQName;
    this.packageName = packageName;
    this.className = simpleName;
    this.signature = signature;
    this.methodSignature = methodSignature;
    this.simpleMethodSignature = simpleMethodSignature;
  }
  
  public static Reference parse(ReferenceTree refTree) {
    String signature = refTree.getSignature();
    return parse(signature);
  }
    
  public static Reference parse(TypeMirror typeMirror) {
    String signature = typeMirror.toString();
    return parse(signature);
  }
  
  private static Reference parse(String signature) {
    if (signature.contains("<")) {
      logger.debug("Signature: {}", signature);
    }
    
    int idx = signature.indexOf("#");
    String classFQName = idx < 0 ? signature : signature.substring(0, idx);
    String methodSignature = idx > 0 ? signature.substring(idx + 1) : null;
    
    idx = classFQName.lastIndexOf(".");
    String packageName = idx < 0 ? "" : classFQName.substring(0, idx);
    String className = idx < 0 ? classFQName : classFQName.substring(idx + 1);
    String simpleMethodSignature = methodSignature == null ?  null : simplifyMethodSignature(methodSignature);
    
    idx = methodSignature == null ? -1 : methodSignature.indexOf("(");
    if (idx > 0) {
      String methodName = methodSignature.substring(0, idx);
      if (methodName.equals(className)) {
        methodSignature = "<init>" + methodSignature.substring(idx);
      }
    }
    
    return new Reference(classFQName, packageName, className, signature, methodSignature, simpleMethodSignature);
  }

  private static final Pattern METHOD_SIGNATURE = Pattern.compile("([^(]+)\\(([^)]+)\\)");
  
  private static String simplifyMethodSignature(String methodSignature) {
    Matcher matcher = METHOD_SIGNATURE.matcher(methodSignature);
    StringBuilder builder = new StringBuilder();
    if (matcher.matches()) {
      logger.debug("Matches: {}", matcher.groupCount());
      for (int i = 0; i <= matcher.groupCount(); ++i) {
        logger.debug("Match {}: {}", i, matcher.group(i));
      }

      builder.append(matcher.group(1));
      builder.append("(");
      String argSpec = matcher.group(2);
      if (argSpec != null && !argSpec.isBlank()) {
        String args[] = argSpec.split(",");
        for (int i = 0; i < args.length; ++i) {
          String arg = args[i];
          arg = arg.trim();
          if (!arg.isBlank()) {
            int idx = arg.lastIndexOf(".");
            builder.append(idx < 0 ? arg : arg.substring(idx + 1));
            if (i < args.length - 1) {
              builder.append(",");
            }
          }
        }        
      }
      builder.append(")");
      return builder.toString();
    } else {
      logger.debug("Signature {} did not match {}", methodSignature, METHOD_SIGNATURE.pattern());
      return methodSignature;
    }
  }

  public String getClassFQName() {
    return classFQName;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getClassName() {
    return className;
  }

  public String getSignature() {
    return signature;
  }

  public String getMethodSignature() {
    return methodSignature;
  }

  public String getSimpleMethodSignature() {
    return simpleMethodSignature;
  }  
  
  /**
   * Return the simpleMethodSignature with any ] escaped to \]
   * 
   * This does the same as replaceAll("\\]", "\\]") should do, but doesn't.
   * 
   * @return the simpleMethodSignature with any ] escaped to \]
   */
  public String getSimpleMethodSignatureEscaped() {
    Pattern pattern = Pattern.compile("\\]");
    StringBuilder result = new StringBuilder();
    Matcher matcher = pattern.matcher(simpleMethodSignature);
    int lastEnd = 0;
    while (matcher.find()) {
      result.append(simpleMethodSignature, lastEnd, matcher.start());
      lastEnd = matcher.end();
      result.append("\\]");
    }
    result.append(simpleMethodSignature, lastEnd, simpleMethodSignature.length());
    return result.toString();
  }  
  
}
