package au.com.dius.pact.model

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.StringSpec
import java.nio.charset.Charset

class OptionalBodySpec : StringSpec() {

    val missingBody = OptionalBody.missing()
    val nullBody = OptionalBody.nullBody()
    val emptyBody = OptionalBody.empty()
    val presentBody = OptionalBody.body("present".toByteArray())

    init {

        "a missing body is missing" {
            (missingBody is OptionalBody.MissingBody) shouldBe true
        }

        "a body that contains a null is not missing" {
            (nullBody is OptionalBody.MissingBody) shouldBe false
        }

        "an empty body is not missing" {
            (emptyBody is OptionalBody.MissingBody) shouldBe false
        }

        "a present body is not missing" {
            (presentBody is OptionalBody.MissingBody) shouldBe false
        }

        "a missing body is not null" {
            (missingBody is OptionalBody.NullBody) shouldBe false
        }

        "a body that contains a null is null" {
            (nullBody is OptionalBody.NullBody) shouldBe true
        }

        "an empty body is not null" {
            (emptyBody is OptionalBody.NullBody) shouldBe false
        }

        "a present body is not null" {
            (presentBody is OptionalBody.NullBody) shouldBe false
        }

        "a missing body is not empty" {
            (missingBody is OptionalBody.EmptyBody) shouldBe false
        }

        "a body that contains a null is not empty" {
            (nullBody is OptionalBody.EmptyBody) shouldBe false
        }

        "an empty body is empty" {
            (emptyBody is OptionalBody.EmptyBody) shouldBe true
        }

        "a present body is not empty" {
            (presentBody is OptionalBody.EmptyBody) shouldBe false
        }

        "a missing body is not present" {
            missingBody.isPresent() shouldBe false
        }

        "a body that contains a null is not present" {
            nullBody.isPresent() shouldBe false
        }

        "an empty body is not present" {
            emptyBody.isPresent() shouldBe false
        }

        "a present body is present" {
            presentBody.isPresent() shouldBe true
        }

        "a missing body or else returns the else" {
            missingBody.orEmptyBinary().toString(Charset.defaultCharset()) shouldEqual ""
        }

        "a body that contains a null or else returns the else" {
            nullBody.orEmptyBinary().toString(Charset.defaultCharset()) shouldEqual ""
        }

        "an empty body or else returns empty" {
            emptyBody.orEmptyBinary().toString(Charset.defaultCharset()) shouldEqual ""
        }

        "a present body or else returns the body" {
            presentBody.orEmptyBinary().toString(Charset.defaultCharset()) shouldEqual "present"
        }
    }
}