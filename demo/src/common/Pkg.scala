package hdl.common

import spinal.core._
import spinal.lib._
import common._
import spinal.core.sim._

case class Pkg(byteNum: Int, idNum: Int) extends Bundle with IMasterSlave{
  val data     = Flow(Bits(byteNum byte))
  val sop, eop = Bool()
  val mty      = UInt(log2Up(byteNum) bits)
  val pid      = UInt(log2Up(idNum) bits)

  override def asMaster(): Unit = {
    out(data, sop, eop, pid, mty)
  }

  def clear = {
    data.payload #= 0
    data.valid   #= false
    sop          #= false
    eop          #= false
    mty          #= 0
    pid          #= 0
  }

  def force(burst: List[Byte], id: Int)(implicit cd: ClockDomain) = {
    val groupd = burst.sliding(byteNum, byteNum).toList
    (0 until groupd.size).foreach{ i =>
      val bytepkg = groupd(i)
      sop #= {if(i == 0) true else false}
      eop #= {if(i == groupd.size -1) true else false}
      drive(bytepkg)
      pid #= id
      cd.waitSampling()
      clear
    }

    def drive(pkg: List[Byte]) = {
      val padNum = byteNum - pkg.size
      val pkgAligned = pkg ++ List.fill(padNum)(0.toByte)
      data.payload #= Bytest2BigInt(pkgAligned)
      data.valid   #= true
      mty          #= padNum
    }
  }
}

object Bytest2BigInt{
  def apply(x: List[Byte]) = {
    val ret = x.map(_.toHexString.takeRight(2)).reverse.mkString
    BigInt(ret, 16)
  }
}

