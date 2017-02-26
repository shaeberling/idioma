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
import com.s13g.idioma.ui.Template;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Frontend for the data ingestion.
 */
public class IngestionServlet extends AbstractIdiomaServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("text/html");
    String html = Template.fromFile("WEB-INF/html/ingestion.html").render();
    resp.getWriter().write(html);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("text/html");
    InputStream fileStream;
    ServletFileUpload upload = new ServletFileUpload();
    try {
      fileStream = upload.getItemIterator(req).next().openStream();
    } catch (FileUploadException e) {
      resp.getWriter().write(e.getMessage());
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    if (fileStream == null) {
      resp.getWriter().write("No file stream.");
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    byte[] bytes = ByteStreams.toByteArray(fileStream);
    String csvString = new String(bytes, Charset.forName("UTF-8"));
    ResultOr<Boolean> result = (new IngestionUtil()).ingestCsv(csvString);
    if (!result.getResult()) {
      resp.getWriter().write("Something went wrong: " + result.getErrorMessage());
      return;
    }
    resp.getWriter().write("Ingestion POST here ...  Loaded " + bytes.length + " bytes.");
  }
}
