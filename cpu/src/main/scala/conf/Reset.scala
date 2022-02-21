package conf

import chisel3._

object Reset {
	val PCR_RESET = 0xbfbffffcL.U(32.W)
	val EXC_ENTRY = 0xbfc00380L.U(32.W)
}

