/*
 * Copyright 2017, Sascha Häberling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.s13g.idioma.data;

import com.s13g.idioma.ResultOr;
import com.s13g.idioma.ingestion.TranslationProvider;
import com.s13g.idioma.ingestion.TranslationProvider.TranslationProvidingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deals with ingesting new translation data.
 */
public class IngestionUtil {
  private static final Logger LOG = Logger.getLogger("IngestionUtil");

  public int ingest(TranslationProvider provider) throws IngestionException {
    try {
      Collection<Translation> translations = provider.getCompleteSet();
      TranslationsUtil.persist(translations);
      return translations.size();
    } catch (TranslationProvidingException e) {
      throw new IngestionException(e.getMessage(), e);
    }
  }

  public static class IngestionException extends Exception {
    public IngestionException(String message, Throwable t) {
      super("Cannot ingest: " + message, t);
    }
  }
}
