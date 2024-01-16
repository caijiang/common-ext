package io.github.caijiang.common.jpa

import io.github.caijiang.common.AttributeConverter

/**
 * @since 0.0.5
 * @author CJ
 */
@Suppress("unused", "com.haulmont.jpb.ConverterNotAnnotatedInspection")
abstract class NullableAttributeConverter<X, Y> : AttributeConverter<X, Y> {
    override fun convertToDatabaseColumn(attribute: X?): Y? {
        if (attribute == null) {
            return null
        }
        return noNullConvertToDatabaseColumn(attribute)
    }

    abstract fun noNullConvertToDatabaseColumn(attribute: X): Y?

    override fun convertToEntityAttribute(dbData: Y?): X? {
        if (dbData == null) {
            return null
        }
        return noNullConvertToEntityAttribute(dbData)
    }

    abstract fun noNullConvertToEntityAttribute(dbData: Y): X?
}