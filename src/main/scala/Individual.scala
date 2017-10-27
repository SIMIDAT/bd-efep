import java.util.BitSet

import org.apache.spark.broadcast.Broadcast

/**
  * Created by angel on 16/02/17.
  */

/**
  * <p>
  *
  * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
  * @version 1.0
  * @since JDK1.5
  *        </p>
  */
trait Individual extends Serializable{
  protected var tamano: Int = 0
  protected var evaluado: Boolean = false
  var cubre: BitSet = null

  protected var rank: Int = 0
  protected var overallConstraintViolation: Double = .0
  protected var numberOfViolatedConstraints: Int = 0
  protected var crowdingDistance: Double = .0

  protected var cubr: Float = 0f
  protected var n_eval: Int = 0
  protected var clas: Int = 0

  var medidas: QualityMeasures = null

  def RndInitInd(Variables: TableVar, neje: Int, clas:Int,  nFile: String)

  def BsdInitInd(Variables: TableVar, porcVar: Float, neje: Int, clas: Int, nFile: String)

  def RndInitIndSmall(Variables: TableVar, neje: Int, subgroup: Individual)

  def BsdInitIndSmall(Variables: TableVar, porcVar: Float, neje: Int, subgroup: Individual)

  def CobInitInd(pop: Population, Variables: TableVar, Examples: TableDat, porcCob: Float, nobj: Int, clas: Int, nFile: String)


  def getClas: Int = clas

  def setClas(value: Int): Unit ={ clas = value }

  /**
    * <p>
    * Returns the position i of the array cubre
    * </p>
    *
    * @param pos Position of example
    * @return Value of the example
    */
  def getIndivCovered(pos: Int): Boolean = cubre.get(pos)

  /**
    * <p>
    * Returns if the individual has been evaluated
    * </p>
    *
    * @return Value of the example
    */
  def getIndivEvaluated: Boolean = evaluado

  /**
    * <p>
    * Sets that the individual has been evaluated
    * </p>
    *
    * @param val Value of the state of the individual
    */
  def setIndivEvaluated(`val`: Boolean) {
    evaluado = `val`
  }

  /**
    * <p>
    * Returns the crowdingDistance of the individual
    * </p>
    *
    * @return Crowding distance of the individual
    */
  def getCrowdingDistance: Double = crowdingDistance

  /**
    * <p>
    * Sets the crowdingDistance of the individual
    * </p>
    *
    * @param cd Crowding distance for the individual
    */
  def setCrowdingDistance(cd: Double) {
    crowdingDistance = cd
  }

  /**
    * <p>
    * Returns the numberOfViolatedConstraints of the individual
    * </p>
    *
    * @return Number of constraints violated
    */
  def getNumberViolatedConstraints: Int = numberOfViolatedConstraints

  /**
    * <p>
    * Sets the numberOfViolatedConstraints of the individual
    * </p>
    *
    * @param novc Number of constraints violated
    */
  def setNumberViolatedConstraints(novc: Int) {
    numberOfViolatedConstraints = novc
  }

  /**
    * <p>
    * Returns the overallConstraintViolation of the individual
    * </p>
    *
    * @return Number over all constraints violated
    */
  def getOverallConstraintViolation: Double = overallConstraintViolation

  /**
    * <p>
    * Sets the overallConstraintViolation of the individual
    * </p>
    *
    * @param ocv Number over all constraints violated
    */
  def setOverallConstraintViolation(ocv: Double) {
    overallConstraintViolation = ocv
  }

  /**
    * <p>
    * Returns the rank of the individual
    * </p>
    *
    * @return Ranking of the individual
    */
  def getRank: Int = rank

  /**
    * <p>
    * Sets the rank of the individual
    * </p>
    *
    * @param arank Ranking of the individual
    */
  def setRank(arank: Int) {
    rank = arank
  }

  /**
    * <p>
    * Returns the number of evaluation when the individual was created
    * </p>
    *
    * @return Number of evalution when the individual was created
    */
  def getNEval: Int = n_eval

  /**
    * <p>
    * Sets the number of evaluation when the individual was created
    * </p>
    *
    * @param eval Number of evaluation when the individual was created
    */
  def setNEval(eval: Int) {
    n_eval = eval
  }

  /**
    * <p>
    * Return the quality measure of the individual
    * </p>
    *
    * @return Quality measures of the individual
    */
  def getMeasures: QualityMeasures = medidas

  /**
    * <p>
    * Gets the value of the quality measure in the position pos
    * </p>
    *
    * @param pos Position of the quality measure
    * @return Value of the quality measure
    */
  def getMeasureValue(pos: Int): Double = medidas.getObjectiveValue(pos)

  /**
    * <p>
    * Sets the value of the quality measure in the position pos
    * </p>
    *
    * @param pos   Position of the quality measure
    * @param value Value of the quality measure
    */
  def setMeasureValue(pos: Int, value: Double) {
    medidas.setObjectiveValue(pos, value)
  }

  /**
    * <p>
    * Sets the value of confidence of the individual
    * </p>
    *
    * @param value Value of confidence of the individual
    */
  def setCnfValue(value: Double) {
    medidas.setCnf(value)
  }

  /**
    * <p>
    * Gets the value of confidence of the individual
    * </p>
    *
    * @return Value of confidence of the individual
    */
  def getCnfValue: Double = medidas.getCnf

  def getCromElem(pos: Int): Int

  def setCromElem(pos: Int, `val`: Int)

  def getCromGeneElem(pos: Int, elem: Int): Boolean

  def setCromGeneElem(pos: Int, elem: Int, `val`: Boolean)

  def getIndivCromCAN: CromCAN

  def getIndivCromDNF: CromDNF

  def copyIndiv(indi: Individual, nobj: Int, neje: Int)

  def evalInd(AG: Genetic, Variables: TableVar, Examples: TableDat)

  def NumInterv(valor: Float, num_var: Int, Variables: Broadcast[TableVar]): Int

  def Print(nFile: String)

  def covers(other: Individual, Variables: TableVar): Boolean

  def length(Variables: TableVar): Int

  def evalExample(Variables: Broadcast[TableVar], data: TypeDat, index: Long): ConfusionMatrix

  /**
    * It calculates the quality measures of the individual and store the objective measures.
    * @param mat
    * @param AG
    * @param Examples
    * @param Variables
    */
  def computeQualityMeasures(mat: ConfusionMatrix, AG: Genetic, Examples: TableDat, Variables: TableVar): Unit = {
    mat.numVarNoInterv /= Examples.getNEx
    //ejCompAntNoClassCrisp = ejCompAntCrisp - ejCompAntClassCrisp // Examples
    // Compute Quality Measures:
    // TPr
    val tpr: Float = if ((mat.tp + mat.fn) != 0)
      mat.tp / (mat.tp + mat.fn)
    else
      0
    medidas.setTPr(tpr)

    // FPr
    val fpr: Float = if ((mat.fp + mat.tn) != 0)
      mat.fp / (mat.fp + mat.tn)
    else
      0

    medidas.setFPr(fpr)

    //LENGTH
    val len: Float = if (mat.tp != 0 && mat.numVarNoInterv < Variables.getNVars)
      1.toFloat / mat.tp
    else
      0

    medidas.setLength(len)

    //SENSITIVITY (ESTO ES TPR !!!)
    medidas.setSensitivity(tpr)
    /*if (Examples.getExamplesClassObj != 0 && mat.numVarNoInterv < Variables.getNVars)
      medidas.setSensitivity(ejCompAntClassCrisp.toFloat / Examples.getExamplesClassObj)
    else
      medidas.setSensitivity(0)*/

    // CONFIDENCE
    val conf: Float = if (mat.tp + mat.fp != 0)
      mat.tp / (mat.tp + mat.fp)
    else
      0

    medidas.setCnf(conf)

    // UNUSUALNESS (NORMALIZED)
    val coverage = (mat.tp + mat.fp) / Examples.getNEx
    val unus : Float = if (mat.tp + mat.fp == 0 || mat.numVarNoInterv >= Variables.getNVars)
      0
    else
      coverage * (conf - ((mat.tp + mat.fn) / Examples.getNEx))

    // normalize unus (Cambiar para adapatar a la representación nueva)
    val classPercent: Float = (mat.tp + mat.fn) / Examples.getNEx
    val minUnus: Float = (1-classPercent) * (0 - classPercent)
    val maxUnus: Float  = classPercent * (1 - classPercent)
    val normUnus: Float = if(maxUnus - minUnus != 0)
      (unus - minUnus) / (maxUnus - minUnus)
    else
      0f
    medidas.setUnus(normUnus)

    // Coverage
    medidas.setCoverage(coverage)
    // GAIN

    val gain: Float = if ((mat.tp + mat.fp == 0) || medidas.getSensitivity == 0)
      if (Examples.getExamplesClass(clas) != 0)
        medidas.getSensitivity * (0 - Math.log10(Examples.getExamplesClass(clas).toFloat / Examples.getNEx).toFloat)
      else
        0
    else
      (medidas.getSensitivity * (Math.log10(medidas.getSensitivity.toDouble / coverage) - Math.log10(Examples.getExamplesClass(clas).toDouble / Examples.getNEx))).toFloat

    medidas.setGain(gain)
    medidas.setSupM(fpr)
    medidas.setSupm(tpr)
    medidas.setSuppDiff(tpr - fpr)

    // GROWTH RATE
    if (tpr != 0 && fpr != 0)
      medidas.setGrowthRate(tpr / fpr)
    else if (tpr != 0 && fpr == 0)
      medidas.setGrowthRate(Float.PositiveInfinity)
    else
      medidas.setGrowthRate(0)

    // FISHER
    val fe = new FisherExact(Examples.getNEx)
    val fisher = fe.getTwoTailedP(mat.tp.toInt, mat.fp.toInt, mat.fn.toInt, mat.tn.toInt).toFloat
    medidas.setFisher(fisher)

    // HELLINGER DISTANCE
    val parte1: Float = if (Examples.getExamplesClass(clas) != 0)
      (Math.sqrt((mat.tp / Examples.getExamplesClass(clas))) - Math.sqrt((mat.fn / Examples.getExamplesClass(clas)))).toFloat
    else
      0

    val parte2: Float = if ((Examples.getNEx - Examples.getExamplesClass(clas)) != 0)
      (Math.sqrt(mat.fp / (Examples.getNEx - Examples.getExamplesClass(clas))) - Math.sqrt(mat.tn / (Examples.getNEx - Examples.getExamplesClass(clas)))).toFloat
    else
      0

    val dh = Math.sqrt(Math.pow(parte1, 2) + Math.pow(parte2, 2)).toFloat
    medidas.setHellinger(dh)

    // AUC
    val success = (1 + (mat.tp / Examples.getExamplesClass(clas)) - (mat.fp / Examples.getExamplesClass(clas))) / 2
    medidas.setAUC(success)

    // Strength
    val strength: Float = if(medidas.getGrowthRate == Float.PositiveInfinity){
      tpr
    } else {
      Math.pow(tpr, 2).toFloat / (tpr + fpr)
    }



    // Sets the objective measures
    for (i <- 0 until AG.getNumObjectives) {
      if (AG.getNObjectives(i).compareToIgnoreCase("AUC") == 0)
        medidas.setObjectiveValue(i, success)
      if (AG.getNObjectives(i).compareToIgnoreCase("COMP") == 0 || AG.getNObjectives(i).compareToIgnoreCase("SENS") == 0 || AG.getNObjectives(i).compareToIgnoreCase("TPR") == 0)
        medidas.setObjectiveValue(i, tpr)
      if (AG.getNObjectives(i).compareToIgnoreCase("CSUP") == 0) {
        val csupport = mat.tp / Examples.getNEx
        // store the values
        val valorComp = csupport
        if (mat.numVarNoInterv >= Variables.getNVars)
          medidas.setObjectiveValue(i, 0)
        else
          medidas.setObjectiveValue(i, valorComp)
      }
      if (AG.getNObjectives(i).compareToIgnoreCase("CCNF") == 0)
        medidas.setObjectiveValue(i, conf)

      if (AG.getNObjectives(i).compareToIgnoreCase("UNUS") == 0)
        if (mat.numVarNoInterv >= Variables.getNVars)
          medidas.setObjectiveValue(i, 0)
        else
          medidas.setObjectiveValue(i, normUnus)

      if (AG.getNObjectives(i).compareToIgnoreCase("ACCU") == 0) {
        val accuracy = (mat.tp + 1) / (mat.tp + mat.fp + Variables.getNClass)
        if (mat.numVarNoInterv >= Variables.getNVars)
          medidas.setObjectiveValue(i, 0)
        else
          medidas.setObjectiveValue(i, accuracy)
      }
      if (AG.getNObjectives(i).compareToIgnoreCase("COVE") == 0)
        if (mat.numVarNoInterv >= Variables.getNVars)
          medidas.setObjectiveValue(i, 0)
        else
          medidas.setObjectiveValue(i, coverage)


      if (AG.getNObjectives(i).compareToIgnoreCase("CONV") == 0) {

        val conviction: Float = if (mat.numVarNoInterv >= Variables.getNVars)
          0
        else if (mat.tp + mat.fp != 0)
          (1 - (mat.tp / Examples.getNEx)) / (1 - (mat.tp / (mat.tp + mat.fp)))
        else 0
        medidas.setObjectiveValue(i, conviction)
      }

      if (AG.getNObjectives(i).compareToIgnoreCase("MEDGEO") == 0) {
        val medgeo: Float = if (mat. numVarNoInterv >= Variables.getNVars) {
          0
        } else {

          val tnr : Float = if ((mat.fp + mat.tn) != 0)
            mat.tn / (mat.fp + mat.tn)
          else
            0

          Math.sqrt(tpr * tnr).toFloat
        }
        medidas.setObjectiveValue(i, medgeo)
      }

      if (AG.getNObjectives(i).compareToIgnoreCase("GROWTH_RATE") == 0)
        medidas.setObjectiveValue(i, medidas.getGrowthRate)
      if (AG.getNObjectives(i).compareToIgnoreCase("FPR") == 0)
        medidas.setObjectiveValue(i, fpr)
      if (AG.getNObjectives(i).compareToIgnoreCase("TPR") == 0)
        medidas.setObjectiveValue(i, tpr)
      if (AG.getNObjectives(i).compareToIgnoreCase("TPR_FPR_DIFF") == 0)
        medidas.setObjectiveValue(i, tpr - fpr)

      if (AG.getNObjectives(i).compareToIgnoreCase("MINIMIZE_FPR") == 0) {
        val valFPR: Float = if (fpr == 0)
          Float.PositiveInfinity
        else
          1.toFloat / fpr

        medidas.setObjectiveValue(i, valFPR)
      }
    }

    // Set the individual as evaluated
    evaluado = true
  }
}


