package com.opentext.otag.sdk.bus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.opentext.otag.sdk.bus.SdkEventBusLog.SDK_EVENT_LOGGING_ENV_VAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SdkEventBusLogTest {

    @Mock
    private SystemEnvironment envMock;

    private SdkEventBusLog underTest;

    @Before
    public void before() {
        underTest = new SdkEventBusLog(envMock);
    }

    @Test
    public void itShouldLogIf_true_IsUsedInTheEnvVar() {
        when(envMock.getenv(SDK_EVENT_LOGGING_ENV_VAR)).thenReturn("true");
        underTest.initializeUsingEnv();

        assertThat(underTest.isWriteToLog()).isTrue();
    }

    @Test
    public void itShouldLogIf_TRUE_IsUsedInTheEnvVar() {
        when(envMock.getenv(SDK_EVENT_LOGGING_ENV_VAR)).thenReturn("TRUE");
        underTest.initializeUsingEnv();

        assertThat(underTest.isWriteToLog()).isTrue();
    }

    @Test
    public void itShouldNotLogIf_false_IsUsedInTheEnvVar() {
        when(envMock.getenv(SDK_EVENT_LOGGING_ENV_VAR)).thenReturn("false");
        underTest.initializeUsingEnv();

        assertThat(underTest.isWriteToLog()).isFalse();
    }

    @Test
    public void itShouldNotLogI_FALSE_IsUsedInTheEnvVar() {
        when(envMock.getenv(SDK_EVENT_LOGGING_ENV_VAR)).thenReturn("FALSE");
        underTest.initializeUsingEnv();

        assertThat(underTest.isWriteToLog()).isFalse();
    }

    @Test
    public void itShouldBeAbleToLog() {
        initStaticInstance();

        ByteArrayOutputStream outS = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outS));

        String some_info = "this is some info";
        SdkEventBusLog.info(some_info);
        // a log line should have been written
        assertThat(outS.toString()).contains(some_info);
    }

    @Test
    public void itShouldBeAbleToLogErrors() {
        initStaticInstance();

        ByteArrayOutputStream outS = new ByteArrayOutputStream();
        System.setErr(new PrintStream(outS));

        String an_error = "this is an error";
        SdkEventBusLog.error(an_error);
        // a log line should have been written
        assertThat(outS.toString()).contains(an_error);
    }

    private void initStaticInstance() {
        when(envMock.getenv(SDK_EVENT_LOGGING_ENV_VAR)).thenReturn("true");
        underTest.initializeUsingEnv();

        // we set the instance to ensure it is not auto-resolved when one of the static
        // methods of the SdkEventBusLog are called
        SdkEventBusLog.setInstance(underTest);
    }

}