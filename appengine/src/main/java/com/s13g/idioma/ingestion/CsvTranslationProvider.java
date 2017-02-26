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

package com.s13g.idioma.ingestion;

import com.s13g.idioma.data.Translation;
import com.s13g.idioma.data.TranslationsUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides translations from a given CSV Source.
 */
public class CsvTranslationProvider implements TranslationProvider {
  private static final Logger LOG = Logger.getLogger("CsvProvider");

  private static final int COLUMN_COUNT = 6;

  private final String mCsvData;

  public CsvTranslationProvider(String csvData) {
    mCsvData = csvData;
  }

  @Override
  public Collection<Translation> getCompleteSet() throws TranslationProvidingException {
    BufferedReader reader = new BufferedReader(new StringReader(mCsvData));
    try {
      String[] firstLine = reader.readLine().split("\\t");
      if (firstLine.length != COLUMN_COUNT) {
        throw new TranslationProvidingException("CSV has invalid column count");
      }
      List<Translation> translations = new LinkedList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        ingestLine(line, translations);
      }
      return translations;
    } catch (IOException e) {
      throw new TranslationProvidingException("Cannot ingest CSV", e);
    }
  }

  /**
   * Ingest a single line from the data spreadsheet.
   *
   * @param line valid CSV line
   * @param translations a list of all translation to which we'll add the new one extracted from the
   * line.
   */
  private void ingestLine(String line, List<Translation> translations) {
    String[] parts = line.split("\\t");
    if (parts.length < 2) {
      LOG.warning("Skipping line: '" + line + "'.");
      return;
    }
    String from = parts[0];
    String to = parts[1];

    String note = parts.length > 2 ? parts[2] : "";
    boolean fromConversation = parts.length > 3 && !parts[3].isEmpty();
    boolean disabled = parts.length > 4 && !parts[4].isEmpty();
    boolean important = parts.length > 5 && !parts[5].isEmpty();
    TranslationsUtil.addInitializedTranslationPairsTo(
        from, to, note, fromConversation, disabled, important, translations);
  }
}
