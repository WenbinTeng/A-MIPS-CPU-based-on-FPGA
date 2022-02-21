package core

import chisel3._
import chisel3.util._
import cons.{Bypass, MemOp}
import memo.SramDataPort

class S4 extends Module {
	val io = IO(new Bundle {
		val in  = Input(new S3S4IO)
		val out = Output(new S4S5IO)

		val sramDataPort = Flipped(new SramDataPort)

		val bypass = Output(new Bypass)

		val nextAllowIn = Input(Bool())
		val currAllowIn = Output(Bool())
		val currReadyGo = Output(Bool())
		val currMemOpGo = Output(UInt(MemOp.WIDTH.W))
	})

	val re = MuxLookup(io.in.memPack.memOp, false.B, Seq(
		MemOp.lb  -> true.B,
		MemOp.lbu -> true.B,
		MemOp.lh  -> true.B,
		MemOp.lhu -> true.B,
		MemOp.lw  -> true.B
	))
	val we = MuxLookup(io.in.memPack.memOp, 0.B, Seq(
		MemOp.sb  -> true.B,
		MemOp.sh  -> true.B,
		MemOp.sw  -> true.B
	))
	val din = MuxLookup(io.in.memPack.memOp, 0.U(32.W), Seq(
		MemOp.sb -> Cat(io.in.memPack.regData(7, 0), io.in.memPack.regData(7, 0), io.in.memPack.regData(7, 0), io.in.memPack.regData(7, 0)),
		MemOp.sh -> Cat(io.in.memPack.regData(15, 0), io.in.memPack.regData(15, 0)),
		MemOp.sw -> io.in.memPack.regData
	))

	io.sramDataPort.data_req := (re || we) && !io.out.excPack.excFlag && io.nextAllowIn
	io.sramDataPort.data_wr  := we
	io.sramDataPort.data_size := MuxLookup(io.in.memPack.memOp, 2.U(2.W), Seq(
		MemOp.sb  -> 0.U(2.W),
		MemOp.sh  -> 1.U(2.W),
		MemOp.sw  -> 2.U(2.W)
	))
	io.sramDataPort.data_addr := Mux(io.out.memPack.aluData(31, 30).andR(),
		io.out.memPack.aluData(31, 0),
		io.out.memPack.aluData(28, 0)
	)
	io.sramDataPort.data_wdata := din

	val received = io.sramDataPort.data_req && io.sramDataPort.data_addr_ok

	io.currAllowIn := received || (!io.sramDataPort.data_req && io.nextAllowIn)
	io.currReadyGo := received || (!io.sramDataPort.data_req && io.nextAllowIn)
	io.currMemOpGo := Mux(io.out.excPack.excFlag, MemOp.nop, io.in.memPack.memOp)

	io.out.memPack <> io.in.memPack
	io.out.regPack <> io.in.regPack
	io.out.mhiPack <> io.in.mhiPack
	io.out.mloPack <> io.in.mloPack
	io.out.cp0Pack <> io.in.cp0Pack

	when(!io.in.excPack.excFlag) {
		when(MuxLookup(io.in.memPack.memOp, false.B, Seq(
			MemOp.lb  -> false.B,
			MemOp.lbu -> false.B,
			MemOp.lh  -> (io.in.memPack.aluData(0) =/= 0.U(1.W)),
			MemOp.lhu -> (io.in.memPack.aluData(0) =/= 0.U(1.W)),
			MemOp.lw  -> (io.in.memPack.aluData(1, 0) =/= 0.U(2.W))
		))) {
			io.out.excPack.excFlag := true.B
			io.out.excPack.excSlot := io.in.excPack.excSlot
			io.out.excPack.excCode := ExcCode.AdEL
			io.out.excPack.excAddr := io.in.memPack.aluData
		}.elsewhen(MuxLookup(io.in.memPack.memOp, false.B, Seq(
			MemOp.sb -> false.B,
			MemOp.sh -> (io.in.memPack.aluData(0) =/= 0.U(1.W)),
			MemOp.sw -> (io.in.memPack.aluData(1, 0) =/= 0.U(2.W))
		))) {
			io.out.excPack.excFlag := true.B
			io.out.excPack.excSlot := io.in.excPack.excSlot
			io.out.excPack.excCode := ExcCode.AdES
			io.out.excPack.excAddr := io.in.memPack.aluData
		}.otherwise {
			io.out.excPack.excFlag := false.B
			io.out.excPack.excSlot := io.in.excPack.excSlot
			io.out.excPack.excCode := ExcCode.Int
			io.out.excPack.excAddr := 0.U(32.W)
		}
	}.otherwise {
		io.out.excPack <> io.in.excPack
	}

	io.bypass.valid := MuxCase(false.B, Seq(
		io.in.mhiPack.hiRe  -> true.B,
		io.in.mloPack.loRe  -> true.B,
		io.in.cp0Pack.cp0Re -> false.B,
		io.in.regPack.ramRe -> false.B,
		io.in.regPack.regWe -> true.B
	))
	io.bypass.cp0Re := io.in.cp0Pack.cp0Re
	io.bypass.ramRe := io.in.regPack.ramRe
	io.bypass.regWe := io.in.regPack.regWe
	io.bypass.regRd := io.in.regPack.regRd
	io.bypass.regDi := MuxCase(0.U(32.W), Seq(
		io.in.mhiPack.hiRe  -> io.in.mhiPack.hi,
		io.in.mloPack.loRe  -> io.in.mloPack.lo,
		io.in.cp0Pack.cp0Re -> DontCare,
		io.in.regPack.ramRe -> DontCare,
		io.in.regPack.regWe -> io.in.memPack.aluData
	))
	io.bypass.excFlag := io.out.excPack.excFlag
}

