import java.util

import org.apache.spark.util.AccumulatorV2

class PobBitSet(neje: Int) extends AccumulatorV2[Int, util.BitSet]{

  private val poblac: util.BitSet = new util.BitSet(neje)

  override def isZero: Boolean = {
    poblac.cardinality() == 0
  }

  override def copy(): AccumulatorV2[Int, util.BitSet] = {
    val a = new PobBitSet(neje)
    a.reset()
    a.poblac.or(this.poblac)
    a
  }

  override def reset(): Unit = {
    poblac.clear(0,neje)
  }

  override def add(v: Int): Unit = {
    poblac.set(v)
  }

  override def merge(other: AccumulatorV2[Int, util.BitSet]): Unit = {
    poblac.or(other.value)
  }

  override def value: util.BitSet = poblac


}
