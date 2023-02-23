/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.distplan.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import org.apache.logging.log4j.Logger;

import javax.persistence.AttributeConverter;
import java.io.IOException;

public abstract class AbsJacksonConverter<T> implements AttributeConverter<T, String> {

  protected abstract String getDefaultDbValue();
  protected abstract T getDefaultEntityAttribute();
  protected abstract TypeReference<?> getTypeReference();

  protected abstract Logger getLogger();

  @Override
  public String convertToDatabaseColumn(T attribute) {
    getLogger().trace(LogStr.LOG_CONVERT_TO_DATABASE_COLUMN, attribute);

    if (attribute == null) {
      return getDefaultDbValue();
    }

    try {
      return new ObjectMapper().writerFor(getTypeReference()).writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      // ignore, use default
    }

    return getDefaultDbValue();
  }

  @Override
  public T convertToEntityAttribute(String dbData) {
    getLogger().trace(LogStr.LOG_CONVERT_TO_ENTITY_ATTRIBUTE, dbData);

    if (dbData == null) {
      return getDefaultEntityAttribute();
    }

    try {
      return new ObjectMapper().readerFor(getTypeReference()).readValue(dbData);
    } catch (IOException e) {
      // ignore, use default
    }

    return getDefaultEntityAttribute();
  }
}
