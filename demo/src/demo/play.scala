package demo

import spinal.core._
import spinal.lib._

class StreamConnect extends Component{
  val io = new Bundle{
    val si = slave(Stream(Bits(32 bit)))
    val so = master(Stream(Bits(32 bit)))
  }
  io.so </< io.si
}

object genrtl extends App{
  import common._

  SpinalVerilog(new StreamConnect)
}
