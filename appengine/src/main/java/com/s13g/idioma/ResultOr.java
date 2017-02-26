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

package com.s13g.idioma;

/**
 * Simple return class that can communicate an error state.
 */
public class ResultOr<T> {
  private final T mResult;
  private final String mErrorMessage;

  public ResultOr(T result, String errorMessage) {
    mResult = result;
    mErrorMessage = errorMessage;
  }

  public ResultOr(T result) {
    mResult = result;
    mErrorMessage = "";
  }

  public T getResult() {
    return mResult;
  }

  public String getErrorMessage() {
    return mErrorMessage;
  }
}
