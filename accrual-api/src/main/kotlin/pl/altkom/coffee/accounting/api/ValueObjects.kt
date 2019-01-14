package pl.altkom.coffee.accounting.api

import org.axonframework.common.IdentifierFactory
import java.io.Serializable
import java.math.BigDecimal
import java.math.MathContext

data class OperationId(val identifier: String = IdentifierFactory.getInstance().generateIdentifier(), val source: String) : Serializable {

    companion object {
        private const val serialVersionUID = -5267104328616955617L
    }
}

data class Money(val value: BigDecimal) {

    constructor(valueString: String) : this(BigDecimal(valueString))

    private val mc = MathContext(2)

    fun add(augend: Money): Money {
        return Money(value.add(augend.value))
    }

    fun subtract(subtrahend: Money): Money {
        return Money(value.subtract(subtrahend.value, mc))
    }

    fun negate(): Money {
        return Money(value.negate(mc))
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Money

        if (value.compareTo(other.value) != 0) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + mc.hashCode()
        return result
    }

}
