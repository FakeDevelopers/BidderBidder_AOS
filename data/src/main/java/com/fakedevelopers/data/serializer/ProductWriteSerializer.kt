package com.fakedevelopers.data.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.fakedevelopers.data.model.ProductWrite
import java.io.InputStream
import java.io.OutputStream

object ProductWriteSerializer : Serializer<ProductWrite> {
    override val defaultValue: ProductWrite =
        ProductWrite.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProductWrite {
        try {
            return ProductWrite.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: ProductWrite, output: OutputStream) =
        t.writeTo(output)
}
