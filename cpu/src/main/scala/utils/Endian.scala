package utils

import chisel3._
import chisel3.util._

object Endian {
    def TransEndian32(data: UInt): UInt = {
        Cat(
            data(7, 0),
            data(15, 8),
            data(23, 16),
            data(31, 24)
        )
    }

    def TransEndian64(data: UInt): UInt = {
        Cat(
            TransEndian32(data(31, 0)),
            TransEndian32(data(63, 32))
        )
    }
}

