import org.core.File
import java.util.BitSet

import org.apache.spark.broadcast.Broadcast

/**
  * Created by angel on 16/02/17.
  */
class IndCAN extends Individual {
  /**
    * <p>
    * Defines the individual of the population
    * </p>
    */
  var cromosoma: CromCAN = _ // Individual contents

  /**
    * </p>
    * Creates new instance of Canonical individual
    * </p>
    *
    * @param lenght Lenght of the individual
    * @param neje   Number of examples
    * @param nobj   Number of objectives
    */
  def this(lenght: Int, neje: Int, nobj: Int, clas: Int) {
    this()
    tamano = lenght
    cromosoma = new CromCAN(lenght)
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
    * Creates random instance of Canonical individual
    * </p>
    *
    * @param Variables Variables structure
    * @param neje      Number of exaples
    * @param nFile     File to write the individual
    */
  def RndInitInd(Variables: TableVar, neje: Int, clas: Int, nFile: String) {
    cromosoma.RndInitCrom(Variables) // Random initialization method
    evaluado = false // Individual not evaluated
    cubre.clear(0, neje)
    overallConstraintViolation = 0.0
    numberOfViolatedConstraints = 0
    crowdingDistance = 0.0
    n_eval = 0
    this.clas = clas
  }

  /**
    * <p>
    * Creates biased instance of Canonical individual for small disjunct
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
    cubre.clear(0, neje)
    overallConstraintViolation = 0.0
    numberOfViolatedConstraints = 0
    crowdingDistance = 0.0
    n_eval = 0
    this.clas = clas
  }

  /**
    * <p>
    * Creates random instance of Canonical individual for small disjunct
    * </p>
    *
    * @param Variables Variables structure
    * @param neje      Number of exaples
    * @param subgroup  Subgroup obtained
    */
  def RndInitIndSmall(Variables: TableVar, neje: Int, subgroup: Individual) {

    n_eval = 0
  }

  /**
    * <p>
    * Creates biased instance of Canonical individual
    * </p>
    *
    * @param Variables Variables structure
    * @param porcVar   Percentage of variables to form the individual
    * @param neje      Number of exaples
    * @param subgroup  Subgroup obtained
    */
  def BsdInitIndSmall(Variables: TableVar, porcVar: Float, neje: Int, subgroup: Individual) {
    n_eval = 0
  }

  /**
    * <p>
    * Creates instance of Canonical individual based on coverage
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
    cromosoma.CobInitCrom(pop, Variables, Examples, porcCob, nobj,clas)
    evaluado = false
    cubre.clear(0, cubre.size())
    overallConstraintViolation = 0.0
    numberOfViolatedConstraints = 0
    crowdingDistance = 0.0
    n_eval = 0
    this.clas = clas
  }

  /**
    * <p>
    * Returns the Chromosome
    * </p>
    *
    * @return Chromosome
    */
  def getIndivCrom: CromCAN = cromosoma

  /**
    * <p>
    * Returns the indicated gene of the Chromosome
    * </p>
    *
    * @param pos Position of the gene
    * @return Value of the gene
    */
  def getCromElem(pos: Int): Int = cromosoma.getCromElem(pos)

  /**
    * <p>
    * Returns the value of the indicated gene for the variable
    * </p>
    *
    * @param pos  Position of the variable
    * @param elem Position of the gene
    * @return Value of the gene
    */
  def getCromGeneElem(pos: Int, elem: Int) = false

  /**
    * <p>
    * Sets the value of the indicated gene of the Chromosome
    * </p>
    *
    * @param pos Position of the variable
    * @param val Value of the variable
    */
  def setCromElem(pos: Int, `val`: Int) {
    cromosoma.setCromElem(pos, `val`)
  }

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
  }

  /**
    * <p>
    * Returns the indicated Chromosome
    * </p>
    *
    * @return The canonical Chromosome
    */
  def getIndivCromCAN: CromCAN = cromosoma

  /**
    * <p>
    * Returns the indicated Chromosome
    * </p>
    *
    * @return The DNF Chromosome
    */
  def getIndivCromDNF = null

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
    for (i <- 0 until this.tamano) {
        this.setCromElem(i, a.getCromElem(i))
    }

    this.setIndivEvaluated(a.getIndivEvaluated)
    cubre.clear(0, cubre.size())
    cubre.or(a.cubre)
    this.setClas(a.getClas)

    this.setCrowdingDistance(a.getCrowdingDistance)
    this.setNumberViolatedConstraints(a.getNumberViolatedConstraints)
    this.setOverallConstraintViolation(a.getOverallConstraintViolation)
    this.setRank(a.getRank)
    this.setNEval(a.getNEval)
    //Copy quality measures
    for(i <- 0 until nobj) {
        this.setMeasureValue(i, a.getMeasureValue(i))
    }
    this.setCnfValue(a.getCnfValue)
    this.medidas.Copy(a.medidas,nobj)
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
    // MIRAR ESTO PARA BIG DATA (Usar Map Partitions)
    //var index = 0
    /*val mat = Examples.datZipped.map(ex => {
        // For each example of the dataset
        // Initialization
      val example = ex._1
      val index = ex._2
        var disparoFuzzy = 1f
        var disparoCrisp = 1
        var numVarNoInterv = 0
      val matrix = new ConfusionMatrix(1)
        // Compute all chromosome value
        for (j <- 0 until Variables.getNVars) {
            if (!Variables.getContinuous(j)) {
              // Discrete Variable
              if (cromosoma.getCromElem(j) <= Variables.getMax(j)) {
                // Variable j takes part in the rule
                if ((example.getDat(j) != cromosoma.getCromElem(j)) && (!example.getLost(Variables, j))) {
                  // If chromosome value <> example value, and example value is not a lost value
                  disparoFuzzy = 0
                  disparoCrisp = 0
                }
              } else {
                numVarNoInterv += 1
              } // Variable does not take part
            }
            else {
              // Continuous variable
              if (cromosoma.getCromElem(j) < Variables.getNLabelVar(j)) {
                // Variable takes part in the rule
                // Fuzzy computation
                if (!example.getLost(Variables, j)) {
                  // If the value is not a lost value
                  val pertenencia = Variables.Fuzzy(j, cromosoma.getCromElem(j), example.getDat(j))
                  disparoFuzzy = Utils.Minimum(disparoFuzzy, pertenencia)
                }
                // Crisp computation
                if (!example.getLost(Variables, j))
                  if (NumInterv(example.getDat(j), j, Variables) != cromosoma.getCromElem(j))
                    disparoCrisp = 0
              } else {
                numVarNoInterv += 1
              }
            }
          } // End FOR all chromosome values


        // Update counters and mark example if needed
        //gradoCompAntFuzzy += disparoFuzzy
        if (disparoFuzzy > AG.getAlphaCut) {
          cubre.set(index)

          /*if ((!Examples.getCovered(i)) && (Examples.getClass(i) == Variables.getNumClassObj)) {
            // If example was not previusly covered and belongs to the target class increments the number of covered examples
            gradoCompAntClassNewEjFuzzy += disparoFuzzy
          }*/
        }
        if (disparoFuzzy > AG.getAlphaCut) {
          //ejCompAntCrisp += 1
          cubre.set(index)
          //Calculate the AUC of the rule
          if (example.getClas == clas)
            matrix.tp += 1
          else
            matrix.fp += 1
        } else if (example.getClas == clas) {
          matrix.fn += 1
        } else {
          matrix.tn += 1
        }
      //index += 1
      matrix.numVarNoInterv = numVarNoInterv
      matrix
        // End of cycle for each example
    }).reduce((x,y) => {
      x.fp += y.fp
      x.numVarNoInterv += y.numVarNoInterv
      x.tp += y.tp
      x.fn += y.fn
      x.tn += y.tn
      x
    })

    this.medidas.confusionMatrix = mat

    // Calculate the quality measures
    this.computeQualityMeasures(mat, AG, Examples, Variables)*/
  }

  /**
    * <p>
    * Returns the number of the interval of the indicated variable to which
    * belongs the value. It is performed seeking the greater belonging degree
    * of the value to the fuzzy sets defined for the variable
    * </p>
    *
    * @param value     Value to calculate
    * @param num_var   Number of the variable
    * @param Variables Variables structure
    * @return Number of the interval
    */
  def NumInterv(value: Float, num_var: Int, Variables: Broadcast[TableVar]): Int = {
    var pertenencia: Float = 0
    var new_pert: Float = 0
    var interv: Int = -1
    for (i <- 0 until Variables.value.getNLabelVar(num_var)) {
        new_pert = Variables.value.Fuzzy(num_var, i, value)
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
    var contents: String = null
    cromosoma.Print(nFile)
    contents = "DistanceCrowding " + this.getCrowdingDistance + "\n"
    contents += "Evaluated - " + evaluado + "\n"
    contents += "Evaluacion Generado " + n_eval + "\n\n"
    if (nFile eq "") {
      System.out.print(contents)
    }
    else {
      File.AddtoFile(nFile, contents)
    }
  }



  /**
    * It checks whether this chromosome cover other
    * @param other the chromosome to compare with
    * @return true if this covers other
    */
  def covers(other: Individual, Variables: TableVar): Boolean = {
    val crom =  other.getIndivCromCAN
    // first, check length of patterns
    val lengthThis = this.length(Variables)
    val lengthOther = other.length(Variables)

    if(lengthThis >= lengthOther)
      return false

    for(i <- 0 until this.cromosoma.num_genes){
      if(this.cromosoma.getCromElem(i) != Variables.getNLabelVar(i)){
        // Variable takes part
        if(this.cromosoma.getCromElem(i) != crom.getCromElem(i))
          // Element is not the same, so is not covered
          return false
      }
    }

    // all elements match, it covers
    true
  }

  /**
    * It returns the number of variables that participate in the rule
    * @return
    */
  def length(Variables: TableVar) : Int = {
    var len = 0
    for(i <- 0 until this.cromosoma.num_genes){
      if(this.cromosoma.getCromElem(i) != Variables.getNLabelVar(i)) {
        // Variable takes part
        len += 1
      }
    }

    len
  }

  def evalExample(Variables: Broadcast[TableVar], data: TypeDat, index: Long, fin: Boolean): ConfusionMatrix = {
    new ConfusionMatrix(1)
  }
}
