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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Wraps a collection of Translations and makes them accessible in a bin-level.
 */
public class Bins {
  private static final Logger LOG = Logger.getLogger("Bins");

  private final List<List<Translation>> mBins;
  private final Map<Long, Translation> mByHash;

  @Nullable
  private static Bins sInstance;

  /**
   * Returns the singletons Bins instance. Creates the instances, if it is not available yet.
   */
  public static Bins getInstance() {
    if (sInstance == null) {
      createInstance();
    }
    return sInstance;
  }

  private static void createInstance() {
    try {
      sInstance = new TranslationsUtil().getBinnedTranslations();
    } catch (TranslationProvider.TranslationProvidingException e) {
      sInstance = null;
      e.printStackTrace();
    }
  }

  /**
   * Call this whenever the datastore was updated, to ensure we reload the data in the bins.
   */
  public static void onDataUpdated() {
    createInstance();
  }

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
   * Returns a random Translation. Can return null, if there are no translations available.
   */
  @Nullable
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
   * Processes a response by the user.
   */
  public void processResponse(long hash, boolean correct) {
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
      LOG.warning("Positive progression not implemented yet.");
    }
    // TODO: Persist.
  }

  /**
   * Get a bin number chosen randomly, with weights of the different bins taken into account. A
   * lower bin therefore will have a higher chance of being selected than a higher bin.
   */
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