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
  public String source;
  public String translated;
  public String note;

  @Index
  boolean reversed;
  @Index
  boolean fromConversation;
  @Index
  boolean disabled;
  @Index
  int bin;

  void setHash() {
    hash = (long) Objects.hashCode(source, translated, note);
  }
}
