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

import java.util.Random;

/**
 * Get a bin number chosen randomly, with weights of the different bins taken into account. A
 * lower bin therefore will have a higher chance of being selected than a higher bin.
 */
public class RandomBinPicker {
  /**
   * Picks a random bin, weighted by bin number.
   */
  public int getRandomBin() {
    // 16 8 4 2 1 = 31
    // Bins: 0000000000000000 11111111 2222 33 4
    int r = (new Random()).nextInt(31);
    if (r < 16) {
      return 0;
    } else if (r < 24) {
      return 1;
    } else if (r < 28) {
      return 2;
    } else if (r < 30) {
      return 3;
    } else {
      return 4;
    }
  }
}
