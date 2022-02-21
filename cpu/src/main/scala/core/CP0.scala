package core

import chisel3._
import chisel3.util._
import cons.ExcOp
import conf.Reset.{EXC_ENTRY, PCR_RESET}

class CP0 extends Module {
	val io = IO(new Bundle {
		val pc = Input(UInt(32.W))

		val excFlag = Input(Bool())
		val excSlot = Input(Bool())
		val excCode = Input(UInt(5.W))
		val excAddr = Input(UInt(32.W))

		val externalInt = Input(UInt(6.W))

		val excOp = Input(UInt(ExcOp.WIDTH.W))
		val cp0Re = Input(Bool())
		val cp0We = Input(Bool())
		val cp0Rd = Input(UInt(5.W))
		val cp0Di = Input(UInt(32.W))
		val cp0Do = Output(UInt(32.W))

		val ehdlFlag = Output(Bool())
		val ehdlAddr = Output(UInt(32.W))
		val eretFlag = Output(Bool())
		val eretAddr = Output(UInt(32.W))
	})

	class StatusReg extends Bundle {
		val BEV = UInt(1.W)
		val IM  = UInt(8.W)
		val EXL = UInt(1.W)
		val IE  = UInt(1.W)
	}

	class CauseReg extends Bundle {
		val BD      = UInt(1.W)
		val TI      = UInt(1.W)
		val IP      = UInt(8.W)
		val ExcCode = UInt(5.W)
	}

	val BadVAddr = RegInit(0.U(32.W))
	val Count    = RegInit(0.U(32.W))
	val Compare  = RegInit(0.U(32.W))
	val Status   = RegInit(0.U.asTypeOf(new StatusReg))
	val Cause    = RegInit(0.U.asTypeOf(new CauseReg))
	val EPC      = RegInit(0.U(32.W))

	val tick = RegInit(false.B)
	tick := ~tick

	val excFlag = io.excFlag & io.excCode =/= ExcCode.Int
	val intFlag = (Cause.IP & Status.IM).orR()

	val pcReg = RegNext(io.pc)

	when(excFlag && (io.excCode === ExcCode.AdEL || io.excCode === ExcCode.AdES)) {
		BadVAddr := io.excAddr
	}

	when(io.cp0We && io.cp0Rd === Cp0Addr.Count) {
		Count := io.cp0Di
	}.elsewhen(tick) {
		Count := Count + 1.U(32.W)
	}

	when(io.cp0We && io.cp0Rd === Cp0Addr.Compare) {
		Compare := io.cp0Di
	}

	when(true.B) {
		Status.BEV := 1.U(1.W)
	}
	when(io.cp0We && io.cp0Rd === Cp0Addr.Status) {
		Status.IM := io.cp0Di(15, 8)
	}
	when(intFlag || excFlag) {
		Status.EXL := 1.U(1.W)
	}.elsewhen(io.excOp === ExcOp.eret) {
		Status.EXL := 0.U(1.W)
	}.elsewhen(io.cp0We && io.cp0Rd === Cp0Addr.Status) {
		Status.EXL := io.cp0Di(1)
	}
	when(io.cp0We && io.cp0Rd === Cp0Addr.Status) {
		Status.IE := io.cp0Di(0)
	}

	when((intFlag || excFlag) && !Status.EXL.asBool()) {
		Cause.BD := io.excSlot
	}
	when(io.cp0We && io.cp0Rd === Cp0Addr.Compare) {
		Cause.TI := 0.U(1.W)
	}.elsewhen(Count === Compare) {
		Cause.TI := 1.U(1.W)
	}
	when (io.cp0We && io.cp0Rd === Cp0Addr.Cause) {
		Cause.IP := Cat(io.externalInt(5)| Cause.TI, io.externalInt(4, 0), io.cp0Di(9, 8))
	}.otherwise {
		Cause.IP := Cat(io.externalInt(5)| Cause.TI, io.externalInt(4, 0), Cause.IP(1, 0))
	}
	when(intFlag || excFlag) {
		Cause.ExcCode := Mux(intFlag, ExcCode.Int, io.excCode)
	}

	when(excFlag && !Status.EXL.asBool()) {
		EPC := Mux(io.excSlot, io.pc - 4.U(32.W), io.pc)
	}.elsewhen(intFlag && !Status.EXL.asBool()){
		EPC := pcReg + 4.U(32.W)
	}.elsewhen(io.cp0We && io.cp0Rd === Cp0Addr.EPC) {
		EPC := io.cp0Di
	}

	io.cp0Do := Mux(io.cp0Re, MuxLookup(io.cp0Rd, 0.U(32.W), Seq(
		Cp0Addr.BadVAddr -> BadVAddr,
		Cp0Addr.Count    -> Count,
		Cp0Addr.Compare  -> Compare,
		Cp0Addr.Status   -> Cat(0.U(9.W), Status.BEV, 0.U(6.W), Status.IM, 0.U(6.W), Status.EXL, Status.IE),
		Cp0Addr.Cause    -> Cat(Cause.BD, Cause.TI, 0.U(14.W), Cause.IP, 0.U(1.W), Cause.ExcCode, 0.U(2.W)),
		Cp0Addr.EPC      -> EPC
		)), 0.U(32.W))

	when((intFlag || excFlag) && !Status.EXL.asBool()) {
		io.ehdlFlag := true.B
		io.ehdlAddr := EXC_ENTRY
	}.otherwise {
		io.ehdlFlag := false.B
		io.ehdlAddr := 0.U(32.W)
	}

	when(io.excOp === ExcOp.eret) {
		io.eretFlag := true.B
		io.eretAddr := EPC
	}.otherwise {
		io.eretFlag := false.B
		io.eretAddr := 0.U(32.W)
	}

}

object Cp0Addr {
	val BadVAddr = 8.U(5.W)
	val Count    = 9.U(5.W)
	val Compare  = 11.U(5.W)
	val Status   = 12.U(5.W)
	val Cause    = 13.U(5.W)
	val EPC      = 14.U(5.W)
}

object ExcCode {
	val Int  = 0x00.U(5.W)
	val AdEL = 0x04.U(5.W)
	val AdES = 0x05.U(5.W)
	val Sys  = 0x08.U(5.W)
	val Bp   = 0x09.U(5.W)
	val RI   = 0x0a.U(5.W)
	val Ov   = 0x0c.U(5.W)
}

