/*
 * Copyright (C) 2024 James Richardson
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package dev.forkhandles.ropes4k.test.bench

import dev.forkhandles.ropes4k.Rope
import dev.forkhandles.ropes4k.test.bench.BenchmarkFiles.Companion.aChristmasCarol
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
open class AppendBenchmark {

    data class Append(val offset: Int, val length: Int)

    val length = aChristmasCarol.length
    val appends = (0 until BenchmarkFiles.PLAN_LENGTH).map {
        val offset = BenchmarkFiles.random.nextInt(length)
        Append(
            offset, BenchmarkFiles.random.nextInt(length - offset)
        )
    }

    val checksum: Int

    init {
        var s = aChristmasCarol
        appends.forEach {
            s = s + s.substring(it.offset, it.offset + it.length)
        }
        checksum = s.length
    }

    @Benchmark
    fun string(): Int {
        var s = aChristmasCarol
        appends.forEach {
            s = s + s.substring(it.offset, it.offset + it.length)
        }
        check(s.length == checksum)
        return s.length
    }

    @Benchmark
    fun stringbuffer(): Int {
        val sb = StringBuilder(aChristmasCarol)

        appends.forEach {
            sb.append(sb.subSequence(it.offset, it.offset + it.length))
        }
        check(sb.length == checksum)
        return sb.length
    }

    @Benchmark
    fun rope(): Int {
        var rope = Rope.of(aChristmasCarol)

        appends.forEach {
            rope = rope.append(rope.subSequence(it.offset, it.offset + it.length))
        }

        check(rope.length == checksum)
        return rope.length
    }
}
