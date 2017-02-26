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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Deals with ingesting new translation data.
 */
public class IngestionUtil {
  private static final Logger LOG = Logger.getLogger("IngestionUtil");

  /**
   * Update the translation data in the data store based on the exported CSV data.
   *
   * @param data valid CSV data in the right format
   * @return Whether the data was imported properly.
   */
  public ResultOr<Boolean> ingestCsv(String data) {
    BufferedReader reader = new BufferedReader(new StringReader(data));
    try {
      String[] firstLine = reader.readLine().split("\\t");
      if (firstLine.length != 5) {
        return new ResultOr<>(false, "Invalid format");
      }
      List<Translation> translations = new LinkedList<>();
      String line = null;
      while ((line = reader.readLine()) != null) {
        ingestLine(line, translations);
      }
      TranslationsUtil.persist(translations);
    } catch (IOException e) {
      return new ResultOr<>(false, e.getMessage());
    }
    return new ResultOr<>(true);
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
    if (!disabled) {
      TranslationsUtil.addTranslationsTo(from, to, note, fromConversation, translations);
    } else {
      LOG.info("Skipping (disabled): " + from + " / " + to);
    }
  }
}
