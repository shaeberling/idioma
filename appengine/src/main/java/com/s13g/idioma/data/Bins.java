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
import java.util.Collections;
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
  private final DataStoreUpdater mUpdater;
  private final RandomBinPicker mBinPicker;

  /**
   * Note: Do not just change this number to change the bin number. More instances in the code
   * need to change to support a different number of bins.
   */
  private static final int NUM_BINS = 5;

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
      RandomBinPicker binPicker = new RandomBinPicker();
      sInstance = new TranslationsUtil().getBinnedTranslations(binPicker);
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

  Bins(List<List<Translation>> bins, DataStoreUpdater updater, RandomBinPicker binPicker) {
    mBins = bins;
    mUpdater = updater;
    mBinPicker = binPicker;

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
      int bin = mBinPicker.getRandomBin();
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

    if (!mBins.get(translation.bin).remove(translation)) {
      LOG.severe(String.format("The translation for %s was not found in bin #%d.",
          translation.source, translation.bin));
      return;
    }
    if (!correct) {
      translation.bin = 0;
      translation.numRepliesIncorrect++;
    } else if (translation.bin < 4) {
      translation.bin += 1;
      translation.numRepliesCorrect++;
    }

    mBins.get(translation.bin).add(translation);
    mUpdater.persist(Collections.singleton(translation));
  }

  /**
   * @return Statistics about the current data in the bins.
   */
  public Statistics getStatistics() {
    Statistics stats = new Statistics();
    for (int i = 0; i < NUM_BINS; ++i) {
      stats.numItemsInBin[i] = mBins.get(i).size();
    }
    return stats;
  }

  public static class Statistics {
    public int[] numItemsInBin = new int[NUM_BINS];
  }
}