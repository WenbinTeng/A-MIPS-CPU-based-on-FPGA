package core

import chisel3._
import chisel3.util.MuxCase
import cons._

class SIO extends Bundle {
	def InitVal(): SIO = 0.U.asTypeOf(this)
}

class FF[T <: SIO](sio: T) extends Module {
	val io = IO(new Bundle {
		val din  = Input(sio)
		val dout = Output(sio)

		val npc = Input(UInt(32.W))
		val pc = Output(UInt(32.W))

		val refillFlag = Input(Bool())
		val refillAddr = Input(UInt(32.W))

		val flushSignal = Input(Bool())

		val nextAllowIn = Input(Bool())
		val prevReadyGo = Input(Bool())
	})

	val pc = RegInit(conf.Reset.PCR_RESET)

	pc := MuxCase(io.npc, Seq(
		io.refillFlag -> (io.refillAddr - 4.U(32.W)),
		io.flushSignal -> conf.Reset.PCR_RESET,
		!io.nextAllowIn -> pc,
		!io.prevReadyGo -> pc
	))

	io.pc := pc

	val ff = RegInit(sio.InitVal())

	ff := MuxCase(io.din, Seq(
		 io.flushSignal -> sio.InitVal(),
		!io.nextAllowIn -> ff,
		!io.prevReadyGo -> sio.InitVal()
	))

	io.dout := ff
}

class S0S1IO extends SIO {
	val fetPack = new FetPack
	val excPack = new ExcPack
}

class S1S2IO extends SIO {
	val decPack = new DecPack
	val excPack = new ExcPack
}

class S2S3IO extends SIO {
	val aluPack = new AluPack
	val memPack = new MemPack
	val regPack = new RegPack
	val mhiPack = new MhiPack
	val mloPack = new MloPack
	val cp0Pack = new Cp0Pack
	val excPack = new ExcPack
}

class S3S4IO extends SIO {
	val memPack = new MemPack
	val regPack = new RegPack
	val mhiPack = new MhiPack
	val mloPack = new MloPack
	val cp0Pack = new Cp0Pack
	val excPack = new ExcPack
}

class S4S5IO extends SIO {
	val memPack = new MemPack
	val regPack = new RegPack
	val mhiPack = new MhiPack
	val mloPack = new MloPack
	val cp0Pack = new Cp0Pack
	val excPack = new ExcPack
}

class S5S6IO extends SIO {
	val memPack = new MemPack
	val regPack = new RegPack
	val mhiPack = new MhiPack
	val mloPack = new MloPack
	val cp0Pack = new Cp0Pack
	val excPack = new ExcPack
}

