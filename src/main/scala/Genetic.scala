/**
  * Created by angel on 15/02/17.
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
import java.util._

import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.execution.datasources.parquet.ParquetOptions

class Genetic extends Serializable {
  /**
    * <p>
    * Methods to define the genetic algorithm and to apply operators and
    * reproduction schema
    * </p>
    */
  private var poblac: Array[Population] = _       // Main Population (A population for each class)
  private var best: Array[Population] = _             // Best population
  private var offspring: Array[Population] = _        // Offspring population
  private var union: Array[Population] = _            // Main+Offspring populations
  var indivPerClass: Int = 0

  private var num_objetivos: Int = 0              // Number of objective of the algorithm
  private var n_objetivos: Array[String] = _   // Name of the number of objective
  private var long_poblacion: Int = 0             // Number of individuals of the population
  private var n_eval: Int = 0                     // Number of evaluations per ejecution
  private var prob_cruce: Float = 0f              // Cross probability
  private var prob_mutacion: Float = 0f           // Mutation probability
  private var Gen: Int = 0                        // Number of generations performed by the GA
  private var Trials: Int = 0                     // Number of evaluated chromosomes

  private var RulesRep: String = "CAN"
  private var StrictDominance: String = "no"
  //    private String Tuning = "no";
  //    private String SmallDisjunct = "no";
  private var onePerClass: String = "yes"
  private var tokenCompetition: String = "ByClass" // Valores: ByClass, AllvsAll, No
  private var frontOrdering: String = "ByClass"    // Valores: ByClass, AllvsAll

  private var ReInitCob: String = "no" // Re-initialization based on coverage for the diversity in the model
  private var porcCob: Float = 1f // Biased initialization for individuals in ReInitCob
  private var minCnf: Float = 0f
  private var diversity: String = ""

  private var long_lambda: Int = 0 // Utility param
  private var lambda = Array.ofDim[Double](0,0) // lambda[][]

  private var alpha_cut: Float = 0f


  def getAlphaCut: Float = alpha_cut

  def setAlphaCut(value: Float): Unit = {
    alpha_cut = value
  }
  def setTokenCompetition(tok: String): Unit ={
    this.tokenCompetition = tok
  }

  def setFrontOrdering(front: String): Unit ={
    this.frontOrdering = front
  }

  def getTokenCompetition: String = tokenCompetition
  def getFrontOrdering: String = frontOrdering

  /**
    * <p>
    * Sets the number of objectives
    * </p>
    *
    * @param nobj Number of objectives
    */
  def setNumObjectives(nobj: Int) {
    num_objetivos = nobj
  }

  /**
    * <p>
    * Gets the number of objectives
    * </p>
    *
    * @return Number of objectives
    */
  def getNumObjectives: Int = num_objetivos

  /**
    * <p>
    * Sets the name of an objective
    * </p>
    *
    * @param pos   Position of the objective
    * @param value Name of the objective
    */
  def setNObjectives(pos: Int, value: String) {
    n_objetivos(pos) = value
  }

  /**
    * <p>
    * Initialises the structure for the name of the objectives
    * </p>
    */
  def iniNObjectives() {
    n_objetivos = new Array[String](num_objetivos)
  }

  /**
    * <p>
    * Gets the name of the objective
    * </p>
    *
    * @param pos Position of the objective
    * @return Name of the objective
    */
  def getNObjectives(pos: Int): String = n_objetivos(pos)

  /**
    * <p>
    * Sets the lenght of the population
    * </p>
    *
    * @param value Lenght of the population
    */
  def setLengthPopulation(value: Int) {
    long_poblacion = value
  }

  /**
    * <p>
    * Gets the lenght of the population
    * </p>
    *
    * @return Lenght of the population
    */
  def getLengthPopulation: Int = long_poblacion

  /**
    * <p>
    * Sets the number of evaluations of the algorithm
    * </p>
    *
    * @param value Number of evaluations
    */
  def setNEval(value: Int) {
    n_eval = value
  }

  /**
    * <p>
    * Gets the number of evalutions of the algorithms
    * </p>
    *
    * @return Number of evaluations
    */
  def getNEval: Int = n_eval

  /**
    * <p>
    * Sets the cross probability in the algorithm
    * </p>
    *
    * @param value Cross probability
    */
  def setProbCross(value: Float) {
    prob_cruce = value
  }

  /**
    * <p>
    * Gets the cross probability
    * </p>
    *
    * @return Cross probability
    */
  def getProbCross: Float = prob_cruce

  /**
    * <p>
    * Sets the mutation probability
    * </p>
    *
    * @param value Mutation probability
    */
  def setProbMutation(value: Float) {
    prob_mutacion = value
  }

  /**
    * <p>
    * Gets the mutation probability
    * </p>
    *
    * @return Mutation probability
    */
  def getProbMutation: Float = prob_mutacion

  /**
    * <p>
    * Sets the value of a gene
    * </p>
    *
    * @param value Value of the gene
    */
  def setGen(value: Int) {
    Gen = value
  }

  /**
    * <p>
    * Gets the value of a gene
    * </p>
    *
    * @return Value of the gene
    */
  def getGen = Gen

  /**
    * <p>
    * Sets the number of trials in the algorithm
    * </p>
    *
    * @param value Number of trials
    */
  def setTrials(value: Int) {
    Trials = value
  }

  /**
    * <p>
    * Gets the number of trials in the algorithm
    * </p>
    *
    * @return Number of trials
    */
  def getTrials = Trials

  /**
    * <p>
    * Gets the type of diversity of the algorithm
    * </p>
    *
    * @return Type of diversity
    */
  def getDiversity: String = diversity

  /**
    * <p>
    * Sets the type of diversity of the algorithm
    * </p>
    *
    * @param value Type of diversity
    */
  def setDiversity(value: String) {
    diversity = value
  }

  /**
    * <p>
    * Gets if the algorithm uses re-initialisation based on coverage
    * </p>
    *
    * @return The uses of re-initialisation based on coverage
    */
  def getReInitCob = ReInitCob

  /**
    * <p>
    * Sets the value of re-initialisation based on coverage
    * </p>
    *
    * @param value Value of the re-inisitalisation based on coverage
    */
  def setReInitCob(value: String) {
    ReInitCob = value
  }


  def getOnePerClass: String = onePerClass

  /**
    * <p>
    * Sets the value of onePerClass
    * </p>
    *
    * @param value Value of the parameter
    */
  def setOnePerClass(value: String) {
    onePerClass = value
  }

  /**
    * <p>
    * Gets the percentage of biased initialisation in the re-initialisation
    * based on coverage
    * </p>
    *
    * @return Percentage of biases
    */
  def getPorcCob: Float = porcCob

  /**
    * <p>
    * Sets the percentage of biased initialisation in the re-initialisation
    * based on coverage
    * </p>
    *
    * @param value Value of the percentage
    */
  def setPorcCob(value: Float) {
    porcCob = value
  }

  /**
    * <p>
    * Gets the minimum confidence
    * </p>
    *
    * @return Minimum confidence
    */
  def getMinCnf: Float = minCnf

  /**
    * <p>
    * Sets the minimum confidence
    * </p>
    *
    * @param value Minimum confidence
    */
  def setMinCnf(value: Float) {
    minCnf = value
  }

  /**
    * <p>
    * Gets the rules representation of the algorithm
    * </p>
    *
    * @return Representation of the rules
    */
  def getRulesRep = RulesRep

  /**
    * <p>
    * Sets the rules representation of the algorithm
    * </p>
    *
    * @param value Representation of the rule
    */
  def setRulesRep(value: String) {
    RulesRep = value
  }

  /**
    * <p>
    * Gets if the algorithm considers strict dominance
    * </p>
    *
    * @return The value of strict dominance
    */
  def getStrictDominance = StrictDominance

  /**
    * <p>
    * Sets if the algorithm considers strict dominance
    * </p>
    *
    * @param value The value of strict dominance
    */
  def setStrictDominance(value: String) {
    StrictDominance = value
  }

  /**
    * <p>
    * Joins two populations. In this case, it joins each bucket
    * </p>
    *
    * @param neje Number of examples
    */
  def JoinTemp(neje: Int) {
    var k = 0
    union.foreach(pop => {
      var j = 0
      for (i <- 0 until poblac(k).getNumIndiv) {
        pop.CopyIndiv(i, neje, num_objetivos, poblac(k).getIndiv(i))
        //pop.indivi(i) = poblac(k).getIndiv(i)
      }
      for (i <- poblac(k).getNumIndiv until pop.getNumIndiv) {
        pop.CopyIndiv(i, neje, num_objetivos, offspring(k).getIndiv(j))
        //pop.indivi(i) = offspring(k).getIndiv(j)
        j += 1
      }
      k += 1
    })
  }

  /**
    * <p>
    * Applies the selection schema of the genetic algorithm. Binary tournament
    * selection from elite to inter
    * </p>
    *
    * @return The class and the Position of the individual selected
    */
  def Select(clas: Int) : Int = {

    val opponent1: Int = Randomize.RandintClosed(0, poblac(clas).getNumIndiv - 1)
    var opponent2: Int = opponent1

    while ((opponent2 == opponent1) && (poblac(clas).getNumIndiv > 1))
      opponent2 = Randomize.RandintClosed(0, poblac(clas).getNumIndiv - 1)

    val winner: Int = if (poblac(clas).getIndiv(opponent2).getRank < poblac(clas).getIndiv(opponent1).getRank)
      opponent2
    else if (poblac(clas).getIndiv(opponent2).getRank > poblac(clas).getIndiv(opponent1).getRank)
      opponent1
    else if (poblac(clas).getIndiv(opponent2).getCrowdingDistance > poblac(clas).getIndiv(opponent1).getCrowdingDistance)
      opponent2
    else if (poblac(clas).getIndiv(opponent2).getCrowdingDistance <= poblac(clas).getIndiv(opponent1).getCrowdingDistance)
      opponent1
    else
      opponent1

    // Return the class and the index of the winner
    winner
  }

  /**
    * <p>
    * Cross operator for the genetic algorithm
    * </p>
    *
    * @param Variables Variables structure
    * @param dad       Position of the daddy
    * @param mom       Position of the mummy
    * @param contador  Position to insert the son
    * @param neje      Number of examples
    */
  def CrossMultipoint(Variables: TableVar, clas: Int, dad: Int, mom: Int, contador: Int, neje: Int) {

    var xpoint2 = 0
    // Copy the individuals to cross
    /*for (i <- 0 until Variables.getNVars) {
        if (RulesRep equalsIgnoreCase "can") {
          // CAN Repr.
          offspring(clas).setCromElem(contador * 2, i, 0, poblac(clas).getCromElem(mom, i, 0, RulesRep), RulesRep)
          offspring(clas).setCromElem((contador * 2) + 1, i, 0, poblac(clas).getCromElem(dad, i, 0, RulesRep), RulesRep)
        } else{
          // DNF Repr.
          val number = offspring(clas).getIndivCromDNF(contador * 2).getCromGeneLenght(i)
          for (ii <- 0 to number) {
            offspring(clas).setCromElem((contador * 2), i, ii, poblac(clas).getCromElem(mom, i, ii, RulesRep), RulesRep)
            offspring(clas).setCromElem((contador * 2) + 1, i, ii, poblac(clas).getCromElem(dad, i, ii, RulesRep), RulesRep)
          }
        }
          offspring(clas).getIndiv(contador * 2).setClas(poblac(clas).getClass(mom))
          offspring(clas).getIndiv(contador * 2 + 1).setClas(poblac(clas).getClass(dad))

    }*/
    offspring(clas).CopyIndiv(contador * 2, neje, num_objetivos, poblac(clas).getIndiv(mom))
    offspring(clas).CopyIndiv(contador * 2 + 1, neje, num_objetivos, poblac(clas).getIndiv(dad))

    // Perfom the cross
    val cruce = Randomize.Randdouble(0.0, 1.0)

    if (cruce <= getProbCross) {

      // Generation of the two point of cross
      val xpoint1 = Randomize.RandintClosed(0, Variables.getNVars - 1)
      if (xpoint1 != Variables.getNVars - 1)
        xpoint2 = Randomize.RandintClosed(xpoint1 + 1, Variables.getNVars - 1)
      else
        xpoint2 = Variables.getNVars - 1

      // Cross the parts between both points
      for (i <- xpoint1 to xpoint2) {
          if (RulesRep equalsIgnoreCase "can") {
            offspring(clas).setCromElem(contador * 2, i, 0, poblac(clas).getCromElem(dad, i, 0, RulesRep), RulesRep)
            offspring(clas).setCromElem((contador * 2) + 1, i, 0, poblac(clas).getCromElem(mom, i, 0, RulesRep), RulesRep)

          } else {

            val number = offspring(clas).getIndivCromDNF(contador * 2).getCromGeneLenght(i)

            for (ii <- 0 to number) {
              offspring(clas).setCromElem(contador * 2, i, ii, poblac(clas).getCromElem(dad, i, ii, RulesRep), RulesRep)
              offspring(clas).setCromElem((contador * 2) + 1, i, ii, poblac(clas).getCromElem(mom, i, ii, RulesRep), RulesRep)
            }

            var aux1 = 0
            var aux2 = 0

            for (ii <- 0 until number) {
              if (offspring(clas).getCromElem(contador * 2, i, ii, RulesRep) == 1) {
                aux1 += 1
              }
              if (offspring(clas).getCromElem(contador * 2 + 1, i, ii, RulesRep) == 1) {
                aux2 += 1
              }
            }

            if ((aux1 == number) || (aux1 == 0)) {
              offspring(clas).setCromElem((contador * 2), i, number, 0, RulesRep)
            } else {
              offspring(clas).setCromElem((contador * 2), i, number, 1, RulesRep)
            }
            if ((aux2 == number) || (aux2 == 0)) {
              offspring(clas).setCromElem((contador * 2) + 1, i, number, 0, RulesRep)
            } else {
              offspring(clas).setCromElem((contador * 2) + 1, i, number, 1, RulesRep)
            }
          }
      }

    } /*else {
      offspring(clas).CopyIndiv(contador * 2, neje, num_objetivos, poblac(clas).getIndiv(dad))
      offspring(clas).CopyIndiv((contador * 2) + 1, neje, num_objetivos, poblac(clas).getIndiv(mom))
    }*/
  }


  /**
    * <p>
    * Cross operator for the genetic algorithm
    * </p>
    *
    * @param Variables Variables structure
    * @param dad       Position of the daddy
    * @param mom       Position of the mummy
    * @param contador  Position to insert the son
    * @param neje      Number of examples
    */
  def CrossOnePoint(Variables: TableVar, clas: Int, dad: Int, mom: Int, contador: Int, neje: Int) {

    var xpoint2 = 0
    // Copy the individuals to cross
    offspring(clas).CopyIndiv(contador * 2, neje, num_objetivos, poblac(clas).getIndiv(mom))
    offspring(clas).CopyIndiv(contador * 2 + 1, neje, num_objetivos, poblac(clas).getIndiv(dad))

    // Perfom the cross
    val cruce = Randomize.Randdouble(0.0, 1.0)

    if (cruce <= getProbCross) {

      // Generation of the two point of cross
      val xpoint1 = Randomize.RandintClosed(0, Variables.getNVars)

      // Cross the parts between both points
      for (i <- xpoint1 until Variables.getNVars) {
        if (RulesRep equalsIgnoreCase "can") {
          // CAN Rules
          offspring(clas).setCromElem(contador * 2, i, 0, poblac(clas).getCromElem(dad, i, 0, RulesRep), RulesRep)
          offspring(clas).setCromElem((contador * 2) + 1, i, 0, poblac(clas).getCromElem(mom, i, 0, RulesRep), RulesRep)

        } else {
          // DNF Rules
          val number = offspring(clas).getIndivCromDNF(contador * 2).getCromGeneLenght(i)

          for (ii <- 0 to number) {
            offspring(clas).setCromElem(contador * 2, i, ii, poblac(clas).getCromElem(dad, i, ii, RulesRep), RulesRep)
            offspring(clas).setCromElem((contador * 2) + 1, i, ii, poblac(clas).getCromElem(mom, i, ii, RulesRep), RulesRep)
          }

          var aux1 = 0
          var aux2 = 0

          for (ii <- 0 until number) {
            if (offspring(clas).getCromElem(contador * 2, i, ii, RulesRep) == 1) {
              aux1 += 1
            }
            if (offspring(clas).getCromElem(contador * 2 + 1, i, ii, RulesRep) == 1) {
              aux2 += 1
            }
          }

          if ((aux1 == number) || (aux1 == 0)) {
            offspring(clas).setCromElem(contador * 2, i, number, 0, RulesRep)
          } else {
            offspring(clas).setCromElem(contador * 2, i, number, 1, RulesRep)
          }
          if ((aux2 == number) || (aux2 == 0)) {
            offspring(clas).setCromElem((contador * 2) + 1, i, number, 0, RulesRep)
          } else {
            offspring(clas).setCromElem((contador * 2) + 1, i, number, 1, RulesRep)
          }
        }
      }

    }
  }


  /**
    * <p>
    * Mutates an individual
    * </p>
    *
    * @param Variables Variables structure
    * @param pos       Position of the individual to mutate
    */
  def Mutation(Variables: TableVar, clas: Int, pos: Int) {

    val posiciones = Variables.getNVars

    if (getProbMutation > 0) {
      for (i <- 0 until posiciones) {
          val mutar = Randomize.Randdouble(0.00, 1.00)
          if (mutar <= getProbMutation) {
            val eliminar = Randomize.RandintClosed(0, 10)
            if (eliminar <= 5) {
               // Remove the variable
              if (!Variables.getContinuous(i)) {

                if (RulesRep equalsIgnoreCase "can") {
                  // CAN Repr.
                  offspring(clas).setCromElem(pos, i, 0, Variables.getMax(i).toInt + 1, RulesRep)
                } else {
                  // DNF Repr.
                  val number = Variables.getNLabelVar(i)
                  for (l <- 0 to number) {
                    offspring(clas).setCromElem(pos, i, l, 0, RulesRep)
                  }
                }

              } else {

                if (RulesRep equalsIgnoreCase "can") {
                  // CAN Repr.
                  offspring(clas).setCromElem(pos, i, 0, Variables.getNLabelVar(i), RulesRep)
                } else {
                  // DNF Repr.
                  val number = Variables.getNLabelVar(i)
                  for (l <- 0 to number) {
                    offspring(clas).setCromElem(pos, i, l, 0, RulesRep)
                  }
                }
              }

            } else {
              // Random modification of the variable
              if (!Variables.getContinuous(i)) {
                if (RulesRep equalsIgnoreCase "can") {
                  // CAN Repr.
                  offspring(clas).setCromElem(pos, i, 0, Randomize.RandintClosed(0, Variables.getMax(i).toInt), RulesRep)
                } else {
                  // DNF Repr.
                  val number = Variables.getNLabelVar(i)
                  val cambio = Randomize.RandintClosed(0,number-1)
                  if(offspring(clas).getCromElem(pos, i, cambio, RulesRep)==0){
                    offspring(clas).setCromElem(pos, i, cambio, 1, RulesRep);
                    var aux1 = 0
                    for( ii <- 0 until number){
                      if(offspring(clas).getCromElem(pos, i, ii, RulesRep)==1)
                        aux1 += 1
                    }
                    if((aux1==number)||(aux1==0))
                      offspring(clas).setCromElem(pos, i, number, 0, RulesRep)
                    else
                      offspring(clas).setCromElem(pos, i, number, 1, RulesRep)
                  } else {
                    for(k <- 0 to number)
                      offspring(clas).setCromElem(pos, i, k, 0, RulesRep)
                  }
                }
              } else {
                if (RulesRep equalsIgnoreCase "can") {
                  // CAN Repr.
                  offspring(clas).setCromElem(pos, i, 0, Randomize.RandintClosed(0, Variables.getNLabelVar(i) - 1), RulesRep)
                } else {
                  // DNF Repr.
                  val number = Variables.getNLabelVar(i)
                  val cambio = Randomize.RandintClosed(0, number - 1)
                  // If gene is zero, activate it, else, erase the variable
                  if (offspring(clas).getCromElem(pos, i, cambio, RulesRep) == 0) {
                    offspring(clas).setCromElem(pos, i, cambio, 1, RulesRep)
                    // Check if all values of the variable are 0 or 1 and erase variable if necessary
                    var aux1 = 0
                    for (ii <- 0 until number) {
                      if (offspring(clas).getCromElem(pos, i, ii, RulesRep) == 1) {
                        aux1 += 1
                      }
                    }
                    if ((aux1 == number) || (aux1 == 0)) {
                      // Erase the variable
                      offspring(clas).setCromElem(pos, i, number, 0, RulesRep)
                    } else {
                      // Mark the variable as participant
                      offspring(clas).setCromElem(pos, i, number, 1, RulesRep)
                    }
                  } else {
                    for (k <- 0 to number) {
                      offspring(clas).setCromElem(pos, i, k, 0, RulesRep)
                    }
                  }
                }
              }
            }

            // Marks the chromosome as not evaluated
            offspring(clas).setIndivEvaluated(pos, false)
          }

      }
    }
  }

  /**
    * <p>
    * Composes the genetic algorithm applying the operators
    * </p>
    *
    * @param Variables Variables structure
    * @param Examples  Examples structure
    * @param nFile     File to write the process
    * @return Final Pareto population
    */
  def GeneticAlgorithm(Variables: Broadcast[TableVar], Examples: TableDat, nFile: String,sc:SparkContext): Population = {
    // Parameters initialisation
    var contents: String = null
    val porcVar = 0.25.toFloat
    val porcPob = 0.75.toFloat
    best = new Array[Population](Variables.value.getNClass)

    //indivPerClass = long_poblacion / Variables.getNClass // Individuals per class exactly
    val modulus: Int = long_poblacion % Variables.value.getNClass // If the division is not exact, some classes must have an extra in

    // Auxiliar population to calculate individuals in MapReduce
    val auxPop: Population = new Population(long_poblacion * Variables.value.getNClass, Variables.value.getNVars, num_objetivos, Examples.getNEx, RulesRep, Variables.value)


    // Initialises the population
    poblac = new Array[Population](Variables.value.getNClass)
    offspring = new Array[Population](Variables.value.getNClass)
    union = new Array[Population](Variables.value.getNClass)

    for(i <- 0 until Variables.value.getNClass) {
      // Initialises the population of each class
      poblac(i) = new Population(long_poblacion, Variables.value.getNVars, num_objetivos, Examples.getNEx, RulesRep, Variables.value)
      offspring(i) = new Population(long_poblacion, Variables.value.getNVars, num_objetivos, Examples.getNEx, RulesRep, Variables.value)
      union(i) = new Population(2 * long_poblacion, Variables.value.getNVars, num_objetivos, Examples.getNEx, RulesRep, Variables.value)

      // Biased initialisation of individuals
      poblac(i).BsdInitPob(Variables.value, porcVar, porcPob, Examples.getNEx, i, nFile)
    }

    Trials = 0
    Gen = 0

    //Evaluates the population for the first time
    var count: Int = 0
    poblac.foreach(pop => {
      pop.indivi.foreach(ind =>{
        auxPop.indivi(count) = ind
        count += 1
      })
    })
    Trials += auxPop.evalPop(this, Variables, Examples, sc)



    do {
          // GA General cycle
          Gen += 1
          // Creates offspring and union

          // Apply the genetic operators on individuals of the same class
          for(clas <- poblac.indices) {
            for (conta <- 0 until (poblac(clas).getNumIndiv / 2)) {
              //val clas = Randomize.Randint(0, poblac.length) // Select the class first

              // Select the daddy and mummy
              var dad = 0
              var mum = 0
              if(poblac(clas).getNumIndiv != 2) {
                val dad = Randomize.RandintClosed(0,poblac(clas).getNumIndiv -1) //Select(clas)
                var mum = Randomize.RandintClosed(0,poblac(clas).getNumIndiv -1) //Select(clas)
                while ((dad == mum) && (poblac(clas).getNumIndiv > 1))
                  mum = Randomize.RandintClosed(0,poblac(clas).getNumIndiv -1) //Select(clas)
              } else {
                mum = 1
              }

              // Crosses
              CrossMultipoint(Variables.value, clas, dad, mum, conta, Examples.getNEx)
              // Mutates
              Mutation(Variables.value, clas, conta * 2)
              Mutation(Variables.value, clas, (conta * 2) + 1)
            }

            if (poblac(clas).getNumIndiv % 2 == 1) {
              val dad = Randomize.RandintClosed(0,poblac(clas).getNumIndiv -1) //Select(clas)
              offspring(clas).CopyIndiv(poblac(clas).getNumIndiv - 1, Examples.getNEx, num_objetivos, poblac(clas).getIndiv(dad))
            }
          }

            // Evaluates the offspring
            count = 0
            offspring.foreach(pop => {
              pop.indivi.foreach(ind =>{
                auxPop.indivi(count) = ind
                count += 1
              })
            })
            Trials += auxPop.evalPop(this, Variables, Examples, sc)
            //Trials += offspring.map(p => p.evalPop(this, Variables, Examples,sc)).sum

            // Join population and offspring in union population
            JoinTemp(Examples.getNEx)

          var ranking: Ranking = null
          if(frontOrdering equalsIgnoreCase "ByClass") {

            // Now, for each class in union:
            // - Makes the ranking by the fast non-dominance sorting algorithm
            // - Check if the individuals of the same class evolves. If not:
            // - Join the Pareto front with the best result and perform the token competition.
            // - Re-initialise based on coverage

            for (clas <- union.indices) {
              ranking = new Ranking(union(clas), Variables.value, num_objetivos, Examples.getNEx, RulesRep, StrictDominance)

              //val ranking = new Ranking(union, Variables, num_objetivos, Examples.getNEx, RulesRep, StrictDominance)
              var remain = poblac(clas).getNumIndiv
              var index = 0

              // Obtains the Pareto front
              var front = ranking.getSubfront(index)
              var contador = 0

              while ((remain > 0) && (remain >= front.getNumIndiv)) {
                CalculateDistanceCrowding(front, num_objetivos)

                // Add the individuals of this front
                for (k <- 0 until front.getNumIndiv) {
                  poblac(clas).CopyIndiv(contador, Examples.getNEx, num_objetivos, front.getIndiv(k))
                  contador += 1
                }

                //Decrement remain
                remain = remain - front.getNumIndiv
                //Obtain the next front
                index += 1
                if (remain > 0) {
                  if (ranking.getNumberOfSubfronts == index) {
                    front = new Population(remain, Variables.value.getNVars, num_objetivos, Examples.getNEx, RulesRep, Variables.value)
                    front = ReInitCoverage(front, Variables.value, Examples, nFile)
                    remain = 0
                  } else {
                    front = ranking.getSubfront(index)
                  }
                } // if
              } // while}

              // remain is less than front(index).size, insert only the best one
              if (remain > 0) {
                // front contains individuals to insert
                // Assign diversity function to individuals
                CalculateDistanceCrowding(front, num_objetivos)

                // Sort population with the diversity function
                val ordenado = new Array[Double](front.getNumIndiv)
                val izq = 0
                val der = front.getNumIndiv - 1
                val indices = new Array[Int](front.getNumIndiv)
                for (i <- 0 until front.getNumIndiv) {
                  indices(i) = i
                  ordenado(i) = front.getIndiv(i).getCrowdingDistance
                }
                Utils.OrCrecIndex(ordenado, izq, der, indices)
                var i = front.getNumIndiv - 1

                for (k <- (remain - 1) to 0 by -1) {
                  poblac(clas).CopyIndiv(contador, Examples.getNEx, num_objetivos, front.getIndiv(indices(i)))
                  i -= 1
                  contador += 1
                } // for
                remain = 0
              }
            }

          } else {

              // Perform the front ordering globally
            val P_t: Population = new Population(long_poblacion * 2, Variables.value.getNVars, num_objetivos, Examples.getNEx, RulesRep, Variables.value)
            var count = 0
            // Join all individuals in union into a single population
            union.foreach(p => {
              p.indivi.foreach(ind => {
                P_t.indivi(count) = ind
                count += 1
              })
            })

            /*// Make poblac bigger in order to fit all possible individuals of the ranking
            poblac = poblac.map(p => {
              val aux = new Population(long_poblacion * 2, Variables.getNVars, num_objetivos, Examples.getNEx, RulesRep, Variables)
              aux.setNumIndiv(0)
              aux
            })*/

            // Perform the ranking
            ranking = new Ranking(P_t, Variables.value, num_objetivos, Examples.getNEx, RulesRep, StrictDominance)

            var remain = long_poblacion
            var index = 0
            val indsClass: Array[Int] = Array.fill[Int](Variables.value.getNClass)(0)

            // Obtains the Pareto front
            var front = ranking.getSubfront(index)
            var contador = 0

            while ((remain > 0) && (remain >= front.getNumIndiv)) {
              CalculateDistanceCrowding(front, num_objetivos)

              // Add the individuals of this front
              for (k <- 0 until front.getNumIndiv) {
                /**/ // THIS PRODUCES AN ERROR WHEN THE FRONT IS BIGGER THAN POPULATION SIZE
                // IT IS NECESSARY TO STUDY HOW TO DEAL WITH THIS BEHAVIOUR
                indsClass(front.getClass(k)) += 1
                poblac(front.getClass(k)).CopyIndiv(contador, Examples.getNEx, num_objetivos, front.getIndiv(k))
                contador += 1
              }

              //Decrement remain
              remain = remain - front.getNumIndiv
              //Obtain the next front
              index += 1
              if (remain > 0) {
                if (ranking.getNumberOfSubfronts == index) {
                  front = new Population(remain, Variables.value.getNVars, num_objetivos, Examples.getNEx, RulesRep, Variables.value)
                  front = ReInitCoverage(front, Variables.value, Examples, nFile)
                  remain = 0
                } else {
                  front = ranking.getSubfront(index)
                }
              } // if
            } // while}

            // remain is less than front(index).size, insert only the best one
            if (remain > 0) {
              // front contains individuals to insert
              // Assign diversity function to individuals
              CalculateDistanceCrowding(front, num_objetivos)

              // Sort population with the diversity function
              val ordenado = new Array[Double](front.getNumIndiv)
              val izq = 0
              val der = front.getNumIndiv - 1
              val indices = new Array[Int](front.getNumIndiv)
              for (i <- 0 until front.getNumIndiv) {
                indices(i) = i
                ordenado(i) = front.getIndiv(i).getCrowdingDistance
              }
              Utils.OrCrecIndex(ordenado, izq, der, indices)
              var i = front.getNumIndiv - 1

              for (k <- (remain - 1) to 0 by -1) {
                poblac(front.getClass(indices(i))).CopyIndiv(contador, Examples.getNEx, num_objetivos, front.getIndiv(indices(i)))
                i -= 1
                contador += 1
              } // for
              remain = 0
            }

            // Here is where you check if all buckets have, at least, one individual.
          }


            for(clas <- poblac.indices){
              poblac(clas) = ReInitCoverage(poblac(clas), Variables.value, Examples, nFile)
            // Gets the best population
              /*if (Gen == 1) {
                // if it is the first generation, best is the actual one.
                best(clas) = new Population(poblac(clas).getNumIndiv, Variables.value.getNVars, num_objetivos, Examples.getNEx, RulesRep, Variables.value)


                // Copy poblac in best
                  for(j <- 0 until poblac(clas).getNumIndiv){
                    best(clas).CopyIndiv(j, Examples.getNEx, this.getNumObjectives, poblac(clas).getIndiv(j))
                    best(clas).ult_cambio_eval = 1
                  }

              } else {

                // If not, for each bucket we check if its population evolves
                // and perform the token competition and reinitialisation if necessary
                // If it is not the first generation:
                // See if the population evolves (i.e. it covers new examples)
                  poblac(clas).examplesCoverPopulation(Examples.getNEx, Trials)
                  val pctCambio = (n_eval * 5) / 100
                  // If the population does not evolve for a 5 % of the total evaluations
                  if (Trials - poblac(clas).getLastChangeEval > pctCambio) {
                    // Join the elite population and the pareto front
                    //val join: Population = best(clas).join(ranking.getSubfront(0), Examples, Variables, this)
                    // best is a new population made by the token competition procedure.
                    //val aux = join.tokenCompetition(Examples, Variables, this, true)

                    // Cooperation schema, if the average unuasualness is better than the elite population, overwrite it
                    /*if(best(clas).getGlobalWRAcc > aux.getGlobalWRAcc){
                      best(clas) = aux
                      best(clas).ult_cambio_eval = Gen
                    } else if(best(clas).getGlobalWRAcc == aux.getGlobalWRAcc && aux.getNumIndiv < best(clas).getNumIndiv) {
                      best(clas) = aux
                      best(clas).ult_cambio_eval = Gen
                    }*/
                  }*/

              }

              // Re-initialisation based on coverage
              //if (getReInitCob.compareTo("yes") == 0)


            //} // if

      } while (Trials <= n_eval)

    // Evaluate for the last time the population

    val auxiliar: Population = new Population(long_poblacion * Variables.value.getNClass, Variables.value.getNVars, num_objetivos, Examples.getNEx, RulesRep, Variables.value)
    count = 0
    poblac.foreach(pop => {
      pop.indivi.foreach(ind =>{
        auxiliar.indivi(count) = ind
        count += 1
      })
    })
    Trials += auxiliar.evalPop(this, Variables, Examples, sc)

    // Extract all rules in best in order to have only one population
    /*var k = 0
    val indivsFinal = best.map(p => p.getNumIndiv).sum
    val result = new Population(indivsFinal, Variables.value.getNVars, num_objetivos, Examples.getNEx, RulesRep, Variables.value)
    best.foreach(pob => {
      pob.indivi.foreach(ind =>{
        result.CopyIndiv(k, Examples.getNEx, this.getNumObjectives, ind)
        k += 1
      })
    })
    result.ult_cambio_eval = best.map(i => i.ult_cambio_eval).max*/

    contents = "\nGenetic Algorithm execution finished\n"
      contents += "\tNumber of Generations = " + Gen + "\n"
      contents += "\tNumber of Evaluations = " + Trials + "\n"
    println(contents)
    //File.AddtoFile(nFile, contents)

    //return ranking.getSubfront(0);
    // Mirar si hacer así o no...
    //result.tokenCompetition(Examples, Variables, this)
    //result

    // Return the population after the token competition
    auxiliar.tokenCompetition(Examples, Variables.value, this)
    }

    /**
      * <p>
      * Function of the re-initialisation based on coverage
      * </p>
      *
      * @param poblac    The actual population
      * @param Variables Variables structure
      * @param Examples  Examples structure
      * @param nFile     File to write the process
      * @return The new population for the next generation
      */
    private def ReInitCoverage(poblac: Population, Variables: TableVar, Examples: TableDat, nFile: String): Population = {

      poblac.examplesCoverPopulation(Examples.getNEx, Trials)

      // Checks the difference between the last and actual evaluations
      val porc_cambio = n_eval / 10
      if ((Trials - poblac.getLastChangeEval) >= porc_cambio) {
        //            Vector marcas;
        //            if (RulesRep.compareTo("CAN") == 0) {
        //                marcas = RemoveRepeatedCAN(poblac);
        //            } else {
        //                marcas = RemoveRepeatedDNF(poblac, Variables);
        //            }
        // Generates new individuals

        // First, do token competition to keep high quality rules
        val pob = poblac.tokenCompetition(Examples, Variables, this)

        // Add the individuals in poblac
        var count = 0
        pob.indivi.foreach(ind => {
          poblac.CopyIndiv(count, Examples.getNEx, this.getNumObjectives,ind)
          count += 1
        })

        // fill the remaining individuals by a reinitialization based on coberture
        for(conta <-  count until poblac.getNumIndiv) {
            var indi: Individual = null
              indi = new IndDNF(Variables.getNVars, Examples.getNEx, num_objetivos, Variables, poblac.getIndiv(conta).getClas)

            indi.CobInitInd(poblac, Variables, Examples, porcCob, num_objetivos, poblac.getClass(0), nFile)
            //indi.evalInd(this, Variables, Examples)
            indi.setIndivEvaluated(false)
            indi.setNEval(Trials)
            Trials += 1
            // Copy the individual in the population
            poblac.CopyIndiv(conta, Examples.getNEx, num_objetivos, indi)
            for (j <- 0 until  Examples.getNEx) {
                if (poblac.getIndiv(conta).getIndivCovered(j) && (!poblac.ej_cubiertos.get(j))) {
                  poblac.ej_cubiertos.set(j)
                  poblac.ult_cambio_eval = Trials
                }
            }

        }
      }

      poblac
    }

    /**
      * <p>
      * Calculates the crowding distance
      * </p>
      *
      * @param pop The actual population
      * @param nobj       The number of objectives
      */
    private def CalculateDistanceCrowding(pop: Population, nobj: Int) = {
      val size = pop.getNumIndiv

      if (size == 1) {
        pop.getIndiv(0).setCrowdingDistance(Double.PositiveInfinity)
      } else if (size == 2) {
        pop.getIndiv(0).setCrowdingDistance(Double.PositiveInfinity)
        pop.getIndiv(1).setCrowdingDistance(Double.PositiveInfinity)
      } else {
        // if

        for (i <- 0 until size) {
          pop.getIndiv(i).setCrowdingDistance(0.0)
        }

        var objetiveMaxn = .0
        var objetiveMinn = .0
        var distance = .0
        var ini = 0
        var fin = 0
        for (i <- 0 until nobj) {
          val ordenado = new Array[Double](pop.getNumIndiv)
          val izq = 0
          val der = pop.getNumIndiv - 1
          val indices = new Array[Int](pop.getNumIndiv)
          var medidas = new QualityMeasures(nobj)
          for (j <- 0 until pop.getNumIndiv) {
            indices(j) = j
            medidas = pop.getIndiv(j).getMeasures
            ordenado(j) = medidas.getObjectiveValue(i)
          }

          Utils.OrCrecIndex(ordenado, izq, der, indices)

          ini = indices(0)
          fin = indices(pop.getNumIndiv - 1)

          medidas = pop.getIndiv(ini).getMeasures
          objetiveMinn = medidas.getObjectiveValue(i)
          medidas = pop.getIndiv(fin).getMeasures
          objetiveMaxn = medidas.getObjectiveValue(i)

          //Set de crowding distance
          pop.getIndiv(ini).setCrowdingDistance(Double.PositiveInfinity)
          pop.getIndiv(fin).setCrowdingDistance(Double.PositiveInfinity)
          var a = .0
          var b = .0
          for (j <- 1 until (size - 1)) {

            medidas = pop.getIndiv(indices(j + 1)).getMeasures
            a = medidas.getObjectiveValue(i)
            medidas = pop.getIndiv(indices(j - 1)).getMeasures
            b = medidas.getObjectiveValue(i)
            distance = a - b
            if (distance != 0) distance = distance / (objetiveMaxn - objetiveMinn)
            distance += pop.getIndiv(indices(j)).getCrowdingDistance
            pop.getIndiv(indices(j)).setCrowdingDistance(distance)
          } // for
        } // for
      }
    }


/*
    /**
      * <p>
      * Calculates the knee value. This function is only valid for two objectives
      * </p>
      *
      * @param population The actual population
      * @param nobj       The number of objectives
      */
    private def CalculateKnee(pop: Population, nobj: Int)
    {
      var i = 0
      var j = 0
      var izq = 0
      var der = 0
      var a = .0
      var b = .0
      var c = .0
      val pi2 = 1.5707963267948966
      val size = pop.getNumIndiv
      if (size == 0) return
      if (size == 1) {
        pop.getIndiv(0).setCrowdingDistance(Double.POSITIVE_INFINITY)
        return
      } // if
      if (size == 2) {
        pop.getIndiv(0).setCrowdingDistance(Double.POSITIVE_INFINITY)
        pop.getIndiv(1).setCrowdingDistance(Double.POSITIVE_INFINITY)
        return
      } // if
      i = 0
      while (i < size) {
        {
          pop.getIndiv(i).setCrowdingDistance(0.0)
        }
        {
          i += 1; i - 1
        }
      }
      val ordenado = new Array[Double](size)
      val ordenado2 = new Array[Double](size)
      val indices = new Array[Int](size)
      val indices2 = new Array[Int](size)
      i = 0
      izq = 0
      der = size - 1
      var medidas = new QualityMeasures(nobj)
      j = 0
      while (j < size) {
        {
          indices(j) = j
          medidas = pop.getIndiv(j).getMeasures
          ordenado(j) = medidas.getObjectiveValue(0)
        }
        {
          j += 1; j - 1
        }
      }
      i = 1
      izq = 0
      der = size - 1
      j = 0
      while (j < size) {
        {
          indices2(j) = j
          medidas = pop.getIndiv(j).getMeasures
          ordenado2(j) = medidas.getObjectiveValue(1)
        }
        {
          j += 1; j - 1
        }
      }
      Utils.OrCrecIndex(ordenado, izq, der, indices)
      Utils.OrCrecIndex(ordenado2, izq, der, indices2)
      j = 0
      while (j < pop.getNumIndiv) {
        {
          izq = j - 1
          while (izq >= 0 && pop.getIndiv(indices2(izq)).getMeasures.getObjectiveValue(1) == pop.getIndiv(indices2(j)).getMeasures.getObjectiveValue(1) && pop.getIndiv(indices2(izq)).getMeasures.getObjectiveValue(0) == pop.getIndiv(indices2(j)).getMeasures.getObjectiveValue(0)) {
            izq -= 1; izq + 1
          }
          der = j
          while (der < size && pop.getIndiv(indices2(der)).getMeasures.getObjectiveValue(1) == pop.getIndiv(indices2(j)).getMeasures.getObjectiveValue(1) && pop.getIndiv(indices2(der)).getMeasures.getObjectiveValue(0) == pop.getIndiv(indices2(j)).getMeasures.getObjectiveValue(0)) {
            der += 1; der - 1
          }
          pop.getIndiv(indices2(j)).setCrowdingDistance(pi2)
          if (izq < 0) {
            val valor = pop.getIndiv(indices2(j)).getCrowdingDistance
            pop.getIndiv(indices2(j)).setCrowdingDistance(valor + pi2)
          }
          else {
            b = (pop.getIndiv(indices2(izq)).getMeasures.getObjectiveValue(0) - pop.getIndiv(indices2(j)).getMeasures.getObjectiveValue(0)) / (pop.getIndiv(indices(pop.getNumIndiv - 1)).getMeasures.getObjectiveValue(0) - pop.getIndiv(indices(0)).getMeasures.getObjectiveValue(0))
            c = (pop.getIndiv(indices2(j)).getMeasures.getObjectiveValue(1) - pop.getIndiv(indices2(izq)).getMeasures.getObjectiveValue(1)) / (pop.getIndiv(indices2(pop.getNumIndiv - 1)).getMeasures.getObjectiveValue(1) - pop.getIndiv(indices2(0)).getMeasures.getObjectiveValue(0) * 1.0)
            a = Math.sqrt(b * b + c * c)
            val valor = pop.getIndiv(indices2(j)).getCrowdingDistance
            pop.getIndiv(indices2(j)).setCrowdingDistance(valor + Math.asin(b / a))
          }
          if (der >= pop.getNumIndiv) {
            val valor = pop.getIndiv(indices2(j)).getCrowdingDistance
            pop.getIndiv(indices2(j)).setCrowdingDistance(valor + pi2)
          }
          else {
            b = (pop.getIndiv(indices2(j)).getMeasures.getObjectiveValue(0) - pop.getIndiv(indices2(der)).getMeasures.getObjectiveValue(0)) / (pop.getIndiv(indices(pop.getNumIndiv - 1)).getMeasures.getObjectiveValue(0) - pop.getIndiv(indices(0)).getMeasures.getObjectiveValue(0))
            c = (pop.getIndiv(indices2(der)).getMeasures.getObjectiveValue(0) - pop.getIndiv(indices2(j)).getMeasures.getObjectiveValue(1)) / (pop.getIndiv(indices2(pop.getNumIndiv - 1)).getMeasures.getObjectiveValue(1) - pop.getIndiv(indices2(0)).getMeasures.getObjectiveValue(1) * 1.0)
            a = Math.sqrt(b * b + c * c)
            val valor = pop.getIndiv(indices2(j)).getCrowdingDistance
            pop.getIndiv(indices2(j)).setCrowdingDistance(valor + Math.asin(c / a))
          }
        }
        {
          j += 1; j - 1
        }
      }
    }

    /**
      * <p>
      * Calculates the utility value. This function is only valid for two
      * objectives
      * </p>
      *
      * @param population The actual population
      * @param nobj       The number of objectives
      */
    private def CalculateUtility(pop: Population, nobj: Int)
    {
      val size = pop.getNumIndiv
      if (size == 0) return
      if (size == 1) {
        pop.getIndiv(0).setCrowdingDistance(Double.POSITIVE_INFINITY)
        return
      } // if
      if (size == 2) {
        pop.getIndiv(0).setCrowdingDistance(Double.POSITIVE_INFINITY)
        pop.getIndiv(1).setCrowdingDistance(Double.POSITIVE_INFINITY)
        return
      }
      // if
      var i = 0
      while (i < size) {
        {
          pop.getIndiv(i).setCrowdingDistance(0.0)
        }
        {
          i += 1; i - 1
        }
      }
      val ordenado = new Array[Double](size)
      val ordenado2 = new Array[Double](size)
      val indices = new Array[Int](size)
      val indices2 = new Array[Int](size)
      var izq = 0
      var der = size - 1
      var medidas = new QualityMeasures(nobj)
      var j = 0
      while (j < size) {
        {
          indices(j) = j
          medidas = pop.getIndiv(j).getMeasures
          ordenado(j) = medidas.getObjectiveValue(0)
        }
        {
          j += 1; j - 1
        }
      }
      izq = 0
      der = size - 1
      var j = 0
      while (j < size) {
        {
          indices2(j) = j
          medidas = pop.getIndiv(j).getMeasures
          ordenado2(j) = medidas.getObjectiveValue(1)
        }
        {
          j += 1; j - 1
        }
      }
      Utils.OrCrecIndex(ordenado, izq, der, indices)
      Utils.OrCrecIndex(ordenado2, izq, der, indices2)
      if (pop.getIndiv(indices2(size - 1)).getMeasures.getObjectiveValue(1) != pop.getIndiv(indices2(0)).getMeasures.getObjectiveValue(1)) {
        var i = 0
        while (i < long_lambda) {
          {
            var min = 0
            var k = 0
            while (k < nobj) {
              {
                if (k == 0) min += lambda(i)(k) * ((pop.getIndiv(indices2(0)).getMeasures.getObjectiveValue(k) - pop.getIndiv(indices(0)).getMeasures.getObjectiveValue(k)) / (pop.getIndiv(indices(size - 1)).getMeasures.getObjectiveValue(k) - pop.getIndiv(indices(0)).getMeasures.getObjectiveValue(k)))
                else min += lambda(i)(k) * ((pop.getIndiv(indices2(0)).getMeasures.getObjectiveValue(k) - pop.getIndiv(indices2(0)).getMeasures.getObjectiveValue(k)) / (pop.getIndiv(indices2(size - 1)).getMeasures.getObjectiveValue(k) - pop.getIndiv(indices2(0)).getMeasures.getObjectiveValue(k)))
              }
              {
                k += 1; k - 1
              }
            }
            var posmin = 0
            var second = Double.POSITIVE_INFINITY
            var possecond = -1
            var j = 1
            while (j < size) {
              {
                var temp = 0.0
                var k = 0
                while (k < nobj) {
                  {
                    if (k == 0) temp += lambda(i)(k) * ((pop.getIndiv(indices2(j)).getMeasures.getObjectiveValue(k) - pop.getIndiv(indices(0)).getMeasures.getObjectiveValue(k)) / (pop.getIndiv(indices(size - 1)).getMeasures.getObjectiveValue(k) - pop.getIndiv(indices(0)).getMeasures.getObjectiveValue(k)))
                    else temp += lambda(i)(k) * ((pop.getIndiv(indices2(j)).getMeasures.getObjectiveValue(k) - pop.getIndiv(indices2(0)).getMeasures.getObjectiveValue(k)) / (pop.getIndiv(indices2(size - 1)).getMeasures.getObjectiveValue(k) - pop.getIndiv(indices2(0)).getMeasures.getObjectiveValue(k)))
                  }
                  {
                    k += 1; k - 1
                  }
                }
                if (temp < min) {
                  second = min
                  possecond = posmin
                  min = temp
                  posmin = j
                }
                else if (temp < second) {
                  second = temp
                  possecond = j
                }
              }
              {
                j += 1; j - 1
              }
            }
            val crowding = pop.getIndiv(indices2(posmin)).getCrowdingDistance
            pop.getIndiv(indices2(posmin)).setCrowdingDistance(crowding + (second - min))
          }
          {
            i += 1; i - 1
          }
        }
      }
    }
*/
    /**
      * <p>
      * Eliminates the repeated individuals for canonical representation
      * </p>
      *
      * @param original A population
      * @return A vector which marks the inviduals repeated
      */
    def RemoveRepeatedCAN(original: Population): BitSet = {
      val marcar = new BitSet(original.getNumIndiv)

      var repes = 0

      for(i <- 0 until original.getNumIndiv) {
          val ini: Individual = original.getIndiv(i)
          val cini: CromCAN = ini.getIndivCromCAN
          val tama_cromo = cini.getCromLength
          for (j <- (i + 1) until original.getNumIndiv) {
              val marca: Boolean = marcar.get(j)
              var cuenta_iguales = 0
              val fin = original.getIndiv(j)
              val cfin = fin.getIndivCromCAN
              if (!marca) {
                for(k <- 0 until tama_cromo) {
                    if (cini.getCromElem(k) == cfin.getCromElem(k))
                      cuenta_iguales += 1
                }
              }
              if (((cuenta_iguales == tama_cromo) && (i < j)) || (fin.getRank != 0)) {
                marcar.set(j)
                repes += 1
              }
          }

      }
      marcar
    }

    /**
      * <p>
      * Eliminates the repeated individuals for DNF representation
      * </p>
      *
      * @param original A population
      * @return A vector which marks the inviduals repeated
      */
    def RemoveRepeatedDNF(original: Population, Variables: TableVar) = {
      val marcar = new BitSet(original.getNumIndiv)
      marcar.clear(0, original.getNumIndiv)
      var repes = 0
      var tama_cromo = 0

      for (i <- 0 until original.getNumIndiv) {
          val ini: Individual = original.getIndiv(i)
          val cini: CromDNF = ini.getIndivCromDNF
          tama_cromo = cini.getCromLenght

          for (j <- (i+1) until original.getNumIndiv) {
              val marca = marcar.get(j)
              var cuenta_iguales = 0
              val fin = original.getIndiv(j)
              val cfin = fin.getIndivCromDNF
              if (marca) {
                for (k <- 0 until tama_cromo) {
                    var genes = true
                    val number = cini.getCromGeneLenght(k)
                    if ((cini.getCromGeneElem(k, number) == true) && (cfin.getCromGeneElem(k, number) == true)) {
                      for (l <- 0 until number) {
                          if (cini.getCromGeneElem(k, l) != cfin.getCromGeneElem(k, l))
                            genes = false
                      }
                    }
                    if (genes)
                      cuenta_iguales += 1
                }
              }
              if (((cuenta_iguales == tama_cromo) && (i < j)) || (fin.getRank != 0)) {
                marcar.set(j)
                repes += 1
              }
          }
      }
      marcar
    }

    /**
      * <p>
      * Evaluates the population to obtain the output files of training and test
      * for classification
      * </p>
      *
      * @param nameFileOutputTra  Output quality file
      * @param pob                Array of different populations for rules for different classes
      * @param Examples           The instances of the dataset
      * @param Variables          The variable definitions
      * @param classificationType How it will classify test instances? "max" ->
      *                           Maximum compatibility degree of a rule "sum" -> Maximum sum of
      *                           compatibilities per class "norm_sum" -> Normalized sum of compatibility
      *                           degrees per class
      * @param classNames         Array with names of the classes.
      */
    def CalcPobOutput(nameFileOutputTra: String, pob: Population, Examples: TableDat, Variables: TableVar, classificationType: String, classNames: Array[String], contents: String) = {
      /*
      var pertenencia: Float = 0f
      val pert = .0
      var chrome: CromCAN = null
      var disparoFuzzy: Float = 1
      var num_var_no_interv: Array[Int] = null
      var compatibility: Array[Float] = null
      var normsum: Array[Float] = null

      Files.writeFile(nameFileOutputTra, contents)
      val indivsPerClass: Array[Int] = new Array[Int](Variables.getNClass)
      for(i <- 0 until Variables.getNClass){
        indivsPerClass(i) = Examples.getExamplesClass(i)
      }

      val numRules = pob.getNumIndiv

      //compatibility = new Array[Float](numRules)
      normsum = new Array[Float](Variables.getNClass)
      val reglasDisparadas = Array.fill[Int](Variables.getNClass)(0)
      num_var_no_interv = Array.fill[Int](numRules)(0)

      // Begin computation: Ver esto para Big Data
      // Here is where it is calculated the class of an example
      //for(i <- 0 until Examples.getNEx) {
      Examples.dat.foreach(example =>{
          // For each example
          var counter = 0
          //for (clase <- 0 until pob.length) {
              //for (j <-0 until pob(clase).getNumIndiv) {
              val compatibility = if(RulesRep equalsIgnoreCase "can"){
                // CAN Rule
                pob.indivi.map(ind =>{
                  // For each rule
                  disparoFuzzy = 1
                  //chrome = ind.cromosoma
                chrome = ind.getIndivCromCAN
                  for(k <- 0 until Variables.getNVars) {
                      // For each var of the rule
                      if (!Variables.getContinuous(k)) {
                        /* Discrete Variable */
                        if (chrome.getCromElem(k) <= Variables.getMax(k)) {
                          // Variable j takes part in the rule
                          if (example.getDat(k) != chrome.getCromElem(k) && (!example.getLost(Variables, k)))
                            disparoFuzzy = 0
                        } else {
                          num_var_no_interv(counter) += 1 // Variable does not take part
                        }
                      } else {
                        // Continuous variable
                        if (chrome.getCromElem(k) < Variables.getNLabelVar(k)) {
                          // Variable takes part in the rule
                          // Fuzzy computation
                          if (!example.getLost(Variables, k)) {
                            pertenencia = 0
                            pertenencia = Variables.Fuzzy(k, chrome.getCromElem(k), example.getDat(k))
                            disparoFuzzy = Utils.Minimum(disparoFuzzy, pertenencia)
                          }
                        } else {
                          num_var_no_interv(counter) += 1
                        }
                    }

                  }
                counter += 1
                  // Update globals counters
                  disparoFuzzy
                })
              } else {
                // DNF Rule
                pob.indivi.map(ind => {
                  disparoFuzzy = 1
                  val cromosoma = ind.getIndivCromDNF
                  for(k <- 0 until Variables.getNVars){
                    if(!Variables.getContinuous(k)){
                      // Discrete variable
                      if(cromosoma.getCromGeneElem(k, Variables.getNLabelVar(k))) {
                        if (!cromosoma.getCromGeneElem(k, example.getDat(k).toInt) && !example.getLost(Variables, k)) {
                          disparoFuzzy = 0
                        }
                      } else {
                        // Variable does not take part
                        num_var_no_interv(counter) += 1
                      }
                    } else {
                      // Continuous variable
                      if(cromosoma.getCromGeneElem(k, Variables.getNLabelVar(k))) {
                        var pertenencia: Float = 0f
                        var pert: Float = 0f
                        for(l <- 0 until Variables.getNLabelVar(k)){
                          if(cromosoma.getCromGeneElem(k,l)){
                            pert = Variables.Fuzzy(k,l, example.getDat(k))
                          } else{
                            pert = 0
                          }
                          pertenencia = Utils.Maximum(pertenencia, pert)
                        }
                        disparoFuzzy = Utils.Minimum(disparoFuzzy, pertenencia)
                      } else {
                        num_var_no_interv(counter) += 1
                      }
                    }
                  }
                  counter += 1
                  disparoFuzzy
                })
              }

          var prediccion: Int = 0
          if (classificationType.equalsIgnoreCase("max")) {
            // max rule compatibility
            var max: Float = -1
            var max_actual: Int = 0
            counter = 0
            //for (j <- 0 until pob(clase).getNumIndiv) {
            if (!compatibility.isEmpty){
              val maxi = compatibility.indexOf(compatibility.max)
              prediccion = pob.getClass(maxi)
            } else {
              prediccion = indivsPerClass.indexOf(indivsPerClass.max)
            }
            /*pob.indivi.foreach(ind =>{
                    if (max <= compatibility(counter))
                      if (max == compatibility(counter)) {
                      // If rule has equal compatibilty but less variables, this is the new rule.
                        if (num_var_no_interv(max_actual) > num_var_no_interv(counter)) {
                          max = compatibility(counter)
                          max_actual = counter
                          prediccion = ind.getClas
                        }
                      } else {
                        max = compatibility(counter)
                        max_actual = counter
                        prediccion = ind.getClas
                      }
                    counter += 1
                })*/

            } else if (classificationType.equalsIgnoreCase("sum")) {
            // sum of compatibility per class
               normsum = normsum.map(x => 0f)
                for(j <-0 until pob.getNumIndiv) {
                    normsum(pob.getClass(j)) += compatibility(j)
                }

            /*var max: Float = -1
            for (j <- 0 until normsum.length) {
                if (max < normsum(j)) {
                  max = normsum(j)
                  prediccion = j
                }
            }*/
            if(!compatibility.isEmpty) {
              val maxi = normsum.indexOf(normsum.max)
              prediccion = pob.getClass(maxi)
            } else {
              prediccion = indivsPerClass.indexOf(indivsPerClass.max)
            }

          } else {

            // Normalized sum
            normsum = normsum.map(x => 0f)
            reglasDisparadas.foreach(x => 0)

                for (j <- 0 until pob.getNumIndiv) {
                    normsum(pob.getClass(j)) += compatibility(j)
                    if (compatibility(j) > 0)
                      reglasDisparadas(pob.getClass(j)) += 1
                }

            // Normalize sum
            for(p <- normsum.indices) {
              if(reglasDisparadas(p) > 0)
                normsum(p) /= reglasDisparadas(p)
            }

            if(!compatibility.isEmpty) {
              val maxi = normsum.indexOf(normsum.max)
              prediccion = pob.getClass(maxi)
            } else {
              prediccion = indivsPerClass.indexOf(indivsPerClass.max)
            }
            /*var max: Float = -1
            for (j <- normsum.indices) {
                if (max < normsum(j)) {
                  max = normsum(j)
                  prediccion = j
                }
            }*/
          }

          Files.addToFile(nameFileOutputTra, classNames(example.getClas) + " " + classNames(prediccion) + "\n")
      })
      */
    }
  }

