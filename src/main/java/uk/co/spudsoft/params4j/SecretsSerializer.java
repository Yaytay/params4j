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
package uk.co.spudsoft.params4j;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * A JsonSerializer that prevents the output of a string value, replacing it with ********.
 * 
 * If the value is null the serializer will output null, and if it is an empty string the output will be "".
 * This behaviour makes it easy to detect whether a password has been set, without displaying it.
 * 
 * @author jtalbut
 */
public class SecretsSerializer extends JsonSerializer<String> {

  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

    if (value == null) {
      gen.writeNull();
    } else if (value.isEmpty()) {
      gen.writeString("");
    } else {
      gen.writeString("********");
    }

  }
  
  /**
   * Return a Jackson AnnotionInspector that disables the SecretsSerializer.
   * This AnnotationInspector extends the base JacksonAnnotationInspect and it should not be necessary to use the {@link com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair}.
   * 
   * Typical usage would be:
   * <pre>{@code
   * ObjectMapper tempMapper = objectMapper.copy().setAnnotationIntrospector(SecretsSerializer.getDisabler());
   * String json = tempMapper.writeValueAsString(creds);
   * }</pre>
   * 
   * @return a Jackson AnnotionInspector that disables the SecretsSerializer.
   */
  public static AnnotationIntrospector getDisabler() {
    return new DisablingSecretsSerializerAnnotationInspector();
  }

  private static class DisablingSecretsSerializerAnnotationInspector extends JacksonAnnotationIntrospector {

    private static final long serialVersionUID = 1L;

    @Override
    public Object findSerializer(Annotated a) {
      if (a.hasAnnotation(JsonSerialize.class)) {
        Annotation ann = a.getAnnotation(JsonSerialize.class);
        if (((JsonSerialize) ann).using().equals(SecretsSerializer.class)) {
          return new StringSerializer();
        }
      }
      return super.findContentSerializer(a);
    }

  }
}
