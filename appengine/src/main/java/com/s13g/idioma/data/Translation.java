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

  void setHash() {
    hash = (long) Objects.hashCode(source, translated, note);
  }
}
