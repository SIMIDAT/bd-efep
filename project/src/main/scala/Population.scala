/**
  * <p>
  *
  * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
  * @version 1.0
  * @since JDK1.5
  *        </p>
  */


import java.util
import java.util.ArrayList
import java.util.BitSet

import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Population extends Serializable {
  /**
    * <p>
    * Population of candidate rules
    * </p>
    */
  var indivi: Array[Individual] = _ // Population individuals
  private var num_indiv: Int = 0 // Max number of individuals in the population
  var num_used: Int = 0 // Number or individuals really used
  var ej_cubiertos: BitSet = _ // Covered examples of the population
  var ult_cambio_eval = 0 // Last change in the population
  /**
    * <p>
    * Creates a population of Individual
    * </p>
    *
    * @param numind    Number of individuals
    * @param numgen    Number of variables
    * @param nobj      Number of objectives
    * @param neje      Number of examples
    * @param RulRep    Rules representation
    * @param Variables Variables structure
    */
  def this(numind: Int, numgen: Int, nobj: Int, neje: Int, RulRep: String, Variables: TableVar) {
    this()
    num_indiv = numind
    num_used = 0

    if(RulRep equalsIgnoreCase "CAN")
      indivi = Array.fill[Individual](numind)(new IndCAN(numgen, neje, nobj, 0))
    else
      indivi = Array.fill[Individual](numind)(new IndDNF(numgen, neje, nobj, Variables,0))

    ej_cubiertos = new BitSet(neje)
    ult_cambio_eval = 0
  }

  /**
    * <p>
    * Biased random population initialization
    * </p>
    *
    * @param Variables Variables structure
    * @param porcVar   Percentage of variables to form the rules
    * @param porcPob   Percentage of population with biased initialisation
    * @param neje      Number of examples
    * @param nFile     File to write the population
    */
  def BsdInitPob(Variables: TableVar, porcVar: Float, porcPob: Float, neje: Int, clas: Int,  nFile: String) {
    var contents: String = ""
    val parteSesg = porcPob * num_indiv
    var j = 0

    indivi.take(Math.ceil(parteSesg).toInt).foreach(ind => {
     ind.BsdInitInd(Variables, porcVar, neje, clas, nFile)
   })

    indivi.takeRight(num_indiv - Math.ceil(parteSesg).toInt).foreach(ind => {
      ind.RndInitInd(Variables, neje, clas,  nFile)
    })

    num_used = num_indiv
    ej_cubiertos.clear(0, ej_cubiertos.size())
  }

  def getClass(pos: Int): Int ={
    indivi(pos).getClas
  }

  def setNumIndiv(value: Int): Unit = {
    num_indiv = value
  }
  /**
    * <p>
    * Evaluates non-evaluated individuals
    * </p>
    *
    * @param AG        Genetic algorithm
    * @param Variables Variables structure
    * @param Examples  Examples structure
    * @return Number of evaluations performed
    */
  def evalPop(AG: Genetic, Variables: Broadcast[TableVar], Examples: TableDat, sc: SparkContext): Int = {
    var trials = 0
    var i = 0
    val neje = Examples.getNEx

    // The number of trials is the number of non-evaluated individuals
    trials = indivi.count(y => {!y.getIndivEvaluated})

    // EvalInd in the map. Reduce takes the number of trials and return
    /*indivi.filter(y => !y.getIndivEvaluated).map(ind => {
      ind.evalInd(AG, Variables, Examples)
      ind.setNEval(AG.getTrials)
      1
    }).sum*/

    // AQUI HAY QUE METER MAS ADELANTE BIG DATA
    val indivsToEval = indivi.filter(y => !y.getIndivEvaluated)

    val pobCovered = Array.fill[PobBitSet](indivsToEval.length)(new PobBitSet(Examples.getNEx))
    pobCovered.foreach(x => sc.register(x))

    //val inds = sc.broadcast(indivsToEval)

    val confusionMatrices = Examples.datosRDD.mapPartitions(x => {
      var matrices: Array[ConfusionMatrix] = new Array[ConfusionMatrix](indivsToEval.length)
      matrices = matrices.map(x => new ConfusionMatrix(neje))

      while(x.hasNext){
        val d = x.next()
        val data = d._2
        val index = d._1
        for (k <- indivsToEval.indices){
          //matrices(i) = matrices(i) + inds.value(i).evalExample(Variables, data, index, false
          val individual = indivsToEval(k)
          var disparoCrisp = 1

          if(AG.getRulesRep equalsIgnoreCase "CAN"){
            val cromosoma = individual.getIndivCromCAN

            for(j <- 0 until Variables.value.getNVars){
              if(! Variables.value.getContinuous(j)){
                // Discrete variable
                if(cromosoma.getCromElem(j) <= Variables.value.getMax(j)){
                  // Variable k participate in the rule
                  if((data.getDat(j) != cromosoma.getCromElem(j)) && ! data.getLost(Variables,0,j)){
                    disparoCrisp = 0
                  }
                } else {
                  matrices(k).numVarNoInterv += 1
                }
              } else {
                // Continuous variable
                if(cromosoma.getCromElem(j) < Variables.value.getNLabelVar(j)){
                  // Variable k take part in the rule
                  // Crisp computation
                  if(! data.getLost(Variables, 0, j)){
                    if(individual.NumInterv(data.getDat(j),j,Variables) != cromosoma.getCromElem(j)){
                      disparoCrisp = 0
                    }
                  }
                } else {
                  matrices(k).numVarNoInterv += 1
                }
              }
            }
          } else {
            // DNF RULES
            val cromosoma = individual.getIndivCromDNF

            for (j <- 0 until Variables.value.getNVars) {
              if (!Variables.value.getContinuous(j)) {
                // Discrete variables
                if (cromosoma.getCromGeneElem(j, Variables.value.getNLabelVar(j))) {
                  if (!cromosoma.getCromGeneElem(j, data.getDat(j).toInt) && !data.getLost(Variables, 0, j)) {
                    disparoCrisp = 0
                  }
                } else {
                  matrices(k).numVarNoInterv += 1
                }
              } else {
                // Continuous variable
                if (cromosoma.getCromGeneElem(j, Variables.value.getNLabelVar(j))) {
                  if (!data.getLost(Variables, 0, j)) {
                    if (!cromosoma.getCromGeneElem(j, individual.NumInterv(data.getDat(j), j, Variables))) {
                      disparoCrisp = 0
                    }
                  }
                } else {
                  matrices(k).numVarNoInterv += 1
                }
              }
            }
          }

            if(disparoCrisp > 0){
              pobCovered(k).add(index.toInt)
              //matrices(k).coveredExamples += index
              //matrices(k).coveredExamples.set(index.toInt)
              matrices(k).ejAntCrisp += 1
              //mat.coveredExamples += index
              if(data.getClas == individual.getClas){
                matrices(k).ejAntClassCrisp += 1
                matrices(k).tp += 1
              } else {
                matrices(k).ejAntNoClassCrisp += 1
                matrices(k).fp += 1
              }
              // cubreClase[Examples.getClass(i)]++; // Como hago yo esto?
              // AQUI TENEMOS UN PROBLEMA CON LOS NUEVOS EJEMPLOS CUBIERTOS

              /*if((!cubiertos.get(index toInt)) && (data.getClas == this.clas)){
                mat.ejAntClassNewCrisp += 1
              }*/
            } else {
              if(data.getClas == individual.getClas){
                matrices(k).fn += 1
              } else {
                matrices(k).tn += 1
              }
            }
          }
        }


      val aux = new Array[Array[ConfusionMatrix]](1)
      aux(0) = matrices
      aux.iterator
    }, true).reduce((x,y) => {
      val ret = new Array[ConfusionMatrix](y.length)
      //trials += y.length
      for(i <- y.indices) {
        val toRet: ConfusionMatrix = new ConfusionMatrix(neje)
        //x(i).coveredExamples.foreach(value => indivsToEval(i).cubre.set(value toInt))
        //y(i).coveredExamples.foreach(value => indivsToEval(i).cubre.set(value toInt))


        toRet.ejAntClassCrisp = x(i).ejAntClassCrisp + y(i).ejAntClassCrisp
        toRet.ejAntClassNewCrisp = x(i).ejAntClassNewCrisp + y(i).ejAntClassNewCrisp
        toRet.ejAntCrisp = x(i).ejAntCrisp + y(i).ejAntCrisp
        toRet.ejAntNoClassCrisp = x(i).ejAntNoClassCrisp + y(i).ejAntNoClassCrisp
        toRet.fn = x(i).fn + y(i).fn
        toRet.fp = x(i).fp + y(i).fp
        toRet.tn = x(i).tn + y(i).tn
        toRet.tp = x(i).tp + y(i).tp
        toRet.numVarNoInterv = x(i).numVarNoInterv + y(i).numVarNoInterv
        ret(i) = toRet
      }
      // Return !
      ret
    })

    // Now we have the complete confusion matrices of all individuals. Calculate their measures!!
    for(i <- indivsToEval.indices){
      indivsToEval(i).cubre.clear(0,Examples.getNEx)
      indivsToEval(i).cubre.or(pobCovered(i).value)
      //indivsToEval(i).Print("")
      indivsToEval(i).computeQualityMeasures(confusionMatrices(i),AG, Examples, Variables.value)
      indivsToEval(i).medidas.confusionMatrix = confusionMatrices(i)
    }



    // Sets all individuals as evaluated
    var k = trials

    indivi.filter(!_.getIndivEvaluated).foreach(y => {
      y.setIndivEvaluated(true)
      y.setNEval(AG.getTrials + trials - k)
      k = k - 1
    })

    trials

    }


  /**
    * <p>
    * Returns the indicated individual of the population
    * </p>
    *
    * @param pos Position of the individual
    * @return Individual
    */
  def getIndiv(pos: Int): Individual = indivi(pos)

  /**
    * <p>
    * Return the number of individuals of the population
    * </p>
    *
    * @return Number of individuals of the population
    */
  def getNumIndiv: Int = num_indiv

  /**
    * <p>
    * Copy the individual in the Individual otro
    * </p>
    *
    * @param pos  Position of the individual to copy
    * @param neje Number of examples
    * @param nobj Number of objectives
    * @param a    Individual to copy
    */
  def CopyIndiv(pos: Int, neje: Int, nobj: Int, a: Individual) {
    indivi(pos).copyIndiv(a, neje, nobj)
  }

  /**
    * <p>
    * Returns the indicated gene of the Chromosome
    * </p>
    *
    * @param num_indiv Position of the individual
    * @param pos       Position of the variable
    * @param elem      Position of the gene of the variable
    * @param RulRep  Rules representation
    * @return Gene of the chromosome
    */
  def getCromElem(num_indiv: Int, pos: Int, elem: Int, RulRep: String): Int =
    if (RulRep.compareTo("CAN") == 0)
      indivi(num_indiv).getCromElem(pos)
    else if (indivi(num_indiv).getCromGeneElem(pos, elem))
      1
    else
      0

  /**
    * <p>
    * Sets the value of the indicated gene of the Chromosome
    * </p>
    *
    * @param num_indiv Position of the individual
    * @param pos       Position of the variable
    * @param elem      Position of the gene of the variable
    * @param val       Value for the gene
    * @param RulRep  Rules representation
    */
  def setCromElem(num_indiv: Int, pos: Int, elem: Int, `val`: Int, RulRep: String) {
    if(RulRep equalsIgnoreCase "can")
      indivi(num_indiv).setCromElem(pos, `val`)
    else
      indivi(num_indiv).setCromGeneElem(pos, elem, `val` == 1)
  }

  /**
    * <p>
    * Returns if the individual of the population has been evaluated
    * </p>
    *
    * @param num_indiv Position of the individual
    */
  def getIndivEvaluated(num_indiv: Int): Boolean = indivi(num_indiv).getIndivEvaluated

  /**
    * <p>
    * Sets the value for de evaluated attribute of the individual
    * </p>
    *
    * @param num_indiv Position of the individual
    * @param val       Value of the individual
    */
  def setIndivEvaluated(num_indiv: Int, `val`: Boolean) {
    indivi(num_indiv).setIndivEvaluated(`val`)
  }

  /**
    * <p>
    * Returns de hole cromosoma of the selected individual
    * </p>
    *
    * @param num_indiv Position of the individual
    * @return Canonical chromosome
    */
  def getIndivCromCAN(num_indiv: Int): CromCAN = indivi(num_indiv).getIndivCromCAN

  /*/**
    * <p>
    * Returns de hole cromosoma of the selected individual
    * </p>
    *
    * @param num_indiv Position of the individual
    * @return DNF chromosome
    */*/
  def getIndivCromDNF(num_indiv: Int): CromDNF = indivi(num_indiv).getIndivCromDNF

  /*
       * <p>
       * Return the number of the evaluation with the last change
       * </p>
       * @return                    Number of the last evaluation
       */
  def getLastChangeEval: Int = ult_cambio_eval

  /**
    * <p>
    * This function marks the examples covered by the actual population.
    * </p>
    *
    * @param neje   Number of examples
    * @param trials Number of trials performed
    */
  def examplesCoverPopulation(neje: Int, trials: Int) {
    //Copies the actual examples structure
    val cubiertos_antes = new util.BitSet(neje)
    cubiertos_antes.clear(0,neje)
    cubiertos_antes.or(ej_cubiertos)
    ej_cubiertos.clear(0, neje)

    // Checks the examples covered by the actual population
      indivi.foreach(ind => {
        // for each rule
        if (ind.getRank == 0) {
          ej_cubiertos.or(ind.cubre)
        }
    })

    //Comparisons both structures
    val aux = new BitSet(ej_cubiertos.size())
    aux.clear(0, ej_cubiertos.size())
    aux.or(cubiertos_antes) // aux es ahora una copia de cubiertos_antes
    // Para comprobar que ha habido cambios, la operacion es:
    // (cubiertos_antes xor ej_cubiertos) and (not cubiertos_antes)
    aux.xor(ej_cubiertos) // Con el xor se obtienen aquellos que son diferentes.
    cubiertos_antes.flip(0, neje) // negado de cubiertos_antes
    aux.and(cubiertos_antes)

    // Si ha habido cambios, la cardinalidad de aux debe ser mayor que cero
    if(aux.cardinality() > 0){
      // There is a change
      ult_cambio_eval = trials
    }

  }




  /**
  * <p>
  * This function marks the examples covered by the actual population.
  * </p>
  *
  * @param neje   Number of examples
  * @param trials Number of trials performed
    */
  def examplesCoverPopulation(neje: Int, trials: Int, pobCovered: BitSet) {
    //Copies the actual examples structure
    val cubiertos_antes = new util.BitSet(neje)
    cubiertos_antes.clear(0,neje)
    cubiertos_antes.or(ej_cubiertos)

    //Comparisons both structures
    val aux = new BitSet(ej_cubiertos.size())
    aux.clear(0, ej_cubiertos.size())
    aux.or(cubiertos_antes) // aux es ahora una copia de cubiertos_antes
    // Para comprobar que ha habido cambios, la operacion es:
    // (cubiertos_antes xor ej_cubiertos) and (not cubiertos_antes)
    aux.xor(pobCovered) // Con el xor se obtienen aquellos que son diferentes.
    cubiertos_antes.flip(0, neje) // negado de cubiertos_antes
    aux.and(cubiertos_antes)

    // Si ha habido cambios, la cardinalidad de aux debe ser mayor que cero
    if(aux.cardinality() > 0){
      // There is a change
      ult_cambio_eval = trials
    }

  }


  /**
    * <p>
    * Prints population individuals
    * </p>
    *
    * @param nFile File to write the population
    * @param v     Vector which indicates if the individual if repeated
    */
  def Print(nFile: String, v: util.Vector[_]) {
    var marca = 0
    for(i <- 0 until num_indiv) {
      marca = v.get(i).asInstanceOf[Int]
      if (marca != 1) indivi(i).Print(nFile)

    }
  }

  /**
    * Apply the token competition procedure to this population.
    *
    *
    * NOTE: The population must be evaluated before calling this method.
    *
    * @param Examples  the Examples of the dataset
    * @param Variables the variables of the dataset
    * @param GA        the actual genetic algorithm execution
    * @return A new population with the token competition applied
    */
  def tokenCompetition(Examples: TableDat, Variables: TableVar, GA: Genetic): Population = {
    // Sort population by the diversity function
    val actual: Population = sortByDiversity(Examples, Variables, GA)


    // Indivuals to add in new population
    val rules: ArrayBuffer[Individual] = new ArrayBuffer[Individual]

    val tokens: BitSet = new BitSet(Examples.getNEx)

    var conta = 0
    var todosCubiertos = false
    // Apply token competition procedure

    do {
      val cubiertoRegla = actual.indivi(conta).cubre
      var cubreNuevo = false
      val clas = actual.indivi(conta).getClas

      // Posibiliad de optimizar esto?
      for (i <-0 until cubiertoRegla.size()) {
          // Get only the tokens of its class
          if(!tokens.get(i) && cubiertoRegla.get(i) && Examples.getClass(i) == clas){
            tokens.set(i)
            cubreNuevo = true
        }
      }

      // if the individual cover new examples not covered by other rules, add it to the result population
      if (cubreNuevo)
        rules += actual.indivi(conta)

      // check if all examples are actually covered
      if (tokens.cardinality() == tokens.size())
        todosCubiertos = true

      conta += 1
    } while (conta < getNumIndiv && !todosCubiertos)

    // Assert that it is at least one individual in the population
    if(rules.isEmpty){
      // Add the individual with the less number of examples??
      // TO-DO
    }

    // Creates the result population
    val result: Population = new Population(rules.size, Variables.getNVars, GA.getNumObjectives, Examples.getNEx, GA.getRulesRep, Variables)

    for (i <- 0 until result.num_indiv) {
        result.CopyIndiv(i, Examples.getNEx, GA.getNumObjectives, rules(i))
    }
    result

  }



  /**
    * Sorts the population according to the diversity function
    *
    * @param Examples  the examples
    * @param Variables the variables
    * @param GA        the genetic algorithm
    * @return A sorted population
    */
  def sortByDiversity(Examples: TableDat, Variables: TableVar, GA: Genetic): Population = {
    val result: Population = new Population(getNumIndiv, Variables.getNVars, GA.getNumObjectives, Examples.getNEx, GA.getRulesRep, Variables)

    // Sort population with the diversity function
    val izq = 0
    val der = getNumIndiv - 1
    val ordenado: Array[Double] = GA.getDiversity.toLowerCase match {
      case "crowding"   => indivi.map(x => x.getCrowdingDistance)
      case "wracc"      => indivi.map(x => x.getMeasures.getUnus.toDouble)
      case "tpr"        => indivi.map(x => x.getMeasures.getTPr.toDouble)
      case "medgeo"     => indivi.map(x => x.getMeasures.getMedGeo.toDouble)
      case "fpr"        => indivi.map(x => x.getMeasures.getFPr.toDouble)
      case "conf"       => indivi.map(x => x.getCnfValue)
      case "growthrate" => indivi.map(x => x.getMeasures.getGrowthRate.toDouble)
      case "jaccard"    => indivi.map(x => x.getMeasures.getJaccard.toDouble)
    }

    //val ordenado = indivi.map(x => x.getCrowdingDistance) // Cambiar en un futuro por la atipicidad (o lo que se crea conveniente)
    val indices = ordenado.indices.toArray

    Utils.OrCrecIndex(ordenado, izq, der, indices)
    // Introduce in decreasing order
    var conta = 0
    for (i <- indices.indices.reverse) {
        result.CopyIndiv(conta, Examples.getNEx, GA.getNumObjectives, getIndiv(indices(i)))
        conta += 1
    }
    result
  }

  /**
    * Perfoms the join of two populations (it does not remove duplicates)
    *
    * @param other     The other population to join with
    * @param Examples  The examples of the dataset
    * @param Variables The variables of the dataset
    * @param GA        The actual genetic algorithm execution
    * @return A new joined population
    */
  def join(other: Population, Examples: TableDat, Variables: TableVar, GA: Genetic): Population = {
    val result = new Population(num_indiv + other.num_indiv, Variables.getNVars, GA.getNumObjectives, Examples.getNEx, GA.getRulesRep, Variables)
    var conta = 0
    for(i <- 0 until num_indiv) {
        result.CopyIndiv(conta, Examples.getNEx, GA.getNumObjectives, indivi(i))
        conta += 1
    }
    for(i <- 0 until other.num_indiv) {
        result.CopyIndiv(conta, Examples.getNEx, GA.getNumObjectives, other.indivi(i))
        conta += 1
    }
    result
  }

  /**
    * It removes duplicates in a population
    * @param marcas
    * @param Examples
    * @param Variables
    * @param GA
    * @return
    */
  def removeRepeated(marcas: BitSet, Examples: TableDat, Variables: TableVar, GA: Genetic): Population = {
    var cuenta = marcas.cardinality()
    val reglas = new ArrayBuffer[Individual]
    var j: Int =  marcas.nextClearBit(0)
    while (j >= 0 && j < num_indiv){
      reglas += indivi(j)
      j = marcas.nextClearBit(j+1)
    }

    val result = new Population(num_indiv - cuenta, Variables.getNVars, GA.getNumObjectives, Examples.getNEx, GA.getRulesRep, Variables)
    for(i <- 0 until reglas.size) {
        result.CopyIndiv(i, Examples.getNEx, GA.getNumObjectives, reglas(i))
    }

    result
  }


  /**
    * Returns the average unusualness of the population
    * @return
    */
  def getGlobalWRAcc: Float ={
    val total = indivi.map(x => x.getMeasures.getUnus).sum

    // Return
    total / indivi.length
  }


  /**
    * Returns the average value of a quality measure of the population
    * @param measure
  */
  def getGlobalMeasure(measure: String): Float ={
    val total: Float = measure.toLowerCase() match {
      case "crowding"   => indivi.map(x => x.getCrowdingDistance.toFloat).sum
      case "wracc"      => indivi.map(x => x.getMeasures.getUnus).sum
      case "tpr"        => indivi.map(x => x.getMeasures.getTPr).sum
      case "medgeo"     => indivi.map(x => x.getMeasures.getMedGeo).sum
      case "fpr"        => indivi.map(x => x.getMeasures.getFPr).sum
      case "conf"       => indivi.map(x => x.getMeasures.getCnf.toFloat).sum
      case "growthrate" => indivi.map(x => x.getMeasures.getGrowthRate).sum
      case "medgeo"     => indivi.map(x => x.getMeasures.getMedGeo).sum
      case "supdiff"    => indivi.map(x => x.getMeasures.getSuppDiff).sum
    }

    total / indivi.length
  }


  /**
    * It calculates the chi value for each individual with respect each other
    * and averages this value, this procedure is applied for each individual in the population
    * @return
    */
  def getAverageChiValue: Float = {
    var sumAvgChi = indivi.map( i => {
      val sumChi = indivi.map(j =>{
        val X: Array[Integer] = new Array[Integer](2)
        X(0) = i.medidas.confusionMatrix.fn.toInt + i.medidas.confusionMatrix.tn.toInt
        X(1) = i.medidas.confusionMatrix.fp.toInt + i.medidas.confusionMatrix.tp.toInt
        val Y: Array[Integer] = new Array[Integer](2)
        Y(0) = j.medidas.confusionMatrix.fn.toInt + j.medidas.confusionMatrix.tn.toInt
        Y(1) = j.medidas.confusionMatrix.fp.toInt + j.medidas.confusionMatrix.tp.toInt

        chi(Y,X)
      }).sum

      sumChi / indivi.length
    }).sum

    (sumAvgChi / indivi.length).toFloat
  }

  private def chi(Y: Array[Integer], X: Array[Integer]): Double = {
    if (Y.length != 2 || X.length != 2)
      return -1
    val observedTable = Array.ofDim[Float](2, 2)
    val expectedTable = Array.ofDim[Float](2, 2)
    val totalSum: Float = Y(0) + Y(1) + X(0) + X(1)
    observedTable(0)(0) = Y(0).toFloat
    observedTable(0)(1) = X(0).toFloat
    observedTable(1)(0) = Y(1).toFloat
    observedTable(1)(1) = X(1).toFloat

    for (i <- 0 until 2) {
      for (j <- 0 until 2) {
        expectedTable(i)(j) = Math.round(((observedTable(0)(j) + observedTable(1)(j)) * (observedTable(i)(0) + observedTable(i)(1))) / totalSum)
      }
    }

    var chiValue: Double = 0
    for (i <- 0 until 2) {
      for (j <- 0 until 2) {
        chiValue += Math.pow(observedTable(i)(j).toDouble - expectedTable(i)(j).toDouble, 2) / expectedTable(i)(j).toDouble
      }
    }

    chiValue
  }
}
