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

import org.core._

class QualityMeasures(var num_objetivos: Int) extends Serializable {

  /**
    * <p>
    * Defines the quality measures of the individual
    * </p>
    */
  private var v_objetivos = Array.fill[Double](num_objetivos)(0)
  private var cnf: Double = .0
  private var nvars: Double =   .0
  private var length: Float = 0
  private var sensitivity: Float = 0
  private var unus: Float = 0
  private var nsup : Float = 0
  private var supM: Float = 0
  private var supm: Float = 0
  private var suppDiff: Float = -1
  private var gain: Double = .0
  private var TPr: Float = 0
  private var FPr: Float = 1
  private var TNr: Float = 0
  private var AUC: Float = 0
  private var medGeo: Float = 0
  private var growthRate: Float = 0
  private var fisher: Float = 1
  private var hellinger: Float = 0
  private var coverage: Float = 0
  private var strength: Float = 0
  private var jaccard: Float = 0
  var confusionMatrix: ConfusionMatrix = new ConfusionMatrix(1)

  def getJaccard: Float = jaccard
  def setJaccard(v: Float) = {
    jaccard = v
  }


  def getNVars: Double = nvars
  def setNVars(v: Double) = {
    nvars = v
  }

  def getStrength: Float = strength
  def setStrength(v: Float) ={
    strength = v
  }

  /**
    * <p>
    * Returns the num_objetivos of the individual
    * </p>
    *
    * @return Number of objectives
    */
  def getNumObjectives: Int = num_objetivos

  /**
    * <p>
    * Sets the num_objetivos of the individual
    * </p>
    *
    * @param a Number of objectives
    */
  def setNumObjectives(a: Int) {
    num_objetivos = a
  }

  /**
    * <p>
    * Gets the value of the objective pos
    * </p>
    *
    * @param pos Position of the objective
    * @return Value of the objective
    */
  def getObjectiveValue(pos: Int): Double = v_objetivos(pos)

  /**
    * <p>
    * Sets the value of the objective pos
    * </p>
    *
    * @param pos   Position of the objective
    * @param value Value of the objective
    */
  def setObjectiveValue(pos: Int, value: Double) {
    v_objetivos(pos) = value
  }

  /**
    * <p>
    * Gets the value of the confidence
    * </p>
    *
    * @return Value of the confidence
    */
  def getCnf: Double = cnf

  /**
    * <p>
    * Sets the value of the confidence
    * </p>
    *
    * @param acnf Value of the confidence
    */
  def setCnf(acnf: Double) {
    cnf = acnf
  }

  /**
    * <p>
    * Copy in this object the values of qmeasures
    * </p>
    *
    * @param qmeasures Quality measures
    * @param nobj      Number of objectives
    */
  def Copy(qmeasures: QualityMeasures, nobj: Int) {
    for (i <- 0 until nobj) {
      this.v_objetivos(i) = qmeasures.v_objetivos(i)
    }
    this.setCnf(qmeasures.getCnf)
    this.cnf = qmeasures.cnf
    this.length = qmeasures.length
    this.sensitivity = qmeasures.sensitivity
    this.unus = qmeasures.unus
    this.nsup = qmeasures.nsup
    this.supM = qmeasures.supM
    this.supm = qmeasures.supm
    this.suppDiff = qmeasures.suppDiff
    this.gain = qmeasures.gain
    this.TPr = qmeasures.TPr
    this.FPr = qmeasures.FPr
    this.TNr = qmeasures.TNr
    this.AUC = qmeasures.AUC
    this.medGeo = qmeasures.medGeo
    this.growthRate = qmeasures.growthRate
    this.fisher = qmeasures.fisher
    this.hellinger = qmeasures.hellinger
    this.coverage = qmeasures.coverage
    this.jaccard = qmeasures.jaccard
    this.confusionMatrix = qmeasures.confusionMatrix
    this.jaccard = qmeasures.jaccard
  }

  /**
    * <p>
    * Prints the measures
    * </p>
    *
    * @param nFile File to write the quality measures
    * @param AG    Genetic algorithm
    */
  def Print(nFile: String, AG: Genetic) {
    var contents: String  = ""
    contents = "Measures: "
    for(i <- 0 until AG.getNumObjectives) {
        contents += AG.getNObjectives(i) + ": "
        contents += getObjectiveValue(i)
        contents += ", "
    }
    contents += "confidence: " + getCnf
    contents += "\n"
    if (nFile eq "")
      print(contents)
    else
      File.AddtoFile(nFile, contents)
  }

  /**
    * @return the length
    */
  def getLength: Float = length

  /**
    * @param length the length to set
    */
  def setLength(length: Float) {
    this.length = length
  }

  /**
    * @return the sensitivity
    */
  def getSensitivity: Float = sensitivity

  /**
    * @param sensitivity the sensitivity to set
    */
  def setSensitivity(sensitivity: Float) {
    this.sensitivity = sensitivity
  }

  /**
    * @return The normalized unusualness
    */
  def getUnus: Float = unus

  /**
    * @param unus the unus to set
    */
  def setUnus(unus: Float) {
    this.unus = unus
  }

  /**
    * @return the nsup
    */
  def getNsup: Float = nsup

  /**
    * @param nsup the nsup to set
    */
  def setNsup(nsup: Float) {
    this.nsup = nsup
  }

  /**
    * Gets the support of the Majority class (i.e. the support for other classes than actual))
    *
    * @return the supM
    */
  def getSupM: Float = supM

  /**
    * Gets the support of the minority class (i.e. the support of the actual class)
    *
    * @param supM the supM to set
    */
  def setSupM(supM: Float) {
    this.supM = supM
  }

  /**
    * @return the supm
    */
  def getSupm: Float = supm

  /**
    * @param supm the supm to set
    */
  def setSupm(supm: Float) {
    this.supm = supm
  }

  /**
    * @return the suppDiff
    */
  def getSuppDiff: Float = suppDiff

  /**
    * @param suppDiff the suppDiff to set
    */
  def setSuppDiff(suppDiff: Float) {
    this.suppDiff = suppDiff
  }

  /**
    * @return the gain
    */
  def getGain: Double = gain

  /**
    * @param gain the gain to set
    */
  def setGain(gain: Double) {
    this.gain = gain
  }

  /**
    * @return the TPr
    */
  def getTPr = TPr

  /**
    * @param TPr the TPr to set
    */
  def setTPr(TPr: Float) {
    this.TPr = TPr
  }

  /**
    * @return the FPr
    */
  def getFPr = FPr

  /**
    * @param FPr the FPr to set
    */
  def setFPr(FPr: Float) {
    this.FPr = FPr
  }

  /**
    * @return the TNr
    */
  def getTNr = TNr

  /**
    * @param TNr the TNr to set
    */
  def setTNr(TNr: Float) {
    this.TNr = TNr
  }

  /**
    * @return the AUC
    */
  def getAUC = AUC

  /**
    * @param AUC the AUC to set
    */
  def setAUC(AUC: Float) {
    this.AUC = AUC
  }

  /**
    * @return the medGeo
    */
  def getMedGeo: Float = medGeo

  /**
    * @param medGeo the medGeo to set
    */
  def setMedGeo(medGeo: Float) {
    this.medGeo = medGeo
  }

  /**
    * @return the growthRate
    */
  def getGrowthRate: Float = growthRate

  /**
    * @param growthRate the growthRate to set
    */
  def setGrowthRate(growthRate: Float) {
    this.growthRate = growthRate
  }

  /**
    * @return the fisher
    */
  def getFisher: Float = fisher

  /**
    * @param fisher the fisher to set
    */
  def setFisher(fisher: Float) {
    this.fisher = fisher
  }

  /**
    * @return the hellinger
    */
  def getHellinger: Float = hellinger

  /**
    * @param hellinger the hellinger to set
    */
  def setHellinger(hellinger: Float) {
    this.hellinger = hellinger
  }

  /**
    * @return the coverage
    */
  def getCoverage: Float = coverage

  /**
    * @param coverage the coverage to set
    */
  def setCoverage(coverage: Float) {
    this.coverage = coverage
  }
}

