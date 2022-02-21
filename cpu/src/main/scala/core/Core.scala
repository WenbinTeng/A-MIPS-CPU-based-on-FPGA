package core

import chisel3._
import cons.DebugPack
import memo.{SramDataPort, SramInstPort}

class Core extends Module {
	val io = IO(new Bundle {
		val extInt = Input(UInt(6.W))

		val sramInstPort = Flipped(new SramInstPort)
		val sramDataPort = Flipped(new SramDataPort)

		val debugPack = Output(new DebugPack)
	})

	val s0     = Module(new S0)
	val s0s1io = Module(new FF(new S0S1IO))
	val s1     = Module(new S1)
	val s1s2io = Module(new FF(new S1S2IO))
	val s2     = Module(new S2)
	val s2s3io = Module(new FF(new S2S3IO))
	val s3     = Module(new S3)
	val s3s4io = Module(new FF(new S3S4IO))
	val s4     = Module(new S4)
	val s4s5io = Module(new FF(new S4S5IO))
	val s5 	   = Module(new S5)
	val s5s6io = Module(new FF(new S5S6IO))
	val s6     = Module(new S6)

	val pipeCtrl = Module(new PipeCtrl)

	s0s1io.io.din <> s0.io.out
	s1.io.in <> s0s1io.io.dout
	s1s2io.io.din <> s1.io.out
	s2.io.in <> s1s2io.io.dout
	s2s3io.io.din <> s2.io.out
	s3.io.in <> s2s3io.io.dout
	s3s4io.io.din <> s3.io.out
	s4.io.in <> s3s4io.io.dout
	s4s5io.io.din <> s4.io.out
	s5.io.in <> s4s5io.io.dout
	s5s6io.io.din <> s5.io.out
	s6.io.in <> s5s6io.io.dout

	s0s1io.io.npc := s0.io.npc
	s1s2io.io.npc := s0s1io.io.pc
	s2s3io.io.npc := s1s2io.io.pc
	s3s4io.io.npc := s2s3io.io.pc
	s4s5io.io.npc := s3s4io.io.pc
	s5s6io.io.npc := s4s5io.io.pc

	s0s1io.io.refillFlag := pipeCtrl.io.refillFlag
	s0s1io.io.refillAddr := pipeCtrl.io.refillAddr
	s1s2io.io.refillFlag := false.B
	s1s2io.io.refillAddr := DontCare
	s2s3io.io.refillFlag := false.B
	s2s3io.io.refillAddr := DontCare
	s3s4io.io.refillFlag := false.B
	s3s4io.io.refillAddr := DontCare
	s4s5io.io.refillFlag := false.B
	s4s5io.io.refillAddr := DontCare
	s5s6io.io.refillFlag := false.B
	s5s6io.io.refillAddr := DontCare

	s0s1io.io.prevReadyGo := s0.io.currReadyGo
	s0s1io.io.nextAllowIn := s1.io.currAllowIn
	s1s2io.io.prevReadyGo := s1.io.currReadyGo
	s1s2io.io.nextAllowIn := s2.io.currAllowIn
	s2s3io.io.prevReadyGo := s2.io.currReadyGo
	s2s3io.io.nextAllowIn := s3.io.currAllowIn
	s3s4io.io.prevReadyGo := s3.io.currReadyGo
	s3s4io.io.nextAllowIn := s4.io.currAllowIn
	s4s5io.io.prevReadyGo := s4.io.currReadyGo
	s4s5io.io.nextAllowIn := s5.io.currAllowIn
	s5s6io.io.prevReadyGo := s5.io.currReadyGo
	s5s6io.io.nextAllowIn := true.B

	s0s1io.io.flushSignal := pipeCtrl.io.flushSignal(0)
	s1s2io.io.flushSignal := pipeCtrl.io.flushSignal(1)
	s2s3io.io.flushSignal := pipeCtrl.io.flushSignal(2)
	s3s4io.io.flushSignal := pipeCtrl.io.flushSignal(3)
	s4s5io.io.flushSignal := pipeCtrl.io.flushSignal(4)
	s5s6io.io.flushSignal := pipeCtrl.io.flushSignal(5)

	s1.io.flushSignal := pipeCtrl.io.flushSignal(0)
	s5.io.flushSignal := pipeCtrl.io.flushSignal(4)

	s0.io.refillFlag := pipeCtrl.io.refillFlag
	s0.io.refillAddr := pipeCtrl.io.refillAddr

	s1.io.branchFlag := s2.io.branchFlag

	s0.io.nextAllowIn := s1.io.currAllowIn
	s1.io.prevReadyGo := s0.io.currReadyGo
	s1.io.nextAllowIn := s2.io.currAllowIn
	s2.io.prevReadyGo := s1.io.currReadyGo
	s2.io.nextAllowIn := s3.io.currAllowIn
	s3.io.nextAllowIn := s4.io.currAllowIn
	s4.io.nextAllowIn := s5.io.currAllowIn
	s5.io.prevReadyGo := s4.io.currReadyGo
	s5.io.prevMemOpGo := s4.io.currMemOpGo
	s5.io.nextAllowIn := true.B

	s2.io.bypass3 := s3.io.bypass
	s2.io.bypass4 := s4.io.bypass
	s2.io.bypass5 := s5.io.bypass
	s2.io.bypass6 := s6.io.bypass

	s0.io.pc := s0s1io.io.pc
	s2.io.pc := s1s2io.io.pc
	s6.io.pc := s5s6io.io.pc

	s3.io.excFlag4 := s4.io.bypass.excFlag
	s3.io.excFlag5 := s5.io.bypass.excFlag
	s3.io.excFlag6 := s6.io.bypass.excFlag

	s6.io.extInt := io.extInt

	pipeCtrl.io.ehdlFlag := s6.io.ehdlFlag
	pipeCtrl.io.ehdlAddr := s6.io.ehdlAddr
	pipeCtrl.io.eretFlag := s6.io.eretFlag
	pipeCtrl.io.eretAddr := s6.io.eretAddr
	pipeCtrl.io.branchMiss := s2.io.branchMiss
	pipeCtrl.io.branchAddr := s2.io.branchAddr

	io.sramInstPort.inst_req 		:= s0.io.sramInstPort.inst_req
	io.sramInstPort.inst_wr  		:= s0.io.sramInstPort.inst_wr
	io.sramInstPort.inst_size 		:= s0.io.sramInstPort.inst_size
	io.sramInstPort.inst_addr 		:= s0.io.sramInstPort.inst_addr
	io.sramInstPort.inst_wdata 		:= s0.io.sramInstPort.inst_wdata
	s0.io.sramInstPort.inst_rdata 	:= DontCare
	s0.io.sramInstPort.inst_addr_ok := io.sramInstPort.inst_addr_ok
	s0.io.sramInstPort.inst_data_ok := DontCare
	s1.io.sramInstPort.inst_rdata 	:= io.sramInstPort.inst_rdata
	s1.io.sramInstPort.inst_addr_ok := DontCare
	s1.io.sramInstPort.inst_data_ok := io.sramInstPort.inst_data_ok

	io.sramDataPort.data_req		:= s4.io.sramDataPort.data_req
	io.sramDataPort.data_wr			:= s4.io.sramDataPort.data_wr
	io.sramDataPort.data_size		:= s4.io.sramDataPort.data_size
	io.sramDataPort.data_addr		:= s4.io.sramDataPort.data_addr
	io.sramDataPort.data_wdata		:= s4.io.sramDataPort.data_wdata
	s4.io.sramDataPort.data_rdata	:= DontCare
	s4.io.sramDataPort.data_addr_ok := io.sramDataPort.data_addr_ok
	s4.io.sramDataPort.data_data_ok := DontCare
	s5.io.sramDataPort.data_rdata	:= io.sramDataPort.data_rdata
	s5.io.sramDataPort.data_addr_ok := DontCare
	s5.io.sramDataPort.data_data_ok := io.sramDataPort.data_data_ok

	io.debugPack.debugPc := s5s6io.io.pc
	io.debugPack.debugWe := s6.io.bypass.regWe
	io.debugPack.debugRd := s6.io.bypass.regRd
	io.debugPack.debugDi := s6.io.bypass.regDi
}

