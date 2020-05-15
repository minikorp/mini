package com.minikorp.mini

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be null`
import org.junit.Before
import org.junit.Test

class ResourceTest {

    private var successValue: Any? = null
    private var errorValue: Any? = null
    private var loadingValue: Any? = null
    private var emptyValue: Any? = null

    @Before
    fun before() {
        successValue = null
        errorValue = null
        loadingValue = null
        emptyValue = null
    }

    private fun <T> check(resource: Resource<T>) {
        var called = 0
        resource
            .onSuccess {
                called++
                successValue = it
            }.onFailure {
                called++
                errorValue = it
            }.onLoading {
                called++
                loadingValue = it
            }.onEmpty {
                called++
                emptyValue = true
            }
        called `should be equal to` 1
    }

    @Test
    fun `success calls`() {
        check(Resource.success("abc"))
        successValue `should equal` "abc"
    }

    @Test
    fun isEmpty() {
        check(Resource.empty<Any>())
        emptyValue `should equal` true
    }

    @Test
    fun isFailure() {
        val ex = RuntimeException("ABC")
        check(Resource.failure<Any>(ex))
        errorValue `should equal` ex
    }

    @Test
    fun isLoading() {
        check(Resource.loading<Any>("abc"))
        loadingValue `should equal` "abc"
    }

    @Test
    fun getOrNull() {
        Resource.empty<Any>().getOrNull().`should be null`()
        Resource.success("abc").getOrNull().`should not be null`()
    }

    @Test
    fun exceptionOrNull() {
        Resource.failure<Any>(RuntimeException()).exceptionOrNull().`should not be null`()
        Resource.success<Any>("abc").exceptionOrNull().`should be null`()
    }

    @Test
    fun map() {
        Resource.success("abc")
            .map { 0 }
            .getOrNull()?.`should be equal to`(0)

        Resource.failure<Any>()
            .map { 0 }
            .getOrNull()?.`should be null`()
    }
}