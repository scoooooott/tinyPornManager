/*
 * Copyright 2012 - 2020 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;

/**
 * a custom null serializer to serialize null strings into "" source:
 * https://digitaljoel.nerd-herders.com/2015/03/18/how-to-make-jackson-serialize-null-strings-differently-than-null-objects/
 * 
 * @author digitaljoel
 */
public class CustomNullStringSerializerProvider extends DefaultSerializerProvider {
  // A couple of constructors and factory methods to keep the compiler happy
  public CustomNullStringSerializerProvider() {
    super();
  }

  public CustomNullStringSerializerProvider(CustomNullStringSerializerProvider provider, SerializationConfig config, SerializerFactory jsf) {
    super(provider, config, jsf);
  }

  @Override
  public CustomNullStringSerializerProvider createInstance(SerializationConfig config, SerializerFactory jsf) {
    return new CustomNullStringSerializerProvider(this, config, jsf);
  }

  // This is the interesting part. When the property has a null value it will call this method to get the
  // serializer for that null value. At this point, we have the BeanProperty, which contains information about
  // the field that we are trying to serialize (including the type!) So we can discriminate on the type to determine
  // which serializer is used to output the null value.
  @Override
  public JsonSerializer<Object> findNullValueSerializer(BeanProperty property) throws JsonMappingException {
    if (property.getType().getRawClass().equals(String.class)) {
      return EmptyStringSerializer.INSTANCE;
    }
    else {
      return super.findNullValueSerializer(property);
    }
  }

  // This is our fancy serializer that takes care of writing the value desired in the case of a null string. We could
  // write whatever we want in here, but in order to maintain backward compatibility we choose the empty string
  // instead of something like "joel is awesome."
  public static class EmptyStringSerializer extends JsonSerializer<Object> {
    public static final JsonSerializer<Object> INSTANCE = new EmptyStringSerializer();

    private EmptyStringSerializer() {
    }

    // Since we know we only get to this seralizer in the case where the value is null and the type is String, we can
    // do our handling without any additional logic and write that empty string we are so desperately wanting.
    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
      jsonGenerator.writeString("");
    }
  }
}
