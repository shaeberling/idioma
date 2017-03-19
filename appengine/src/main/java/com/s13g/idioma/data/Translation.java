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

import com.google.common.base.Objects;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import javax.annotation.Nullable;

/**
 * A translation item.
 */
@Entity
@Cache
public class Translation {
  @Id
  public Long hash;

  /**
   * From where to translate.
   */
  @Index
  public String source;

  /**
   * The translated string. If there are multiple translation options, they will be captured in
   * separate 'Translation' object, since they could also have separate notes.
   */
  public String translated;

  /**
   * A note to display which might make it easier to understand which translation is asked for.
   */
  public String note;

  /**
   * Whether this is a reversed translation, which we automatically generate during ingestion.
   */
  @Index
  boolean reversed;

  /**
   * Whether this is from a conversation, or false then from a reputable source like DuoLingo.
   */
  @Index
  boolean fromConversation;

  /**
   * Temporarily disabled items will not be asked, but will remain in the data store.
   */
  @Index
  boolean disabled;

  /**
   * Whether this is an important vocabulary pair to learn.
   */
  @Index
  boolean important;

  /**
   * The bin this item is currently in.
   */
  @Index
  int bin;

  /**
   * Counts the number of times the user got the response incorrect.
   */
  int numRepliesCorrect;

  /**
   * Counts the number of times the user got the response correct.
   */
  int numRepliesIncorrect;

  /**
   * Sets the 'hash' property of this translation item.
   */
  void setHash() {
    if (hash != null && hash != 0) {
      throw new RuntimeException("Hash already set for item " + source + "/" + translated);
    }
    hash = (long) Objects.hashCode(source, translated);
  }

  /**
   * @return A hash of all the 'extra' elements.
   */
  private int getExtraHash() {
    return Objects.hashCode(this.fromConversation, this.disabled, this.important);
  }

  /**
   * Updates the data in this translation with the data from the ingested translation, ensuring
   * that data that needs to be persisted (like bin number) will keep persisted.
   */
  @Nullable
  Translation updateFromIngested(Translation ingested) {
    if (this.getExtraHash() == ingested.getExtraHash()) {
      // Nothing to update.
      return null;
    }

    this.fromConversation = ingested.fromConversation;
    this.disabled = ingested.disabled;
    this.important = ingested.important;
    // Needs to stay: bin, numProcessed, all hashed values.
    return this;
  }

  @Override
  public int hashCode() {
    // This works because our hash is an integer in reality. See #setHash.
    return (int) (long) hash;
  }

  @Override
  public boolean equals(Object other) {
    return other != null && other instanceof Translation && other.hashCode() == this.hashCode();
  }
}
