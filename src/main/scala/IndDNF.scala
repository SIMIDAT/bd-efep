/**
  * <p>
  *
  * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
  * @version 1.0
  * @since JDK1.5
  *        </p>
  */

import org.core.File
import java.util.BitSet

import org.apache.spark.broadcast.Broadcast
//import keel.Dataset._

class IndDNF extends Individual{

  private var cromosoma: CromDNF = _

/**
  * <p>
  * Creates new instance of Individual
  * </p>
  *
  * @param length    Lenght of the individual
  * @param neje      Number of examples
  * @param nobj      Number of objectives
  * @param Variables Variables structure
  * @param clas      The class of the individual
  */
def this(length: Int, neje: Int, nobj: Int, Variables: TableVar, clas: Int) {
  this()
  tamano = length
  cromosoma = new CromDNF(length, Variables)
  medidas = new QualityMeasures(nobj)
  evaluado = false
  cubre = new BitSet(neje)
  overallConstraintViolation = 0.0
  numberOfViolatedConstraints = 0
  crowdingDistance = 0.0
  n_eval = 0
  this.clas = clas
}

  /**
    * <p>
    * Creates random instance of DNF individual
    * </p>
    *
    * @param Variables Variables structure
    * @param neje      Number of exaples
    * @param nFile     File to write the individual
    */
  def RndInitInd(Variables: TableVar, neje: Int, clas: Int, nFile: String) {
    this.clas = clas
    cromosoma.RndInitCrom // Random initialization method
    evaluado = false // Individual not evaluated
    cubre.clear(0,neje)
    overallConstraintViolation = 0.0
    numberOfViolatedConstraints = 0
    crowdingDistance = 0.0
    n_eval = 0
  }

  /**
    * <p>
    * Creates biased instance of DNF individual
    * </p>
    *
    * @param Variables Variables structure
    * @param porcVar   Percentage of variables to form the individual
    * @param neje      Number of exaples
    * @param nFile     File to write the individual
    */
  def BsdInitInd(Variables: TableVar, porcVar: Float, neje: Int, clas: Int, nFile: String) {
    cromosoma.BsdInitCrom(Variables, porcVar)
    evaluado = false
 this.clas = clas
    cubre.clear(0,neje)
    overallConstraintViolation = 0.0
    numberOfViolatedConstraints = 0
    crowdingDistance = 0.0
    n_eval = 0
  }

  def RndInitIndSmall(Variables: TableVar, neje: Int, subgroup: Individual) {
  }

  def BsdInitIndSmall(Variables: TableVar, porcVar: Float, neje: Int, subgroup: Individual) {
  }

  /**
    * <p>
    * Creates nstance of DNF individual based on coverage
    * </p>
    *
    * @param pop       Actual population
    * @param Variables Variables structure
    * @param Examples  Examples structure
    * @param porcCob   Percentage of variables to form the individual
    * @param nobj      Number of objectives
    * @param nFile     File to write the individual
    */
  def CobInitInd(pop: Population, Variables: TableVar, Examples: TableDat, porcCob: Float, nobj: Int, clas: Int, nFile: String) {
    cromosoma.CobInitCrom(pop, Variables, Examples, porcCob, nobj, clas)
    evaluado = false
    cubre.clear(0, Examples.getNEx)
    this.clas = clas
    overallConstraintViolation = 0.0
    numberOfViolatedConstraints = 0
    crowdingDistance = 0.0
    n_eval = 0
  }

  /**
    * <p>
    * Returns the Chromosome
    * </p>
    *
    * @return Chromosome
    */
  def getIndivCrom: CromDNF = cromosoma

  /**
    * <p>
    * Returns the indicated gene of the Chromosome
    * </p>
    *
    * @param pos  Position of the variable
    * @param elem Position of the gene
    * @return Value of the gene
    */
  def getCromGeneElem(pos: Int, elem: Int): Boolean = cromosoma.getCromGeneElem(pos, elem)

  /**
    * <p>
    * Returns the indicated gene of the Chromosome
    * </p>
    *
    * @param pos Position of the gene
    * @return Value of the gene
    */
  def getCromElem(pos: Int) = 0

  /**
    * <p>
    * Sets the value of the indicated gene of the Chromosome
    * </p>
    *
    * @param pos  Position of the variable
    * @param elem Position of the gene
    * @param val  Value of the variable
    */
  def setCromGeneElem(pos: Int, elem: Int, `val`: Boolean) {
    cromosoma.setCromGeneElem(pos, elem, `val`)
  }

  /**
    * <p>
    * Sets the value of the indicated gene of the Chromosome
    * </p>
    *
    * @param pos Position of the variable
    * @param val Value of the variable
    */
  def setCromElem(pos: Int, `val`: Int) {
  }

  /**
    * <p>
    * Returns the indicated Chromosome
    * </p>
    *
    * @return The DNF Chromosome
    */
  def getIndivCromDNF: CromDNF= cromosoma

  /**
    * <p>
    * Returns the indicated Chromosome
    * </p>
    *
    * @return The canonical Chromosome
    */
  def getIndivCromCAN = null

  /**
    * <p>
    * Copy the indicaded individual in "this" individual
    * </p>
    *
    * @param a    The individual to Copy
    * @param neje Number of examples
    * @param nobj Number of objectives
    */
  def copyIndiv(a: Individual, neje: Int, nobj: Int) {
    this.setClas(a.getClas)
    for (i <- 0 until this.tamano) {
        val number = a.getIndivCromDNF.getCromGeneLenght(i)
        for (j <- 0 to number) {
            this.setCromGeneElem(i, j, a.getCromGeneElem(i, j))
        }
    }
    this.setIndivEvaluated(a.getIndivEvaluated)
    cubre.clear(0,neje)
    cubre.or(a.cubre)
    this.setCrowdingDistance(a.getCrowdingDistance)
    this.setNumberViolatedConstraints(a.getNumberViolatedConstraints)
    this.setOverallConstraintViolation(a.getOverallConstraintViolation)
    this.setRank(a.getRank)

    // Copy the objective measures
    for (i <- 0 until nobj) {
        this.setMeasureValue(i, a.getMeasureValue(i))
    }
    // Copy the rest of measures
    this.medidas.Copy(a.medidas,nobj)

    this.setCnfValue(a.getCnfValue)
    this.setNEval(a.getNEval)

  }

  /**
    * <p>
    * Evaluate a individual. This function evaluates an individual.
    * </p>
    *
    * @param AG        Genetic algorithm
    * @param Variables Variables structure
    * @param Examples  Examples structure
    */
  def evalInd(AG: Genetic, Variables: TableVar, Examples: TableDat) {
    /*var ejCompAntFuzzy: Int = 0              // Number of compatible examples with the antecedent of any class - fuzzy version --- unused
    var ejCompAntCrisp: Int = 0              // Number of compatible examples with the antecedent of any class - crisp version
    var ejCompAntClassFuzzy: Int = 0         // Number of compatible examples (antecedent and class) - fuzzy version
    var ejCompAntClassCrisp: Int = 0         // Number of compatible examples (antecedent and class) - crisp version
    var ejCompAntClassNewFuzzy: Int = 0      // Number of new covered compatible examples (antec and class) - fuzzy version
    var ejCompAntClassNewCrisp: Int = 0      // Number of new covered compatible examples (antec and class) - crisp version
    var gradoCompAntFuzzy: Float = 0           // Total compatibility degree with the antecedent - fuzzy version
    var gradoCompAntClassFuzzy: Float = 0      // Tot compatibility degree with antecedent and class - fuzzy version
    var gradoCompAntClassNewEjFuzzy: Float = 0 // Tot compatibility degree with antecedent and class of new covered examples - fuzzy version
    var disparoFuzzy: Float = 0               // Final compatibility degree of the example with the individual - fuzzy version
    var disparoCrisp: Float = 0               // Final compatibility degree of the example with the individual - crisp version
    var completitud = .0
    var fsupport = .0
    var csupport = .0
    var confianza = .0
    var cconfianza = .0
    var unusualness = .0
    var coverage = .0
    var accuracy = .0
    var significance = .0
    var valorConf = .0
    var valorComp = .0
    // Variables to store the selected measures
    var tp = 0
    var fp = 0
    val ejClase = new Array[Int](Variables.getNClass)
    val cubreClase = new Array[Int](Variables.getNClass)
    var i = 0
    while (i < Variables.getNClass) {
      {
        cubreClase(i) = 0
        ejClase(i) = Examples.getExamplesClass(i)
      }
      {
        i += 1; i - 1
      }
    }
    //int por_cubrir;        // Number of examples of the class not covered yet - for fuzzy version
    var numVarNoInterv = 0
    // Number of variables not taking part in the individual
    var i = 0
    */

    /*
    val mat = Examples.datZipped.map( ex => { // BIG DATA!
        // For each example of the dataset
        // Initialisation
        var disparoFuzzy: Float = 1
        var disparoCrisp: Float = 1
        var numVarNoInterv: Int = 0
        val example: TypeDat = ex._1
        val index = ex._2
        val matrix: ConfusionMatrix = new ConfusionMatrix(1)

        // Compute all chromosome values
        for(j <- 0 until Variables.getNVars) {
            if (!Variables.getContinuous(j)) {
              // Discrete Variable
              if (cromosoma.getCromGeneElem(j, Variables.getNLabelVar(j))) {
                // Variable j does not take part in the rule
                if (!cromosoma.getCromGeneElem(j, example.getDat(j).toInt) && (!example.getLost(Variables, j))) {
                  disparoFuzzy = 0
                  disparoCrisp = 0
                }
              } else {
                numVarNoInterv += 1
              }
            } else {
              // Continuous variable
              if (cromosoma.getCromGeneElem(j, Variables.getNLabelVar(j))) {
                // Variable takes part in the rule
                // Fuzzy computation
                if (!example.getLost(Variables, j)) {
                  var pertenencia: Float = 0
                  var pert: Float = 0
                  for (k <- 0 until Variables.getNLabelVar(j)) {
                      if (cromosoma.getCromGeneElem(j, k))
                        pert = Variables.Fuzzy(j, k, example.getDat(j))
                      else
                        pert = 0
                      pertenencia = Utils.Maximum(pertenencia, pert)
                  }
                  disparoFuzzy = Utils.Minimum(disparoFuzzy, pertenencia)
                }
                // Crisp computation
                if (!example.getLost(Variables, j)) {
                  // If chromosome value <> example value, and example value != lost value (lost value are COMPATIBLES
                  if (!cromosoma.getCromGeneElem(j, NumInterv(example.getDat(j), j, Variables)))
                    disparoCrisp = 0
                }

              } else {
                numVarNoInterv += 1 // Variable does not take part
              }
            }
        }*/
        // Update globals counters
        /*gradoCompAntFuzzy += disparoFuzzy

        if (disparoFuzzy > 0) {
          ejCompAntFuzzy += 1
          if (Examples.getClass(i) eq Variables.getNumClassObj) {
            gradoCompAntClassFuzzy += disparoFuzzy
            ejCompAntClassFuzzy += 1
          }
          if ((!Examples.getCovered(i)) && (Examples.getClass(i) eq Variables.getNumClassObj)) {
            ejCompAntClassNewFuzzy += 1
            gradoCompAntClassNewEjFuzzy += disparoFuzzy
            cubre(i) = true
            Examples.setCovered(i, true)
          }
          //Calculate the AUC of the rule
          if (Examples.getClass(i) eq Variables.getNumClassObj) tp += 1
          else {
            fp += 1; fp - 1
          }
        }*/
    /*
        if (disparoFuzzy > AG.getAlphaCut) {
          cubre.set(index)
          if (example.getClas == this.clas)
            matrix.tp += 1
          else
            matrix.fp += 1
          /*if ((!Examples.getCovered(i)) && (Examples.getClass(i) eq Variables.getNumClassObj)) {
            ejCompAntClassNewCrisp += 1
            cubre(i) = true
            Examples.setCovered(i, true)
          }*/
        } else {
          if(example.getClas == this.clas){
            matrix.fn += 1
          } else {
            matrix.tn += 1
          }
        }
      matrix.numVarNoInterv += numVarNoInterv
      // Return
      matrix
    }).reduce((x,y) => {
      x.fp += y.fp
      x.numVarNoInterv += y.numVarNoInterv
      x.tp += y.tp
      x.fn += y.fn
      x.tn += y.tn
      x
    })

    this.medidas.confusionMatrix = mat
    // Compute the measures
    this.computeQualityMeasures(mat, AG, Examples, Variables) */
  }

  /**
    * <p>
    * Returns the number of the interval of the indicated variable to which belongs
    * the value. It is performed seeking the greater belonging degree of the
    * value to the fuzzy sets defined for the variable
    * </p>
    *
    * @param valor     Value to calculate
    * @param num_var   Number of the variable
    * @param Variables Variables structure
    * @return Number of the interval
    */
  def NumInterv(valor: Float, num_var: Int, Variables: Broadcast[TableVar]): Int = {
    var pertenencia: Float = Float.NegativeInfinity
    var new_pert: Float = Float.NegativeInfinity
    var interv = -1
    for (i <- 0 until Variables.value.getNLabelVar(num_var)) {
        new_pert = Variables.value.Fuzzy(num_var, i, valor)
        if (new_pert > pertenencia) {
          interv = i
          pertenencia = new_pert
        }
    }
    interv
  }

  /**
    * <p>
    * Method to Print the contents of the individual
    * </p>
    *
    * @param nFile File to write the individual
    */
  def Print(nFile: String) {
    cromosoma.Print(nFile)
    var contents = "DistanceCrowding " + this.getCrowdingDistance + "\n"
    contents += "Evaluated - " + evaluado + "\n"
    contents += "Evaluacion Generado " + n_eval + "\n\n"
    if (nFile eq "")
      print(contents)
    else
      File.AddtoFile(nFile, contents)
  }


  def covers(other: Individual, Variables: TableVar): Boolean ={
    val crom = other.getIndivCromDNF
    val lengthThis = this.length(Variables)
    val lengthOther = other.length(Variables)

    if(lengthThis > lengthOther)
      return false

    for(i <- 0 until cromosoma.getCromLenght){
      for(j <- 0 until cromosoma.getCromGeneLenght(i)){
        if(! this.getCromGeneElem(i,j) && crom.getCromGeneElem(i,j)){
          // individual not covered.
          return false
        }
      }
    }

    //return
    true
  }




  def length(Variables: TableVar) : Int = {
    var len = 0
    for(i <- 0 until cromosoma.getCromLenght){
      if(cromosoma.getCromGeneElem(i,Variables.getNLabelVar(i))){
        // Variable takes part
        len += 1
      }
    }
    len
  }



  override def evalExample(Variables: Broadcast[TableVar], data: TypeDat, index: Long): ConfusionMatrix = {
    val mat = new ConfusionMatrix(1)
    var disparoCrisp = 1
    for (i <- 0 until Variables.value.getNVars) {
      if (!Variables.value.getContinuous(i)) {
        // Discrete variables
        if (cromosoma.getCromGeneElem(i, Variables.value.getNLabelVar(i))) {
          if (!cromosoma.getCromGeneElem(i, data.getDat(i).toInt) && !data.getLost(Variables, 0, i)) {
            disparoCrisp = 0
          }
        } else {
          mat.numVarNoInterv += 1
        }
      } else {
        // Continuous variable
        if (cromosoma.getCromGeneElem(i, Variables.value.getNLabelVar(i))) {
          if(!data.getLost(Variables,0,i)){
            if(!cromosoma.getCromGeneElem(i, NumInterv(data.getDat(i),i,Variables))){
              disparoCrisp = 0
            }
          }
        } else {
          mat.numVarNoInterv += 1
        }
      }
    }

    if(disparoCrisp > 0){
      cubre.set(index toInt)
      mat.ejAntCrisp += 1
      //mat.coveredExamples += index
      if(data.getClas == this.clas){
        mat.ejAntClassCrisp += 1
        mat.tp += 1
      } else {
        mat.ejAntNoClassCrisp += 1
        mat.fp += 1
      }
      // cubreClase[Examples.getClass(i)]++; // Como hago yo esto?
      // AQUI TENEMOS UN PROBLEMA CON LOS NUEVOS EJEMPLOS CUBIERTOS

      /*if((!cubiertos.get(index toInt)) && (data.getClas == this.clas)){
        mat.ejAntClassNewCrisp += 1
      }*/
    } else {
      if(data.getClas == this.clas){
        mat.fn += 1
      } else {
        mat.tn += 1
      }
    }

    mat
  }

}
