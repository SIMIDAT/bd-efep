/**
  * The MIT License
  *
  * Copyright 2016 Ángel Miguel García-Vico.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */

/**
  * Created by Ángel Miguel García-Vico (agvico@ujaen.es) on 15/12/16.
  */
import java.util

import scala.collection.mutable.ArrayBuffer



/**
  * Class that store a confusion matrix and some auxiliar information
  * in order to calculate the quality measures of an individual following the
  * Map-Reduce paradigm
  */
class ConfusionMatrix(neje: Int) extends Serializable{

  /**
    * Values for the confusion matrix
    */
  var tp: Int = 0
  var tn: Int = 0
  var fp: Int = 0
  var fn: Int = 0


  /**
    * Other values
    */
  var ejAntCrisp: Int = 0                // Number of compatible examples with the antecedent of any class - crisp version
  var ejAntClassCrisp: Int = 0           // Number of compatible examples (antecedent and class) - crisp version
  var ejAntNoClassCrisp: Int = 0         // Number of compatible examples (antecedent and NOT class) - crisp version
  var ejAntClassNewCrisp: Int = 0        // Number of new covered compatible examples (antec and class) - crisp version
  var numVarNoInterv: Int = 0
  var gradoCompAntFuzzy: Float = 0           // Total compatibility degree with the antecedent - fuzzy version
  var gradoCompAntClassFuzzy: Float = 0      // Tot compatibility degree with antecedent and class - fuzzy version


  var coveredExamples: ArrayBuffer[Long] = new ArrayBuffer[Long](0)
  //var coveredExamples: java.util.BitSet = new util.BitSet(neje)


  def +(that: ConfusionMatrix): ConfusionMatrix = {
    val a = new ConfusionMatrix(1)
    a.tn = this.tn + that.tn
    a.tp=this.tp + that.tp
    a.fp=this.fp + that.fp
    a.fn=this.fn + that.fn

    a.ejAntCrisp=this.ejAntCrisp + that.ejAntCrisp
    a.ejAntClassCrisp=this.ejAntClassCrisp + that.ejAntClassCrisp
    a.ejAntNoClassCrisp=this.ejAntNoClassCrisp + that.ejAntNoClassCrisp
    a.ejAntClassNewCrisp=this.ejAntClassNewCrisp + that.ejAntClassNewCrisp
    a.numVarNoInterv=this.numVarNoInterv + that.numVarNoInterv
    a.gradoCompAntClassFuzzy=this.gradoCompAntClassFuzzy + that.gradoCompAntClassFuzzy
    a.gradoCompAntFuzzy=this.gradoCompAntFuzzy + that.gradoCompAntFuzzy
    a.coveredExamples=this.coveredExamples ++ that.coveredExamples

    a
  }
}
