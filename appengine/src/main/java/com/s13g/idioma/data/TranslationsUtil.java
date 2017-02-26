/*
 * Copyright 2016, Sascha Häberling
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Utilities for managing Translations.
 */
public class TranslationsUtil {
  private static final Logger LOG = Logger.getLogger("TranslationsUtil");

  private static final int NUM_BINS = 5;

  public static Bins getBinnedTranslations() {
    List<Translation> translations = ofy().load().type(Translation.class).list();
    LOG.info("Loaded translations: " + translations.size());

    ArrayList<List<Translation>> bins = new ArrayList<>(NUM_BINS);
    for (int i = 0; i < NUM_BINS; ++i) {
      bins.add(new LinkedList<Translation>());
    }
    for (Translation t : translations) {
      bins.get(t.bin).add(t);
    }
    return new Bins(bins);
  }

  static void addTranslationsTo(String from,
                                String to,
                                String note,
                                boolean fromConversation,
                                List<Translation> list) {
    {
      // Non-Reverse
      Translation t = new Translation();
      t.source = from;
      t.translated = to;
      t.note = note;
      t.fromConversation = fromConversation;
      t.disabled = false;
      t.reversed = false;
      t.bin = 0;
      t.setHash();
      list.add(t);
    }

    {
      // Reverse
      Translation t = new Translation();
      t.source = to;
      t.translated = from;
      t.note = note;
      t.fromConversation = fromConversation;
      t.disabled = false;
      t.reversed = true;
      t.bin = 0;
      t.setHash();
      list.add(t);
    }
  }

  static void persist(List<Translation> translations) {
    ofy().save().entities(translations).now();
  }

  public static class Bins {
    private final List<List<Translation>> mBins;
    private final Map<Long, Translation> mByHash;

    Bins(List<List<Translation>> bins) {
      mBins = bins;

      mByHash = new HashMap<>();
      for (List<Translation> bin : bins) {
        for (Translation translation : bin) {
          mByHash.put(translation.hash, translation);
        }
      }
    }

    /**
     * Can return null.
     */
    public Translation getRandom() {
      int tries = 0;
      List<Translation> translations;
      do {
        // Get a random, non-empty bin.
        int bin = getRandomBin();
        translations = mBins.get(bin);
        if (++tries > 100) {
          return null;
        }
      } while (translations.size() <= 0);

      // Get a random item from the bin.
      int itemIdx = (new Random()).nextInt(translations.size());
      return translations.get(itemIdx);
    }

    /**
     * Progress a translation based on the result.
     */
    public void progress(long hash, boolean correct) {
      LOG.info("Hash: " + hash + " correct: " + correct);
      Translation translation = mByHash.get(hash);
      if (translation == null) {
        LOG.severe("Could not found translation: " + hash);
        return;
      }
      int oldBin = translation.bin;
      if (!correct) {
        translation.bin = 0;
      } else {

      }

    }

    private static int getRandomBin() {
      // 16 8 4 2 1 = 31
      // Bins: 0000000000000000 11111111 2222 33 4
      int r = (new Random()).nextInt(31);
      if (r < 16) {
        return 0;
      } else if (r < 24) {
        return 1;
      } else if (r < 28) {
        return 2;
      } else if (r < 30) {
        return 3;
      } else {
        return 4;
      }
    }
  }
}
