package dev.jovanni0.utils

enum class CoverSize(val query_value: String, val max_width: Int?)
{
    ORIGINAL("original", null),
    THUMBNAIL("thumbnail", 360),
    ;

    companion object
    {
        fun fromQuery(value: String?): CoverSize?
        {
            if (value == null)
            {
                return ORIGINAL
            }

            return entries.firstOrNull { it.query_value == value }
        }
    }
}