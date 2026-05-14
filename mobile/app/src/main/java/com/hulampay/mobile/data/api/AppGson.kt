package com.hulampay.mobile.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Shared Gson instance for the entire app.
 *
 * Registers a String TypeAdapter that converts JSON `null` to `""` so non-null
 * Kotlin String fields never end up holding null after Gson reflectively
 * deserializes them.
 *
 * Why this exists: Gson uses `Unsafe.allocateInstance` to bypass Kotlin's
 * primary constructor, so default values like `val name: String = ""` are
 * never applied. A JSON null on such a field would land in the field as
 * actual null and the first String operation (.isBlank, .lowercase, .equals,
 * .trim, …) crashes with `Parameter specified as non-null is null`.
 *
 * Trade-off: nullable `String?` fields also receive `""` instead of `null`
 * from JSON. For display logic this is harmless because consumers typically
 * use `.isBlank()` / `.isNullOrBlank()` checks. If a specific field must
 * preserve null semantics, attach a per-field @JsonAdapter override.
 *
 * Use this everywhere the app deserializes JSON. Inject the Hilt-provided
 * `Gson` in production code; reach for [AppGson.instance] only in non-DI
 * call sites (utility helpers, top-level objects).
 */
object AppGson {
    val instance: Gson = GsonBuilder()
        .registerTypeAdapter(String::class.java, NullSafeStringAdapter)
        .create()
}

private object NullSafeStringAdapter : TypeAdapter<String>() {
    override fun write(out: JsonWriter, value: String?) {
        if (value == null) out.nullValue() else out.value(value)
    }

    override fun read(reader: JsonReader): String {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return ""
        }
        return reader.nextString()
    }
}
