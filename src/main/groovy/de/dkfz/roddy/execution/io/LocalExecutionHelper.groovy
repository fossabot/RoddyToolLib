/*
 * Copyright (c) 2017 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.execution.io

import de.dkfz.roddy.core.InfoObject
import de.dkfz.roddy.tools.LoggerWrapper

import java.lang.reflect.Field

@groovy.transform.CompileStatic
class LocalExecutionHelper {
    private static final LoggerWrapper logger = LoggerWrapper.getLogger(LocalExecutionHelper.class.name);

    public static String getProcessID(Process process) {
        Field f = process.getClass().getDeclaredField("pid");
        f.setAccessible(true);
        String processID = f.get(process)
        return processID
    }

    /**
     * Use ExecutionResult instead. Deprecated and kept for compatibility.
     */
    @Deprecated
    static class ExtendedProcessExecutionResult extends InfoObject {
        final int exitValue;
        final String processID;
        final List<String> lines = [];

        ExtendedProcessExecutionResult(int exitValue, String processID, List<String> lines) {
            this.exitValue = exitValue
            this.processID = processID
            this.lines = lines
        }

        boolean isSuccessful() {
            return exitValue == 0
        }
    }

    public static String executeSingleCommand(String command) {
        //TODO What an windows systems?
        //Process process = Roddy.getLocalCommandSet().getShellExecuteCommand(command).execute();
        Process process = (["bash", "-c", "${command}"]).execute();

        final String separator = System.getProperty("line.separator");
        process.waitFor();
        if (process.exitValue()) {
            throw new RuntimeException("Process could not be run" + separator + "\tCommand: bash -c " + command + separator + "\treturn code is: " + process.exitValue())
        }

        def text = process.text
        return chomp(text) //Cut off trailing "\n"
    }

    public static String chomp(String text) {
        text.length() >= 2 ? text[0..-2] : text
    }

    /**
     * Execute a command using the local command interpreter (For Linux this might be bash)
     *
     * If outputStream is set, the full output is going to this stream. Otherwise it is stored
     * in the returned object.
     *
     * @param command
     * @param outputStream
     * @return
     */
    public static ExecutionResult executeCommandWithExtendedResult(String command, OutputStream outputStream = null) {
        //Process process = Roddy.getLocalCommandSet().getShellExecuteCommand(command).execute();
        Process process = ["bash", "-c", command].execute();

        //TODO Put to a custom class which can handle things for Windows as well.
        String processID = getProcessID(process)

        StringBuilder outStream = new StringBuilder()
        StringBuilder errStream = new StringBuilder()
        if (logger.isVerbosityHigh())
            println("Executing the command ${command} locally.");

        if (outputStream)
            process.waitForProcessOutput(outputStream, outputStream)
        else {
            process.waitForProcessOutput(outStream, errStream)
        }
        return new ExecutionResult(process.exitValue(), outStream.toString(), errStream.toString(), processID)
    }

    public static Process executeNonBlocking(String command) {
        Process process = ("sleep 1; " + command).execute();
        return process;
    }


    static String execute(String cmd) {
        def proc = cmd.execute();
        int res = proc.waitFor();
        if (res == 0) {
            return proc.in.text;
        }
        return "";
    }
}
