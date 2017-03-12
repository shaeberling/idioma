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

package com.s13g.idioma;

import com.google.common.io.ByteStreams;
import com.s13g.idioma.data.IngestionUtil;
import com.s13g.idioma.data.IngestionUtil.IngestionException;
import com.s13g.idioma.data.IngestionUtil.UpdateStats;
import com.s13g.idioma.data.Translation;
import com.s13g.idioma.data.TranslationProvider.TranslationProvidingException;
import com.s13g.idioma.data.TranslationsUtil;
import com.s13g.idioma.ingestion.CsvTranslationProvider;
import com.s13g.idioma.ingestion.SpreadsheetsTranslationProvider;
import com.s13g.idioma.data.TranslationProvider;
import com.s13g.idioma.ui.Template;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Frontend for the data ingestion.
 */
public class IngestionServlet extends AbstractIdiomaServlet {
  private static final Logger LOG = Logger.getLogger("IngestionServlet");

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html");
    String html = Template.fromFile("WEB-INF/html/ingestion.html").render();
    resp.getWriter().write(html);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html");

    int numTranslations;
    TranslationProvider provider;
    LOG.info("Source param: " + req.getParameter("source"));
    if ("gsheets".equals(req.getParameter("source"))) {
      provider = SpreadsheetsTranslationProvider.create();
    } else {
      try {
        provider = getCsvProvider(req);
      } catch (Exception e) {
        resp.getWriter().write(e.getMessage());
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    }

    if (provider == null) {
      resp.getWriter().write("No ingestion provider found.");
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    UpdateStats updateStats;
    try {
      TranslationsUtil translationsUtil = new TranslationsUtil();
      Collection<Translation> ingested = provider.getCompleteSet();
      Collection<Translation> existing = translationsUtil.getCompleteSet();

      // Ingest the new items.
      updateStats = (new IngestionUtil()).ingest(existing, ingested, translationsUtil);
    } catch (IngestionException | TranslationProvidingException e) {
      resp.getWriter().write("Something went wrong: " + e.getMessage());
      return;
    }
    resp.getWriter().write(String.format("Access. Added: %d  Deleted: %d  Updated: %d",
        updateStats.numAdded, updateStats.numDeleted, updateStats.numUpdated));
  }

  private TranslationProvider getCsvProvider(HttpServletRequest req) throws IOException,
      FileUploadException {
    InputStream fileStream;
    ServletFileUpload upload = new ServletFileUpload();
    fileStream = upload.getItemIterator(req).next().openStream();

    if (fileStream == null) {
      throw new IOException("No file stream");
    }
    byte[] bytes = ByteStreams.toByteArray(fileStream);
    String csvString = new String(bytes, Charset.forName("UTF-8"));
    return new CsvTranslationProvider(csvString);
  }
}
