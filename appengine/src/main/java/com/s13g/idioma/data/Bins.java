package com.s13g.idioma.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class Bins {
  private static final Logger LOG = Logger.getLogger("Bins");

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
      LOG.warning("Positive progression not implemented yet.");
    }
    // TODO: Persist.
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