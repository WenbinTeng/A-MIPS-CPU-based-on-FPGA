package core

import chisel3._
import chisel3.util._
import memo.SramInstPort

class S0 extends Module {
    val io = IO(new Bundle {
        val out = Output(new S0S1IO)

        val pc  = Input(UInt(32.W))
        val npc = Output(UInt(32.W))

        val refillFlag = Input(Bool())
        val refillAddr = Input(UInt(32.W))

        val sramInstPort = Flipped(new SramInstPort)

        val nextAllowIn = Input(Bool())
        val currAllowIn     = Output(Bool())
        val currReadyGo     = Output(Bool())
    })

    io.npc := Mux(io.refillFlag, io.refillAddr, io.pc + 4.U(32.W))

    io.sramInstPort.inst_req   := !this.reset.asBool() && io.nextAllowIn
    io.sramInstPort.inst_wr    := false.B
    io.sramInstPort.inst_size  := 2.U(2.W)
    io.sramInstPort.inst_addr  := Cat(io.npc(31, 2), 0.U(2.W))
    io.sramInstPort.inst_wdata := 0.U(32.W)

    val received = io.sramInstPort.inst_req && io.sramInstPort.inst_addr_ok

    io.currAllowIn := received
    io.currReadyGo := received

    io.out.fetPack.send := received

    when (io.sramInstPort.inst_req && io.npc(1, 0) =/= 0.U(2.W)) {
        io.out.excPack.excFlag := true.B
        io.out.excPack.excSlot := false.B
        io.out.excPack.excCode := ExcCode.AdEL
        io.out.excPack.excAddr := io.npc
    }.otherwise {
        io.out.excPack.excFlag := false.B
        io.out.excPack.excSlot := false.B
        io.out.excPack.excCode := ExcCode.Int
        io.out.excPack.excAddr := DontCare
    }
}

