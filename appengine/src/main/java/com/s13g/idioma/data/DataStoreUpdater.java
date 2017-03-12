package com.s13g.idioma.data;

import java.util.Collection;

/**
 * Classes implementing this interface can update an underlying datastore.
 */
public interface DataStoreUpdater {
  /**
   * Adds or updates the given translations.
   */
  void persist(Collection<Translation> translations);

  /**
   * Removes the given translations, if they exist.
   */
  void remove(Collection<Translation> translations);
}
