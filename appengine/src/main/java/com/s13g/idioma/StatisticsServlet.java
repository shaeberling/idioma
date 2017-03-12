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
import com.s13g.idioma.data.Bins.Statistics;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Renders statistics about the current data, e.g. how many items are in the bins.
 */
public class StatisticsServlet extends AbstractIdiomaServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    Statistics statistics = Bins.getInstance().getStatistics();
    resp.setContentType("text/plain");

    for (int i = 0; i < statistics.numItemsInBin.length; ++i) {
      resp.getWriter().append(String.format("Bin #%d -> %d\n", i, statistics.numItemsInBin[i]));
    }
  }
}
