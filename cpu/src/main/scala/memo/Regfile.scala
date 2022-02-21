package memo

import chisel3._

class Regfile extends Module {
	val io = IO(new Bundle {
		val rio = new RegfileRIO
		val wio = new RegfileWIO
	})

	val regfile = RegInit(VecInit(Seq.fill(32) { 0.U(32.W) }))

	io.rio.qs := regfile(io.rio.rs)
	io.rio.qt := regfile(io.rio.rt)

	when(io.wio.we && io.wio.rd =/= 0.U) {
		regfile(io.wio.rd) := io.wio.di
	}
}

class RegfileRIO extends Bundle {
	val rs = Input(UInt(5.W))
	val rt = Input(UInt(5.W))
	val qs = Output(UInt(32.W))
	val qt = Output(UInt(32.W))
}

class RegfileWIO extends Bundle {
	val we = Input(Bool())
	val rd = Input(UInt(5.W))
	val di = Input(UInt(32.W))
}

