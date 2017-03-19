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

import com.s13g.idioma.data.Bins;
import com.s13g.idioma.data.Translation;
import com.s13g.idioma.data.TranslationProvider;
import com.s13g.idioma.data.TranslationSet;
import com.s13g.idioma.data.TranslationsUtil;
import com.s13g.idioma.ui.Template;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enables adding/removing of users.
 */
public class MainServlet extends AbstractIdiomaServlet {
  private static final Logger LOG = Logger.getLogger("MainServlet");

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doServe(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long hash = Long.parseLong(req.getParameter("hash"));
    boolean correct = Boolean.parseBoolean(req.getParameter("correct"));
    Bins.getInstance().processResponse(hash, correct);
    doServe(req, resp);
  }

  private void doServe(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html; charset=UTF-8");
    resp.setCharacterEncoding("UTF-8");
    TranslationSet set = Bins.getInstance().getRandom();
    if (set == null) {
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
      resp.getWriter().write("No data found.");
      return;
    }

    String html = Template.fromFile("WEB-INF/html/index.html")
        .with("source", set.mainTranslation.source)
        .with("note", set.mainTranslation.note)
        .with("hash", set.mainTranslation.hash)
        .with("solution", set.mainTranslation.translated)
        .with("alt_solutions", createAltSolutionParam(set.alternatives))
        .with("alt_solution_hashes", createAltSolutionHashesParam(set.alternatives))
        .render();
    resp.getWriter().write(html);
  }

  private static String createAltSolutionParam(List<Translation> translations) {
    String result = "";
    for (Translation translation : translations) {
      if (!result.isEmpty()) {
        result += ",";
      }
      result += translation.translated;
    }
    return result;
  }

  private static String createAltSolutionHashesParam(List<Translation> translations) {
    String result = "";
    for (Translation translation : translations) {
      if (!result.isEmpty()) {
        result += ",";
      }
      result += translation.hash;
    }
    return result;
  }
}
