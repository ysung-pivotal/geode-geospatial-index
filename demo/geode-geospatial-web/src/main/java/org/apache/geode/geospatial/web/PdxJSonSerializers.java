/*
 * Copyright [2016] Charlie Black
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

package org.apache.geode.geospatial.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.gemstone.gemfire.pdx.PdxInstance;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

/**
 * Created by Charlie Black on 9/23/16.
 */
@JsonComponent
public class PdxJSonSerializers {
    public static class Serializer extends JsonSerializer<PdxInstance> {
        @Override
        public void serialize(PdxInstance value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            gen.writeStartObject();
            for (String name : value.getFieldNames()) {
                Object object = value.getField(name);
                gen.writeObjectField(name, object);
            }
            gen.writeEndObject();
        }
    }
}
