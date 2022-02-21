package core

import chisel3._
import chisel3.util.MuxCase

class PipeCtrl extends Module {
	val io = IO(new Bundle {
		val ehdlFlag = Input(Bool())
		val ehdlAddr = Input(UInt(32.W))
		val eretFlag = Input(Bool())
		val eretAddr = Input(UInt(32.W))

		val branchMiss = Input(Bool())
		val branchAddr = Input(UInt(32.W))

		val refillFlag = Output(Bool())
		val refillAddr = Output(UInt(32.W))

		val flushSignal = Output(UInt(6.W))
	})

	io.flushSignal := Mux(io.ehdlFlag || io.eretFlag, "b111111".U(6.W), "b000000".U(6.W))

	io.refillFlag := this.reset.asBool() || io.ehdlFlag || io.eretFlag || io.branchMiss
	io.refillAddr := MuxCase(0.U(32.W), Seq(
		this.reset.asBool()	-> conf.Reset.PCR_RESET,
		io.ehdlFlag 		-> io.ehdlAddr,
		io.eretFlag 		-> io.eretAddr,
		io.branchMiss		-> io.branchAddr
	))

}

