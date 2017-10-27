/**
  * <p>
  *
  * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
  * @version 1.0
  * @since JDK1.5
  *        </p>
  */

import org.core._

/**
  * Defines the structure and manage the contents of a rule
  * This implementation uses disjunctive formal norm to store the gens.
  * So, variables are codified in binary genes
  */
class CromDNF extends Serializable {

  private var num_genes: Int = 0
  private var cromosoma: Array[Gene] = _

/**
  * <p>
  * Creates new instance of chromosome, no initialization
  * </p>
  *
  * @param length    Length of the chromosome
  * @param Variables Structure of variables of the dataset
  */
  def this(length: Int, Variables: TableVar) {
    this()
    num_genes = length
    this.cromosoma = new Array[Gene](length)
    for (i <- 0 until num_genes) {
        this.cromosoma(i) = new Gene(Variables.getNLabelVar(i))
    }
}
  /**
    * <p>
    * Random initialization of an existing chromosome
    * </p>
    */
  def RndInitCrom() {
    cromosoma.foreach(gene => gene.RndInitGene)
  }

  /**
    * <p>
    * Biased Random initialization of an existing chromosome
    * </p>
    *
    * @param Variables Contents the type of the variable, and the number of labels.
    * @param porcVar   Participating variables in the chromosom
    */
  def BsdInitCrom(Variables: TableVar, porcVar: Float) {

    // This array indicates if every chromosome has been initialised
    val crom_inic = Array.fill[Boolean](num_genes)(false)

    // Firtly, we obtain the numbero of variable which are in the chromosome
    val numInterv = Randomize.RandintClosed(1, Math.round(porcVar * Variables.getNVars))

    for(v <- 0 until numInterv) {
      val num_var = Randomize.RandintClosed(0, num_genes - 1)
      // If the variable is not in the chromosome
      if (!crom_inic(num_var)) {
        cromosoma(num_var).RndInitGene
        crom_inic(num_var) = true
      }
    }

    // Initialise the rest of genes as not participants
    for(i <- crom_inic.indices){
      if(!crom_inic(i)){
        cromosoma(i).NoTakeInitGene
      }
    }
  }

  /**
    * <p>
    * Initialization based on coverage
    * </p>
    *
    * @param pop        Main population
    * @param Variables  Contents the type of the variable, and the number of labels.
    * @param Examples   Dataset
    * @param porcCob    Percentage of participating variables
    * @param nobj Number of objectives of the algorithm
    * @return The value of the class for the example covered, in order to be represented in the chromosome
    */
  def CobInitCrom(pop: Population, Variables: TableVar, Examples: TableDat, porcCob: Float, nobj: Int, clas: Int): Int = {
  /*
    val crom_inic = Array.fill[Boolean](num_genes)(false)

    // Number of participating variables in the chromosome
    val numInterv = Randomize.RandintClosed(1, Math.round(porcCob * Variables.getNVars))

    // Get an example not covered yet to create individuals that covers it
    var centi = false
    var aleatorio: Int = 0
    var ii = 0
    while ((!centi) && (ii < Examples.getNEx)) {
      aleatorio = Randomize.RandintClosed(0, Examples.getNEx - 1)
      if (pop.ej_cubiertos.get(aleatorio) && (Examples.getClass(aleatorio) == clas ))
        centi = true
      ii += 1
    }

    for (v <- 0 until numInterv) {
      val num_var = Randomize.RandintClosed(0, num_genes - 1)

      // If the variable is not in the chromosome
      if (! crom_inic(num_var)) {
        if (Variables.getContinuous(num_var)) {
          //Continuous variable
          // Get the interval of tha variable that correspond to the example value
          var pertenencia: Float = 0
          var interv = Variables.getNLabelVar(num_var)
          for(i <- 0 until Variables.getNLabelVar(num_var)) {
              val new_pert = Variables.Fuzzy(num_var, i, Examples.getDat(aleatorio, num_var))
              if (new_pert > pertenencia) {
                interv = i
                pertenencia = new_pert
              }
          }

          // Initialise the gene on this interval (this interval to 1, the rest to 0)
          val number = Variables.getNLabelVar(num_var)
          cromosoma(num_var).NoTakeInitGene
          this.setCromGeneElem(num_var, interv, true)
          this.setCromGeneElem(num_var, number, true)

        } else {

          //Discrete variable
          // Put in the correspondent value //
          val number = Variables.getNLabelVar(num_var)
          cromosoma(num_var).NoTakeInitGene
          this.setCromGeneElem(num_var, Examples.getDat(aleatorio, num_var).toInt, true)
          this.setCromGeneElem(num_var, number, true)
        }
        crom_inic(num_var) = true
      }
    }

    // Initialise the rest variables
    for(i <- crom_inic.indices){
      if(!crom_inic(i)){
        cromosoma(i).NoTakeInitGene
      }
    }

    // Return the class of the individual covered to be represented in the rule
    Examples.getClass(aleatorio)*/
    1
  }

  /**
    * <p>
    * Retuns the lenght of the chromosome
    * </p>
    *
    * @return Lenght of the chromosome
    */
  def getCromLenght: Int = num_genes

  /**
    * <p>
    * Retuns the gene lenght of the chromosome
    * </p>
    *
    * @return Lenght of the gene
    */
  def getCromGeneLenght(pos: Int): Int = cromosoma(pos).getGeneLenght

  /**
    * <p>
    * Retuns the value of the gene indicated
    * </p>
    *
    * @param pos  Position of the variable
    * @param elem Position of the gene
    */
  def getCromGeneElem(pos: Int, elem: Int): Boolean = cromosoma(pos).getGeneElem(elem)

  /**
    * <p>
    * Sets the value of the indicated gene of the Chromosome
    * </p>
    *
    * @param pos  Position of the variable
    * @param elem Position of the gene
    * @param val  Value to insert
    */
  def setCromGeneElem(pos: Int, elem: Int, `val`: Boolean) {
    cromosoma(pos).setGeneElem(elem, `val`)
  }

  /**
    * <p>
    * Prints the chromosome genes
    * </p>
    *
    * @param nFile File to write the chromosome
    */
  def Print(nFile: String) {
    var contents = "Chromosome: \n"
    for(i <- 0 until num_genes) {
        contents += "Var " + i + ": "

        for (l <- 0 to getCromGeneLenght(i)) {
            contents += this.getCromGeneElem(i, l)
            contents += " "
        }

        contents += "\n"
    }

    if (nFile eq "")
      print(contents)
    else
      File.AddtoFile(nFile, contents)
  }


}
