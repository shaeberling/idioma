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

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * For some translation sources there are multiple translations possible. For a selected
 * translation chosen from the bin, this set also includes the other valid translations.
 */
public class TranslationSet {
  public final Translation mainTranslation;
  /**
   * Keyed by translation.
   */
  public final List<Translation> alternatives;

  private TranslationSet(Translation mainTranslation, List<Translation> alternatives) {
    Preconditions.checkArgument(!alternatives.contains(mainTranslation),
        "TranslationSet's main translation may not be part of the alternatives.");
    this.mainTranslation = mainTranslation;
    this.alternatives = alternatives;
  }

  /**
   * Creates a new translation set.
   *
   * @param mainTranslation   the main translation
   * @param allWithSameSource other translations (which may include the main translation) with
   *                          the same source. If the main translation is part of this, it will
   *                          ve filtered out.
   * @return A valid TranslationSet.
   */
  public static TranslationSet from(Translation mainTranslation,
                                    Collection<Translation> allWithSameSource) {
    Preconditions.checkNotNull(mainTranslation);
    Preconditions.checkNotNull(allWithSameSource);

    List<Translation> alternatives = new ArrayList<>();
    for (Translation alternative : allWithSameSource) {
      if (!mainTranslation.equals(alternative)) {
        alternatives.add(alternative);
      }
    }
    return new TranslationSet(mainTranslation, alternatives);
  }
}
