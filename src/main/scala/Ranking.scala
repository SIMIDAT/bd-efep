/**
  * Created by fpulgar on 1/6/15.
  */

class Ranking extends Serializable {

  /**
    * This class implements some facilities for ranking solutions.
    * Given a Population object, their solutions are ranked
    * according to scheme proposed in NSGA-II; as a result, a set of subsets
    * are obtained. The subsets are numbered starting from 0 (in NSGA-II, the
    * numbering starts from 1); thus, subset 0 contains the non-dominated
    * solutions, subset 1 contains the non-dominated solutions after removing those
    * belonging to subset 0, and so on.
    */

  /**
    * An array containing all the fronts found during the search
    */
  var ranking: Array[Population] = null

  /**
    * Constructor of the Ranking
    * @param pop              Actual population
    * @param Variables           Variables structure
    * @param nobj                Number of objectives
    * @param neje                Number of examples
    * @param RulRep              Rules representation
    * @param SDomin              Strict dominance
    */
  def this (pop:Population, Variables: TableVar, nobj: Int, neje: Int, RulRep: String, SDomin: String) = {
    this()

    // dominateMe[i] contains the number of solutions dominating i
    val dominateMe: Array[Int] = new Array[Int](pop.getNumIndiv)

    // iDominate[k] contains the list of solutions dominated by k
    val iDominate: Array[List[Int]] = new Array[List[Int]](pop.getNumIndiv)

    // front[i] contains the list of individuals belonging to the front i
    val front: Array[List[Int]] = new Array[List[Int]](pop.getNumIndiv+1)

    // Initialize the fronts
    (0 to front.length -1).foreach(i => front(i)= List ())

    // Fast non dominated sorting algorithm
    front(0)=(0 to pop.getNumIndiv-1).map(p => {
      // Initialice the list of individuals that i dominate and the number
      // of individuals that dominate me
      // For all q individuals , calculate if p dominates q or vice versa
      val aux = (0 to pop.getNumIndiv-1).map(q => {

        var flagDominate = compareConstraint(pop.getIndiv(p), pop.getIndiv(q))

        if (flagDominate == 0) {
          flagDominate = compareDominance(pop.getIndiv(p), p, pop.getIndiv(q), q,nobj, SDomin)
        }

        if (flagDominate == -1) {
          (0,List(q))
        } else if (flagDominate == 1){
          (1,List())
        } else {
          (0,List(q))
        }
      }).reduce((a,b)=>(a._1+b._1,a._2:::b._2))

      iDominate(p) = aux._2
      dominateMe(p) = aux._1
      // If nobody dominates p, p belongs to the first front
      if (dominateMe(p) == 0) {
        pop.getIndiv(p).setRank(0)
        List(p)
      }else{
        List()
      }
    }).reduce((a,b)=>a:::b)

    //Obtain the rest of fronts
    var i = 0

    while (front(i).length != 0){
      i+=1
      val it1 = front(i-1).iterator
      it1.foreach(v1 => {
        val it2 = iDominate(v1).iterator
        it2.foreach(v2=>{
          dominateMe(v2)-=1
          if(dominateMe(v2)==0){
            front(i)=front(i):+ v2
            pop.getIndiv(v2).setRank(i)
          }
        })
      })
    }

    ranking = new Array[Population](i)

    (0 to i-1).foreach(j => {
      ranking(j) = new Population(front(j).length, Variables.getNVars, nobj, neje, RulRep, Variables)

      val it1 = front(j).iterator
      var contador = 0
      it1.foreach(v1 => {
        ranking(j).CopyIndiv(contador,neje,nobj,pop.getIndiv(v1))
        contador+=1
      })
    })
  } // Ranking

  /**
    * Returns a Population containing the solutions of a given rank.
    * @param rank                    Value of the rank
    * @return                        Population of this rank.
    */
  def getSubfront(rank: Int): Population = {
    ranking(rank)
  } // getSubFront

  /**
    * Returns the total number of subFronts founds.
    * @return            Number of fronts in the population
    */
  def getNumberOfSubfronts : Int = {
    ranking.length
  } // getNumberOfSubfronts

  /**
    * Gets the comparison constraint
    * @param a           Individual
    * @param b           Individual
    * @return            Result of the comparison between the individuals
    */
  def compareConstraint(a: Individual, b: Individual): Int = {

    val overall1 = a.getOverallConstraintViolation
    val overall2 = b.getOverallConstraintViolation

    if ((overall1 < 0) && (overall2 < 0)) {
      if (overall1 > overall2)-1
      else if (overall2 > overall1)1
      else 0
    } else if ((overall1 == 0) && (overall2 < 0)) {
      -1
    } else if ((overall1 < 0) && (overall2 == 0)) {
      1
    } else 0
  }


  /**
    * Gets the comparison Dominance
    * @param a           Individual
    * @param posa        Position of the individual a
    * @param b           Individual
    * @param posb        Position of the individual b
    * @param nobj        Number of objectives of the algorithm
    * @param SDomin      Strict Dominance for comparison
    * @return            Result of the comparison between the individuals
    */
  def compareDominance(a: Individual, posa: Int, b: Individual, posb: Int, nobj: Int, SDomin: String): Int = {
    //NOTE for this algorithm:
    // If A   domains   B THEN flag == -1
    // If A   equals    B THEN flag == 0
    // Si A  !domains   B THEN flag == 1
    var toRet = 1
    if (a == null) toRet = 1
    else if (b == null) toRet = -1
    else {
      var dominate1 = 0 // dominate1 indicates if some objective of solution1
      // dominates the same objective in solution2. dominate2
      var dominate2 = 0 // is the complementary of dominate1.

      var flag = 0

      if (a.getOverallConstraintViolation!=
        b.getOverallConstraintViolation &&
        (a.getOverallConstraintViolation < 0) ||
        (b.getOverallConstraintViolation < 0)){
        toRet = compareConstraint(a,b)
      }else {
        (0 to nobj-1).foreach(i=> {
          var medidas = a.getMeasures
          val value1 = medidas.getObjectiveValue(i)
          medidas = b.getMeasures
          val value2 = medidas.getObjectiveValue(i)
          if (SDomin.compareTo("YES") == 0) {
            if (value1 < value2) {
              flag = 1
            } else if (value1 > value2) {
              flag = -1
            } else flag = 0
          } else {
            if (value1 < value2) {
              flag = 1
            } else {
              flag = -1
            }
          }

          if (flag == -1) {
            dominate1 = 1
          }

          if (flag == 1) {
            dominate2 = 1
          }
        })

        if (dominate1 == dominate2) {
          toRet = 0 //No one dominate the other
        }
        if (dominate1 == 1) {
          toRet = -1 // solution1 dominate
        }
        // solution2 dominate (initial value 1)
      }
    }
    toRet
  }


}