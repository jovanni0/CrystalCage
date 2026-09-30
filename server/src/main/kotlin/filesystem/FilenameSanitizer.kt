package dev.jovanni0.filesystem

import java.text.Normalizer



object FilenameSanitizer
{
    private val FORBIDDEN_CHARS = charArrayOf('<', '>', ':', '"', '/', '\\', '|', '?', '*')


    /**
     * transliterates accented/regional characters to their closest SCII equivalent, strips characters reserved for
     * common filesystems, and collapses whitespaces.
     */
    fun sanitize(input: String): String
    {
        val decomposed = Normalizer.normalize(input, Normalizer.Form.NFKD)
        val without_diacritics = decomposed.replace(Regex("\\p{M}+"), "")

        val cleaned = without_diacritics
            .filter { it !in FORBIDDEN_CHARS }
            .map {
                if(it.code in 32.. 126)
                    it
                else
                    '_'
            }
            .joinToString("")

        return cleaned
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
