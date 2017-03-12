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
