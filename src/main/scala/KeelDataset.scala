import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

/**
  * The MIT License
  *
  * Copyright 2017 Ángel Miguel García-Vico.
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
  * Created by Ángel Miguel García-Vico (agvico@ujaen.es) on 09/10/2017.
  */

/**
  * Wrapper for class Variables and Examples in order to store
  * a KEEL dataset as an RDD
  * Class that reads a KEEL dataset and stores it as a RDD
  *
  * @param file The path of the file to read.
  * @param sc The SparkContext.
  * @param numPartitions The number of partitions the data will be splitted.
  * @param nLabels The numbers of linguistic labels to use to represente numeric variables.
  */
class KeelDataset(file: String, sc: SparkContext, numPartitions: Int, nLabels: Int) {

  val Variables: TableVar = new TableVar()
  val Examples: TableDat = new TableDat()
  //var VariablesB: Broadcast[TableVar] = null

  /**
    * Reads a KEEL dataset, fuzzifies numeric variables and store the results
    * in "variables" the information of the variables and in
    * "instanciasTotal.datosRDD" the RDD of the instances.
    */
  def readDataset() {

    // Catch the dataset, and parse it
    val fichero = sc.textFile(file, numPartitions)

    // Parse the fields of the datasets
    val head = fichero.filter { linea => linea.startsWith("@") }
    val linesHead = head.filter { line => line.startsWith("@attribute") }
    val linesInstances = fichero.filter(linea => !linea.contains("@") && !linea.trim().isEmpty)//.cache()
    val inputs = fichero.filter { linea => linea.startsWith("@inputs") }.first()
    val outputs = fichero.filter { linea => linea.startsWith("@output") }.first()

    // Gets values of the output values
    val outputs_values = linesHead
      .map(line => line.split("( *)(\\{)( *)|( *)(\\})( *)|( *)(\\[)( *)|( *)(\\])( *)|( *)(,)( *)| "))
      .filter(l =>
        outputs.matches(".* " concat l(1))
      ).first().drop(2)

    Variables.classNames = new Array[String](outputs_values.length)
    var i = -1
    outputs_values.foreach { x =>
      i += 1
      Variables.classNames(i) = x
    }
    // Parse, initialize and fuzzify the variables
    Variables.setNClass(outputs_values.length)
    Variables.setNLabel(nLabels) // Sets the number of fuzzy labels for numeric class
    Variables.setNClass(outputs_values.length)
    Variables.loadVars(linesHead, inputs)
    Variables.InitSemantics("")
    //Variables.imprimir()

    // Read the instances
    Examples.loadData2(linesInstances, Variables, outputs_values, sc, numPartitions)
    Examples.datosRDD.cache()
  }

  /**
    * Get the data in RDD format.
    * @return
    */
  def getDataRDD: RDD[(Long, TypeDat)] ={
    Examples.datosRDD
  }

  /**
    * Get the fuzzy belonging degree of the value in a given value of a variable
    * @param i  The variable
    * @param j  The value in the variable
    * @param x  The value to calculate the belonging degree
    */
  def Fuzzy(i: Int, j: Int, x: Float): Float ={
    Variables.Fuzzy(i,j,x)
  }


  /**
    * Get the number of labels for a given variable
    * @param pos
    * @return
    */
  def getNLabelVar(pos: Int): Int ={
      Variables.getNLabelVar(pos)
  }


  /**
    * Get the umber of variables
    * @return
    */
  def getNVars: Int ={
    Variables.getNVars
  }

  /**
    * Gets the name of the variable
    * @param pos
    * @return
    */
  def getNameVar(pos: Int): String = {
    Variables.getNameVar(pos)
  }


  /**
    * Set the 'Variables' variable as broadcast, i.e., it is sent as read-only to all maps.
    *
    * This is more efficient than using the 'Variables' variable directly inside of the map.
    *
    * @return
    */
  def BroadcastVariables: Broadcast[TableVar] = {
    sc.broadcast(Variables)
  }

}
