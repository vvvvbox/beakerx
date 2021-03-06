/*
 *  Copyright 2017 TWO SIGMA OPEN SOURCE, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.twosigma.beakerx.jupyter.handler;

import static com.twosigma.beakerx.evaluator.EvaluatorResultTestWatcher.waitForIdleMessage;
import static com.twosigma.beakerx.kernel.magic.command.functionality.JavaScriptMagicCommand.JAVASCRIPT;
import static org.assertj.core.api.Assertions.assertThat;

import com.twosigma.beakerx.KernelTest;
import com.twosigma.beakerx.evaluator.EvaluatorResultTestWatcher;
import com.twosigma.beakerx.evaluator.EvaluatorTest;
import com.twosigma.beakerx.kernel.handler.ExecuteRequestHandler;
import com.twosigma.beakerx.message.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExecuteRequestHandlerMagicCommandTest {

  public static final String DEMO_FILES_DEMO_RESOURCES_BEAKER_XCLASSPATH_TEST_JAR = "../../doc/resources/jar/BeakerXClasspathTest.jar";
  public static final String DEMO_FILES_DEMO_RESOURCES_BEAKERX_TEST_LIBRARY_JAR = "../../doc/resources/jar/demo.jar";

  private KernelTest kernel;
  private EvaluatorTest evaluator;
  private ExecuteRequestHandler executeRequestHandler;

  @Before
  public void setUp() {
    evaluator = new EvaluatorTest();
    kernel = new KernelTest("sid", evaluator);
    executeRequestHandler = new ExecuteRequestHandler(kernel);
  }

  @After
  public void tearDown() throws Exception {
    kernel.clearMessages();
    evaluator.exit();
  }

  @Test
  public void handleMagicClasspathAddJarAndExecuteTheCode() throws InterruptedException {
    //given
    String code = "" +
            "%classpath add jar " + DEMO_FILES_DEMO_RESOURCES_BEAKER_XCLASSPATH_TEST_JAR + "\n" +
            "import com.example.Demo;\n" +
            "Demo demo = new Demo();\n" +
            "demo.getObjectTest()\n";

    Message magicMessage = JupyterHandlerTest.createExecuteRequestMessage(code);
    //when
    executeRequestHandler.handle(magicMessage);
    waitForIdleMessage(kernel);
    //then
    assertThat(kernel.getPublishedMessages().size()).isEqualTo(11);
  }

  @Test
  public void handleMagicClasspathAddJar() throws InterruptedException {
    //when
    String code = "" +
            "%classpath add jar " + DEMO_FILES_DEMO_RESOURCES_BEAKER_XCLASSPATH_TEST_JAR;
    Message magicMessage = JupyterHandlerTest.createExecuteRequestMessage(code);
    executeRequestHandler.handle(magicMessage);
    waitForIdleMessage(kernel);
    //then
    assertThat(kernel.getPublishedMessages().size()).isEqualTo(10);
  }

  @Test
  public void handleMagicClasspathAddJarWithCode() throws InterruptedException {
    //when
    String code = "" +
            "%classpath add jar " + DEMO_FILES_DEMO_RESOURCES_BEAKER_XCLASSPATH_TEST_JAR + "\n" +
            "1+1";
    Message magicMessage = JupyterHandlerTest.createExecuteRequestMessage(code);
    executeRequestHandler.handle(magicMessage);
    waitForIdleMessage(kernel);
    //then
    assertThat(kernel.getPublishedMessages().size()).isEqualTo(11);
  }

  @Test
  public void noResetEnvironmentForDuplicatedPath() throws Exception {
    //when
    String code = "" +
            "%classpath add jar " + DEMO_FILES_DEMO_RESOURCES_BEAKER_XCLASSPATH_TEST_JAR + "\n" +
            "%classpath add jar " + DEMO_FILES_DEMO_RESOURCES_BEAKER_XCLASSPATH_TEST_JAR + "\n" +
            "%classpath add jar " + DEMO_FILES_DEMO_RESOURCES_BEAKER_XCLASSPATH_TEST_JAR + "\n";
    Message magicMessage = JupyterHandlerTest.createExecuteRequestMessage(code);
    executeRequestHandler.handle(magicMessage);
    //then
    assertThat(evaluator.getResetEnvironmentCounter()).isEqualTo(0);
  }

  @Test
  public void handleMagicJavaScriptCommand() throws Exception {
    //given
    String jsCode = System.lineSeparator() + "alert()";
    Message magicMessage = JupyterHandlerTest.createExecuteRequestMessage(JAVASCRIPT + jsCode);
    //when
    executeRequestHandler.handle(magicMessage);
    waitForIdleMessage(kernel);
    //then
    assertThat(kernel.getPublishedMessages().size()).isEqualTo(4);
  }

  public void handleImportMagicCommandAndExecuteTheCode() throws Exception {
    //given
    String code = "" +
            "%import com.twosigma.beakerx.widget.IntSlider\n" +
            "w = new IntSlider()";
    Message magicMessage = JupyterHandlerTest.createExecuteRequestMessage(code);
    //when
    executeRequestHandler.handle(magicMessage);
    waitForIdleMessage(kernel);
    //then
    assertThat(kernel.getPublishedMessages().size()).isEqualTo(8);
  }

  @Test
  public void noResetEnvironmentForDuplicatedImportPath() throws Exception {
    //when
    String code = "" +
            "%import com.twosigma.beakerx.widget.IntSlider\n" +
            "%import com.twosigma.beakerx.widget.IntSlider\n" +
            "%import com.twosigma.beakerx.widget.IntSlider\n";
    Message magicMessage = JupyterHandlerTest.createExecuteRequestMessage(code);
    executeRequestHandler.handle(magicMessage);
    waitForIdleMessage(kernel);
    //then
    assertThat(evaluator.getResetEnvironmentCounter()).isEqualTo(0);
  }

  @Test
  public void noCodeToExecute() throws InterruptedException {
    //given
    String code = "%classpath add jar " + DEMO_FILES_DEMO_RESOURCES_BEAKERX_TEST_LIBRARY_JAR;
    noCode(code);
  }

  public void noCodeToExecuteWithWhiteSpaces() throws InterruptedException {
    //given
    String code = "%classpath add jar " + DEMO_FILES_DEMO_RESOURCES_BEAKERX_TEST_LIBRARY_JAR + "\n" +
            " \n" +
            " \n" +
            "    ";
    noCode(code);
  }

  private void noCode(String code) throws InterruptedException {
    Message magicMessage = JupyterHandlerTest.createExecuteRequestMessage(code);
    //when
    executeRequestHandler.handle(magicMessage);
    waitForIdleMessage(kernel);
    //then
    assertThat(kernel.getPublishedMessages().size()).isEqualTo(10);
    //assertThat(kernel.getSentMessages().size()).isEqualTo(1);
    assertThat(kernel.getCode()).isNull();
  }

  @Test
  public void codeToExecute() throws InterruptedException {
    //given
    String code = "%classpath add jar " + DEMO_FILES_DEMO_RESOURCES_BEAKERX_TEST_LIBRARY_JAR + "\n" +
            "code code code";
    Message magicMessage = JupyterHandlerTest.createExecuteRequestMessage(code);
    //when
    executeRequestHandler.handle(magicMessage);
    waitForIdleMessage(kernel);
    //then
    assertThat(kernel.getPublishedMessages().size()).isEqualTo(11);
    assertThat(kernel.getCode()).isEqualTo("code code code");
  }


  @Test
  public void handleMagicClasspathAddJarAndShowClasspath() throws Exception {
    //given
    String code = "" +
            "%classpath add jar " + DEMO_FILES_DEMO_RESOURCES_BEAKERX_TEST_LIBRARY_JAR + "\n" +
            "%classpath";
    Message magicMessage = JupyterHandlerTest.createExecuteRequestMessage(code);
    //when
    executeRequestHandler.handle(magicMessage);
    waitForIdleMessage(kernel);
    //then
    assertThat(kernel.getPublishedMessages().size()).isEqualTo(11);
  }

  @Test
  public void handleMagicClasspathAddJarAndShowClasspathWithCode() throws Exception {
    //given
    String code = "" +
            "%classpath add jar " + DEMO_FILES_DEMO_RESOURCES_BEAKERX_TEST_LIBRARY_JAR + "\n" +
            "%classpath" + "\n" +
            "5+5";
    Message magicMessage = JupyterHandlerTest.createExecuteRequestMessage(code);
    //when
    executeRequestHandler.handle(magicMessage);
    waitForIdleMessage(kernel);
    //then
    assertThat(kernel.getPublishedMessages().size()).isEqualTo(12);
  }
}
