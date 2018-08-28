/**
  * <p>
  *
  * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
  * @version 1.0
  * @since JDK1.5
  *        </p>
  */
import java.util

import org.core._
import java.util.BitSet

class Gene extends Serializable {

  private var gen: BitSet = _
  private var num_elem: Int = 0

/**
  * <p>
  * Creates new instance of gene
  * </p>
  *
  * @param lenght Number of posibles values for the variable
  */

  def this(lenght: Int) = {
    this()
    this.num_elem = lenght
    this.gen = new util.BitSet(lenght+1)
  }
  /**
    * <p>
    * Random initialization of an existing gene
    * </p>
    */
  def RndInitGene: Unit = {
    var interv = 0
    for(i <- 0 until num_elem) {
        // Gene num_elem
        val aux = Randomize.Randdouble(0.0, 1.0)
        // Rand returns a random doble from 0 to 1, including 0 but excluding 1
        if (aux < 0.5) {
          gen.clear(i)
        } else {
          gen.set(i)
          interv += 1 // Counts the number of 1 of the variable
        }
    }
    // If number of 1 equals 0 or num of values, the variable does not take part
    if (interv == 0 || interv == num_elem)
      gen.clear(num_elem)
    else
      gen.set(num_elem)
  }

  /**
    * <p>
    * Non-intervene Initialization of an existing gene
    * </p>
    */
  def NoTakeInitGene: Unit =  {
    gen.clear(0,num_elem)
  }

  /**
    * <p>
    * Retuns the value of the gene indicated
    * </p>
    *
    * @param pos Position of the gene
    * @return Value of the gene
    */
  def getGeneElem(pos: Int): Boolean = gen.get(pos)

  /**
    * <p>
    * Sets the value of the indicated gene of the chromosome
    * </p>
    *
    * @param pos   Position of the gene
    * @param value Value of the gene
    */
  def setGeneElem(pos: Int, value: Boolean) {
    if(value)
      gen.set(pos)
    else
      gen.clear(pos)
  }

  /**
    * <p>
    * Retuns the gene lenght of the chromosome
    * </p>
    *
    * @return Lenght of the gene
    */
  def getGeneLenght: Int = num_elem

  /**
    * <p>
    * Prints the gene
    * </p>
    *
    * @param nFile Name of the file to write the gene
    */
  def Print(nFile: String) {
    var contents = "Gene: "
    for(i <- 0 until num_elem) {
        if (gen.get(i))
          contents += "1 "
        else
          contents += "0 "
    }
    contents += "\n"
    File.AddtoFile(nFile, contents)
  }
}
