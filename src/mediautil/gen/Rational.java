/* MediaUtil LLJTran - $RCSfile: Rational.java,v $
 * Copyright (C) 1999-2005 Dmitriy Rogatkin, Suresh Mahalingam.  All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *	$Id: Rational.java,v 1.1.1.1 2005/07/27 03:03:16 drogatkin Exp $
 *
 * Some ideas and algorithms were borrowed from:
 * Thomas G. Lane, and James R. Weeks
 */
package mediautil.gen;

import java.io.Serializable;

/**
 * Mediautil reworked for KissPhoto bei Dringo
 *
 * ik: this class represents a fraction (rational) consisting of
 * num = numerator   (Zaehler)
 * den = denominator (Nenner)
 *
 *
 * @since 2021-11-09 some simplifications because of later Java version made for better readability
 * @version 2022-01-14 use double instead of float
 * @author Dringo. Originally Dmitriy Rogatkin and Suresh Mahalingam (msuresh@cheerful.com)
 */

public class Rational implements Serializable {
  int num, den;

  public Rational(int num, int den) {
    this.num = num;
    this.den = den;
  }

  public Rational(float value) { //original implementation
    den = 1000;
    num = (int) (value * den);
  }

  public Rational(double value) { //more Java like implementation with higher precision by dringo
    den = 1000;
    num = (int) (value * den);
  }

  public String toString() {
    return "" + num + "/" + den;
  }

  public float floatValue() { //original implementation
    return (float) num / den;
  }
  public double doubleValue() { //more Java like implementation with higher precision by dringo
    return (double) num / den;
  }

  public int intValue() {
    return num / den;
  }

  public String toExposureString() {
    if (num > 0)
      return "1/" + Math.round(1.0 * den / num);
    return "";
  }

  public int getDen() {
    return den;
  }

  public int getNum() {
    return num;
  }

  public void normalize() {
  }

  public static String toExposureString(Object o) {
    if (o instanceof Rational)
      return ((Rational) o).toExposureString();
    return o.toString();
  }
}
