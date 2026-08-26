package com.racktrack.i18n

/** Resolves [StringKey] values for the active locale. */
fun interface StringProvider {
    fun get(key: StringKey): String
}

/**
 * Provider that overlays a locale map on [fallback] (English by default).
 * Missing keys fall back so catalogs can grow incrementally.
 */
class OverlayStringProvider(
    private val overlay: Map<StringKey, String>,
    private val fallback: StringProvider = EnStrings,
) : StringProvider {
    override fun get(key: StringKey): String = overlay[key] ?: fallback.get(key)
}

/** Process-wide provider; Android sets this from the system locale at cold start. */
object Strings {
    var provider: StringProvider = EnStrings

    fun get(key: StringKey): String = provider.get(key)

    /** Replaces `{0}`, `{1}`, … placeholders in the resolved string. */
    fun format(
        key: StringKey,
        vararg args: Any,
    ): String {
        var result = get(key)
        args.forEachIndexed { index, arg ->
            result = result.replace("{$index}", arg.toString())
        }
        return result
    }

    fun format(
        provider: StringProvider,
        key: StringKey,
        vararg args: Any,
    ): String {
        var result = provider.get(key)
        args.forEachIndexed { index, arg ->
            result = result.replace("{$index}", arg.toString())
        }
        return result
    }
}
