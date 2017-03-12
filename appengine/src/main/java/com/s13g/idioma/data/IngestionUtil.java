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

import java.util.*;
import java.util.logging.Logger;

/**
 * Deals with ingesting new translation data.
 */
public class IngestionUtil {
  private static final Logger LOG = Logger.getLogger("IngestionUtil");
  private static final boolean TESTRUN = false;

  /**
   * This method takes the existing translation in the datastore as well as the ground truth of
   * the import. It then determines, which translations should be kept, which ones should be
   * deleted and which ones need to be added.
   * <p>
   * Note that the translations selected to remain in the datastore will be updated if some of
   * their data has changed in the ground truth ingested data.
   *
   * @param existingTranslations the existing translations in the data store.
   * @param ingestedGroundTruth  the ingested ground truth.
   * @param updater              used to update the underlying data store.
   * @throws IngestionException if something goes wrong during ingestion.
   */
  public UpdateStats ingest(Collection<Translation> existingTranslations,
                            Collection<Translation> ingestedGroundTruth,
                            DataStoreUpdater updater) throws IngestionException {
    Map<Long, Translation> existingMap = new HashMap<>(existingTranslations.size());
    for (Translation t : existingTranslations) {
      existingMap.put(t.hash, t);
    }
    Map<Long, Translation> ingestedMap = new HashMap<>(ingestedGroundTruth.size());
    for (Translation t : ingestedGroundTruth) {
      ingestedMap.put(t.hash, t);
    }

    UpdateResult updateResult = determineUpdates(existingMap, ingestedMap);
    updateResult.logStats();

    if (!TESTRUN) {
      updater.remove(updateResult.toDelete);
      updater.persist(updateResult.toAdd);
      updater.persist(updateResult.toUpdate);
      LOG.info("Data store updated");
    } else {
      LOG.warning("Not performing any action, TESTRUN enabled.");
    }
    return UpdateStats.from(updateResult);
  }

  /**
   * Determine which entries have to be updated in which way.
   *
   * @param existing    the existing translations
   * @param groundTruth the ground truth of translations
   * @return The update result detailing which entries to add, remove and update.
   */
  private UpdateResult determineUpdates(Map<Long, Translation> existing,
                                        Map<Long, Translation> groundTruth) {
    UpdateResult updateResult = new UpdateResult();
    // Determine which translations need to be deleted.
    for (Translation t : existing.values()) {
      if (!groundTruth.keySet().contains(t.hash)) {
        updateResult.toDelete.add(t);
      }
    }

    // Determine which translations will be added.
    for (Translation t : groundTruth.values()) {
      if (!existing.keySet().contains(t.hash)) {
        updateResult.toAdd.add(t);
      }
    }

    // Last but not least, this will determine the set of translation that will remain, because
    // they are in both set. But we need to ensure we update the fields in case something has
    // changed.
    for (Translation t : existing.values()) {
      if (groundTruth.keySet().contains(t.hash)) {
        Translation updatedTranslation = t.updateFromIngested(groundTruth.get(t.hash));
        if (updatedTranslation != null) {
          updateResult.toUpdate.add(updatedTranslation);
        }
      }
    }
    return updateResult;
  }

  private static class UpdateResult {
    final Set<Translation> toDelete = new HashSet<>();
    final Set<Translation> toAdd = new HashSet<>();
    final Set<Translation> toUpdate = new HashSet<>();

    void logStats() {
      LOG.info(String.format("UpdateResult: Deleting: %d, adding: %d, updating: %d.",
          toDelete.size(), toAdd.size(), toUpdate.size()));
    }
  }

  public static class UpdateStats {
    public final int numDeleted;
    public final int numAdded;
    public final int numUpdated;

    private UpdateStats(int numDeleted, int numAdded, int numUpdated) {
      this.numDeleted = numDeleted;
      this.numAdded = numAdded;
      this.numUpdated = numUpdated;
    }

    static UpdateStats from(UpdateResult result) {
      return new UpdateStats(
          result.toDelete.size(),
          result.toAdd.size(),
          result.toUpdate.size());
    }
  }

  public static class IngestionException extends Exception {
    IngestionException(String message, Throwable t) {
      super("Cannot ingest: " + message, t);
    }
  }
}
