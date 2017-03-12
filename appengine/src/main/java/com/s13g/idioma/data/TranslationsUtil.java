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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Utilities for managing Translations.
 */
public class TranslationsUtil implements TranslationProvider, DataStoreUpdater {
  private static final Logger LOG = Logger.getLogger("TranslationsUtil");

  private static final int NUM_BINS = 5;

  @Override
  public Collection<Translation> getCompleteSet() throws TranslationProvidingException {
    List<Translation> translations = ofy().load().type(Translation.class).list();
    LOG.info("Loaded translations: " + translations.size());
    return translations;
  }

  public Bins getBinnedTranslations() throws TranslationProvidingException {
    ArrayList<List<Translation>> bins = new ArrayList<>(NUM_BINS);
    for (int i = 0; i < NUM_BINS; ++i) {
      bins.add(new LinkedList<Translation>());
    }
    for (Translation t : getCompleteSet()) {
      bins.get(t.bin).add(t);
    }
    return new Bins(bins);
  }

  @Override
  public void persist(Collection<Translation> translations) {
    ofy().save().entities(translations).now();
  }

  @Override
  public void remove(Collection<Translation> translations) {
    ofy().delete().entities(translations).now();
  }

  /**
   * Creates 'Translation' objects, two for each call. The regular one and the reversed one. It
   * also ensues that the hash is set.
   */
  public static void addInitializedTranslationPairsTo(String from,
                                               String to,
                                               String note,
                                               boolean fromConversation,
                                               boolean disabled,
                                               boolean important,
                                               Collection<Translation> list) {
    {
      // Non-Reverse
      Translation t = new Translation();
      t.source = from;
      t.translated = to;
      t.note = note;
      t.fromConversation = fromConversation;
      t.disabled = disabled;
      t.important = important;
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
      t.disabled = disabled;
      t.important = important;
      t.reversed = true;
      t.bin = 0;
      t.setHash();
      list.add(t);
    }
  }
}
