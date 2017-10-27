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
class TypeVar (aNombre:String, aTipo:Char, aRangoMin:Float, aRangoMax:Float, aNominales:Array[String], aUso: String, aEtiq: Int, aContinua: Boolean) extends Serializable {
  private var nombre: String = aNombre
  private var tipo: Char = aTipo
  private var rangoMin: Float = aRangoMin
  private var rangoMax: Float = aRangoMax
  private var nominales: Array[String] = aNominales
  private var uso:String = aUso
  private var n_etiq: Int = aEtiq
  private var continua: Boolean = aContinua

  /*
   * Gets the use of the variable
   * @return      Use of the variable
   */
  def getUse: String = {
    uso
  }

  /*
   * Gets the name of the variable
   * @return      Name of the variable
   */
  def getName: String = {
    nombre
  }

  /**
    * <p>
    * Sets the name of the variable
    * </p>
    * @param valor       Name of the variable
    */
  def setName (valor: String)= {
    nombre = valor
  }


  /**
    * Gets the char with the type used in the variable
    * @return      The char with the type used in the variable: 'i' 'r' 'e'
    */
  def getType : Char ={
    tipo
  }

  /**
    * Sets the char with the type used in the variable
    * @param valor   The char with the type used in the variable: 'i' 'r' 'e'
    */
  def setType (valor: Char)={
    tipo = valor
  }


  /**
    * Gets if the variable is continuous or discrete
    * @return      True for continuous variable and false for discrete
    */
  def getContinuous : Boolean = {
    continua
  }

  /**
    * Sets the type of the variable
    * @param valor   Value true if variable is continuos and false otherwise
    */
  def setContinuous (valor: Boolean) = {
    continua = valor
  }

  /**
    * Gets the number of labels used by the continuous variable
    * @return      Number of labels in continuous variable
    */
  def getNLabels :Int = {
    n_etiq
  }

  /**
    * Sets the number of labels used by the continuous variable
    * @param valor   Number of labels in continuous variable
    */
  def setNlabels (valor: Int)={
    n_etiq = valor
  }

  /**
    * Gets the minimum value for the variable
    * @return      Minimum value for the variable
    */
  def getMin : Float = {
    rangoMin
  }

  /**
    * Sets the minimum value for the variable
    * @param valor      Minimum value for the variable
    */
  def setMin (valor : Float) = {
    rangoMin = valor
  }

  /**
    * Gets the maximum value for the variable
    * @return      Maximum value for the variable
    */
  def getMax : Float = {
    rangoMax
  }

  /**
    * Sets the maximum value for the variable
    * @param valor    Minimum value for the variable
    */
  def setMax (valor: Float) = {
    rangoMax = valor
  }

  /**
    * Gets the pos of labels used by the continuous variable
    * @param name  Name of label
    * @return      Pos of labels in continuous variable
    */
  def getPosLabel (name:String) :Int = {
    nominales.indexOf(name)
  }

  /**
    * Gets the name of labels used by the continuous variable
    * @param pos   Pos of label
    * @return      Name of labels in continuous variable
    */
  def getNameLabel (pos:Int) :String = {
    nominales(pos)
  }

  /**
    * Print the variable
    */
  def imprimir(): Unit = {
    println("Nombre: "+nombre)
    println("Tipo: "+tipo)
    println("Rango: "+rangoMin + "-" + rangoMax)
    if(tipo=='e') {
      println("Valores nominales: ")
      nominales.foreach(println)
    }
    println("Uso: "+uso)
    println("Número de etiquetas: "+n_etiq)
    println("--------------------")
  }
}