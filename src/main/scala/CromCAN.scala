/**
  * <p>
  *
  * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
  * @author Imported to Scala By Angel M. Garcia-Vico (University of Jaen) 15/12/17
  * @version 1.0
  * </p>
  */


import java.util

import org.core._
import java.util.BitSet


class CromCAN(var num_genes: Int // Number of genes
             ) extends Serializable
{

  private var cromosoma = new Array[Int](num_genes) // Individual content - integer representation



  /**
    * <p>
    * Random initialisation of an existing chromosome
    * </p>
    *
    * @param Variables Contents the type of the variable, and the number of labels.
    */
  def RndInitCrom(Variables: TableVar): Unit = {
    for(i <- 0 until num_genes) {
        cromosoma(i) = Randomize.RandintClosed(0, Variables.getNLabelVar(i))
    }
  }

  /**
    * <p>
    * Biased Random initialization of an existing chromosome
    * </p>
    *
    * @param Variables Contents the type of the variable, and the number of labels.
    * @param porcVar   Percentage of participating variables
    */
  def BsdInitCrom(Variables: TableVar, porcVar: Float) {

    var num_var = 0
    // This array indicates if every chromosome has been initialised
    val crom_inic = new BitSet(num_genes)



    // Firtly, we obtain the numbero of variable which are in the chromosome
    val numInterv = Randomize.RandintClosed(1, Math.round(porcVar * Variables.getNVars))
    var `var` = 0
    while (`var` < numInterv) {
      num_var = Randomize.RandintClosed(0, num_genes - 1)
      // If the variable is not in the chromosome
      if (!crom_inic.get(num_var)) {
        cromosoma(num_var) = Randomize.RandintClosed(0, Variables.getNLabelVar(num_var) - 1)
        crom_inic.set(num_var)
        `var` += 1
      }
    }
    // Initialise the rest variables

    for (i <- 0 until num_genes) {
        if (!crom_inic.get(i)) cromosoma(i) = Variables.getNLabelVar(i)
    }
  }

  /*/**
    * <p>
    * Random initialization of an existing chromosome for small disjunct
    * </p>
    *
    * @param Variables Contents the type of the variable, and the number of labels.
    */
  def RndInitCromSmall(Variables: TableVar, subgroup: Individual) {
    val sd = subgroup.getIndivCromCAN
    var i = 0
    while (i < num_genes) {
      {
        if (sd.getCromElem(i) < Variables.getNLabelVar(i)) cromosoma(i) = sd.getCromElem(i)
        else cromosoma(i) = Randomize.Randint(0, Variables.getNLabelVar(i))
      }
      {
        i += 1; i - 1
      }
    }
  }

  /**
    * <p>
    * Biased Random initialization of an existing chromosome for small disjunct
    * </p>
    *
    * @param Variables Contents the type of the variable, and the number of labels.
    * @param porcVar   Percentage of participating variables
    */
  def BsdInitCromSmall(Variables: TableVar, porcVar: Float, subgroup: Individual) {
    var num_var = 0
    val sd = subgroup.getIndivCromCAN
    // This array indicates if every chromosome has been initialised
    val crom_inic = new Array[Boolean](num_genes)
    var i = 0
    while (i < num_genes) {
      {
        if (sd.getCromElem(i) < Variables.getNLabelVar(i)) crom_inic(i) = true
        else crom_inic(i) = false
      }
      {
        i += 1; i - 1
      }
    }
    // Firtly, we obtain the numbero of variable which are in the chromosome
    val numInterv = Randomize.Randint(1, porcVar * Variables.getNVars.round)
    var `var` = 0
    while (`var` < numInterv) {
      num_var = Randomize.Randint(0, num_genes - 1)
      // If the variable is not in the chromosome
      if (crom_inic(num_var) == false) {
        cromosoma(num_var) = Randomize.Randint(0, Variables.getNLabelVar(num_var) - 1)
        crom_inic(num_var) = true
        `var` += 1
      }
    }
    // Initialise the rest variables
    var i = 0
    while (i < num_genes) {
      {
        if (crom_inic(i) == false) cromosoma(i) = Variables.getNLabelVar(i)
      }
      {
        i += 1; i - 1
      }
    }
  }*/

  /**
    * <p>
    * Initialization based on coverage
    * </p>
    *
    * @param pop       Main population
    * @param Variables Contents the type of the variable, and the number of labels.
    * @param Examples  Dataset
    * @param porcCob   Percentage of participating variables
    * @param nobj      Number of objectives of the algorithm
    */
  def CobInitCrom(pop: Population, Variables: TableVar, Examples: TableDat, porcCob: Float, nobj: Int, clas: Int): Int = {
  /*
    val crom_inic = new BitSet(num_genes)

    // Number of participating variables in the chromosome
    val numInterv = Randomize.RandintClosed(1, Math.round(porcCob * Variables.getNVars))
    var centi = false
    var aleatorio = 0
    var ii = 0
    while ((!centi) && (ii < Examples.getNEx)) {
      aleatorio = Randomize.RandintClosed(0, Examples.getNEx - 1)
      // OJO! TIENES QUE CAMBIAR LO DE LA CLASE!! NO ES NECESARIO QUE SEAN DE LA MISMA CLASE!
      if (!pop.ej_cubiertos.get(aleatorio) && (Examples.getClass(aleatorio) == clas))
        centi = true
      ii += 1
    }
    var `var` = 0
    while (`var` < numInterv) {
      val num_var: Int = Randomize.RandintClosed(0, num_genes - 1)
      // If the variable is not in the chromosome
      if (!crom_inic.get(num_var)) {
        if (Variables.getContinuous(num_var)) {
          //Continuous variable
          // Put in the correspondent interval
          var pertenencia: Float = 0
          var new_pert: Float = 0
          var interv = Variables.getNLabelVar(num_var)

          for (i: Int <- 0 until Variables.getNLabelVar(num_var)) {
              new_pert = Variables.Fuzzy(num_var, i, Examples.getDat(aleatorio, num_var))
              if (new_pert > pertenencia) {
                interv = i
                pertenencia = new_pert
              }
          }
          cromosoma(num_var) = interv
        }
        else {
          //Discrete variable
          // Put in the correspondent value //
          cromosoma(num_var) = Examples.getDat(aleatorio, num_var).toInt
        }
        crom_inic.set(num_var)
        `var` += 1
      }
    }
    // Initialise the rest variables

    for (i <- 0 until num_genes) {

        if (!crom_inic.get(i)) {
          if (Variables.getContinuous(i))
            cromosoma(i) = Variables.getNLabelVar(i)
          else
            cromosoma(i) = Variables.getMax(i).toInt + 1
        }
    }
    Examples.getClass(aleatorio)
    */
    1
  }

  /**
    * <p>
    * Retuns the value of the gene indicated
    * </p>
    *
    * @param pos Position of the gene
    * @return Value of the gene
    */
  def getCromElem(pos: Int): Int = cromosoma(pos)

  /**
    * <p>
    * Sets the value of the indicated gene of the chromosome
    * </p>
    *
    * @param pos   Position of the gene
    * @param value Value of the gene
    */
  def setCromElem(pos: Int, value: Int) {
    cromosoma(pos) = value
  }

  /**
    * <p>
    * Retuns the gene lenght of the chromosome
    * </p>
    *
    * @return Gets the lenght of the chromosome
    */
  def getCromLength: Int = num_genes

  /**
    * <p>
    * Prints the chromosome genes
    * </p>
    *
    * @param nFile File to write the cromosome
    */
  def Print(nFile: String) {
    var contents: String = "Chromosome: "

    for (i <- 0 until num_genes)
      contents += cromosoma(i) + " "
    contents += "\n"

    if (nFile eq "")
      print(contents)
    else
      File.AddtoFile(nFile, contents)
  }

  def copy: CromCAN = {
    val copia = new CromCAN(cromosoma.length)
    copia.cromosoma = cromosoma.clone()

    copia
  }
}



