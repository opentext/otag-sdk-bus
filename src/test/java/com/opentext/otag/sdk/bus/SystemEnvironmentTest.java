package com.opentext.otag.sdk.bus;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemEnvironmentTest {

    @Test
    public void itShouldBeAbleToRemoveLineFeedCharsFromStrings() {
        assertThat(SystemEnvironment.removeFormattingChars("true\r")).isEqualTo("true");
    }

    @Test
    public void itShouldBeAbleToRemoveNewLineCharsFromStrings() {
        assertThat(SystemEnvironment.removeFormattingChars("true\n")).isEqualTo("true");
    }

    @Test
    public void itShouldBeAbleToRemoveWindowsCrlfCharsFromStrings() {
        assertThat(SystemEnvironment.removeFormattingChars("true\r\n")).isEqualTo("true");
    }

    @Test
    public void itShouldNotReturnNullEvenIfTheUnderlyingEnvVarIs() {
        assertThat(new SystemEnvironment().getenv("doesNotExist")).isNotNull();
    }

    @Test
    public void itShouldNotReturnNullEvenIfGivenEmptyInput() {
        assertThat(new SystemEnvironment().getenv("")).isNotNull();

    }

    @Test
    public void itShouldNotReturnNullEvenIfGivenNull() {
        assertThat(new SystemEnvironment().getenv(null)).isNotNull();

    }

}