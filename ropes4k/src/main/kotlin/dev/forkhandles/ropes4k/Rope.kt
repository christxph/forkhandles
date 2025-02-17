/*
 * Copyright (C) 2024 James Richardson
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package dev.forkhandles.ropes4k

import dev.forkhandles.ropes4k.impl.FlatCharArrayRope
import dev.forkhandles.ropes4k.impl.FlatCharSequenceRope
import java.io.IOException
import java.io.Serializable
import java.io.Writer
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A rope represents character strings. Ropes are immutable.
 *
 * Rope operations scale well to very
 * long character strings. Most mutation operations run in O(log n)
 * time or better.
 *
 * However, random-access character retrieval is
 * generally slower than for a String. By traversing consecutive
 * characters with an iterator instead, performance improves to
 * O(1).
 *
 * This rope implementation implements all performance optimizations
 * outlined in "[Ropes: an Alternative to Strings](http://www.cs.ubc.ca/local/reading/proceedings/spe91-95/spe/vol25/issue12/spe986.pdf)"
 * by Hans-J. Boehm, Russ Atkinson and Michael Plass, including,
 * notably, deferred evaluation of long substrings and automatic
 * rebalancing.
 *
 * <h4>Immutability (a Caveat)</h4>
 * A rope is immutable. Specifically, calling any mutator function
 * on a rope always returns a modified copy; the original rope is
 * left untouched. However, care must be taken to build ropes from
 * immutable `CharSequences` such as `Strings`,
 * or else from mutable `CharSequences` that your program
 * <emph>guarantees will not change</emph>. Failure to do so will result in
 * logic errors.
 */
public interface Rope : CharSequence, Iterable<Char>, Comparable<CharSequence>, Serializable {
    /**
     * Returns a new rope created by appending the specified character to
     * this rope.
     */
    public fun append(c: Char): Rope

    /**
     * Returns a new rope created by appending the specified character sequence to
     * this rope.
     */
    public fun append(chars: CharSequence): Rope

    /**
     * Returns a new rope created by appending the specified character range to
     * this rope.
     */
    public fun append(chars: CharSequence, start: Int, end: Int): Rope

    /**
     * Creats a new rope by delete the specified character substring.
     * The substring begins at the specified `start` and extends to
     * the character at index `end - 1` or to the end of the
     * sequence if no such character exists. If
     * `start` is equal to `end`, no changes are made.
     *
     * @param      start  The beginning index, inclusive.
     * @param      end    The ending index, exclusive.
     */
    public fun delete(start: Int, end: Int): Rope

    /**
     * Returns the index within this rope of the first occurrence of the
     * specified character, beginning at the specified index. If a character
     * with value `ch` occurs in the character sequence
     * represented by this `Rope` object, then the index of the
     * first such occurrence is returned;that is, the smallest value k
     * such that:
     *
     *
     * `this.charAt(k) == ch`
     *
     *
     * is `true`. If no such character occurs in this string, then
     * `-1` is returned.
     * @param ch a character.
     * @param fromIndex the index to start searching from.
     * @return the index of the first occurrence of the character in the character
     * sequence represented by this object, or -1 if the character does not occur.
     */
    public fun indexOf(ch: Char, fromIndex: Int = 0): Int

    /**
     * Returns the index within this rope of the first occurrence of the
     * specified string, beginning at the specified index. The value returned
     * is the smallest *k* such that:
     * <pre>
     * k >= fromIndex && this.startsWith(str, k)
    </pre> *
     * If no such *k* exists, then -1 is returned.
     * @param sequence the string to find.
     * @param fromIndex the index to start searching from.
     * @return the index of the first occurrence of the specified string, or
     * -1 if the specified string does not occur.
     */
    public fun indexOf(sequence: CharSequence, fromIndex: Int = 0): Int

    /**
     * Creates a new rope by inserting the specified `CharSequence`
     * into this rope.
     *
     *
     * The characters of the `CharSequence` argument are inserted,
     * in order, into this rope at the indicated offset.
     *
     * @param      at the offset.
     * @param      chars the sequence to be inserted
     * @return     a reference to the new Rope.
     * @throws     IndexOutOfBoundsException  if the offset is invalid.
     */
    public fun insert(at: Int, chars: CharSequence): Rope

    /**
     * Returns an iterator positioned to start at the specified index.
     * @param start the start position.
     * @return an iterator positioned to start at the specified index.
     */
    public fun iterator(start: Int): Iterator<Char>

    /**
     * Trims all whitespace as well as characters less than 0x20 from
     * the beginning of this string.
     * @return a rope with all leading whitespace trimmed.
     */
    public fun trimStart(): Rope

    /**
     * Creates a matcher that will match this rope against the
     * specified pattern. This method produces a higher performance
     * matcher than:
     * <pre>
     * Matcher m = pattern.matcher(this);
    </pre> *
     * The difference may be asymptotically better in some cases.
     * @param pattern the pattern to match this rope against.
     * @return a matcher.
     */
    public fun matcher(pattern: Pattern): Matcher

    /**
     * Reverses this rope.
     * @return a reversed copy of this rope.
     */
    public fun reverse(): Rope

    /**
     * Returns a reverse iterator positioned to start at the specified index.
     * A reverse iterator moves backwards instead of forwards through a rope.
     * @param start the start position.

     * @return a reverse iterator positioned to start at the specified index from
     * the end of the rope. For example, a value of 1 indicates the iterator
     * should start 1 character before the end of the rope.
     */
    public fun reverseIterator(start: Int = 0): Iterator<Char>

    /**
     * Trims all whitespace as well as characters less than `0x20` from
     * the end of this rope.
     * @return a rope with all trailing whitespace trimmed.
     */
    public fun trimEnd(): Rope

    override fun subSequence(startIndex: Int, endIndex: Int): Rope

    /**
     * Trims all whitespace as well as characters less than `0x20` from
     * the beginning and end of this string.
     * @return a rope with all leading and trailing whitespace trimmed.
     */
    public fun trim(): Rope

    /**
     * Write this rope to a `Writer`.
     */
    @Throws(IOException::class)
    public fun write(out: Writer)

    /**
     * Write a range of this rope to a `Writer`.
     */
    @Throws(IOException::class)
    public fun write(out: Writer, offset: Int, length: Int)

    /**
     * Increase the length of this rope to the specified length by repeatedly
     * prepending the specified character to this rope. If the specified length
     * is less than or equal to the current length of the rope, the rope is
     * returned unmodified.
     */
    public fun padStart(toLength: Int, padChar: Char = ' '): Rope

    /**
     * Increase the length of this rope to the specified length by repeatedly
     * appending the specified character to this rope. If the specified length
     * is less than or equal to the current length of the rope, the rope is
     * returned unmodified.
     */
    public fun padEnd(toLength: Int, padChar: Char = ' '): Rope

    /**
     * Returns `true` if this rope, beginning from a specified
     * offset, starts with the specified prefix.
     */
    public fun startsWith(prefix: CharSequence, offset: Int = 0): Boolean

    /**
     * Returns `true` if this rope, terminated at a specified
     * offset, ends with the specified suffix.
     * @param suffix the suffix to test.
     * @param offset the termination offset, counted from the end of the
     * rope.
     * @return `true` if this rope starts with the
     * specified prefix and `false` otherwise.
     */
    public fun endsWith(suffix: CharSequence, offset: Int = 0): Boolean

    public operator fun plus(other: Rope): Rope = append(other)
    public operator fun plus(other: CharSequence): Rope = append(of(other))
    public operator fun plus(other: CharArray): Rope = append(ofCopy(other))
    public operator fun times(n: Int): Rope {
        require(n >= 0) { "Count 'n' must be non-negative, but was $n." }

        return when (n) {
            0 -> of("")
            1 -> this
            else -> {
                when (length) {
                    0 -> this
                    else -> {
                        (1 until n).fold(of(this)) { acc: Rope, _: Int ->
                            acc + this
                        }
                    }
                }
            }
        }
    }

    public fun repeat(n: Int): Rope = this * n

    public companion object {

        public fun ofCopy(chars: CharArray): Rope {
            return FlatCharArrayRope(chars)
        }

        public fun of(chars: CharSequence): Rope {
            return when (chars) {
                is Rope -> chars
                else -> FlatCharSequenceRope(chars)
            }
        }
    }
}
