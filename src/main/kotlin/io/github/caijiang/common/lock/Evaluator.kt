package io.github.caijiang.common.lock

import org.springframework.beans.factory.BeanFactory
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.context.expression.CachedExpressionEvaluator
import org.springframework.context.expression.MethodBasedEvaluationContext
import org.springframework.expression.EvaluationContext
import org.springframework.expression.Expression
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * @author CJ
 */
internal class Evaluator(
    private val beanFactory: BeanFactory
) : CachedExpressionEvaluator() {

    private val keyCache: MutableMap<Any, Expression> = ConcurrentHashMap(64)

    internal fun createEvaluationContext(method: Method, args: Array<Any>, target: Any): EvaluationContext {
        val root = Root(method, args, target)
        val context = MethodBasedEvaluationContext(root, method, args, parameterNameDiscoverer)
        context.beanResolver = BeanFactoryResolver(beanFactory)
        return context
    }

    fun getCachedExpression(expressionString: String, key: Any): Expression {
        return keyCache.getOrPut(expressionString to key) {
            parseExpression(expressionString)
        }
    }

    data class Root(val method: Method, val args: Array<Any>, val target: Any) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Root) return false

            if (method != other.method) return false
            if (target != other.target) return false

            return true
        }

        override fun hashCode(): Int {
            var result = method.hashCode()
            result = 31 * result + target.hashCode()
            return result
        }
    }
}