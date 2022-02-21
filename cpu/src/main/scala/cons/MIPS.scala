package cons

import chisel3.util.BitPat

object MIPS {
	val NOP = BitPat("b00000000000000000000000000000000")

	val ADD   = BitPat("b000000???????????????00000100000")
	val ADDI  = BitPat("b001000??????????????????????????")
	val ADDU  = BitPat("b000000???????????????00000100001")
	val ADDIU = BitPat("b001001??????????????????????????")
	val SUB   = BitPat("b000000???????????????00000100010")
	val SUBU  = BitPat("b000000???????????????00000100011")
	val SLT   = BitPat("b000000???????????????00000101010")
	val SLTI  = BitPat("b001010??????????????????????????")
	val SLTU  = BitPat("b000000???????????????00000101011")
	val SLTIU = BitPat("b001011??????????????????????????")
	val DIV   = BitPat("b000000??????????0000000000011010")
	val DIVU  = BitPat("b000000??????????0000000000011011")
	val MULT  = BitPat("b000000??????????0000000000011000")
	val MULTU = BitPat("b000000??????????0000000000011001")

	val AND  = BitPat("b000000???????????????00000100100")
	val ANDI = BitPat("b001100??????????????????????????")
	val LUI  = BitPat("b00111100000?????????????????????")
	val NOR  = BitPat("b000000???????????????00000100111")
	val OR   = BitPat("b000000???????????????00000100101")
	val ORI  = BitPat("b001101??????????????????????????")
	val XOR  = BitPat("b000000???????????????00000100110")
	val XORI = BitPat("b001110??????????????????????????")

	val SLLV = BitPat("b000000???????????????00000000100")
	val SLL  = BitPat("b00000000000???????????????000000")
	val SRAV = BitPat("b000000???????????????00000000111")
	val SRA  = BitPat("b00000000000???????????????000011")
	val SRLV = BitPat("b000000???????????????00000000110")
	val SRL  = BitPat("b00000000000???????????????000010")

	val BEQ    = BitPat("b000100??????????????????????????")
	val BNE    = BitPat("b000101??????????????????????????")
	val BGEZ   = BitPat("b000001?????00001????????????????")
	val BGTZ   = BitPat("b000111?????00000????????????????")
	val BLEZ   = BitPat("b000110?????00000????????????????")
	val BLTZ   = BitPat("b000001?????00000????????????????")
	val BGEZAL = BitPat("b000001?????10001????????????????")
	val BLTZAL = BitPat("b000001?????10000????????????????")
	val J      = BitPat("b000010??????????????????????????")
	val JAL    = BitPat("b000011??????????????????????????")
	val JR     = BitPat("b000000?????000000000000000001000")
	val JALR   = BitPat("b000000?????00000?????00000001001")

	val LB  = BitPat("b100000??????????????????????????")
	val LBU = BitPat("b100100??????????????????????????")
	val LH  = BitPat("b100001??????????????????????????")
	val LHU = BitPat("b100101??????????????????????????")
	val LW  = BitPat("b100011??????????????????????????")
	val SB  = BitPat("b101000??????????????????????????")
	val SH  = BitPat("b101001??????????????????????????")
	val SW  = BitPat("b101011??????????????????????????")

	val MFHI = BitPat("b0000000000000000?????00000010000")
	val MFLO = BitPat("b0000000000000000?????00000010010")
	val MTHI = BitPat("b000000?????000000000000000010001")
	val MTLO = BitPat("b000000?????000000000000000010011")

	val BREAK   = BitPat("b000000????????????????????001101")
	val SYSCALL = BitPat("b000000????????????????????001100")

	val ERET = BitPat("b01000010000000000000000000011000")
	val MFC0 = BitPat("b01000000000??????????00000000???")
	val MTC0 = BitPat("b01000000100??????????00000000???")

	val RI = BitPat("b????????????????????????????????")
}
