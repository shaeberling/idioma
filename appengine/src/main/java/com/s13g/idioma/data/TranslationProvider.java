package com.s13g.idioma.data;

import com.s13g.idioma.data.Translation;

import java.util.Collection;

/**
 * Classes implementing this interface can provide new translation sets.
 */
public interface TranslationProvider {
  /**
   * @return A complete set to update the data store with.
   */
  Collection<Translation> getCompleteSet() throws TranslationProvidingException;

  class TranslationProvidingException extends Exception {
    private static final String MSG = "Cannot fetch translations: %s";

    public TranslationProvidingException(String reason) {
      super(String.format(MSG, reason));
    }

    public TranslationProvidingException(String reason, Throwable t) {
      super(String.format(MSG, reason), t);
    }
  }
}
