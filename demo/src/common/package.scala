import spinal.core.sim.SimConfig

package object common {

  final case class NicException(private val message: String = "",
                               private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

  object NicError{
    def apply() = throw NicException("Error")
    def apply(msg: String) = throw NicException(msg)
  }

  import spinal.core._

  val spinalConfig = SpinalConfig(
    defaultConfigForClockDomains = ClockDomainConfig(clockEdge = RISING,
      resetKind = ASYNC,
      resetActiveLevel = LOW
    ),
    defaultClockDomainFrequency = FixedFrequency(200 MHz),
    targetDirectory = "./out/rtl/",
    headerWithDate = true,
    anonymSignalPrefix = "t",
    mergeAsyncProcess  = true)

  val spinalConfigWithSV = SpinalConfig(
    mode = SystemVerilog,
    anonymSignalPrefix = "t",
    defaultConfigForClockDomains = ClockDomainConfig(clockEdge = RISING,
      resetKind = ASYNC,
      resetActiveLevel = LOW
    ),
    targetDirectory = "./out/rtl")

  implicit class Int2Byte(x: Int) {
    def byte: BitCount = new BitCount(x * 8)
  }

  implicit class Byte2Bin(x: Byte) {
    def toBinst = x.toBinaryString
    4.toBinaryString.reverse.padTo(8, "0").reverse.mkString
  }

  implicit class BitsReverse(srcb: Bits) {
    def >>(dest: Bundle): Unit = {
      require(dest.getBitsWidth == srcb.getWidth, "width mismatch, can't auto extract")
      autoDeconstructAndConnect(dest.elements.map(_._2).toList, srcb)
    }

    def >>[T <: Data](dest: Vec[T]): Unit = {
      require(dest.getBitsWidth == srcb.getWidth, "width mismatch, can't auto extract")
      autoDeconstructAndConnect(dest.toList, srcb)
    }

    protected def autoDeconstructAndConnect[T <: Data](elements: List[T], source: Bits): Unit = {
      var pos = 0
      for (i <- 0 until elements.size) {
        val element = elements(i)
        val width = element.getBitsWidth
        if(width == 1){
          element match{
            case elem: Bool => elem := source(pos)
            case _ => autoConnect(element, source(pos downto pos))
          }
        }else{
          val slide = source(width + pos -1 downto pos)
          autoConnect(element, slide)
        }
        pos += width
      }
    }

    protected def autoConnect[T <: Data](element: T, slidebits: Bits): Unit = {
      element match {
        case elem: Vec[T] => slidebits >> elem
        case elem: Bundle => slidebits >> elem
        case elem: UInt => elem := U(slidebits)
        case elem: SInt => elem := S(slidebits)
        case elem: Bits => elem := slidebits
        case elem: Bool => elem := slidebits.lsb
        case _ => SpinalError(s"${element} not recognized")
      }
    }
  }

  implicit class ListSplit[T <: Any](lst: List[T]) {
    def split(elem: T): List[List[T]] = iter(lst, elem)

    private def iter[T <: Any](lst: List[T], elem: T): List[List[T]] = {
      val pos =  lst.indexOf(elem)
      if(pos < 0){
        List(lst)
      } else {
        val ret = lst.splitAt(pos)
        if(ret._2.tail.isEmpty){
          List(ret._1)
        } else {
          List(ret._1) ++ iter(ret._2.tail, elem)
        }
      }
    }
  }

  class Twin[T <: Data](val payloadType: HardType[T]) extends Bundle{
    val foo: T = payloadType()
    val bar: T = payloadType()
  }

  object FileWrite{
    def creatIfNotExits(dir: String)  = {
      import java.io.File
      if(! new File(dir).exists()){
        new File(dir).mkdirs()
      }
    }

    def apply(filePath: String, data: List[String]) = {
      import java.io.PrintWriter
      val dir = filePath.split('/').dropRight(1).mkString("/")
      creatIfNotExits(dir)
      new PrintWriter(filePath){write(data.mkString("\n"));close}
    }
  }

  object MacConfig {
    val clockDomainCfg = ClockDomainConfig(resetKind = ASYNC,
      clockEdge = RISING,
      resetActiveLevel = LOW)

    val spinalcfg = SpinalConfig(
      mode = Verilog,
      defaultConfigForClockDomains = clockDomainCfg,
      defaultClockDomainFrequency = FixedFrequency(100 MHz),
      targetDirectory = "rtl/",
      headerWithDate = true,
      anonymSignalPrefix = "t",
      mergeAsyncProcess  = true)

    val simcfg = SimConfig
      .withConfig(spinalcfg)
      .allOptimisation
      .workspacePath("./simWorkspace")

    def MyAssert(a: Any, b: Any, pos: Int = 0): Unit = {
      if(a != b) {
        println(s"Faild @${pos}:")
        println(a, b)
      }
    }
  }
}
