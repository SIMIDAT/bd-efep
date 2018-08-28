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
import org.apache.spark.rdd.RDD

class TableVar extends Serializable {
  private var nVars: Int= 0       //Number of variables
  private var nEtiq: Int = 0      //Number of labels for the continuous variables
  private var maxEtiq: Int = 0    //Max number of for all of the cont variab
  private var maxVal: Int = 0     //Mas number of values

  private var nClases: Int = 0    //Number of clases of the target variable
  private var claseObj: String = ""       // Name of the target class
  private var nClaseObj: Int = 0          // Number of the target class selected
  var classNames: Array[String] = new Array[String](0)

  //var variables: RDD[TypeVar] = null          // Variables characteristics (type, min-max values)
  private var variables: Array[TypeVar] = null
  private var baseDatos: Array[Array[Fuzzy]] = null   // Definitions for the fuzzy sets

  /**
    * Returns the number of variables
    * @return      Number of variables
    */
  def getNVars :Int = {
    nVars
  }


  /**
    * Returns the number of labels for all the continuous variables
    * @return      Number of labels for continuous variables
    */
  def getNLabel :Int = {
    nEtiq
  }

  /**
    * Sets the number of labels for all the continuous variables
    * @param valor       Number of linguistic labels
    */
  def setNLabel (valor: Int) = {
    nEtiq = valor
  }

  /**
    * Returns the maximum number of labels of all the variables
    * @return      The maximum number of labels of all variables
    */
  def getMaxLabel :Int = {
    maxEtiq
  }


  /**
    * Returns the maximum number of values of all the variables
    * @return      Maximum value of all variables
    */
  def getMaxVal :Int = {
    maxVal
  }


  /**
    * Returns the number of classes of the target variable
    * @return      Number of values for the target variable
    */
  def getNClass : Int = {
    nClases
  }

  /**
    * Sets the number of classes of the target variable
    * @param value     Number of classes
    */
  def setNClass (value: Int) = {
    nClases = value
  }


  /**
    * Returns the number of the class of the target variable
    * @return      Number of the class of the target variable
    */
  def getNumClassObj :Int = {
    nClaseObj
  }

  /**
    * Sets the number of the class of the target variable
    * @param clas         Number of the class of the target variable
    */
  def setNumClassObj (clas:Int) = {
    nClaseObj = clas
  }


  /**
    * Returns the name of the class of the target variable
    * @return              Name of the class
    */
  def getNameClassObj :String = {
    claseObj
  }

  /**
    * Returns the name of the variable
    * @param v     Pos of the variable
    * @return              Name of the variable
    */
  def getNameVar (v:Int) :String = {
    variables(v).getName
  }

  /**
    * Returns if the value is bound in the range of the variable
    * @param pos           Position of the variable
    * @param value         Value to verify
    * @return              True if the value is correct, false if not
    */
  def isBound (pos:Int, value:Float) :Boolean = {
    if (getMax(pos)>=value && value>=getMin(pos)){
      true
    }else{
      false
    }
  }

  /**
    * Sets the name of the class of the target variable
    * @param clas         Name of the target variable
    */
  def setNameClassObj (clas: String) = {
    claseObj = clas
  }

  /**
    * Return the belonging of a value
    * @ param i         Position of the variable
    * @ param j         Position of the value
    * @ param X         Value
    * @ return          Belonging grade of the value
    */
  def Fuzzy (i: Int, j: Int, X: Float): Float = {
    baseDatos(i)(j).Fuzzy(X)
  }
  def FuzzyT (i:Int, j:Int, X: Float, chrome: Double): Float = {
    if (chrome!=0.0)
      baseDatos(i)(j).FuzzyT(X, chrome)
    else baseDatos(i)(j).Fuzzy(X)
  }


  /**
    *  Method to return the value of the cut points X0
    * @ param i         Position of the variable
    * @ param j         Position of the value
    * @ return          Value of the cut points X0
    */
  def getX0 (i: Int, j: Int): Float = {
    baseDatos(i)(j).getX0
  }

  /**
    *  Method to return the value of the cut points X1
    * @param i         Position of the variable
    * @param j         Position of the value
    * @return          Value of the cut points X1
    */
  def getX1 (i:Int, j:Int): Float = {
    baseDatos(i)(j).getX1
  }

  /**
    *  Method to return the value of the cut points X3
    * @param i         Position of the variable
    * @param j         Position of the value
    * @return          Value of the cut points X3
    */
  def getX3 (i: Int, j: Int): Float ={
    baseDatos(i)(j).getX3
  }

  /**
    * Rounds the generated value for the semantics when necesary
    */
  def Round (valor: Float, tope: Float): Float = {
    if (valor > -0.0001 && valor < 0.0001) 0
    else if (valor > tope - 0.0001 && valor < tope + 0.0001) tope.toFloat
    else valor
  }


  /**
    * Defined to manage de semantics of the linguistic variables
    * Generates the semantics of the linguistic variables using a partition
    * consisting of triangle simetrics fuzzy sets. The cut points al stored
    * at 0.5 level of the fuzzy sets to be considered in the computation of
    * the gain of information. Also writes the semantics of the linguistic
    * variables in the specified file
    * @param nFile       Name of file to write the semantics
    **/

  def InitSemantics (nFile: String) = {
    var marca, valor, p_corte: Float = 0
    var auxX0, auxX1, auxX3, auxY: Float = 0
    var i = -1
    var contents = ""

    contents = "\n--------------------------------------------\n"
    contents+=   "|  Semantics for the continuous variables  |\n"
    contents+=   "--------------------------------------------\n"


    variables.foreach(v => { i += 1
      if (v.getContinuous) {
        marca = (v.getMax - v.getMin) / (v.getNLabels - 1)
        p_corte = v.getMin + marca / 2
        for (etq <- 0 to v.getNLabels - 1) {
          valor = v.getMin + marca * (etq - 1)
          auxX0 = Round(valor, v.getMax)
          valor = v.getMin + marca * etq
          auxX1 = Round(valor, v.getMax)
          valor = v.getMin + marca * (etq + 1)
          auxX3 = Round(valor, v.getMax)

          auxY = 1
          baseDatos(i)(etq).setVal(auxX0,auxX1,auxX3,auxY)
          p_corte += marca
          contents+= "\tLabel " + etq + ": " +  baseDatos(i)(etq).getX0 + " " +  baseDatos(i)(etq).getX1 + " " +  baseDatos(i)(etq).getX3 + "\n"
        }
      }
    })

    contents+= "\n"
    //Nota: La parte de escritura en el ficher no se hace aún
    //println(contents)
    //if (nFile!="")
    //File.AddtoFile(nFile, contents);

  }

  /**
    * Fill TableVar with the characteristics of the variables and creates
    * characteristics and intervals for the fuzzy sets
    * @param lines          Line of input file
    * @param inputs         Input Variables
    */
  def loadVars (lines: RDD[String], inputs: String) = {

    val wordsLines = lines.map(line => line.split("( *)(\\{)( *)|( *)(\\})( *)|( *)(\\[)( *)|( *)(\\])( *)|( *)(,)( *)| ")).collect()

    val auxVariables = wordsLines.filter(l => inputs.contains(l(1))).map {
      words =>
        var auxVar :TypeVar = null
        var auxMaxEtiq: Int = 0
        var auxMaxVal: Int = 0
        if (words(2) == "real") {
          auxMaxEtiq = nEtiq
          auxMaxVal = nEtiq
          auxVar = new TypeVar(words(1), 'r', words(3).toFloat, words(4).toFloat, null,"Input", nEtiq, true)
          auxVar.setContinuous(true)
        }
        else if (words(2) == "integer") {
          // Integer variables are treated as categorical variables within the range
          auxMaxEtiq = -1
          val min = words(3).toInt
          val max = words(4).toInt
          val range = max - min
          val catVals: Array[String] = (min to max).toArray.map(x => x.toString)

          if(range <= 10 && min > 0){ // If range is less or equal 10, treat this variable as categorical.
            auxMaxVal = catVals.length
            auxVar = new TypeVar(words(1), 'e', 0, range, catVals,"Input", catVals.length, false)
          } else {
            auxMaxVal = nEtiq
            auxVar = new TypeVar(words(1), 'i', words(3).toFloat, words(4).toFloat, null, "Input", nEtiq, true)
          }
          //auxVar.setContinuous(true)
        }
        else {
          auxMaxEtiq = -1
          auxMaxVal = words.length - 2
          auxVar = new TypeVar(words(1), 'e', 0, words.length - 3, words.slice(2, words.length), "Input", words.length - 2, false)
        }
        val auxArrVar:Array[TypeVar]=new Array[TypeVar](1)
        auxArrVar(0)=auxVar
        (auxArrVar,auxMaxEtiq,auxMaxVal,1)
    }.reduce((a,b) => (a._1.union(b._1),Math.max(a._2,b._2),Math.max(a._3,b._3),a._4+b._4))

    variables = auxVariables._1
    nVars = auxVariables._4
    maxEtiq = auxVariables._2
    maxVal = auxVariables._3

    // Creates "Fuzzy" characteristics and intervals
    baseDatos = Array.ofDim[Fuzzy](nVars, maxVal)
    for (i <- 0 to nVars -1)
      for (j<- 0 to maxVal -1)
        baseDatos(i)(j) = new Fuzzy
  }


  /**
    * Returns the maximum valid value for the variable "pos"
    * @param pos           Position of the variable
    * @return              Maximum value for the variable
    */
  def getMax (pos:Int):Float ={
    variables(pos).getMax
  }

  /**
    * Returns the minimum valid value for the variable "pos"
    * @param pos           Position of the variable
    * @return              Minimum value for the variable
    */
  def getMin (pos:Int):Float ={
    variables(pos).getMin
  }


  /**
    * Returns the number of labels of the var indicated by "pos"
    * @param pos           Position of the variable
    * @return              Number of labels for the variable
    */
  def getNLabelVar (pos:Int):Int ={
    variables(pos).getNLabels
  }


  /**
    * Returns if the variable "pos" is or not continua
    * @param pos           Position of the variable
    * @return              Value true if the variable is continuous or false in otherwise
    */
  def getContinuous (pos:Int):Boolean ={
    variables(pos).getContinuous
  }


  /**
    * Returns the type of the variable "pos"
    * @param pos           Position of the variable
    * @return              Type of the variable
    */
  def getType (pos:Int):Char ={
    variables(pos).getType
  }

  /**
    * Returns the name of the value of the variable "pos"
    * @param pos           Position of the variable
    * @param value         Value of the variable
    * @return              Name of the variable
    */
  def getNameLabel (pos:Int,value:Int):String ={
    variables(pos).getNameLabel(value)
  }

  /**
    * Gets the pos of labels used by the continuous variable
    * @param pos   Posicion de la variable
    * @param name  Name of label
    * @return      Pos of labels in continuous variable
    */
  def getPosLabel (pos:Int, name:String) :Int = {
    variables(pos).getPosLabel(name)
  }

  /**
    * Print the variables data
    */
  def imprimir(): Unit = {
    for (vari <- variables){
      vari.imprimir()
    }
  }
}
