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

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.s13g.idioma.data.Translation;
import com.s13g.idioma.data.TranslationsUtil;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Fetches the translations from a Google Sheet through the Google Client API.
 */
public class SpreadsheetsTranslationProvider implements TranslationProvider {
  private static final Logger LOG = Logger.getLogger("SheetsProvider");

  private static final String RANGE = "Words!A:F";
  private static final int COLUMN_COUNT = 6;

  private static final String APP_NAME = "Idioma App";

  // FIXME: Put this in a properties file
  private static final String SPREADSHEET_ID = "1kNAqOaHDc3eHuP1764s47LljOQheCw2_0uBgaqq9Snc";
  /**
   * Global instance of the scopes required by this quickstart.
   * <p>
   * If modifying these scopes, delete your previously saved credentials
   * at ~/.credentials/sheets.googleapis.com-java-quickstart
   */
  private static final List<String> SCOPES =
      Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

  private final Sheets mSheets;

  /**
   * @return An instance of this provider.
   */
  public static SpreadsheetsTranslationProvider create() {
    AppIdentityCredential credential = new AppIdentityCredential(SCOPES);
    return new SpreadsheetsTranslationProvider(
        new Sheets.Builder(new UrlFetchTransport(), new JacksonFactory(), credential)
            .setApplicationName(APP_NAME)
            .build());
  }

  @Override
  public Collection<Translation> getCompleteSet() throws TranslationProvidingException {
    try {
      ValueRange resp = mSheets.spreadsheets().values().get(SPREADSHEET_ID, RANGE).execute();
      List<List<Object>> values = resp.getValues();
      LOG.info("About to ingest " + (values.size() - 1) + " translations.");
      List<Translation> translations = new ArrayList<>(values.size() - 1);

      // First row is the heading. Sanity check.
      for (int i = 0; i < COLUMN_COUNT; ++i) {
        if (Strings.isNullOrEmpty((String) values.get(0).get(i))) {
          throw new TranslationProvidingException("Invalid column count");
        }
      }

      for (int r = 1; r < values.size(); ++r) {
        RowData row = new RowData(values.get(r));
        String from = row.getString(0);
        String translated = row.getString(1);
        String note = row.getString(2);
        boolean fromConversation = row.getBoolean(3);
        boolean disabled = row.getBoolean(4);
        boolean important = row.getBoolean(5);
        TranslationsUtil.addInitializedTranslationPairsTo(
            from, translated, note, fromConversation, disabled, important, translations);
      }

      return translations;
    } catch (IOException e) {
      throw new TranslationProvidingException("Cannot import through Sheets API", e);
    }
  }

  private SpreadsheetsTranslationProvider(Sheets sheets) {
    mSheets = sheets;
  }

  private static class RowData {
    private final List<Object> data;

    private RowData(List<Object> data) {
      this.data = data;
    }

    String getString(int i) {
      if (data.size() > i) {
        return (String) data.get(i);
      } else {
        return "";
      }
    }

    boolean getBoolean(int i) {
      return getString(i).trim().length() > 0;
    }
  }
}
