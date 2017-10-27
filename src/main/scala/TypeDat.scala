import org.apache.spark.broadcast.Broadcast

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
class TypeDat (aEjemplo: Array[Float], aClase: Int) extends Serializable{
  var ejemplo: Array[Float] = aEjemplo
  var clase: Int = aClase
  var cubierto: Boolean = false
  var exampleNumber: Int = 0

  /**
    * Initialise a variable of an example
    * @param pos       Position of the variable
    * @param value     Value to initialise
    */
  def setDat (pos:Int, value:Float) = {
    ejemplo(pos) = value
  }

  /**
    * Gets the value of a variable
    * @param pos       Position of the variable
    * @return          The float value of the variable
    */
  def getDat (pos:Int) : Float = {
    ejemplo(pos)
  }

  /**
    * Returns if the value of the gen of an example is a lost value or not
    * lost = max value of the variable + 1
    * @param variables         Structure of the variables for the dataset
    * @param example           Position of the examples
    * @param pos               Position of the variable
    * @return                  If is a lost value
    **/
  def getLost(variables: Broadcast[TableVar], example: Int, pos: Int): Boolean = {
    ejemplo(pos) == variables.value.getMax(pos) + 1
  }


  /**
    * Gets the class
    * @return      The value of the position of the class
    */
  def getClas : Int = {
    clase
  }

  /**
    * Sets the value of a class
    * @param valor       Value of the position of the class
    */
  def setClas (valor: Int) = {
    clase = valor
  }

  /**
    * Gets if the example is covered
    * @return      Value true if the example is covered
    */
  def getCovered : Boolean = {
    cubierto
  }

  /**
    * Sets the state of the example
    * @param valor   Value correspondent to the state of the example
    */
  def setCovered (valor: Boolean) = {
    cubierto = valor
  }

  /**
    * Print instance
    */
  def imprimir(): Unit = {
    var i=0
    ejemplo.foreach(println)
    println("Clase: "+clase)
    println("Cubierto: "+cubierto)
    println("--------------------")
  }

}
