package com.thebinaryheap.ispradius.settings.validation

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.thebinaryheap.ispradius.common.command.CommandStatus
import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class CronPatternValidatorTest {

    @InjectMocks
    lateinit var cronPatternValidator: CronPatternValidator

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `test isValid should return true for valid cron pattern`() {
        val validCronPattern = "0 0 12 * * ?"
        val cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ)

        assertEquals(true, cronPatternValidator.isValid(validCronPattern,
            Mockito.mock(ConstraintValidatorContext::class.java)
        ))
    }

    @Test
    fun `test isValid should return false for invalid cron pattern`() {
        val invalidCronPattern = "invalid-cron-pattern"
        assertEquals(false, cronPatternValidator.isValid(invalidCronPattern,
            Mockito.mock(ConstraintValidatorContext::class.java)
        ))
    }
}

class ValidateCronPatternTest {

    @Test
    fun `test execute should return success for valid cron pattern`() {
        val validCronPattern = "0 0 12 * * ?"
        val command = ValidateCronPattern(validCronPattern)
        val response = command.execute()
        assertEquals(CommandStatus.SUCCESS, response.status)
    }

    @Test
    fun `test execute should return error for invalid cron pattern`() {
        val invalidCronPattern = "invalid-cron-pattern"
        val command = ValidateCronPattern(invalidCronPattern)
        val response = command.execute()
        assertEquals(CommandStatus.ERROR, response.status)
    }
}
