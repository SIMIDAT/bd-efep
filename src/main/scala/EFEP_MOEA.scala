/**
  * <p>
  * EFEP-MOEA Extracting Fuzzy Emerging Patterns through a Multi-Objective Evolutionary Algorithm
  * </p>
  * <p>
  * Algorithm for the discovery of emergining patterns with high interpretability
  *
  * @author Angel M. Garcia-Vico and Cristobal J. Carmona
  * @version 1.0
  * @since JDK1.5
  *        </p>
  */
//import keel.Algorithms.Subgroup_Discovery.EFEP_MOEA.Calculate.*;
//import keel.Dataset._
import org.core._
import java.util.Vector
import java.io.IOException
import java.io.FileNotFoundException
import java.text.DecimalFormat
import java.util
import java.util.StringTokenizer
import java.util.BitSet

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

object EFEP_MOEA {
  private var seed: Int = 0
  // Seed for the random generator
  private var nombre_alg: String = _
  // Algorithm Name
  private var claseSelec: Boolean = false // Indicates if there is a selected class to run the algorithm or not

  private var input_file_tra: String = _
  // Input mandatory file training
  private var input_file_tst: String = _
  // Input mandatory file test
  private var rule_file: String = _
  // Auxiliary output file for rules
  private var measure_file: String = _
  // Auxiliary output file for quality measures of the rules
  private var seg_file: String = _
  // Auxiliary output file for tracking
  private var qmeasure_file: String = _
  // Output quality measure file
  private var output_file_tra: String = _
  // Output file .tra
  private var output_file_tst: String = _
  // Output file .tst
  private var classification_type: String = _
  // Classification type for .tra and .tst files
  private var confidence_filter: Boolean = false
  // Apply the confidence filter?
  private val alpha: Double = 0.1 // Signification level for exact fisher's test
  private var chi: Boolean = true
  private var max: Boolean = true

  private var numPartitions: Int = 4

  private var confidence_filter_measure: String = _
  private var confidence_filter_qmeasre: String = _
  private var confidence_filter_tra: String = _
  private var confidence_filter_tst: String = _
  private var confidence_filter_rules: String = _

  private var chi_filter_measure: String = _
  private var chi_filter_qmeasure: String = _
  private var chi_filter_tra: String = _
  private var chi_filter_tst: String = _
  private var chi_filter_rules: String = _
  private var minGR: Float = 5
  private var minSupp: Float = 0.02f

  private var max_filter_measure: String = _
  private var max_filter_qmeasure: String = _
  private var max_filter_tra: String = _
  private var max_filter_tst: String = _
  private var max_filter_rules: String = _

  // Structures
  var Data: InstanceSet = _
  var Variables: TableVar = _
  // Set of variables of the dataset and their characteristics
  var Ejemplos: TableDat = _
  // Set of instances of the dataset
  // Preparar luego más adelante otra estructura para los ejemplos en Big Data
  var AG: Genetic = _ // Genetic Algorithm

  private var time: Double = 0

  private var imbalanced: Boolean = false

  /**
    * <p>
    * Auxiliar Gets the name for the output files, eliminating "" and skiping
    * "="
    * </p>
    *
    * @param s String of the output files
    */
  private def GetOutputFiles(s: StringTokenizer) {
    val `val` = s.nextToken
    output_file_tra = s.nextToken.replace('"', ' ').trim
    output_file_tst = s.nextToken.replace('"', ' ').trim
    rule_file = s.nextToken.replace('"', ' ').trim
    measure_file = s.nextToken.replace('"', ' ').trim
    seg_file = s.nextToken.replace('"', ' ').trim
    qmeasure_file = s.nextToken.replace('"', ' ').trim
  }

  /**
    * <p>
    * Auxiliar Gets the name for the input files, eliminating "" and skiping
    * "="
    * </p>
    *
    * @param s String of the input files
    */
  private def GetInputFiles(s: StringTokenizer) {
    val `val` = s.nextToken // skip "="
    //input_file_ref = s.nextToken().replace('"',' ').trim();
    input_file_tra = s.nextToken.replace('"', ' ').trim
    input_file_tra = s.nextToken.replace('"', ' ').trim
    input_file_tst = s.nextToken.replace('"', ' ').trim
  }

  /**
    * <p>
    * Reads the parameters from the file specified and stores the values
    * </p>
    *
    * @param nFile File of parameters
    */
  def ReadParameters(nFile: String) {
    claseSelec = false
    var contents: String = ""
    try {
      var nl = 0
      var fichero: String = null
      var linea: String = null
      var tok: String = null
      var lineasFichero: StringTokenizer = null
      var tokens: StringTokenizer = null

      fichero = File.readFile(nFile)
      //fichero = fichero.toLowerCase() + "\n ";
      lineasFichero = new StringTokenizer(fichero, "\n\r")
      nl = 0
      linea = lineasFichero.nextToken
      while (lineasFichero.hasMoreTokens) {

        nl += 1
        tokens = new StringTokenizer(linea, " ,\t")
        if (tokens.hasMoreTokens) {
          tok = tokens.nextToken
          if (tok.equalsIgnoreCase("algorithm")) nombre_alg = Utils.getParamString(tokens)
          else if (tok.equalsIgnoreCase("inputdata")) GetInputFiles(tokens)
          else if (tok.equalsIgnoreCase("outputdata")) GetOutputFiles(tokens)
          else if (tok.equalsIgnoreCase("RulesRep")) AG.setRulesRep(Utils.getParamString(tokens).toUpperCase)
          else if (tok.equalsIgnoreCase("StrictDominance")) AG.setStrictDominance(Utils.getParamString(tokens).toUpperCase)
          else if (tok.equalsIgnoreCase("seed")) seed = Utils.getParamInt(tokens)
          else if (tok.equalsIgnoreCase("targetClass")) {
            Variables.setNameClassObj(Utils.getParamString(tokens))
            claseSelec = true
          }
          else if (tok.equalsIgnoreCase("nLabels")) Variables.setNLabel(Utils.getParamInt(tokens))
          else if (tok.equalsIgnoreCase("nEval")) AG.setNEval(Utils.getParamInt(tokens))
          else if (tok.equalsIgnoreCase("popLength")) AG.setLengthPopulation(Utils.getParamInt(tokens))
          else if (tok.equalsIgnoreCase("crossProb")) AG.setProbCross(Utils.getParamFloat(tokens))
          else if (tok.equalsIgnoreCase("mutProb")) AG.setProbMutation(Utils.getParamFloat(tokens))
          else if (tok.equalsIgnoreCase("diversity")) AG.setDiversity(Utils.getParamString(tokens).toUpperCase)
          else if (tok.equalsIgnoreCase("ReInitCob")) AG.setReInitCob(Utils.getParamString(tokens))
          else if (tok.equalsIgnoreCase("OnePerClass")) AG.setOnePerClass(Utils.getParamString(tokens))
          else if (tok.equalsIgnoreCase("porcCob")) AG.setPorcCob(Utils.getParamFloat(tokens))
          else if (tok.equalsIgnoreCase("minCnf")) AG.setMinCnf(Utils.getParamFloat(tokens))
          else if (tok.equalsIgnoreCase("Obj1")) {
            AG.setNumObjectives(3)
            AG.iniNObjectives()
            AG.setNObjectives(0, Utils.getParamString(tokens).toUpperCase)
          }
          else if (tok.equalsIgnoreCase("Obj2")) AG.setNObjectives(1, Utils.getParamString(tokens).toUpperCase)
          else if (tok.equalsIgnoreCase("Obj3")) {
            val nil = Utils.getParamString(tokens)
            if (nil.toUpperCase.compareTo("NULL") != 0) AG.setNObjectives(2, nil.toUpperCase)
            else AG.setNumObjectives(2)
          }
          else if (tok.equalsIgnoreCase("classificationType")) classification_type = Utils.getParamString(tokens).toLowerCase
          else if (tok.equalsIgnoreCase("confidenceFilter")) confidence_filter = Utils.getParamString(tokens).equalsIgnoreCase("yes")
          else if (tok.equalsIgnoreCase("TokenCompetition")) AG.setTokenCompetition(Utils.getParamString(tokens))
          else if (tok.equalsIgnoreCase("FrontOrdering")) AG.setFrontOrdering(Utils.getParamString(tokens))
          else if (tok.equalsIgnoreCase("ChiFilter")) chi = Utils.getParamString(tokens).equalsIgnoreCase("yes")
          else if (tok.equalsIgnoreCase("imbalanced"))
            imbalanced = Utils.getParamString(tokens).equalsIgnoreCase("yes")
          else if (tok.equalsIgnoreCase("minGR_Chi"))
            minGR = Utils.getParamFloat(tokens)
          else if (tok.equalsIgnoreCase("minSupp_Chi"))
            minSupp = Utils.getParamFloat(tokens)
          else if (tok.equalsIgnoreCase("alphaCut"))
            AG.setAlphaCut(Utils.getParamFloat(tokens))
          else if (tok.equalsIgnoreCase("numPartitions"))
            numPartitions = Utils.getParamInt(tokens)

          else throw new IOException("Syntax error on line " + nl + ": [" + tok + "]\n")
        }

        linea = lineasFichero.nextToken
      }

      confidence_filter_measure = measure_file.substring(0, measure_file.length - 4) + "_CONF.txt"
      confidence_filter_qmeasre = qmeasure_file.substring(0, qmeasure_file.length - 4) + "_CONF.txt"
      confidence_filter_tra = output_file_tra.substring(0, output_file_tra.length - 4) + "_CONF.txt"
      confidence_filter_tst = output_file_tst.substring(0, output_file_tst.length - 4) + "_CONF.txt"
      confidence_filter_rules = rule_file.substring(0, rule_file.length - 4) + "_CONF.txt"

      chi_filter_measure = measure_file.substring(0, measure_file.length - 4) + "_CHI.txt"
      chi_filter_qmeasure = qmeasure_file.substring(0, qmeasure_file.length - 4) + "_CHI.txt"
      chi_filter_tra = output_file_tra.substring(0, output_file_tra.length - 4) + "_CHI.txt"
      chi_filter_tst = output_file_tst.substring(0, output_file_tst.length - 4) + "_CHI.txt"
      chi_filter_rules = rule_file.substring(0, rule_file.length - 4) + "_CHI.txt"

      max_filter_measure = measure_file.substring(0, measure_file.length - 4) + "_MAX.txt"
      max_filter_qmeasure = qmeasure_file.substring(0, qmeasure_file.length - 4) + "_MAX.txt"
      max_filter_tra = output_file_tra.substring(0, output_file_tra.length - 4) + "_MAX.txt"
      max_filter_tst = output_file_tst.substring(0, output_file_tst.length - 4) + "_MAX.txt"
      max_filter_rules = rule_file.substring(0, rule_file.length - 4) + "_MAX.txt"

    } catch {

      case e: FileNotFoundException => {
        System.err.println(e + " Parameter file")
      }
      case e: IOException => {
        System.err.println(e + "Aborting program")
        System.exit(-1)
      }
    }
    File.writeFile(seg_file, "")
    contents = "--------------------------------------------\n"
    contents += "|              Parameters Echo             |\n"
    contents += "--------------------------------------------\n"
    contents += "Algorithm name: " + nombre_alg + "\n"
    contents += "Input file name training: " + input_file_tra + "\n"
    contents += "Rules file name: " + rule_file + "\n"
    contents += "Tracking file name: " + seg_file + "\n"
    contents += "Representation of the Rules: " + AG.getRulesRep + "\n"
    contents += "Strict dominance: " + AG.getStrictDominance + "\n"
    contents += "Random generator seed: " + seed + "\n"
    contents += "Selected class of the target variable: "
    if (claseSelec) contents += Variables.getNameClassObj + "\n"
    else contents += "not established\n"
    contents += "Number of labels for the continuous variables: " + Variables.getNLabel + "\n"
    contents += "Number of evaluations: " + AG.getNEval + "\n"
    contents += "Number of individuals in the Population: " + AG.getLengthPopulation + "\n"
    contents += "Cross probability: " + AG.getProbCross + "\n"
    contents += "Mutation probability: " + AG.getProbMutation + "\n"
    contents += "Diversity: " + AG.getDiversity + "\n"
    contents += "Perform ReInitCob: " + AG.getReInitCob + "\n"
    contents += "Percentage of the ReInitCob: " + AG.getPorcCob + "\n"
    contents += "Minimum confidence threshold: " + AG.getMinCnf + "\n"
    contents += "One per Class: " + AG.getOnePerClass + "\n"
    contents += "Number of objetives: " + AG.getNumObjectives + "\n"

    for (i <- 0 until AG.getNumObjectives) {
        contents += "\tObjetive " + i + ": " + AG.getNObjectives(i) + "\n"
    }
    File.AddtoFile(seg_file, contents)
  }

  /**
    * <p>
    * Read the dataset and stores the values
    * </p>
    */
  @throws[IOException]
  def CaptureDataset(file: String, isTrain: Boolean) {
    try {
      // Declaration of the dataset and load in memory
      Data = new InstanceSet
      Data.readSet(file, isTrain)
      // Check that there is only one output variable
      if (Attributes.getOutputNumAttributes > 1) {
        println("This algorithm can not process MIMO datasets")
        println("All outputs but the first one will be removed")
      }
      var noOutputs = false
      if (Attributes.getOutputNumAttributes < 1) {
        println("This algorithm can not process datasets without outputs")
        println("Zero-valued output generated")
        noOutputs = true
      }
      // Chek that the output variable is nominal
      if (Attributes.getOutputAttribute(0).getType != Attribute.NOMINAL) {
        // If the output variables is not enumeratad, the algorithm can not be run
        try
          throw new IllegalAccessException("Finish")

        catch {
          case term: IllegalAccessException => {
            System.err.println("Target variable is not a discrete one.")
            System.err.println("Algorithm can not be run.")
            System.out.println("Program aborted.")
            System.exit(-1)
          }
        }
      }
      // Set the number of classes of the output attribute - this attribute must be nominal
      Variables.setNClass(Attributes.getOutputAttribute(0).getNumNominalValues)
      // Screen output of the output variable and selected class
      println("Output variable: " + Attributes.getOutputAttribute(0).getName)
      // Creates the space for the variables and load the values.
      //Variables.Load(Attributes.getInputNumAttributes)
      // Setting and file writing of fuzzy sets characteristics for continuous variables
      val nombreF = seg_file
      Variables.InitSemantics(nombreF)
      // Creates the space for the examples and load the values
      //Ejemplos.Load(Data, Variables)

    } catch {
      case e: Exception => {
        println("DBG: Exception in readSet")
        e.printStackTrace()
      }
    }
  }

  /**
    * <p>
    * Dataset file writting to output file
    * </p>
    *
    * @param filename Output file
    */
  def WriteOutDataset(filename: String) {
    var contents: String = ""
    contents = Data.getHeader
    contents += Attributes.getInputHeader + "\n"
    contents += Attributes.getOutputHeader + "\n\n"
    contents += "@data \n"
    File.writeFile(filename, contents)
  }

  /**
    * <p>
    * Dataset file writting to tracking file
    * </p>
    *
    * @param filename Tracking file
    */
  def WriteSegDataset(filename: String) {
    var contents = "\n"
    contents += "--------------------------------------------\n"
    contents += "|               Dataset Echo               |\n"
    contents += "--------------------------------------------\n"
    contents += "Number of examples: " + Ejemplos.getNEx + "\n"
    contents += "Number of variables: " + Variables.getNVars + "\n"
    contents += Data.getHeader + "\n"
    if (filename ne "") File.AddtoFile(filename, contents)
  }

  /**
    * <p>
    * Writes the rule and the quality measures
    * </p>
    *
    * @param pob              Actual population
    * @param nobj             Number of objectives
    * @param fileRule         File of rules
    * @param fileQuality      File of quality measures
    * @param cab_measure_file File of header measure
    */
  def WriteRule(pob: Population, nobj: Int, fileRule: String, fileQuality: String, cab_measure_file: String /*, Vector vmarca*/) {
    var NumRules = 0
    val marca = 0
    val aux1_ini = fileRule.substring(0, fileRule.lastIndexOf("."))
    val aux1_fin = fileRule.substring(fileRule.lastIndexOf(".") + 1, fileRule.length)
    val aux2_ini = fileQuality.substring(0, fileQuality.lastIndexOf("."))
    val aux2_fin = fileQuality.substring(fileQuality.lastIndexOf(".") + 1, fileQuality.length)
      //            double cnf_min = AG.getMinCnf();
      //            for (int cnf = 0; cnf <= 3; cnf++) {
      //                String cnfVal = String.valueOf(Math.round(cnf_min * 10));
      //                File.writeFile(aux1_ini + "_0" + cnfVal + "." + aux1_fin, "");
      //                File.writeFile(aux2_ini + "_0" + cnfVal + "." + aux2_fin, cab_measure_file);
      //                cnf_min += 0.10;
      //            }
      File.writeFile(aux1_ini + "." + aux1_fin, "")
      File.writeFile(aux2_ini + "." + aux2_fin, cab_measure_file)

    val cnf_min = AG.getMinCnf
    //for (int cnf = 0; cnf <= 3; cnf++) {
    //String cnfVal = String.valueOf(Math.round(cnf_min * 10));
    NumRules = -1
    for (aux <- 0 until pob.getNumIndiv) {
      // Write the quality measures of the rule in "measure_file"
      var Result = new QualityMeasures(nobj)
      Result = pob.getIndiv(aux).getMeasures
      //marca = (Integer) vmarca.get(aux);
      if ( /*!confidence_filter || confidence_filter && ((Result.getCnf() > cnf_min) && (marca != 1) )*/ true) {
        // Unmark 'true' and comment the rest to apply the confidence filter

        NumRules += 1
        // Rule File
        var contents = "GENERATED RULE " + NumRules + "\n"
        contents += "\tAntecedent\n"
        if(AG.getRulesRep equalsIgnoreCase "can") {
          //Canonical rules
          val regla = pob.getIndivCromCAN(aux)
          var auxi = 0
          for (auxi <- 0 until Variables.getNVars) {
            if (!Variables.getContinuous(auxi)) {
              // Discrete variable
              if (regla.getCromElem(auxi) < Variables.getNLabelVar(auxi)) {
                contents += "\t\tVariable " + Attributes.getInputAttribute(auxi).getName + " = "
                contents += Attributes.getInputAttribute(auxi).getNominalValue(regla.getCromElem(auxi)) + "\n"
              }
            }
            else {
              // Continuous variable
              if (regla.getCromElem(auxi) < Variables.getNLabelVar(auxi)) {
                contents += "\t\tVariable " + Attributes.getInputAttribute(auxi).getName + " = "
                contents += "Label " + regla.getCromElem(auxi)
                contents += " \t (" + Variables.getX0(auxi, regla.getCromElem(auxi).toInt)
                contents += " " + Variables.getX1(auxi, regla.getCromElem(auxi).toInt)
                contents += " " + Variables.getX3(auxi, regla.getCromElem(auxi).toInt) + ")\n"
              }
            }
          }
        } else {
          // DNF Rules
          val regla: CromDNF = pob.getIndivCromDNF(aux)

          for (i <- 0 until Variables.getNVars) {
            if (regla.getCromGeneElem(i, Variables.getNLabelVar(i))) {
              if (!Variables.getContinuous(i)) {
                // Discrete variable
                contents += "\tVariable " + Attributes.getInputAttribute(i).getName + " = "
                for (j <- 0 until Variables.getNLabelVar(i)) {
                  if (regla.getCromGeneElem(i, j)) {
                    contents += Attributes.getInputAttribute(i).getNominalValue(j) + " "
                  }
                }
                contents += "\n"
              } else {
                // Continuous variable
                contents += "\tVariable " + Attributes.getInputAttribute(i).getName + " = "
                for (j <- 0 until Variables.getNLabelVar(i)) {
                  if (regla.getCromGeneElem(i, j)) {
                    contents += "Label " + j
                    contents += " (" + Variables.getX0(i, j)
                    contents += " " + Variables.getX1(i, j)
                    contents += " " + Variables.getX3(i, j) + ")\t"
                  }
                }
                contents += "\n";
              }
            }
          }
        }

        contents += "\tConsecuent: " + Attributes.getOutputAttribute(0).getNominalValue(pob.getIndiv(aux).getClas) + "\n\n"
        // File.AddtoFile(aux1_ini + "_0" + cnfVal + "." + aux1_fin, contents);
        File.AddtoFile(aux1_ini + "." + aux1_fin, contents)
        val sixDecimals = new DecimalFormat("0.000000")
        contents = "" + Variables.getNumClassObj
        for (auxi <- 0 until AG.getNumObjectives) {
          val value = Result.getObjectiveValue(auxi)
          if (value == Double.PositiveInfinity) // if growth rate is an objective
            contents += "\tINFINITY"
          else
            contents += "\t" + sixDecimals.format(Result.getObjectiveValue(auxi))
        }
        contents += "\t" + sixDecimals.format(Result.getCnf) + "\n"
        //File.AddtoFile(aux2_ini + "_0" + cnfVal + "." + aux2_fin, contents);
        File.AddtoFile(aux2_ini + "." + aux2_fin, contents)
      }
    }
    //  cnf_min = cnf_min + 0.10;
    //}
  }

  /**
    * <p>
    * Writes the rule and the quality measures
    * </p>
    *
    * @param pob              Actual population
    * @param max              Position of the individual with the maximum confidence
    * @param nobj             Number of objectives
    * @param fileRule         File of rules
    * @param fileQuality      File of quality measures
    * @param cab_measure_file File of header measure
    */
  def WriteRuleMax(pob: Population, max: Int, nobj: Int, fileRule: String, fileQuality: String, cab_measure_file: String, filter: Int) {
    val aux1_ini = fileRule.substring(0, fileRule.lastIndexOf("."))
    val aux1_fin = fileRule.substring(fileRule.lastIndexOf(".") + 1, fileRule.length)
    val aux2_ini = fileQuality.substring(0, fileQuality.lastIndexOf("."))
    val aux2_fin = fileQuality.substring(fileQuality.lastIndexOf(".") + 1, fileQuality.length)
    var cnf_min = 0.0
    cnf_min = AG.getMinCnf
    val cnfVal = String.valueOf(cnf_min * 10.round + filter)
    // Write the quality measures of the rule in "measure_file"
    var Result = new QualityMeasures(nobj)
    Result = pob.getIndiv(max).getMeasures
    // Rule File
    var contents = "GENERATED RULE " + 0 + "\n"
    contents += "\tAntecedent\n"
    val regla = pob.getIndivCromCAN(max)

    for (auxi <- 0 until Variables.getNVars) {
      if (!Variables.getContinuous(auxi)) {
        // Discrete variable
        if (regla.getCromElem(auxi) < Variables.getNLabelVar(auxi)) {
          contents += "\t\tVariable " + Attributes.getInputAttribute(auxi).getName + " = "
          contents += Attributes.getInputAttribute(auxi).getNominalValue(regla.getCromElem(auxi)) + "\n"
        }
      }
      else {
        // Continuous variable
        if (regla.getCromElem(auxi) < Variables.getNLabelVar(auxi)) {
          contents += "\t\tVariable " + Attributes.getInputAttribute(auxi).getName + " = "
          contents += "Label " + regla.getCromElem(auxi)
          contents += " \t (" + Variables.getX0(auxi, regla.getCromElem(auxi).toInt)
          contents += " " + Variables.getX1(auxi, regla.getCromElem(auxi).toInt)
          contents += " " + Variables.getX3(auxi, regla.getCromElem(auxi).toInt) + ")\n"
        }
      }
    }
    contents += "\tConsecuent: " + Attributes.getOutputAttribute(0).getNominalValue(max) + "\n\n"
    File.AddtoFile(aux1_ini + /*"_0" + cnfVal +*/ "." + aux1_fin, contents)
    val sixDecimals = new DecimalFormat("0.000000")
    contents = "" + max

    for (auxi <- 0 until AG.getNumObjectives) {
      contents += "\t" + sixDecimals.format(Result.getObjectiveValue(auxi))
    }
    contents += "\t" + sixDecimals.format(Result.getCnf) + "\n"
    /**/
    // Uncomment if more confidence filters are added
    File.AddtoFile(aux2_ini + /*"_0" + cnfVal + */ "." + aux2_fin, contents)
  }

  /**
    * <p>
    * Main method of the algorithm
    * </p>
    *
    */
  def main(arg: Array[String]) {

    var contents: String = ""
    // String for the file contents
    var NameRule: String = ""
    var NameMeasure: String = ""

    // String containing de original names for the rules and measures files
    var terminar: Boolean = false

    // Indicates no more repetition for the rule generation of diferent classes
    val t_ini = System.currentTimeMillis
    var result: Population = null
    if (arg.length != 1) {
      System.err.println("Syntax error. Usage: java AGI <parameterfile.txt>")
      return
    }

    val GrowthRateThreshold = 10d
    val supportThreshold = 0.02
    val chiThreshols = 3.84

    // Initialise the Spark Context
    val conf = new SparkConf().setMaster("local[*]").setAppName("MOEA_BigData").set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(conf)

    sc.setLogLevel("ERROR")

    // Initial echo
    println("\nEFEP-MOEA implementation\n")

    // Initalise the main structures of the algorithm
    Variables = new TableVar
    Ejemplos = new TableDat
    AG = new Genetic

    // Read parameter file and initialize parameters
    ReadParameters(arg(0))
    //ReadParameters("param.txt")

    NameRule = rule_file
    NameMeasure = measure_file

    // Read the dataset, store values and echo to output and seg files
    //CaptureDataset(input_file_tra, true)
    readDataset(input_file_tra,sc,numPartitions,Variables.getNLabel)

    // Screen output of same parameters
    println("\nSeed: " + seed) // Random Seed
    println("Generate rules for all the classes")



    time = System.currentTimeMillis
    // Fetch class names to save later the .tra and .tst files.
    val classNames = Variables.classNames

    // This do-while loop is not necessary anymore. We already obtain rules for all classes in the genetic algorithm
    do {
      // Initialization of random generator seed. Done after load param values
      if (seed != 0)
        Randomize.setSeed(seed)

      println("Processing...")

      // Broadcast the Variables structure to all mappers and execute the genetic algorithm
      val broadcastVariables = sc.broadcast(Variables)
      result = AG.GeneticAlgorithm(broadcastVariables, Ejemplos, seg_file,sc)
      println("GENERATION OF ELITE: " + result.ult_cambio_eval)

      val marcar: BitSet =  if(AG.getRulesRep equalsIgnoreCase "can")
        AG.RemoveRepeatedCAN(result)
        else
        AG.RemoveRepeatedDNF(result, Variables)


      // Remove repeated rules
      result = result.removeRepeated(marcar, Ejemplos, Variables, AG)

      terminar = true

    } while (! terminar)


    result.indivi.foreach(i => i.setIndivEvaluated(false))
    val broadcastVariables = sc.broadcast(Variables)
    result.evalPop(AG,broadcastVariables, Ejemplos,sc)

    //If imbalanced mode is selected, then get only the patterns of the minority class
    /*if(imbalanced){
      result.setNumIndiv(result.indivi.length)
    }*/

    // Filter rules if neccesary and write rules to file
    //if (confidence_filter)
    // allRules = FilterPopulation(allRules, AG.getMinCnf)
    //WriteRule(result, AG.getNumObjectives, NameRule, NameMeasure,  cab_measure_file)

    // Save the .tra file
    //AG.CalcPobOutput(output_file_tra, result, Ejemplos, Variables, classification_type, classNames, Data.getHeader)


    /*int maximumInd = -1;

        if((conta1==0)/*||(conta2==0)||(conta3==0)||(conta4==0)){ // Filter by confidence
            maximumInd = -1;
            double maximumConfInd = -1;
            for(int i=0; i<auxMax.getNumIndiv(); i++){
                if(auxMax.getIndiv(i).getMeasures().getCnf()>maximumConfInd){
                    maximumInd = i;
                    maximumConfInd = auxMax.getIndiv(i).getMeasures().getCnf();
                }
            }
        }
        WriteRuleMax(auxMax, 1, AG.getNumObjectives(), NameRule, NameMeasure, cab_measure_file, 0);
        if(conta1==0) WriteRuleMax(auxMax, maximumInd, AG.getNumObjectives(), NameRule, NameMeasure, cab_measure_file,0);
        //if(conta2==0) WriteRuleMax(auxMax, maximumInd, AG.getNumObjectives(), NameRule, NameMeasure, cab_measure_file,1);
        //if(conta3==0) WriteRuleMax(auxMax, maximumInd, AG.getNumObjectives(), NameRule, NameMeasure, cab_measure_file,2);
        //if(conta4==0) WriteRuleMax(auxMax, maximumInd, AG.getNumObjectives(), NameRule, NameMeasure, cab_measure_file,3);
*/*/

    time = System.currentTimeMillis - time
    val cadtime: Double = time.toDouble / 1000
    val d: DecimalFormat = new DecimalFormat("0.000")

    contents = "Algorithm terminated\n" + "--------------------\n" + "####### Execution time: " + d.format(cadtime) + " sec.\n"
    println(contents)
    File.AddtoFile(seg_file, "\n\n" + contents)

    //Calculate the quality measures
    //val aux1_ini: String = rule_file.substring(0, rule_file.lastIndexOf("."))
    //val aux1_fin: String = rule_file.substring(rule_file.lastIndexOf(".") + 1, rule_file.length)
    //val aux2_ini: String = qmeasure_file.substring(0, qmeasure_file.lastIndexOf("."))
    //val aux2_fin: String = qmeasure_file.substring(qmeasure_file.lastIndexOf(".") + 1, qmeasure_file.length)

    println("Calculating values of the quality measures\n")

    // Filter by min confidence and save the results
    val confidence: Array[Individual] = result.indivi.filter(i => i.medidas.getCnf >= AG.getMinCnf)
    val confPop = filterByConfidence(result)
    //val confPop = new Population(confidence.length, Variables.getNVars, AG.getNumObjectives, Ejemplos.getNEx, AG.getRulesRep, Variables)
    //confPop.indivi = confidence
    //WriteRule(confPop,AG.getNumObjectives,confidence_filter_rules, confidence_filter_measure, cab_measure_file)
    //AG.CalcPobOutput(confidence_filter_tra, confPop, Ejemplos, Variables, classification_type,classNames)

    // Now, filter by chi-EPs and save the result


    /******************************************
                  FILTER BY MINIMALS
     *******************************************/
    var chiPop = new Population()
    if(chi) {
      val marcas = new Array[Boolean](result.getNumIndiv)
      for (i <- 0 until result.getNumIndiv) {
        for (j <- 0 until result.getNumIndiv) {
          if (i != j && !marcas(j)) {
            if (result.indivi(i).covers(result.indivi(j), Variables) && result.indivi(i).medidas.getGrowthRate > result.indivi(j).medidas.getGrowthRate) {
              // individual i is minimal, check the conditions 3 and 4 in order to remove individual j
              // Condition 3

              marcas(j) = true
            }
          }
        }
      }


      val chiP = new ArrayBuffer[Individual]()
      for (i <- marcas.indices) {
        if (!marcas(i)) {
          chiP += result.indivi(i)
        }
      }

      //************************************************************************

      chiPop = new Population(chiP.length, Variables.getNVars, AG.getNumObjectives, Ejemplos.getNEx, AG.getRulesRep, Variables)
      chiPop.indivi = chiP.toArray
      //WriteRule(chiPop, AG.getNumObjectives, chi_filter_rules, chi_filter_measure, cab_measure_file)
      //AG.CalcPobOutput(chi_filter_tra, chiPop, Ejemplos, Variables, classification_type, classNames)
    }

    var maxPop = new Population()
    if(max) {
      val marcas = new Array[Boolean](result.getNumIndiv)
      for (i <- 0 until result.getNumIndiv) {
        for (j <- (result.getNumIndiv -1) until 0 by -1) {
          if (i != j && !marcas(j)) {
            if (result.indivi(j).covers(result.indivi(i), Variables) && result.indivi(i).medidas.getGrowthRate > result.indivi(j).medidas.getGrowthRate) {
              // individual i is minimal, check the conditions 3 and 4 in order to remove individual j
              // Condition 3

              marcas(j) = true
            }
          }
        }
      }


      val maxP = new ArrayBuffer[Individual]()
      for (i <- marcas.indices) {
        if (!marcas(i)) {
          maxP += result.indivi(i)
        }
      }

      //************************************************************************

      maxPop = new Population(maxP.length, Variables.getNVars, AG.getNumObjectives, Ejemplos.getNEx, AG.getRulesRep, Variables)
      maxPop.indivi = maxP.toArray
      //WriteRule(maxPop, AG.getNumObjectives, max_filter_rules, max_filter_measure, cab_measure_file)
      //AG.CalcPobOutput(max_filter_tra, chiPop, Ejemplos, Variables, classification_type, classNames)
    }
    //        float cnf_min = AG.getMinCnf();
    //        for (int cnf = 0; cnf <= 3; cnf++) {
    //            String cnfVal = String.valueOf(Math.round(cnf_min * 10));
    //            Calculate.Calculate(input_file_tst, aux1_ini + "_0" + cnfVal + "." + aux1_fin, aux2_ini + "_0" + cnfVal + "." + aux2_fin, Variables.getNLabel());
    //            cnf_min += 0.10;
    //        }


    // Calculate the .tst and the quac file
    CalculateTest(output_file_tst, qmeasure_file, result, classification_type, AG.getRulesRep)
    CalculateTest(confidence_filter_tst, confidence_filter_qmeasre, confPop, classification_type, AG.getRulesRep)
    if(chi)
      CalculateTest(chi_filter_tst, chi_filter_qmeasure, chiPop, classification_type, AG.getRulesRep)
    if(max)
      CalculateTest(max_filter_tst, max_filter_qmeasure, maxPop, classification_type, AG.getRulesRep)


    val t_fin: Long = System.currentTimeMillis
    val cadtimeTotal: Double = time / 1000
    System.out.println("####### Total time: " + d.format(cadtimeTotal) + "sec.")
    File.AddtoFile(seg_file, "####### Total time: " + d.format(cadtimeTotal) + "sec.")
  }

  /**
    * It calculates the .tra and .tst for classification and the quac file too.
    *
    * @param outputFile         -> The name of the .tra and .tst file
    * @param pob                -> An array of populations, obtained for each execution of a
    *                           genetic algorithm
    * @param classificationType -> Type of classification used: "max" -> the
    *                           maximum compatibility degree. "sum" -> the maximum sum of compatiblity
    *                           degrees per class. "norm_sum" -> the maximum sum of compatibility degrees
    *                           per class normalized by the number of examples.
    * @throws IOException
    */
  @throws[IOException]
  def CalculateTest(outputFile: String, measureFile:String,  pob: Population, classificationType: String, ruleRep: String) {
    var pertenencia: Float = 0f
    val pert: Float = 0
    var chrome: CromCAN = null
    var disparoFuzzy: Float = 1
    val success: Float = 0
    val error: Float = 0
    var num_var_no_interv: Array[Int] = null
    val tp: Float = 0
    val fp: Float = 0
    var normsum: Array[Float] = null
    //CaptureDataset(input_file_tst, false); // Read test set and initialize its values
    CaptureDatasetTest() // Read test set and initialize its values

    var contents: String = Data.getHeader
    Files.writeFile(outputFile, contents)
    val numRules = pob.getNumIndiv

    normsum = new Array[Float](Variables.getNClass)
    val reglasDisparadas: Array[Int] = Array.fill[Int](Variables.getNClass)(0)

    num_var_no_interv = Array.fill[Int](numRules)(0)
    var aciertos: Int = 0


    // Begin computation of the predictions
    // BIG DATA!!
    //for (i <- 0 until Ejemplos.getNEx) {
    var index: Int = -1

    val cubiertosSoporte_Positiva = new BitSet(Ejemplos.getNEx)
    val cubiertosSoporte_Negativa = new BitSet(Ejemplos.getNEx)

  /*
    Ejemplos.dat.foreach(example =>{
        // For each example
       var counter: Int = 0
        index += 1
       //for(clase <- 0 until pob.length) {
            //val marca = AG.RemoveRepeatedCAN(pob(clase))
            //for(j <- 0 until pob(clase).getNumIndiv) {
            val compatibility = if(ruleRep equalsIgnoreCase "can"){
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
                if(disparoFuzzy > AG.getAlphaCut){
                  if(example.getClas == ind.getClas){
                    // Correctly covered examples, set as covered for the positive class
                    cubiertosSoporte_Positiva.set(index)
                  } else {
                    // Example covered, but for the negative class
                    cubiertosSoporte_Negativa.set(index)
                  }
                }
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
                if(disparoFuzzy > AG.getAlphaCut){
                  if(example.getClas == ind.getClas){
                    // Correctly covered examples, set as covered for the positive class
                    cubiertosSoporte_Positiva.set(index)
                  } else {
                    // Example covered, but for the negative class
                    cubiertosSoporte_Negativa.set(index)
                  }
                }
                disparoFuzzy
              })
            }

        //}
        // Calculate predicted class for classification
        var prediccion: Int = 0
        if (classificationType.equalsIgnoreCase("max")) {
          // max rule compatibility
          var max: Float = -1
          var max_actual: Int = 0
          counter = 0
          //for( clase <- 0 until pob.length){
              //val marca = AG.RemoveRepeatedCAN(pob(clase))
              //for(j <- 0 until pob(clase).getNumIndiv) {
              pob.indivi.foreach(ind =>{
                  //val m = marca.get(j)
                  if (!confidence_filter || confidence_filter && (ind.getMeasures.getCnf > AG.getMinCnf)) {
                    if (max <= compatibility(counter)) {
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
                    }
                  }
                counter += 1
              })

          //}
        } else if (classificationType.equalsIgnoreCase("sum")) {
          // sum of compatibility per class
          for (j <- 0 until Variables.getNClass) {
              normsum(j) = 0
          }
          counter = 0
          //for (clase <- pob.indices) {
              //val marca= AG.RemoveRepeatedCAN(pob(clase))
              //for (j <- 0 until pob(clase).getNumIndiv) {
              for(j <- 0 until pob.getNumIndiv) {
                //val m = marca.get(j)
                if (!confidence_filter || confidence_filter && (pob.getIndiv(j).getCnfValue > AG.getMinCnf)) {
                  normsum(pob.getIndiv(j).getClas) += compatibility(j)
                }
              }
              //}
          //}
          /*var max: Float = -1
          for (j <-  normsum.indices) {
              if (max < normsum(j)) {
                max = normsum(j)
                prediccion = j
              }
          }*/
          prediccion = normsum.indexOf(normsum.max)
        } else {

          // Normalized sum
          for(j <- 0 until Variables.getNClass) {
              normsum(j) = 0
              reglasDisparadas(j) = 0
          }
          counter = 0
          //for(clase <- pob.indices) {
             // val marca = AG.RemoveRepeatedCAN(pob(clase))
              for (j <- 0 until pob.getNumIndiv) {
                  //val m = marca.get(j)
                  if (!confidence_filter || confidence_filter && (pob.getIndiv(j).getCnfValue > AG.getMinCnf)) {
                    normsum(pob.getIndiv(j).getClas) += compatibility(j)
                    if (compatibility(j) > 0) {
                      reglasDisparadas(pob.getIndiv(j).getClas) += 1
                    }
                  }
              }
          //}
          // Normalize sum
          for(p <- normsum.indices) {
              if(reglasDisparadas(p) > 0)
              normsum(p) /= reglasDisparadas(p)
          }
          // Get the prediction
          prediccion = normsum.indexOf(normsum.max)

          /*var max: Float = -1

          for(j <- normsum.indices) {
              if (max < normsum(j)) {
                max = normsum(j)
                prediccion = j
              }
          }*/
        }
        // Calculate test matching to get accuracy
        /*if (Attributes.getOutputAttribute(0).getNominalValue(Ejemplos.getClass(i)).equalsIgnoreCase(Attributes.getOutputAttribute(0).getNominalValue(prediccion))) {
          aciertos += 1
        }*/
      if(prediccion == example.getClas)
        aciertos += 1

        contents = Attributes.getOutputAttribute(0).getNominalValue(example.getClas) + " " + Attributes.getOutputAttribute(0).getNominalValue(prediccion) + "\n"
        Files.addToFile(outputFile, contents)

    })

    println("TEST ACCURACY: " + (aciertos / Ejemplos.getNEx.toFloat))

    // Write measures in the file
    val sixDecimals: DecimalFormat = new DecimalFormat("0.000000")
    val threeInts: DecimalFormat = new DecimalFormat("000")
    contents = "Number \tClass \tSize \tNVar \tLength \tUnusualness \tGain" + "\tSensitivity \tSupportP \tSupporN \tSupportDif \tFConfidence \tGrowthRate \tTEFisher \tHellinger \tTPr \tFPr \n"
    Files.writeFile(measureFile, contents)
    var AvNVAR: Float = 0
    var AvLENG: Float = 0
    var AvUNUS: Float = 0
    var AvGAIN: Double = .0
    var AvSENS: Float = 0
    var AvDIFS: Float = 0
    var AvFCNF: Double = .0
    var AvTPr: Float = 0
    var AvFPr: Float = 0
    var AvDH: Float = 0
    var AvTF: Float = 0
    var AvSupm: Float = 0
    var AvSupM: Float = 0
    // Calculate quac file
    var rule: Int = 0
    var countGR: Int = 0
    // number of rules with GR >= 1
    num_var_no_interv = num_var_no_interv.map(x => x / Ejemplos.getNEx)
    //for(clase <- pob.) {
    pob.indivi.foreach(ind => {
        /*Ejemplos.setExamplesClassObj(clase)
        Variables.setNumClassObj(clase)
        Variables.setNameClassObj(Attributes.getOutputAttribute(0).getNominalValue(clase))
        */
        // Evaluates the individual against test data
        ind.evalInd(AG, Variables, Ejemplos)
        //for (i <- 0 until pob.getNumIndiv) {
            // Evals the individual against test data
            //pob.getIndiv(i).evalInd(AG, Variables, Ejemplos)

      val measures: QualityMeasures = ind.medidas
            contents = "" + threeInts.format(rule) + "   "
            contents += "\t" + threeInts.format(ind.getClas)
            contents += "\t-"
            contents += "\t" + sixDecimals.format(Variables.getNVars - num_var_no_interv(rule))
            contents += "\t" + sixDecimals.format(measures.getLength)
            contents += "\t" + sixDecimals.format(measures.getUnus)
            if (measures.getGain == Double.PositiveInfinity) {
              contents += "\tINFINITY"
            } else {
              contents += "\t" + sixDecimals.format(measures.getGain)
            }
            contents += "\t" + sixDecimals.format(measures.getSensitivity)
            contents += "\t" + sixDecimals.format(measures.getSupm)
            contents += "\t" + sixDecimals.format(measures.getSupM)
            contents += "\t" + sixDecimals.format(measures.getSuppDiff)
            contents += "\t" + sixDecimals.format(measures.getCnf)
            if (measures.getGrowthRate == Float.PositiveInfinity) {
              contents += "\tINFINITY"
            }
            else {
              contents += "\t" + sixDecimals.format(measures.getGrowthRate)
            }
            if (measures.getGrowthRate >= 1) {
              countGR += 1
            }
            contents += "\t" + sixDecimals.format(measures.getFisher)
            contents += "\t" + sixDecimals.format(measures.getHellinger)
            contents += "\t" + sixDecimals.format(measures.getTPr)
            contents += "\t" + sixDecimals.format(measures.getFPr)
            contents += "\n"
            AvNVAR += Variables.getNVars - num_var_no_interv(rule)
            AvLENG += measures.getLength
            AvDH += measures.getHellinger
            AvDIFS += measures.getSuppDiff
            AvFCNF += measures.getCnf
            AvFPr += measures.getFPr
            AvGAIN += measures.getGain
            AvSENS += measures.getSensitivity
            if (measures.getFisher <= alpha) {
              AvTF += 1
            }
            AvTPr += measures.getTPr
            AvUNUS += measures.getUnus
            AvSupM += measures.getSupM
            AvSupm += measures.getSupm
            // Add values os quality measures to the file
            Files.addToFile(measureFile, contents)
            rule += 1
        //}
    })

    //Calcular soporte con Examples

    // Para calcular los ejemplos que han sido cubiertos,

    contents = "---\t"
    contents += "---"
    contents += "\t" + rule  // NRULES
    contents += "\t" + sixDecimals.format(AvNVAR / rule) // Nvars
    contents += "\t" + sixDecimals.format(AvLENG / rule) // Length
    contents += "\t" + sixDecimals.format(AvUNUS / rule) // Unus
    if (AvGAIN == Float.PositiveInfinity)  // Gain
      contents += "\tINFINITY"
    else
      contents += "\t" + sixDecimals.format(AvGAIN / rule)

    contents += "\t" + sixDecimals.format(AvSENS / rule) // Sensitivity

    if(imbalanced){ // SupportP
      // If imbalanced, it is calculated the global TPR
      contents += "\t" + sixDecimals.format(cubiertosSoporte_Positiva.cardinality().toFloat / Ejemplos.getExamplesClass(pob.indivi(0).getClas).toFloat)
    } else {
      // If not, it is calculated the global SUPPORT of the set of rules with respect the positve class of each rule
      contents += "\t" + sixDecimals.format(cubiertosSoporte_Positiva.cardinality().toFloat / cubiertosSoporte_Positiva.length().toFloat)
    }

    contents += "\t" + sixDecimals.format(cubiertosSoporte_Negativa.cardinality().toFloat / cubiertosSoporte_Negativa.length().toFloat) // SupportN
    contents += "\t" + sixDecimals.format(AvDIFS / rule) // SuppDiff
    contents += "\t" + sixDecimals.format(AvFCNF / rule) // Conf
    contents += "\t" + sixDecimals.format(countGR.toFloat / rule.toFloat) // GR
    contents += "\t" + sixDecimals.format(AvTF / rule.toFloat) //Fisher
    contents += "\t" + sixDecimals.format(AvDH / rule) // Hellinger
    contents += "\t" + sixDecimals.format(AvTPr / rule) // TPR
    contents += "\t" + sixDecimals.format(AvFPr / rule) // FPR
    contents += "\n"

    // Add average values to the quac file
    Files.addToFile(measureFile, contents)
    */
  }







  def FilterPopulation(pob: Array[Population], minCnf: Double): Array[Population] = {
    for (clase <- pob.indices) {
        val marcas: BitSet = new BitSet(pob(clase).getNumIndiv)
        var conta: Int = 0
       for(i <- pob(clase).indivi.indices) {
            val Result: QualityMeasures = pob(clase).getIndiv(i).getMeasures
            if (Result.getCnf > minCnf) {
              marcas.clear(i)
              conta += 1
            } else {
              marcas.set(i)
            }
        }
        // If all rules of a given class can't pass the confidence filter, return the individual with the best
        // confidence
        if (conta == 0) {
          var maximumInd: Int = -1
          var maximumConfInd: Double = -1
          for(i <- 0 until pob(clase).getNumIndiv) {
              if (pob(clase).getIndiv(i).getMeasures.getCnf > maximumConfInd) {
                maximumInd = i
                maximumConfInd = pob(clase).getIndiv(i).getMeasures.getCnf
              }
          }
          marcas.set(maximumInd, 0) // the best individual is not removed

        }
        // Remove marked examples (those that don't have the minimum confidence)
        pob(clase) = pob(clase).removeRepeated(marcas, Ejemplos, Variables, AG)
    }
    pob
  }


  @throws[IOException]
  def CaptureDatasetTest() {
    /*
    try {
      // Declaration of the dataset and load in memory
      Data = new InstanceSet
      Data.readSet(input_file_tst, false)
      // Check that there is only one output variable
      if (Attributes.getOutputNumAttributes > 1) {
        println("This algorithm can not process MIMO datasets")
        println("All outputs but the first one will be removed")
      }
      var noOutputs: Boolean = false
      if (Attributes.getOutputNumAttributes < 1) {
        println("This algorithm can not process datasets without outputs")
        println("Zero-valued output generated")
        noOutputs = true
      }
      // Chek that the output variable is nominal
      if (Attributes.getOutputAttribute(0).getType != Attribute.NOMINAL) {
        // If the output variables is not enumeratad, the algorithm can not be run
        try {
          throw new IllegalAccessException("Finish")
        }
        catch {
          case term: IllegalAccessException => {
            System.err.println("Target variable is not a discrete one.")
            System.err.println("Algorithm can not be run.")
            System.out.println("Program aborted.")
            System.exit(-1)
          }
        }
      }
      // Set the number of classes of the output attribute - this attribute must be nominal
      Variables.setNClass(Attributes.getOutputAttribute(0).getNumNominalValues)
      // Screen output of the output variable and selected class
      println("Output variable: " + Attributes.getOutputAttribute(0).getName)
      // Creates the space for the variables and load the values.
      Variables.Load(Attributes.getInputNumAttributes)
      // Setting and file writing of fuzzy sets characteristics for continuous variables
      val nombreF: String = seg_file
      Variables.InitSemantics(nombreF)
      // Creates the space for the examples and load the values
      Ejemplos.Load(Data, Variables)
      Ejemplos.datZipped = Ejemplos.dat.zipWithIndex
    }
    catch {
      case e: Exception => {
        System.out.println("DBG: Exception in readSet")
        e.printStackTrace()
      }
    }*/
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


  /**
    * Returns a population filter by confidence.
    * NOTE: it keeps at least one individual per class, even if the confidence is lower than the treshold.
    * @param pop
    * @return
    */
  def filterByConfidence(pop: Population): Population = {
    val salida = new ArrayBuffer[Individual]()
      for(i <- 0 until Variables.getNClass){
        val aux: Array[Individual] = pop.indivi.filter(x => x.getClas == i).sortWith((x,y) => x.getCnfValue > y.getCnfValue)
        // Keep at least one individual for each class even if the confidence can not reach the minimum level
        if(!aux.isEmpty) {
          if (aux(0).getCnfValue < AG.getMinCnf) {
            salida += aux(0)
          } else {
            aux.filter(x => x.getCnfValue >= AG.getMinCnf).foreach(
              j => salida += j
            )
          }
        }
      }

    val result = new Population(salida.length, Variables.getNVars, AG.getNumObjectives, Ejemplos.getNEx, AG.getRulesRep, Variables)

    for(i <- salida.indices){
      result.CopyIndiv(i,Ejemplos.getNEx,AG.getNumObjectives,salida(i))
    }

    return result
  }


  /**
    * Reads a KEEL dataset, fuzzifies numeric variables and store the results
    * in "variables" the information of the variables and in
    * "instanciasTotal.datosRDD" the RDD of the instances.
    */
  def readDataset(file: String, sc: SparkContext, numPartitions: Int, nLabels: Int) {
    // Catch the dataset, and parse ir
    //val stream = getClass.getResourceAsStream(file)
    //val fich = scala.io.Source.fromInputStream( stream ).getLines().toList
    val fichero = sc.textFile(file, numPartitions)
    //val fichero = sc.parallelize(fich,numPartitions).cache()
    val head = fichero.filter { linea => linea.startsWith("@") }
    val linesHead = head.filter { line => line.startsWith("@attribute") }
    val linesInstances = fichero.filter(linea => !linea.contains("@") && !linea.trim().isEmpty)//.cache()
    val inputs = fichero.filter { linea => linea.startsWith("@inputs") }.first()
    val outputs = fichero.filter { linea => linea.startsWith("@output") }.first()

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
    Ejemplos.loadData2(linesInstances, Variables, outputs_values, sc, numPartitions)
  }




}


