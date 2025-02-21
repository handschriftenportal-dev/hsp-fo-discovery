/*
 * MIT License
 *
 * Copyright (c) 2021 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.staatsbibliothek.berlin.hsp.fo.discovery.dto;

import java.io.Serializable;

/**
 * 
 * @author Lutz Helm {@literal <helm@ub.uni-leipzig.de>}
 *
 */

public class Stats implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private Double min;
  private Double max;
  private long count;
  private long missing;

  public Stats(Double min, Double max, long count, long missing) {
    super();
    this.min = min;
    this.max = max;
    this.count = count;
    this.missing = missing;
  }

  public Double getMin() {
    return min;
  }

  public void setMin(Double min) {
    this.min = min;
  }

  public Double getMax() {
    return max;
  }

  public void setMax(Double max) {
    this.max = max;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }

  public long getMissing() {
    return missing;
  }

  public void setMissing(long missing) {
    this.missing = missing;
  }
}
