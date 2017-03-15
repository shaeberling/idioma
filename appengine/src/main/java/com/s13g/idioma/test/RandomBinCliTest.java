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

package com.s13g.idioma.test;

import com.s13g.idioma.data.RandomBinPicker;

/**
 * Testing the distribution of the random bin picker.
 */
public class RandomBinCliTest {
  public static void main(String[] args) {
    RandomBinPicker picker = new RandomBinPicker();
    int[] count = new int[5];
    for (int i = 0; i < 1000000; ++i) {
      count[picker.getRandomBin()]++;
    }

    System.out.println("===== Result:");
    for (int i = 0; i < count.length; ++i) {
      System.out.println(String.format("Bin #%d -> %d", i, count[i]));
    }
  }
}
