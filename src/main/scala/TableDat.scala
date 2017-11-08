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
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, Dataset}



class TableDat extends Serializable {
  private var nEjem: Int = 0
  //Number of examples in dataset
  var datosRDD: RDD[(Long,TypeDat)] = null
  var dataframe: DataFrame = null
  // Dataset instances
  private var ejClase: Array[Int] = null
  //Number of examples of each class
  private var ejClaseObj: Int = 0 //Number of examples of the target class
  private var total_ej_cubiertos: Int = 0;         // Total covered examples
  private var total_ej_cubiertos_clase: Int = 0;   // Total covered examples for class
  var cubiertos: java.util.BitSet = new java.util.BitSet(0)
  var classes: Array[Int] = new Array[Int](0)

  def setExamplesCovered(value: Int) = {
    total_ej_cubiertos = value
  }

  def getExamplesCovered: Int = {
    total_ej_cubiertos
  }

  def setExamplesCoveredClass(value: Int) = {
    total_ej_cubiertos_clase = value
  }

  def getExamplesCoveredClass: Int = {
    total_ej_cubiertos_clase
  }
  /**
    * Returns the number of examples of the DataSet
    * @return      Number of examples
    **/
  def getNEx: Int = {
    nEjem
  }


  /**
    * Returns the number of examples belonging to the class specified
    * @param clas         A value of the target variable
    * @return              Number of examples of the class
    **/
  def getExamplesClass(clas: Int): Int = {
    ejClase(clas)
  }


  /**
    * Stores and gets in "ej_clase_obj" the number of examples of the target class
    * @param clas         A value of the target variable
    * @return              Number of examples of the class
    **/
  def setExamplesClassObj(clas: Int): Int = {
    ejClaseObj = ejClase(clas)
    ejClaseObj
  }

  /**
    * Returns the number of examples of the target class
    * @return              Number of examples of the objective class
    **/
  def getExamplesClassObj: Int = {
    ejClaseObj
  }


  /**
    * Returns if the value of the gen of an example is a lost value or not
    * lost = max value of the variable + 1
    * @param variables         Structure of the variables for the dataset
    * @param pos               Position of the variable
    * @param value             Read Value
    * @return                  Final Value
    **/
  def getValue(variables: TableVar, pos: Int, value: String): Float = {
    val a = List ("?","<null>")
    if (variables.getType(pos) == 'e') {
      if (a.contains(value)){
        variables.getMax(pos)+1
      }else {
        val toRet=variables.getPosLabel(pos, value)
        if(toRet== -1){
          throw new IllegalArgumentException("Value "+value+" read for a nominal attribute that is not in the bounds fixed in the attribute '"+ variables.getNameVar(pos) +"' definition.")
        }else{
          toRet
        }
      }
    } else {
      if (a.contains(value)){
        variables.getMax(pos)+1
      }else {
        val value2 = value.toFloat
        if (variables.isBound(pos,value2)){
          value2
        }else{
          throw new IllegalArgumentException(variables.getMin(pos)+"--"+variables.getMax(pos)+"Value "+value+" read for a numeric attribute that is not in the bounds fixed in the attribute "+ variables.getNameVar(pos) +" definition.")
        }
      }
    }
  }

  /**
    * Creates and fill TableDat with the examples of the dataset
    * @param lines         lines of input files with example data
    * @param variables     Variables structure of the dataset
    */

  def loadData2(lines: RDD[String], variables: TableVar, outputsValues: Array[String], sc : SparkContext, numPartitions : Int) = {
    ejClase = new Array[Int](variables.getNClass) // Creates space to store the number of examples of each class
    val auxInstances = lines.map { line => line.split("( *)(,)( *)") }

    // Create a zipped with index rdd to get the data later
    datosRDD = auxInstances.map { line =>
      val arrInput = new Array[Float](line.length - 1)
      var clase = 0
      var posi = -1
      for(x<-line) {
        posi += 1
        if (posi < line.length - 1) {
          arrInput(posi) = getValue(variables, posi, x)
        } else {
          clase = outputsValues.indexOf(x.trim)
        }
      }
      new TypeDat(arrInput, clase)
    }.zipWithIndex().map(x => (x._2, x._1)).cache()

    // Count the number of Examples belonging to each class
    ejClase = datosRDD.map(x =>{
      val data = x._2
      val numExamplesClass = new Array[Int](variables.getNClass)
      numExamplesClass(data.getClas) += 1
      numExamplesClass
    }).reduce((x,y) => {
      // In the reduce phase we sum each array
      val result = new Array[Int](variables.getNClass)
      for(i <- 0 until result.length){
       result(i) = x(i) + y(i)
      }
      result
    })



    // Count the number of examples
    nEjem = datosRDD.count().toInt // Set Sthe number of examples (instances) of the dataset
    cubiertos = new java.util.BitSet(nEjem)

    // Get the class of each example
    val aux = datosRDD.map(x => (x._1,x._2.getClas)).collect().sortBy(_._1)
      classes = aux.map(x => x._2)

    println("Number of examples: " + nEjem)

  }


  /**
    * Returns the class of the example in position pos
    * @param pos       Position of the example
    */
  def getClass (pos:Int): Int = {
    //indexKey.lookup(pos toLong)(0).getClas
    //datos(pos).getClas
    classes(pos)
  }

  /**
    * Sets the class of the example in position pos
    * @param pos       Position of the example
    * @param value       Value of the class
    */
  def setClass (pos: Int, value: Int) = {
    classes(pos) = value
  }


  /**
    * Returns if the example in position pos is yet covered or not
    * @param pos       Position of the example
    * @return          State of the example
    */
  def getCovered (pos:Int) = {
    //datos(pos).getCovered
    cubiertos.get(pos)
  }

  /**
    * Sets to covered or not the the example in position pos
    * @param pos       Position of the example
    * @param valor      Value of the state of the example
    */
  def setCovered (pos:Int, valor:Boolean) = {
    //datos(pos).setCovered(valor)
    if(valor){
      cubiertos.set(pos)
    } else {
      cubiertos.clear(pos)
    }
  }




  /**
    * Print the examples data
    */
  def imprimir(): Unit = {
    for (i <- 0 to ejClase.length - 1) {
      println("Class " + i + ": " + ejClase(i) + " examples")
      println("Total examples: " + nEjem)
    }
    /*
    for (dato <- datos){
      dato.imprimir()
    }*/
  }




}
